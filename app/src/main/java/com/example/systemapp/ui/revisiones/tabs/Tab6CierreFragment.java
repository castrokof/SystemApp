package com.example.systemapp.ui.revisiones.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.systemapp.R;
import com.example.systemapp.data.model.DBOrdenRevision;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Tab 6: Observación General y Cierre
 */
public class Tab6CierreFragment extends Fragment {

    private DBOrdenRevision orden;
    private TextInputEditText etObservacionGeneral;
    private TextView tvResumenLectura;
    private TextView tvResumenFirma;
    private TextView tvResumenCenso;
    private TextView tvResumenCausa;
    private Button btnCerrar;
    private Button btnGenerarPdf;
    private Button btnImprimir;
    private Button btnEnviarApi;

    private OnCierreListener listener;

    public interface OnCierreListener {
        void onCerrarRevision();
        void onGenerarPDF();
        void onImprimir();
        void onEnviarAPI();
    }

    public static Tab6CierreFragment newInstance(DBOrdenRevision orden) {
        Tab6CierreFragment fragment = new Tab6CierreFragment();
        fragment.orden = orden;
        return fragment;
    }

    public void setOnCierreListener(OnCierreListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.tab6_cierre, container, false);

        etObservacionGeneral = root.findViewById(R.id.et_observacion_general);
        tvResumenLectura = root.findViewById(R.id.tv_resumen_lectura);
        tvResumenFirma = root.findViewById(R.id.tv_resumen_firma);
        tvResumenCenso = root.findViewById(R.id.tv_resumen_censo);
        tvResumenCausa = root.findViewById(R.id.tv_resumen_causa);
        btnCerrar = root.findViewById(R.id.btn_cerrar_revision);
        btnGenerarPdf = root.findViewById(R.id.btn_generar_pdf);
        btnImprimir = root.findViewById(R.id.btn_imprimir);
        btnEnviarApi = root.findViewById(R.id.btn_enviar_api);

        cargarDatos();
        setupListeners();
        actualizarResumen();

        return root;
    }

    private void cargarDatos() {
        if (orden != null && orden.getObservacion_general() != null) {
            etObservacionGeneral.setText(orden.getObservacion_general());
        }
    }

    private void setupListeners() {
        btnCerrar.setOnClickListener(v -> {
            if (validar()) {
                if (listener != null) {
                    listener.onCerrarRevision();
                }
            }
        });

        btnGenerarPdf.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGenerarPDF();
            }
        });

        btnImprimir.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImprimir();
            }
        });

        btnEnviarApi.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEnviarAPI();
            }
        });
    }

    public void actualizarResumen() {
        if (orden == null) return;

        // Resumen lectura
        Integer consumo = orden.getConsumo();
        if (consumo != null && consumo > 0) {
            tvResumenLectura.setText("✓ Lectura: " + consumo + " m³");
        } else {
            tvResumenLectura.setText("✗ Lectura: Pendiente");
        }

        // Resumen firma
        if (orden.getFirma_path() != null && !orden.getFirma_path().isEmpty()) {
            tvResumenFirma.setText("✓ Firma: Capturada");
            tvResumenFirma.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvResumenFirma.setText("✗ Firma: Pendiente");
            tvResumenFirma.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // Resumen censo (simplificado)
        tvResumenCenso.setText("✓ Censo completado");

        // Resumen causa
        if (orden.getCodigo_causa() != null && orden.getCodigo_causa() > 0) {
            tvResumenCausa.setText("✓ Causa: " + (orden.getDesc_causa() != null ? orden.getDesc_causa() : "Seleccionada"));
            tvResumenCausa.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvResumenCausa.setText("✗ Causa: No seleccionada");
            tvResumenCausa.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    public boolean validar() {
        // Guardar observación general
        if (orden != null) {
            orden.setObservacion_general(etObservacionGeneral.getText().toString().trim());
        }

        // Validar que tenga datos mínimos
        if (orden == null) {
            return false;
        }

        if (orden.getConsumo() == null || orden.getConsumo() <= 0) {
            Toast.makeText(getContext(), "Debe completar la lectura en Tab 1", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (orden.getFirma_path() == null || orden.getFirma_path().isEmpty()) {
            Toast.makeText(getContext(), "Debe capturar la firma en Tab 2", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (orden.getCodigo_causa() == null || orden.getCodigo_causa() <= 0) {
            Toast.makeText(getContext(), "Debe seleccionar la causa en Tab 5", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
