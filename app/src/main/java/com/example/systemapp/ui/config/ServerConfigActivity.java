package com.example.systemapp.ui.config;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.systemapp.R;
import com.example.systemapp.data.ApiConfig;
import com.example.systemapp.ui.login.LoginActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Actividad para configurar la URL del servidor API
 * Permite usar la app en diferentes acueductos
 */
public class ServerConfigActivity extends AppCompatActivity {

    private EditText etServerUrl;
    private Button btnGuardar;
    private Button btnProbarConexion;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_config);

        etServerUrl = findViewById(R.id.etServerUrl);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnProbarConexion = findViewById(R.id.btnProbarConexion);
        progressBar = findViewById(R.id.progressBar);

        // Cargar URL guardada si existe
        String urlGuardada = ApiConfig.getBaseUrl(this);
        if (urlGuardada != null && !urlGuardada.isEmpty()) {
            etServerUrl.setText(urlGuardada);
        }

        btnProbarConexion.setOnClickListener(v -> probarConexion());
        btnGuardar.setOnClickListener(v -> guardarYContinuar());
    }

    private void probarConexion() {
        String url = etServerUrl.getText().toString().trim();

        if (TextUtils.isEmpty(url)) {
            etServerUrl.setError("Ingrese la URL del servidor");
            return;
        }

        // Validar formato de URL
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            etServerUrl.setError("La URL debe comenzar con http:// o https://");
            return;
        }

        // Agregar /api si no está
        if (!url.endsWith("/api")) {
            if (url.endsWith("/")) {
                url = url + "api";
            } else {
                url = url + "/api";
            }
            etServerUrl.setText(url);
        }

        progressBar.setVisibility(View.VISIBLE);
        btnProbarConexion.setEnabled(false);

        final String finalUrl = url;

        // Probar conexión en segundo plano
        new Thread(() -> {
            try {
                URL testUrl = new URL(finalUrl + "/ping");
                HttpURLConnection conn = (HttpURLConnection) testUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnProbarConexion.setEnabled(true);

                    if (responseCode == 200) {
                        Toast.makeText(this, "✅ Conexión exitosa", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "⚠️ Servidor responde pero con código: " + responseCode, Toast.LENGTH_LONG).show();
                    }
                });

                conn.disconnect();

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnProbarConexion.setEnabled(true);
                    Toast.makeText(this, "❌ Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void guardarYContinuar() {
        String url = etServerUrl.getText().toString().trim();

        if (TextUtils.isEmpty(url)) {
            etServerUrl.setError("Ingrese la URL del servidor");
            return;
        }

        // Validar formato
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            etServerUrl.setError("La URL debe comenzar con http:// o https://");
            return;
        }

        // Agregar /api si no está
        if (!url.endsWith("/api")) {
            if (url.endsWith("/")) {
                url = url + "api";
            } else {
                url = url + "/api";
            }
        }

        // Guardar URL
        ApiConfig.setBaseUrl(this, url);

        Toast.makeText(this, "✅ Configuración guardada", Toast.LENGTH_SHORT).show();

        // Ir a pantalla de login
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // No permitir volver atrás si no hay URL configurada
        String urlGuardada = ApiConfig.getBaseUrl(this);
        if (urlGuardada == null || urlGuardada.isEmpty()) {
            Toast.makeText(this, "Debe configurar la URL del servidor", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }
}
