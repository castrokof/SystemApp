<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class CensoHidraulico extends Model
{
    use HasFactory;

    protected $table = 'censo_hidraulico';

    protected $fillable = [
        'revision_id',
        'elemento',
        'cantidad',
        'estado',
        'foto_path',
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
        if ($this->foto_path) {
            return url('storage/' . $this->foto_path);
        }
        return null;
    }
}
