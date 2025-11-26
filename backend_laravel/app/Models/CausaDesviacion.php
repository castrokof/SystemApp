<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class CausaDesviacion extends Model
{
    use HasFactory;

    protected $table = 'causas_desviacion';
    protected $primaryKey = 'codigo';
    public $incrementing = false;

    protected $fillable = [
        'codigo',
        'tipo',
        'descripcion',
        'activo',
    ];

    protected $casts = [
        'activo' => 'boolean',
    ];

    /**
     * Scopes
     */
    public function scopeActivas($query)
    {
        return $query->where('activo', true);
    }

    public function scopeAltoConsumo($query)
    {
        return $query->where('tipo', 'ALTO');
    }

    public function scopeBajoConsumo($query)
    {
        return $query->where('tipo', 'BAJO');
    }
}
