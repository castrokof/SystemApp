<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('fotos_revision', function (Blueprint $table) {
            $table->id();
            $table->string('revision_id', 30);
            $table->integer('tab_numero');
            $table->string('descripcion')->nullable();
            $table->text('ruta_foto');
            $table->timestamp('fecha_captura')->nullable();
            $table->timestamps();

            $table->foreign('revision_id')
                  ->references('id')
                  ->on('revisiones')
                  ->onDelete('cascade');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('fotos_revision');
    }
};
