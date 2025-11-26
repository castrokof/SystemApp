<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class AuthController extends Controller
{
    /**
     * Ping - Verificar que la API está funcionando
     */
    public function ping()
    {
        return response()->json([
            'status' => 'ok',
            'message' => 'API funcionando correctamente',
            'version' => '1.0.0',
            'timestamp' => now()->format('Y-m-d H:i:s'),
        ]);
    }

    /**
     * Login - Autenticar usuario
     */
    public function login(Request $request)
    {
        $request->validate([
            'usuario' => 'required|string',
            'clave' => 'required|string',
        ]);

        // Buscar usuario
        $user = User::where('usuario', $request->usuario)->first();

        // Verificar credenciales
        if (!$user || !Hash::check($request->clave, $user->clave)) {
            return response()->json([
                'success' => false,
                'message' => 'Credenciales inválidas',
            ], 401);
        }

        // Verificar estado activo
        if ($user->estado !== 'activo') {
            return response()->json([
                'success' => false,
                'message' => 'Usuario inactivo',
            ], 403);
        }

        // Generar token
        $token = $user->generateApiToken();

        return response()->json([
            'success' => true,
            'data' => [
                'id' => (string) $user->id,
                'usuario' => $user->usuario,
                'nombre' => $user->nombre,
                'tipodeusuario' => $user->tipodeusuario,
                'email' => $user->email,
                'empresa' => $user->empresa,
                'estado' => $user->estado,
                'api_token' => $token,
                'firma_url' => $user->firma_url,
            ],
        ]);
    }

    /**
     * Logout - Cerrar sesión
     */
    public function logout(Request $request)
    {
        $user = $request->user();

        if ($user) {
            $user->api_token = null;
            $user->save();

            return response()->json([
                'success' => true,
                'message' => 'Sesión cerrada correctamente',
            ]);
        }

        return response()->json([
            'success' => false,
            'message' => 'No hay sesión activa',
        ], 401);
    }
}
