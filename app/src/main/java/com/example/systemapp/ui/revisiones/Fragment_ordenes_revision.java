package com.example.systemapp.ui.revisiones;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systemapp.R;
import com.example.systemapp.data.AdminSQLiteOpenHelperRevisiones;
import com.example.systemapp.data.model.DBOrdenRevision;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment principal para el módulo de REVISIONES
 * Muestra lista de órdenes pendientes con reordenamiento drag and drop
 */
public class Fragment_ordenes_revision extends Fragment {

    private RecyclerView rvOrdenes;
    private TextView tvContador;
    private TextView tvSinDatos;
    private TextInputEditText etBuscar;
    private OrdenesRevisionAdapter adapter;
    private AdminSQLiteOpenHelperRevisiones dbHelper;
    private List<DBOrdenRevision> ordenesList;
    private List<DBOrdenRevision> ordenesOriginal;
    private ItemTouchHelper itemTouchHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ordenes_revision, container, false);

        // Inicializar vistas
        rvOrdenes = root.findViewById(R.id.rv_ordenes_revision);
        tvContador = root.findViewById(R.id.tv_contador_revisiones);
        tvSinDatos = root.findViewById(R.id.tv_sin_datos_revision);
        etBuscar = root.findViewById(R.id.et_buscar_revision);

        // Inicializar base de datos
        dbHelper = new AdminSQLiteOpenHelperRevisiones(getContext());

        // Configurar RecyclerView
        rvOrdenes.setLayoutManager(new LinearLayoutManager(getContext()));
        ordenesList = new ArrayList<>();
        ordenesOriginal = new ArrayList<>();

        adapter = new OrdenesRevisionAdapter(getContext(), ordenesList);
        rvOrdenes.setAdapter(adapter);

        // Configurar drag and drop
        setupDragAndDrop();

        // Configurar listeners
        setupListeners();

        // Cargar datos
        cargarOrdenes();

        return root;
    }

    private void setupDragAndDrop() {
        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                return makeMovementFlags(dragFlags, 0); // 0 = no swipe
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                // Mover item en el adapter
                adapter.moveItem(fromPosition, toPosition);

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // No implementado - no queremos swipe
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder.itemView.setAlpha(0.7f);
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setAlpha(1.0f);

                // Guardar nuevo orden en BD
                guardarNuevoOrden();
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false; // Desactivar long press, solo arrastre desde el handle
            }
        };

        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvOrdenes);
    }

    private void setupListeners() {
        // Click en item: mostrar modal de confirmación
        adapter.setOnItemClickListener(orden -> {
            mostrarDialogConfirmacion(orden);
        });

        // Iniciar drag cuando se toca el handle
        adapter.setOnStartDragListener(viewHolder -> {
            itemTouchHelper.startDrag(viewHolder);
        });

        // Búsqueda
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarOrdenes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void cargarOrdenes() {
        // Cargar órdenes pendientes ordenadas por orden_personalizado
        ordenesList.clear();
        ordenesOriginal.clear();

        List<DBOrdenRevision> ordenes = dbHelper.getRevisionesByEstado("PENDIENTE");

        if (ordenes != null && !ordenes.isEmpty()) {
            ordenesList.addAll(ordenes);
            ordenesOriginal.addAll(ordenes);
            tvSinDatos.setVisibility(View.GONE);
            rvOrdenes.setVisibility(View.VISIBLE);
        } else {
            tvSinDatos.setVisibility(View.VISIBLE);
            rvOrdenes.setVisibility(View.GONE);
        }

        actualizarContador();
        adapter.updateList(ordenesList);
    }

    private void filtrarOrdenes(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            ordenesList.clear();
            ordenesList.addAll(ordenesOriginal);
        } else {
            String busqueda = texto.trim().toUpperCase();
            ordenesList.clear();

            for (DBOrdenRevision orden : ordenesOriginal) {
                String medidor = orden.getRef_Medidor() != null ? orden.getRef_Medidor().toUpperCase() : "";
                String nombre = orden.getNombre() != null ? orden.getNombre().toUpperCase() : "";

                if (medidor.contains(busqueda) || nombre.contains(busqueda)) {
                    ordenesList.add(orden);
                }
            }
        }

        if (ordenesList.isEmpty()) {
            tvSinDatos.setVisibility(View.VISIBLE);
            rvOrdenes.setVisibility(View.GONE);
        } else {
            tvSinDatos.setVisibility(View.GONE);
            rvOrdenes.setVisibility(View.VISIBLE);
        }

        actualizarContador();
        adapter.updateList(ordenesList);
    }

    private void actualizarContador() {
        tvContador.setText("Total: " + ordenesList.size() + " revisiones");
    }

    private void guardarNuevoOrden() {
        // Actualizar orden_personalizado según la nueva posición
        List<DBOrdenRevision> ordenesActuales = adapter.getOrdenes();
        for (int i = 0; i < ordenesActuales.size(); i++) {
            DBOrdenRevision orden = ordenesActuales.get(i);
            orden.setOrden_personalizado(i + 1);
            dbHelper.insertOrUpdateRevision(orden, true);
        }

        // Actualizar también la lista original para mantener sincronización
        ordenesOriginal.clear();
        ordenesOriginal.addAll(ordenesActuales);

        // Actualizar visualmente los números
        adapter.notifyDataSetChanged();
    }

    private void mostrarDialogConfirmacion(DBOrdenRevision orden) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirmacion_apertura, null);

        // Referencias a las vistas del diálogo
        TextView tvMedidor = dialogView.findViewById(R.id.tv_dialog_medidor);
        TextView tvNombre = dialogView.findViewById(R.id.tv_dialog_nombre);
        TextView tvDireccion = dialogView.findViewById(R.id.tv_dialog_direccion);
        TextView tvTipo = dialogView.findViewById(R.id.tv_dialog_tipo);
        Button btnCancelar = dialogView.findViewById(R.id.btn_cancelar);
        Button btnConfirmar = dialogView.findViewById(R.id.btn_confirmar);

        // Llenar datos
        tvMedidor.setText("Medidor: " + (orden.getRef_Medidor() != null ? orden.getRef_Medidor() : "N/A"));
        tvNombre.setText("Nombre: " + (orden.getNombre() != null ? orden.getNombre() : "Sin nombre"));
        tvDireccion.setText("Dirección: " + (orden.getDireccion() != null ? orden.getDireccion() : "Sin dirección"));

        String tipoDesv = orden.getTipo_desviacion() != null ? orden.getTipo_desviacion() : "N/A";
        tvTipo.setText("Tipo desviación: " + tipoDesv + " CONSUMO");

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            // Cambiar estado a EN_EJECUCION
            orden.setEstado("EN_EJECUCION");
            dbHelper.insertOrUpdateRevision(orden, true);

            // TODO: Navegar a Fragment_form_revision con los 6 tabs
            Toast.makeText(getContext(), "Orden abierta. Navegando al formulario...", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
            cargarOrdenes(); // Recargar lista
        });

        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarOrdenes(); // Recargar al volver al fragment
    }
}
