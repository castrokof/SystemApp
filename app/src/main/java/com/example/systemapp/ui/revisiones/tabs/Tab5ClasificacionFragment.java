package com.example.systemapp.ui.revisiones.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.systemapp.R;
import com.example.systemapp.data.AdminSQLiteOpenHelperRevisiones;
import com.example.systemapp.data.model.DBCausaDesviacion;
import com.example.systemapp.data.model.DBOrdenRevision;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab 5: Clasificación de Desviación
 */
public class Tab5ClasificacionFragment extends Fragment {

    private DBOrdenRevision orden;
    private Spinner spinnerCausa;
    private TextInputEditText etObservacionCausa;
    private List<DBCausaDesviacion> causas;
    private AdminSQLiteOpenHelperRevisiones dbHelper;

    public static Tab5ClasificacionFragment newInstance(DBOrdenRevision orden) {
        Tab5ClasificacionFragment fragment = new Tab5ClasificacionFragment();
        fragment.orden = orden;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.tab5_clasificacion, container, false);

        spinnerCausa = root.findViewById(R.id.spinner_causa_desviacion);
        etObservacionCausa = root.findViewById(R.id.et_observacion_causa);

        dbHelper = new AdminSQLiteOpenHelperRevisiones(getContext());

        cargarCausas();
        cargarDatos();

        return root;
    }

    private void cargarCausas() {
        // Cargar causas desde BD
        causas = dbHelper.getAllCausas();

        if (causas == null || causas.isEmpty()) {
            // Si no hay causas, crear causas predeterminadas
            causas = crearCausasPredeterminadas();
        }

        // Configurar spinner
        ArrayAdapter<DBCausaDesviacion> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, causas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCausa.setAdapter(adapter);
    }

    private List<DBCausaDesviacion> crearCausasPredeterminadas() {
        List<DBCausaDesviacion> lista = new ArrayList<>();

        // Causas para ALTO consumo
        lista.add(new DBCausaDesviacion(1, "ALTO", "Fuga interna"));
        lista.add(new DBCausaDesviacion(2, "ALTO", "Fuga externa"));
        lista.add(new DBCausaDesviacion(3, "ALTO", "Aumento de habitantes"));
        lista.add(new DBCausaDesviacion(4, "ALTO", "Medidor dañado"));
        lista.add(new DBCausaDesviacion(5, "ALTO", "Uso indebido"));
        lista.add(new DBCausaDesviacion(6, "ALTO", "Llenado de tanque/piscina"));

        // Causas para BAJO consumo
        lista.add(new DBCausaDesviacion(7, "BAJO", "Predio desocupado"));
        lista.add(new DBCausaDesviacion(8, "BAJO", "Disminución de habitantes"));
        lista.add(new DBCausaDesviacion(9, "BAJO", "Medidor bloqueado"));
        lista.add(new DBCausaDesviacion(10, "BAJO", "Suspensión temporal"));

        return lista;
    }

    private void cargarDatos() {
        if (orden != null) {
            // Observación de causa
            if (orden.getObservacion_causa() != null) {
                etObservacionCausa.setText(orden.getObservacion_causa());
            }

            // Seleccionar causa si ya está guardada
            if (orden.getCodigo_causa() != null) {
                for (int i = 0; i < causas.size(); i++) {
                    if (causas.get(i).getCodigo() == orden.getCodigo_causa()) {
                        spinnerCausa.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    public boolean validar() {
        if (spinnerCausa.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Debe seleccionar una causa", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (orden != null) {
            DBCausaDesviacion causaSeleccionada = (DBCausaDesviacion) spinnerCausa.getSelectedItem();
            orden.setCodigo_causa(causaSeleccionada.getCodigo());
            orden.setDesc_causa(causaSeleccionada.getDescripcion());
            orden.setObservacion_causa(etObservacionCausa.getText().toString().trim());
        }

        return true;
    }
}
