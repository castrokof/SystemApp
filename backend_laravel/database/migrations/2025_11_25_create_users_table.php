<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('users', function (Blueprint $table) {
            $table->id();
            $table->string('usuario')->unique();
            $table->string('clave');
            $table->string('nombre');
            $table->enum('tipodeusuario', ['TECNICO', 'REVISOR', 'ADMIN'])->default('TECNICO');
            $table->string('email')->unique();
            $table->string('empresa')->nullable();
            $table->string('firma_path')->nullable(); // Ruta de la firma del técnico
            $table->string('api_token', 80)->unique()->nullable();
            $table->enum('estado', ['activo', 'inactivo'])->default('activo');
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('users');
    }
};
