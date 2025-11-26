<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Revision extends Model
{
    use HasFactory;

    protected $table = 'revisiones';
    protected $primaryKey = 'id';
    public $incrementing = false;
    protected $keyType = 'string';

    protected $fillable = [
        'id',
        'ciclo',
        'categoria_orden',
        'tipo_orden',
        'periodo',
        'suscriptor',
        'ref_medidor',
        'direccion',
        'nombre',
        'apell',
        'promedio',
        'LA',
        'usuario',
        'estado',
        'tipo_desviacion',
        'ruta',
        'consecutivo_ruta',
        'observacion_inicial',
        'lectura_actual',
        'consumo',
        'nombre_residente',
        'firma_path',
        'estado_acometida',
        'estado_sellos',
        'que_surte',
        'censo_poblacional_familiar',
        'censo_poblacional_personas',
        'censo_poblacional_adultos',
        'censo_poblacional_ninos',
        'codigo_causa',
        'desc_causa',
        'observacion_causa',
        'observacion_general',
        'fecha_inicio',
        'fecha_cierre',
        'cantidad_modificaciones',
        'orden_personalizado',
        'latitud',
        'longitud',
        'ruta_pdf',
        'enviado_api',
    ];

    protected $casts = [
        'fecha_inicio' => 'datetime',
        'fecha_cierre' => 'datetime',
        'cantidad_modificaciones' => 'integer',
        'orden_personalizado' => 'integer',
    ];

    /**
     * Relación con censo hidráulico
     */
    public function censoHidraulico()
    {
        return $this->hasMany(CensoHidraulico::class, 'revision_id', 'id');
    }

    /**
     * Relación con fotos
     */
    public function fotos()
    {
        return $this->hasMany(FotoRevision::class, 'revision_id', 'id');
    }

    /**
     * Relación con usuario
     */
    public function tecnico()
    {
        return $this->belongsTo(User::class, 'usuario', 'usuario');
    }

    /**
     * Verificar si puede ser modificada
     */
    public function puedeSerModificada()
    {
        return $this->cantidad_modificaciones < 3;
    }

    /**
     * Incrementar contador de modificaciones
     */
    public function incrementarModificaciones()
    {
        $this->cantidad_modificaciones++;
        $this->save();
    }

    /**
     * URL del PDF
     */
    public function getPdfUrlAttribute()
    {
        if ($this->ruta_pdf) {
            return url('storage/' . $this->ruta_pdf);
        }
        return null;
    }

    /**
     * Scopes
     */
    public function scopePendientes($query)
    {
        return $query->where('estado', 'PENDIENTE');
    }

    public function scopeEjecutadas($query)
    {
        return $query->where('estado', 'EJECUTADA');
    }

    public function scopeDelUsuario($query, $usuario)
    {
        return $query->where('usuario', $usuario);
    }
}
