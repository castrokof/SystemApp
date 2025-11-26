<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Support\Str;

class User extends Authenticatable
{
    use HasFactory;

    protected $fillable = [
        'usuario',
        'clave',
        'nombre',
        'tipodeusuario',
        'email',
        'empresa',
        'firma_path',
        'api_token',
        'estado',
    ];

    protected $hidden = [
        'clave',
    ];

    protected $casts = [
        'created_at' => 'datetime',
        'updated_at' => 'datetime',
    ];

    /**
     * Generar API token único
     */
    public function generateApiToken()
    {
        $this->api_token = Str::random(80);
        $this->save();
        return $this->api_token;
    }

    /**
     * URL completa de la firma
     */
    public function getFirmaUrlAttribute()
    {
        if ($this->firma_path) {
            return url('storage/' . $this->firma_path);
        }
        return null;
    }

    /**
     * Revisiones asignadas al usuario
     */
    public function revisiones()
    {
        return $this->hasMany(Revision::class, 'usuario', 'usuario');
    }

    /**
     * Verificar si es técnico de lecturas
     */
    public function isTecnico()
    {
        return $this->tipodeusuario === 'TECNICO';
    }

    /**
     * Verificar si es técnico de revisiones
     */
    public function isRevisor()
    {
        return $this->tipodeusuario === 'REVISOR';
    }

    /**
     * Verificar si es administrador
     */
    public function isAdmin()
    {
        return $this->tipodeusuario === 'ADMIN';
    }
}
