package com.example.systemapp.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gestión de configuración de API dinámica
 * Permite cambiar la URL base para diferentes acueductos
 */
public class ApiConfig {

    private static final String PREFS_NAME = "API_CONFIG_PREFS";
    private static final String KEY_BASE_URL = "BASE_URL";
    private static final String KEY_ACUEDUCTO_NOMBRE = "ACUEDUCTO_NOMBRE";

    /**
     * Obtener la URL base configurada
     */
    public static String getBaseUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_BASE_URL, null);
    }

    /**
     * Guardar la URL base
     */
    public static void setBaseUrl(Context context, String baseUrl) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_BASE_URL, baseUrl);
        editor.apply();
    }

    /**
     * Guardar nombre del acueducto (opcional)
     */
    public static void setAcueductoNombre(Context context, String nombre) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACUEDUCTO_NOMBRE, nombre);
        editor.apply();
    }

    /**
     * Obtener nombre del acueducto
     */
    public static String getAcueductoNombre(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ACUEDUCTO_NOMBRE, "Acueducto");
    }

    /**
     * Verificar si ya está configurada la URL
     */
    public static boolean isConfigured(Context context) {
        String url = getBaseUrl(context);
        return url != null && !url.isEmpty();
    }

    /**
     * Limpiar configuración
     */
    public static void clearConfig(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Construir URL completa para un endpoint
     */
    public static String buildUrl(Context context, String endpoint) {
        String baseUrl = getBaseUrl(context);
        if (baseUrl == null) {
            throw new IllegalStateException("URL base no configurada");
        }

        // Asegurar que no haya doble slash
        if (baseUrl.endsWith("/") && endpoint.startsWith("/")) {
            endpoint = endpoint.substring(1);
        } else if (!baseUrl.endsWith("/") && !endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }

        return baseUrl + endpoint;
    }
}
