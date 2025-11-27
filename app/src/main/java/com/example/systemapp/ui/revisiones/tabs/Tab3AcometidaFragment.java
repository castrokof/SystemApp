package com.example.systemapp.ui.revisiones.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.systemapp.R;
import com.example.systemapp.data.model.DBOrdenRevision;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Tab 3: Revisión de Acometida
 */
public class Tab3AcometidaFragment extends Fragment {

    private DBOrdenRevision orden;
    private Spinner spinnerEstadoAcometida;
    private Spinner spinnerEstadoSellos;
    private TextInputEditText etQueSurte;

    public static Tab3AcometidaFragment newInstance(DBOrdenRevision orden) {
        Tab3AcometidaFragment fragment = new Tab3AcometidaFragment();
        fragment.orden = orden;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.tab3_acometida, container, false);

        spinnerEstadoAcometida = root.findViewById(R.id.spinner_estado_acometida);
        spinnerEstadoSellos = root.findViewById(R.id.spinner_estado_sellos);
        etQueSurte = root.findViewById(R.id.et_que_surte);

        configurarSpinners();
        cargarDatos();

        return root;
    }

    private void configurarSpinners() {
        // Estado de acometida
        String[] estadosAcometida = {"BUENO", "REGULAR", "MALO", "DETERIORADO", "PENDIENTE REVISION"};
        ArrayAdapter<String> adapterAcometida = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, estadosAcometida);
        adapterAcometida.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstadoAcometida.setAdapter(adapterAcometida);

        // Estado de sellos
        String[] estadosSellos = {"BUENO", "ROTO", "FALTA", "NO APLICA"};
        ArrayAdapter<String> adapterSellos = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, estadosSellos);
        adapterSellos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstadoSellos.setAdapter(adapterSellos);
    }

    private void cargarDatos() {
        if (orden != null) {
            // Estado acometida
            if (orden.getEstado_acometida() != null) {
                setSpinnerValue(spinnerEstadoAcometida, orden.getEstado_acometida());
            }

            // Estado sellos
            if (orden.getEstado_sellos() != null) {
                setSpinnerValue(spinnerEstadoSellos, orden.getEstado_sellos());
            }

            // Qué surte
            if (orden.getQue_surte() != null) {
                etQueSurte.setText(orden.getQue_surte());
            }
        }
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    public boolean validar() {
        if (orden != null) {
            orden.setEstado_acometida(spinnerEstadoAcometida.getSelectedItem().toString());
            orden.setEstado_sellos(spinnerEstadoSellos.getSelectedItem().toString());
            orden.setQue_surte(etQueSurte.getText().toString().trim());
        }
        return true;
    }
}
