package com.example.systemapp.ui.revisiones;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systemapp.R;
import com.example.systemapp.data.AdminSQLiteOpenHelperRevisiones;
import com.example.systemapp.data.model.DBOrdenRevision;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment para lista de revisiones ejecutadas
 * Permite reapertura de órdenes (máximo 3 modificaciones)
 */
public class Fragment_ejecutadas_revision extends Fragment {

    private RecyclerView rvEjecutadas;
    private TextView tvContador;
    private TextView tvSinDatos;
    private TextInputEditText etBuscar;
    private EjecutadasRevisionAdapter adapter;
    private AdminSQLiteOpenHelperRevisiones dbHelper;
    private List<DBOrdenRevision> ejecutadasList;
    private List<DBOrdenRevision> ejecutadasOriginal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ejecutadas_revision, container, false);

        // Inicializar vistas
        rvEjecutadas = root.findViewById(R.id.rv_ejecutadas_revision);
        tvContador = root.findViewById(R.id.tv_contador_ejecutadas);
        tvSinDatos = root.findViewById(R.id.tv_sin_datos_ejecutadas);
        etBuscar = root.findViewById(R.id.et_buscar_ejecutada);

        // Inicializar base de datos
        dbHelper = new AdminSQLiteOpenHelperRevisiones(getContext());

        // Configurar RecyclerView
        rvEjecutadas.setLayoutManager(new LinearLayoutManager(getContext()));
        ejecutadasList = new ArrayList<>();
        ejecutadasOriginal = new ArrayList<>();

        adapter = new EjecutadasRevisionAdapter(getContext(), ejecutadasList);
        rvEjecutadas.setAdapter(adapter);

        // Configurar listeners
        setupListeners();

        // Cargar datos
        cargarEjecutadas();

        return root;
    }

    private void setupListeners() {
        // Click en item: intentar reabrir o mostrar mensaje
        adapter.setOnItemClickListener(orden -> {
            verificarYReabrir(orden);
        });

        // Búsqueda
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarEjecutadas(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void cargarEjecutadas() {
        // Cargar órdenes ejecutadas y procesadas
        ejecutadasList.clear();
        ejecutadasOriginal.clear();

        List<DBOrdenRevision> ejecutadas = dbHelper.getRevisionesByEstado("EJECUTADA");
        List<DBOrdenRevision> procesadas = dbHelper.getRevisionesByEstado("PROCESADA");

        if (ejecutadas != null) {
            ejecutadasList.addAll(ejecutadas);
            ejecutadasOriginal.addAll(ejecutadas);
        }

        if (procesadas != null) {
            ejecutadasList.addAll(procesadas);
            ejecutadasOriginal.addAll(procesadas);
        }

        if (ejecutadasList.isEmpty()) {
            tvSinDatos.setVisibility(View.VISIBLE);
            rvEjecutadas.setVisibility(View.GONE);
        } else {
            tvSinDatos.setVisibility(View.GONE);
            rvEjecutadas.setVisibility(View.VISIBLE);
        }

        actualizarContador();
        adapter.updateList(ejecutadasList);
    }

    private void filtrarEjecutadas(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            ejecutadasList.clear();
            ejecutadasList.addAll(ejecutadasOriginal);
        } else {
            String busqueda = texto.trim().toUpperCase();
            ejecutadasList.clear();

            for (DBOrdenRevision orden : ejecutadasOriginal) {
                String medidor = orden.getRef_Medidor() != null ? orden.getRef_Medidor().toUpperCase() : "";
                String nombre = orden.getNombre() != null ? orden.getNombre().toUpperCase() : "";

                if (medidor.contains(busqueda) || nombre.contains(busqueda)) {
                    ejecutadasList.add(orden);
                }
            }
        }

        if (ejecutadasList.isEmpty()) {
            tvSinDatos.setVisibility(View.VISIBLE);
            rvEjecutadas.setVisibility(View.GONE);
        } else {
            tvSinDatos.setVisibility(View.GONE);
            rvEjecutadas.setVisibility(View.VISIBLE);
        }

        actualizarContador();
        adapter.updateList(ejecutadasList);
    }

    private void actualizarContador() {
        tvContador.setText("Total: " + ejecutadasList.size() + " revisiones ejecutadas");
    }

    private void verificarYReabrir(DBOrdenRevision orden) {
        // Verificar si ya fue enviada a la API
        if ("SI".equals(orden.getEnviado_api())) {
            Toast.makeText(getContext(),
                "Esta revisión ya fue enviada a la API y no puede ser modificada",
                Toast.LENGTH_LONG).show();
            return;
        }

        // Verificar si puede ser modificada (máximo 3 veces)
        if (!orden.puedeSerModificada()) {
            Toast.makeText(getContext(),
                "Esta revisión ha alcanzado el límite de 3 modificaciones",
                Toast.LENGTH_LONG).show();
            return;
        }

        // Mostrar diálogo de confirmación
        mostrarDialogReapertura(orden);
    }

    private void mostrarDialogReapertura(DBOrdenRevision orden) {
        Integer modificaciones = orden.getCantidad_modificaciones() != null ?
            orden.getCantidad_modificaciones() : 0;
        int restantes = 3 - modificaciones;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Reabrir revisión");
        builder.setMessage(
            "Medidor: " + orden.getRef_Medidor() + "\n" +
            "Nombre: " + orden.getNombre() + "\n\n" +
            "Modificaciones realizadas: " + modificaciones + "/3\n" +
            "Modificaciones restantes: " + restantes + "\n\n" +
            "¿Desea reabrir esta revisión para modificarla?\n\n" +
            "Al confirmar, el contador de modificaciones se incrementará."
        );

        builder.setPositiveButton("Reabrir", (dialog, which) -> {
            // Incrementar contador de modificaciones
            orden.setCantidad_modificaciones(modificaciones + 1);

            // Cambiar estado a EN_EJECUCION
            orden.setEstado("EN_EJECUCION");

            // Guardar en BD
            dbHelper.insertOrUpdateRevision(orden, true);

            // Navegar a Fragment_form_revision con los 6 tabs (modo edición)
            Fragment_form_revision formFragment = Fragment_form_revision.newInstance(orden.getId());

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, formFragment)
                    .addToBackStack(null)
                    .commit();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarEjecutadas(); // Recargar al volver al fragment
    }
}
