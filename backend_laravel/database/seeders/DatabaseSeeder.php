<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;

class DatabaseSeeder extends Seeder
{
    /**
     * Seed the application's database.
     */
    public function run(): void
    {
        $this->call([
            UserSeeder::class,
            RevisionSeeder::class,
        ]);

        $this->command->info('');
        $this->command->info('🎉 Base de datos inicializada correctamente');
        $this->command->info('');
        $this->command->info('Próximos pasos:');
        $this->command->info('1. Configurar las firmas de los técnicos desde el panel web');
        $this->command->info('2. Probar el login desde la app móvil');
        $this->command->info('3. Sincronizar órdenes de revisión');
    }
}
