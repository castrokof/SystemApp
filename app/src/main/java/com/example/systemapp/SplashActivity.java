package com.example.systemapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.systemapp.data.ApiConfig;
import com.example.systemapp.data.SessionPrefs;
import com.example.systemapp.ui.config.ServerConfigActivity;
import com.example.systemapp.ui.login.LoginActivity;

/**
 * Pantalla de inicio que determina a dónde dirigir al usuario:
 * 1. Si no hay URL configurada → ServerConfigActivity
 * 2. Si no hay sesión activa → LoginActivity
 * 3. Si hay sesión activa → MainActivity
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500; // 1.5 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            decidirDestino();
        }, SPLASH_DELAY);
    }

    private void decidirDestino() {
        // Verificar si hay URL configurada
        if (!ApiConfig.isConfigured(this)) {
            // No hay URL → Ir a configuración
            Intent intent = new Intent(this, ServerConfigActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Verificar si hay sesión activa
        if (!SessionPrefs.get(this).isLoggedIn()) {
            // No hay sesión → Ir a login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Hay sesión activa → Ir a MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
