<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('censo_hidraulico', function (Blueprint $table) {
            $table->id();
            $table->string('revision_id', 30);
            $table->string('elemento', 100);
            $table->integer('cantidad');
            $table->enum('estado', ['BUENO', 'MALO']);
            $table->text('foto_path')->nullable();
            $table->timestamps();

            $table->foreign('revision_id')
                  ->references('id')
                  ->on('revisiones')
                  ->onDelete('cascade');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('censo_hidraulico');
    }
};
