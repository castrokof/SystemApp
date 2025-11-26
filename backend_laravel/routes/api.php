<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\RevisionController;

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
|
| Rutas de la API para la aplicación SystemApp
| Incluye autenticación y gestión de revisiones
|
*/

// ========== RUTAS PÚBLICAS (sin autenticación) ==========

// Ping - Verificar estado de la API
Route::get('/ping', [AuthController::class, 'ping']);

// Login - Autenticación de usuarios
Route::post('/login', [AuthController::class, 'login']);

// ========== RUTAS PROTEGIDAS (requieren autenticación) ==========

Route::middleware('api.token.auth')->group(function () {

    // ===== AUTENTICACIÓN =====
    Route::post('/logout', [AuthController::class, 'logout']);

    // ===== REVISIONES =====

    // Obtener órdenes de revisión para descargar
    Route::get('/revisiones/ordenes', [RevisionController::class, 'getOrdenes']);

    // Enviar revisión completada
    Route::post('/revisiones/enviar', [RevisionController::class, 'enviarRevision']);

    // Actualizar revisión (reapertura)
    Route::put('/revisiones/{id}', [RevisionController::class, 'updateRevision']);

    // Obtener causas de desviación
    Route::get('/revisiones/causas', [RevisionController::class, 'getCausas']);

    // ===== LECTURAS (módulo existente - mantener compatibilidad) =====
    // Aquí se pueden agregar las rutas del módulo de lecturas actual
    // Route::get('/lecturas/ordenes', [LecturaController::class, 'getOrdenes']);
    // Route::post('/lecturas/enviar', [LecturaController::class, 'enviarLectura']);

});
