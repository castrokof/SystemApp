<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\Revision;
use Carbon\Carbon;

class RevisionSeeder extends Seeder
{
    /**
     * Crear órdenes de revisión de ejemplo
     */
    public function run(): void
    {
        // Limpiar tabla
        Revision::truncate();

        $periodo = Carbon::now()->format('F Y');
        $ciclo = Carbon::now()->format('Y-m');

        // ===== REVISIONES PARA revisor01 =====

        Revision::create([
            'id' => 'REV-2025-001',
            'ciclo' => $ciclo,
            'categoria_orden' => 'DESVIACION',
            'tipo_orden' => 'REVISIONES',
            'periodo' => $periodo,
            'suscriptor' => '10023456',
            'ref_medidor' => 'M-789456',
            'direccion' => 'Calle 50 #23-45',
            'nombre' => 'Carlos',
            'apell' => 'Rodríguez',
            'promedio' => 25,
            'LA' => '1245',
            'usuario' => 'revisor01',
            'estado' => 'PENDIENTE',
            'tipo_desviacion' => 'ALTO',
            'ruta' => 'RUTA-05',
            'consecutivo_ruta' => '15',
            'observacion_inicial' => 'Consumo superior a 80 m³ en los últimos 3 meses',
            'orden_personalizado' => 1,
        ]);

        Revision::create([
            'id' => 'REV-2025-002',
            'ciclo' => $ciclo,
            'categoria_orden' => 'DESVIACION',
            'tipo_orden' => 'REVISIONES',
            'periodo' => $periodo,
            'suscriptor' => '10023457',
            'ref_medidor' => 'M-789457',
            'direccion' => 'Carrera 30 #15-20',
            'nombre' => 'Ana',
            'apell' => 'Martínez',
            'promedio' => 18,
            'LA' => '856',
            'usuario' => 'revisor01',
            'estado' => 'PENDIENTE',
            'tipo_desviacion' => 'BAJO',
            'ruta' => 'RUTA-05',
            'consecutivo_ruta' => '16',
            'observacion_inicial' => 'Consumo menor a 5 m³ en los últimos 2 meses',
            'orden_personalizado' => 2,
        ]);

        Revision::create([
            'id' => 'REV-2025-003',
            'ciclo' => $ciclo,
            'categoria_orden' => 'DESVIACION',
            'tipo_orden' => 'REVISIONES',
            'periodo' => $periodo,
            'suscriptor' => '10023458',
            'ref_medidor' => 'M-789458',
            'direccion' => 'Avenida 10 #45-67',
            'nombre' => 'Luis',
            'apell' => 'García',
            'promedio' => 30,
            'LA' => '2340',
            'usuario' => 'revisor01',
            'estado' => 'PENDIENTE',
            'tipo_desviacion' => 'ALTO',
            'ruta' => 'RUTA-05',
            'consecutivo_ruta' => '17',
            'observacion_inicial' => 'Incremento del 150% respecto al promedio',
            'orden_personalizado' => 3,
        ]);

        // ===== REVISIONES PARA revisor02 =====

        Revision::create([
            'id' => 'REV-2025-004',
            'ciclo' => $ciclo,
            'categoria_orden' => 'DESVIACION',
            'tipo_orden' => 'REVISIONES',
            'periodo' => $periodo,
            'suscriptor' => '10023459',
            'ref_medidor' => 'M-789459',
            'direccion' => 'Calle 80 #12-34',
            'nombre' => 'Patricia',
            'apell' => 'López',
            'promedio' => 22,
            'LA' => '1450',
            'usuario' => 'revisor02',
            'estado' => 'PENDIENTE',
            'tipo_desviacion' => 'BAJO',
            'ruta' => 'RUTA-08',
            'consecutivo_ruta' => '20',
            'observacion_inicial' => 'Consumo cero en los últimos 2 meses',
            'orden_personalizado' => 1,
        ]);

        $this->command->info('✅ Órdenes de revisión creadas correctamente');
        $this->command->info('  - 3 órdenes para revisor01');
        $this->command->info('  - 1 orden para revisor02');
    }
}
