<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;
use App\Models\User;

class UserSeeder extends Seeder
{
    /**
     * Crear usuarios de prueba
     */
    public function run(): void
    {
        // Limpiar tabla
        User::truncate();

        // ===== TÉCNICOS DE LECTURAS =====

        User::create([
            'usuario' => 'tecnico01',
            'clave' => Hash::make('password123'),
            'nombre' => 'Juan Pérez',
            'tipodeusuario' => 'TECNICO',
            'email' => 'tecnico01@acueducto.com',
            'empresa' => 'Acueducto Municipal',
            'firma_path' => null, // Se configura desde la web
            'estado' => 'activo',
        ]);

        User::create([
            'usuario' => 'tecnico02',
            'clave' => Hash::make('password123'),
            'nombre' => 'María González',
            'tipodeusuario' => 'TECNICO',
            'email' => 'tecnico02@acueducto.com',
            'empresa' => 'Acueducto Municipal',
            'firma_path' => null,
            'estado' => 'activo',
        ]);

        // ===== TÉCNICOS DE REVISIONES =====

        User::create([
            'usuario' => 'revisor01',
            'clave' => Hash::make('password123'),
            'nombre' => 'Carlos Rodríguez',
            'tipodeusuario' => 'REVISOR',
            'email' => 'revisor01@acueducto.com',
            'empresa' => 'Acueducto Municipal',
            'firma_path' => null,
            'estado' => 'activo',
        ]);

        User::create([
            'usuario' => 'revisor02',
            'clave' => Hash::make('password123'),
            'nombre' => 'Ana Martínez',
            'tipodeusuario' => 'REVISOR',
            'email' => 'revisor02@acueducto.com',
            'empresa' => 'Acueducto Municipal',
            'firma_path' => null,
            'estado' => 'activo',
        ]);

        // ===== ADMINISTRADOR =====

        User::create([
            'usuario' => 'admin',
            'clave' => Hash::make('admin123'),
            'nombre' => 'Administrador Sistema',
            'tipodeusuario' => 'ADMIN',
            'email' => 'admin@acueducto.com',
            'empresa' => 'Acueducto Municipal',
            'firma_path' => null,
            'estado' => 'activo',
        ]);

        $this->command->info('✅ Usuarios creados correctamente');
        $this->command->info('');
        $this->command->info('Credenciales de acceso:');
        $this->command->info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
        $this->command->info('TÉCNICOS DE LECTURAS:');
        $this->command->info('  Usuario: tecnico01 | Clave: password123');
        $this->command->info('  Usuario: tecnico02 | Clave: password123');
        $this->command->info('');
        $this->command->info('TÉCNICOS DE REVISIONES:');
        $this->command->info('  Usuario: revisor01 | Clave: password123');
        $this->command->info('  Usuario: revisor02 | Clave: password123');
        $this->command->info('');
        $this->command->info('ADMINISTRADOR:');
        $this->command->info('  Usuario: admin | Clave: admin123');
        $this->command->info('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    }
}
