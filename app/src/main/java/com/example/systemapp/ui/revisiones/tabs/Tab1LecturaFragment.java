package com.example.systemapp.ui.revisiones.tabs;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android:view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.systemapp.R;
import com.example.systemapp.data.model.DBOrdenRevision;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Tab 1: Toma de Lectura
 */
public class Tab1LecturaFragment extends Fragment {

    private DBOrdenRevision orden;
    private TextInputEditText etLecturaAnterior;
    private TextInputEditText etLecturaActual;
    private TextInputEditText etConsumo;
    private TextView tvIndicador;

    public static Tab1LecturaFragment newInstance(DBOrdenRevision orden) {
        Tab1LecturaFragment fragment = new Tab1LecturaFragment();
        fragment.orden = orden;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.tab1_lectura, container, false);

        etLecturaAnterior = root.findViewById(R.id.et_lectura_anterior);
        etLecturaActual = root.findViewById(R.id.et_lectura_actual);
        etConsumo = root.findViewById(R.id.et_consumo);
        tvIndicador = root.findViewById(R.id.tv_indicador_consumo);

        cargarDatos();
        setupListeners();

        return root;
    }

    private void cargarDatos() {
        if (orden != null) {
            // Lectura anterior
            String lecturaAnt = orden.getLA() != null ? orden.getLA() : "0";
            etLecturaAnterior.setText(lecturaAnt);

            // Lectura actual si ya fue ingresada
            if (orden.getLectura_actual() != null && orden.getLectura_actual() > 0) {
                etLecturaActual.setText(String.valueOf(orden.getLectura_actual()));
                calcularConsumo();
            }
        }
    }

    private void setupListeners() {
        etLecturaActual.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularConsumo();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void calcularConsumo() {
        try {
            String lectAntStr = etLecturaAnterior.getText().toString();
            String lectActStr = etLecturaActual.getText().toString();

            if (!lectActStr.isEmpty()) {
                int lecturaAnterior = Integer.parseInt(lectAntStr.isEmpty() ? "0" : lectAntStr);
                int lecturaActual = Integer.parseInt(lectActStr);

                int consumo = lecturaActual - lecturaAnterior;

                etConsumo.setText(String.valueOf(consumo));

                // Actualizar modelo
                if (orden != null) {
                    orden.setLectura_actual(lecturaActual);
                    orden.setConsumo(consumo);
                }

                // Indicador visual
                if (consumo < 0) {
                    tvIndicador.setText("⚠️ Lectura actual menor que anterior");
                    tvIndicador.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                } else {
                    tvIndicador.setText("✓ Consumo: " + consumo + " m³");
                    tvIndicador.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                }
            } else {
                etConsumo.setText("");
                tvIndicador.setText("Ingrese la lectura actual");
                tvIndicador.setBackgroundColor(getResources().getColor(R.color.design_default_color_primary_variant));
            }
        } catch (NumberFormatException e) {
            etConsumo.setText("");
        }
    }

    public boolean validar() {
        String lectActual = etLecturaActual.getText().toString();
        if (lectActual.isEmpty()) {
            etLecturaActual.setError("Lectura actual requerida");
            return false;
        }

        try {
            int consumo = Integer.parseInt(etConsumo.getText().toString());
            if (consumo < 0) {
                etLecturaActual.setError("Lectura actual no puede ser menor que la anterior");
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
