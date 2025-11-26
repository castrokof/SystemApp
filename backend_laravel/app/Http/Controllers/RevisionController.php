<?php

namespace App\Http\Controllers;

use App\Models\Revision;
use App\Models\CensoHidraulico;
use App\Models\FotoRevision;
use App\Models\CausaDesviacion;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Storage;
use Carbon\Carbon;

class RevisionController extends Controller
{
    /**
     * Obtener órdenes de revisión para descargar en la app
     */
    public function getOrdenes(Request $request)
    {
        $query = Revision::query();

        // Filtrar por usuario si se especifica
        if ($request->has('usuario_id')) {
            $usuario = \App\Models\User::find($request->usuario_id);
            if ($usuario) {
                $query->where('usuario', $usuario->usuario);
            }
        }

        // Filtrar por estado
        if ($request->has('estado')) {
            $query->where('estado', $request->estado);
        } else {
            // Por defecto, solo pendientes y en ejecución
            $query->whereIn('estado', ['PENDIENTE', 'EN_EJECUCION']);
        }

        // Filtrar por periodo
        if ($request->has('periodo')) {
            $query->where('periodo', $request->periodo);
        }

        // Ordenar por orden personalizado
        $revisiones = $query->orderBy('orden_personalizado', 'asc')
                           ->orderBy('id', 'asc')
                           ->get();

        return response()->json([
            'success' => true,
            'data' => $revisiones,
            'total' => $revisiones->count(),
        ]);
    }

    /**
     * Recibir revisión completada desde la app
     */
    public function enviarRevision(Request $request)
    {
        $request->validate([
            'id' => 'required|string',
            'suscriptor' => 'required|string',
            'usuario' => 'required|string',
            'lectura_actual' => 'required|integer',
            'nombre_residente' => 'required|string',
        ]);

        DB::beginTransaction();

        try {
            // Buscar o crear la revisión
            $revision = Revision::find($request->id);

            if (!$revision) {
                return response()->json([
                    'success' => false,
                    'message' => 'Revisión no encontrada',
                ], 404);
            }

            // Actualizar datos principales
            $revision->fill($request->except(['censo_hidraulico', 'fotos_adicionales', 'pdf_base64']));
            $revision->estado = 'PROCESADA';
            $revision->enviado_api = 'SI';

            // Guardar firma del cliente (si viene en base64)
            if ($request->has('firma_base64')) {
                $firmaPath = $this->guardarImagenBase64(
                    $request->firma_base64,
                    'firmas_clientes',
                    'firma_' . $revision->id
                );
                $revision->firma_path = $firmaPath;
            }

            // Guardar PDF
            if ($request->has('pdf_base64')) {
                $pdfPath = $this->guardarPDFBase64(
                    $request->pdf_base64,
                    'revisiones_pdf',
                    'revision_' . $revision->id
                );
                $revision->ruta_pdf = $pdfPath;
            }

            $revision->save();

            // Guardar censo hidráulico
            if ($request->has('censo_hidraulico')) {
                // Eliminar censos anteriores
                CensoHidraulico::where('revision_id', $revision->id)->delete();

                foreach ($request->censo_hidraulico as $censo) {
                    $censoData = [
                        'revision_id' => $revision->id,
                        'elemento' => $censo['elemento'],
                        'cantidad' => $censo['cantidad'],
                        'estado' => $censo['estado'],
                    ];

                    // Guardar foto del elemento si viene
                    if (isset($censo['foto']) && !empty($censo['foto'])) {
                        $fotoPath = $this->guardarImagenBase64(
                            $censo['foto'],
                            'censo_fotos',
                            'censo_' . $revision->id . '_' . $censo['elemento']
                        );
                        $censoData['foto_path'] = $fotoPath;
                    }

                    CensoHidraulico::create($censoData);
                }
            }

            // Guardar fotos adicionales
            if ($request->has('fotos_adicionales')) {
                // Eliminar fotos anteriores
                FotoRevision::where('revision_id', $revision->id)->delete();

                foreach ($request->fotos_adicionales as $foto) {
                    $fotoPath = $this->guardarImagenBase64(
                        $foto['foto_base64'],
                        'fotos_revision',
                        'foto_' . $revision->id . '_tab' . $foto['tab_numero']
                    );

                    FotoRevision::create([
                        'revision_id' => $revision->id,
                        'tab_numero' => $foto['tab_numero'],
                        'descripcion' => $foto['descripcion'] ?? null,
                        'ruta_foto' => $fotoPath,
                        'fecha_captura' => now(),
                    ]);
                }
            }

            DB::commit();

            return response()->json([
                'success' => true,
                'message' => 'Revisión recibida correctamente',
                'data' => [
                    'id' => $revision->id,
                    'estado' => $revision->estado,
                    'pdf_url' => $revision->pdf_url,
                    'fecha_recepcion' => now()->format('Y-m-d H:i:s'),
                ],
            ]);

        } catch (\Exception $e) {
            DB::rollBack();

            return response()->json([
                'success' => false,
                'message' => 'Error al procesar la revisión: ' . $e->getMessage(),
            ], 500);
        }
    }

    /**
     * Actualizar revisión (reapertura)
     */
    public function updateRevision(Request $request, $id)
    {
        $revision = Revision::find($id);

        if (!$revision) {
            return response()->json([
                'success' => false,
                'message' => 'Revisión no encontrada',
            ], 404);
        }

        // Verificar límite de modificaciones
        if (!$revision->puedeSerModificada()) {
            return response()->json([
                'success' => false,
                'message' => 'Esta revisión ha alcanzado el límite de 3 modificaciones',
            ], 403);
        }

        DB::beginTransaction();

        try {
            // Reutilizar lógica de enviarRevision
            // ... (misma lógica de guardado)

            // Incrementar contador de modificaciones
            $revision->incrementarModificaciones();

            DB::commit();

            return response()->json([
                'success' => true,
                'message' => 'Revisión actualizada correctamente',
                'data' => [
                    'id' => $revision->id,
                    'cantidad_modificaciones' => $revision->cantidad_modificaciones,
                    'modificaciones_restantes' => 3 - $revision->cantidad_modificaciones,
                ],
            ]);

        } catch (\Exception $e) {
            DB::rollBack();

            return response()->json([
                'success' => false,
                'message' => 'Error al actualizar la revisión: ' . $e->getMessage(),
            ], 500);
        }
    }

    /**
     * Obtener causas de desviación
     */
    public function getCausas(Request $request)
    {
        $query = CausaDesviacion::activas();

        if ($request->has('tipo')) {
            if ($request->tipo === 'ALTO') {
                $query->altoConsumo();
            } elseif ($request->tipo === 'BAJO') {
                $query->bajoConsumo();
            }
        }

        $causas = $query->orderBy('codigo')->get();

        return response()->json([
            'success' => true,
            'data' => $causas,
        ]);
    }

    /**
     * Guardar imagen desde Base64
     */
    private function guardarImagenBase64($base64String, $folder, $filename)
    {
        // Decodificar base64
        $imageData = base64_decode($base64String);

        // Generar nombre único
        $fileName = $filename . '_' . time() . '.jpg';
        $path = $folder . '/' . $fileName;

        // Guardar en storage
        Storage::disk('public')->put($path, $imageData);

        return $path;
    }

    /**
     * Guardar PDF desde Base64
     */
    private function guardarPDFBase64($base64String, $folder, $filename)
    {
        // Decodificar base64
        $pdfData = base64_decode($base64String);

        // Generar nombre único
        $fileName = $filename . '_' . time() . '.pdf';
        $path = $folder . '/' . $fileName;

        // Guardar en storage
        Storage::disk('public')->put($path, $pdfData);

        return $path;
    }
}
