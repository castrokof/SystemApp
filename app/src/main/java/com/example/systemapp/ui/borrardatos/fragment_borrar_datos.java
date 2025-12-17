package com.example.systemapp.ui.borrardatos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.systemapp.R;
import com.example.systemapp.data.AdminSQLiteOpenHelper;
import com.example.systemapp.data.SessionPrefs;
import com.example.systemapp.data.VariablesSesion;
import com.example.systemapp.data.model.DBdefinicionOrdenes;
import com.example.systemapp.databinding.FragmentBorrarDatosBinding;

public class fragment_borrar_datos extends Fragment {

    private static final String TAG = "BorrarDatosFragment";

    // objeto para transacciones con BD
    private AdminSQLiteOpenHelper adminSQLiteOpenHelper;
    private FragmentBorrarDatosBinding binding;

    // Vistas
    private View backDisabled;
    private Button btnCierre;
    private CardView cardProgress;
    private ProgressBar pgBCierre;
    private TextView txtProgressMessage;
    private TextView txtTotalAsignadas;
    private TextView txtProcesadas;
    private TextView txtPendientes;
    private TextView txtPendEnvio;

    // Items para animación
    private View itemAsignadas;
    private View itemProcesadas;
    private View itemPendientes;
    private View itemPendEnvio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentBorrarDatosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configurar ActionBar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setTitle("Cierre de Ruta");
        }

        // Inicializar vistas
        initViews(root);

        // Cargar datos
        loadData();

        // Configurar botón de cierre
        setupCierreButton();

        // Animar estadísticas
        animateStats();

        return root;
    }

    private void initViews(View root) {
        // Progress
        backDisabled = root.findViewById(R.id.back_disabled);
        cardProgress = root.findViewById(R.id.card_progress);
        pgBCierre = root.findViewById(R.id.pgB_cierre);
        txtProgressMessage = root.findViewById(R.id.txt_progress_message);

        // Botón
        btnCierre = root.findViewById(R.id.btn_cierre);

        // TextViews de números
        txtTotalAsignadas = root.findViewById(R.id.txtTotalAsignadas);
        txtProcesadas = root.findViewById(R.id.txtProcesadas);
        txtPendientes = root.findViewById(R.id.txtPendientes);
        txtPendEnvio = root.findViewById(R.id.txtPend_envio);

        // Items completos para animación
        itemAsignadas = root.findViewById(R.id.item_asignadas);
        itemProcesadas = root.findViewById(R.id.item_procesadas);
        itemPendientes = root.findViewById(R.id.item_pendientes);
        itemPendEnvio = root.findViewById(R.id.item_pend_envio);
    }

    private void loadData() {
        // Abrir acceso a las preferencias
        final SharedPreferences mPrefs = getActivity().getSharedPreferences("SYSTEMAPP_PREFS", Context.MODE_PRIVATE);

        // Instanciar DBHelper
        adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(getContext());

        // Obtener cantidades
        int cantidadPendEnvio = adminSQLiteOpenHelper.getCount("lecturas",
                "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA'" +
                        " AND (Uploadlec IS NULL OR Uploadlec = '') ");

        int cantidadProcesadas = adminSQLiteOpenHelper.getCount("lecturas",
                "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA'");

        int cantidadAsignadas = adminSQLiteOpenHelper.getCount("lecturas",
                "Tipo_orden = 'RUTAS' ");

        int cantidadPendientes = mPrefs.getInt("PREF_RUTAS_PENDIENTES", 0) +
                mPrefs.getInt("PREF_RUTAS_REASIGNADAS", 0);

        // Actualizar UI
        updateStats(cantidadAsignadas, cantidadProcesadas, cantidadPendientes, cantidadPendEnvio);

        Log.d(TAG, "Datos cargados - Asignadas: " + cantidadAsignadas +
                ", Procesadas: " + cantidadProcesadas +
                ", Pendientes: " + cantidadPendientes +
                ", Pend. Envío: " + cantidadPendEnvio);
    }

    private void updateStats(int asignadas, int procesadas, int pendientes, int pendEnvio) {
        txtTotalAsignadas.setText(String.valueOf(asignadas));
        txtProcesadas.setText(String.valueOf(procesadas));
        txtPendientes.setText(String.valueOf(pendientes));
        txtPendEnvio.setText(String.valueOf(pendEnvio));
    }

    private void setupCierreButton() {
        btnCierre.setOnClickListener(v -> {
            // Animación de botón presionado
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();

                        // Obtener cantidad de pendientes de envío
                        int cantidadPendEnvio = adminSQLiteOpenHelper.getCount("lecturas",
                                "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA'" +
                                        " AND (Uploadlec IS NULL OR Uploadlec = '') ");

                        SharedPreferences mPrefs = getActivity().getSharedPreferences("SYSTEMAPP_PREFS", Context.MODE_PRIVATE);
                        int cantPendientes = mPrefs.getInt("PREF_RUTAS_PENDIENTES", 0);

                        // Mostrar diálogo de confirmación
                        displayPromptForConfirmLogout(getActivity(), cantPendientes, cantidadPendEnvio);
                    })
                    .start();
        });
    }

    private void animateStats() {
        View[] items = {itemAsignadas, itemProcesadas, itemPendientes, itemPendEnvio};

        for (int i = 0; i < items.length; i++) {
            View item = items[i];
            item.setAlpha(0f);
            item.setTranslationY(50f);

            item.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .setStartDelay(i * 100L)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    private void showProgress(boolean show) {
        if (show) {
            backDisabled.setVisibility(View.VISIBLE);
            cardProgress.setVisibility(View.VISIBLE);
            btnCierre.setEnabled(false);

            // Animar aparición
            cardProgress.setAlpha(0f);
            cardProgress.setScaleX(0.9f);
            cardProgress.setScaleY(0.9f);
            cardProgress.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start();
        } else {
            backDisabled.setVisibility(View.GONE);
            cardProgress.setVisibility(View.GONE);
            btnCierre.setEnabled(true);
        }
    }

    public void displayPromptForConfirmLogout(final Activity activity, int cantPendingRoutes, int cantidadPendEnvio) {

        if (cantidadPendEnvio > 0) {
            // Mostrar toast y diálogo informativo
            Toast.makeText(getActivity(), "⚠️ Primero envíe los ejecutados", Toast.LENGTH_LONG).show();

            new AlertDialog.Builder(activity)
                    .setTitle("⚠️ Advertencia")
                    .setMessage("Hay " + cantidadPendEnvio + " lectura(s) pendiente(s) de envío.\n\n" +
                            "Por favor, envíe primero todas las lecturas antes de cerrar la ruta.")
                    .setPositiveButton("Entendido", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return;
        }

        // Mensaje de confirmación
        String message = getString(R.string.req_comfirm_cierre_ruta, cantPendingRoutes);

        new AlertDialog.Builder(activity)
                .setTitle("🗑️ Confirmar Cierre de Ruta")
                .setMessage(message + "\n\n⚠️ Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, Cerrar Ruta", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int id) {
                        cierreRuta(activity);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(activity, "Operación cancelada", Toast.LENGTH_SHORT).show();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(false)
                .show();
    }

    public void cierreRuta(Activity activity) {
        final Activity activity1 = activity;

        // Mostrar progreso
        showProgress(true);
        txtProgressMessage.setText("Eliminando datos...");

        // Deshabilitar interacción con la pantalla
        getActivity().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        );

        // Simular proceso de cierre con delay (para mostrar animación)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                Log.d(TAG, "Iniciando cierre de ruta...");

                // Actualizar mensaje
                txtProgressMessage.setText("Limpiando base de datos...");

                // Limpiar los contadores
                SessionPrefs.get(activity1).setPrefRutasPendientes(0);
                SessionPrefs.get(activity1).setPrefRutasReasignadas(0);
                SessionPrefs.get(activity1).setPrefRutasProcesadas(0);

                // Borrar la base de datos
                boolean deleted = activity1.deleteDatabase(DBdefinicionOrdenes.DATABASE_NAME);
                Log.d(TAG, "Base de datos eliminada: " + deleted);

                // Borrar datos de sesión
                VariablesSesion.setAllRutasGobal(null);
                VariablesSesion.setRutasGobalAsignadas(null);
                VariablesSesion.posicionSelec = 0;

                // Actualizar mensaje
                txtProgressMessage.setText("Actualizando interfaz...");

                // Esperar un momento antes de actualizar UI
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Animar cambio de números a 0
                    animateNumbersToZero();

                    // Habilitar interacción
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    // Ocultar progreso después de la animación
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        showProgress(false);

                        // Mostrar mensaje de éxito
                        Toast.makeText(getActivity(), "✅ Datos eliminados correctamente", Toast.LENGTH_LONG).show();

                        Log.d(TAG, "Cierre de ruta completado exitosamente");
                    }, 800);

                }, 500);

            } catch (Exception e) {
                Log.e(TAG, "Error durante cierre de ruta", e);

                // Habilitar interacción
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                showProgress(false);

                Toast.makeText(getActivity(), "❌ Error al eliminar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, 1000);
    }

    private void animateNumbersToZero() {
        TextView[] textViews = {txtTotalAsignadas, txtProcesadas, txtPendientes, txtPendEnvio};

        for (TextView tv : textViews) {
            tv.animate()
                    .alpha(0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        tv.setText("0");
                        tv.animate()
                                .alpha(1f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(300)
                                .start();
                    })
                    .start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}