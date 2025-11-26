<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('revisiones', function (Blueprint $table) {
            $table->string('id', 30)->primary();
            $table->string('ciclo', 100);
            $table->string('categoria_orden', 30)->nullable();
            $table->string('tipo_orden', 30)->default('REVISIONES');
            $table->string('periodo', 100);
            $table->string('suscriptor', 100);
            $table->string('ref_medidor', 100);
            $table->string('direccion', 100);
            $table->string('nombre', 100);
            $table->string('apell', 100);
            $table->integer('promedio')->nullable();
            $table->string('LA', 100)->nullable(); // Lectura Anterior
            $table->string('usuario', 100);
            $table->enum('estado', ['PENDIENTE', 'EN_EJECUCION', 'EJECUTADA', 'PROCESADA'])->default('PENDIENTE');
            $table->enum('tipo_desviacion', ['ALTO', 'BAJO'])->nullable();
            $table->string('ruta', 100)->nullable();
            $table->string('consecutivo_ruta', 100)->nullable();
            $table->text('observacion_inicial')->nullable();

            // Tab 1: Lectura
            $table->integer('lectura_actual')->nullable();
            $table->integer('consumo')->nullable();

            // Tab 2: Residente
            $table->string('nombre_residente', 200)->nullable();
            $table->text('firma_path')->nullable();

            // Tab 3: Acometida
            $table->string('estado_acometida', 50)->nullable();
            $table->string('estado_sellos', 50)->nullable();
            $table->string('que_surte', 200)->nullable();

            // Tab 4: Censos
            $table->integer('censo_poblacional_familiar')->nullable();
            $table->integer('censo_poblacional_personas')->nullable();
            $table->integer('censo_poblacional_adultos')->nullable();
            $table->integer('censo_poblacional_ninos')->nullable();

            // Tab 5: Clasificación
            $table->integer('codigo_causa')->nullable();
            $table->text('desc_causa')->nullable();
            $table->text('observacion_causa')->nullable();

            // Tab 6: Observación General
            $table->text('observacion_general')->nullable();

            // Control
            $table->timestamp('fecha_inicio')->nullable();
            $table->timestamp('fecha_cierre')->nullable();
            $table->integer('cantidad_modificaciones')->default(0);
            $table->integer('orden_personalizado')->default(0);
            $table->string('latitud', 50)->nullable();
            $table->string('longitud', 50)->nullable();
            $table->text('ruta_pdf')->nullable();
            $table->enum('enviado_api', ['SI', 'NO'])->default('NO');

            $table->timestamps();

            // Índices
            $table->index('usuario');
            $table->index('estado');
            $table->index('suscriptor');
            $table->index(['usuario', 'estado']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('revisiones');
    }
};
