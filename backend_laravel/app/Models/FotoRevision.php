<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class FotoRevision extends Model
{
    use HasFactory;

    protected $table = 'fotos_revision';

    protected $fillable = [
        'revision_id',
        'tab_numero',
        'descripcion',
        'ruta_foto',
        'fecha_captura',
    ];

    protected $casts = [
        'fecha_captura' => 'datetime',
    ];

    /**
     * Relación con revisión
     */
    public function revision()
    {
        return $this->belongsTo(Revision::class, 'revision_id', 'id');
    }

    /**
     * URL de la foto
     */
    public function getFotoUrlAttribute()
    {
        if ($this->ruta_foto) {
            return url('storage/' . $this->ruta_foto);
        }
        return null;
    }
}
