<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('causas_desviacion', function (Blueprint $table) {
            $table->integer('codigo')->primary();
            $table->enum('tipo', ['ALTO', 'BAJO']);
            $table->text('descripcion');
            $table->boolean('activo')->default(true);
            $table->timestamps();
        });

        // Insertar causas predefinidas
        DB::table('causas_desviacion')->insert([
            // ALTO CONSUMO
            ['codigo' => 1, 'tipo' => 'ALTO', 'descripcion' => 'Fuga interna en sanitario'],
            ['codigo' => 2, 'tipo' => 'ALTO', 'descripcion' => 'Fuga en tubería principal'],
            ['codigo' => 3, 'tipo' => 'ALTO', 'descripcion' => 'Llave de jardín abierta'],
            ['codigo' => 4, 'tipo' => 'ALTO', 'descripcion' => 'Filtración en ducha'],
            ['codigo' => 5, 'tipo' => 'ALTO', 'descripcion' => 'Aumento de habitantes'],
            ['codigo' => 6, 'tipo' => 'ALTO', 'descripcion' => 'Uso comercial no declarado'],
            ['codigo' => 7, 'tipo' => 'ALTO', 'descripcion' => 'Fuga en lavamanos'],
            ['codigo' => 8, 'tipo' => 'ALTO', 'descripcion' => 'Filtración en tanque'],
            ['codigo' => 9, 'tipo' => 'ALTO', 'descripcion' => 'Conexión clandestina detectada'],

            // BAJO CONSUMO
            ['codigo' => 10, 'tipo' => 'BAJO', 'descripcion' => 'Vivienda desocupada'],
            ['codigo' => 11, 'tipo' => 'BAJO', 'descripcion' => 'Predio temporal'],
            ['codigo' => 12, 'tipo' => 'BAJO', 'descripcion' => 'Disminución de habitantes'],
            ['codigo' => 13, 'tipo' => 'BAJO', 'descripcion' => 'Medidor averiado'],
            ['codigo' => 14, 'tipo' => 'BAJO', 'descripcion' => 'Medidor frenado'],
            ['codigo' => 15, 'tipo' => 'BAJO', 'descripcion' => 'Servicio suspendido'],
            ['codigo' => 16, 'tipo' => 'BAJO', 'descripcion' => 'Acueducto alterno (pozo, aljibe)'],
        ]);
    }

    public function down(): void
    {
        Schema::dropIfExists('causas_desviacion');
    }
};
