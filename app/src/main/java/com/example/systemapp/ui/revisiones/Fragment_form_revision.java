package com.example.systemapp.ui.revisiones;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.systemapp.R;
import com.example.systemapp.data.AdminSQLiteOpenHelperRevisiones;
import com.example.systemapp.data.model.DBOrdenRevision;
import com.example.systemapp.ui.revisiones.tabs.Tab6CierreFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment principal con sistema de 6 tabs para formulario de revisión
 */
public class Fragment_form_revision extends Fragment {

    private DBOrdenRevision orden;
    private String ordenId;

    private TextView tvHeaderMedidor;
    private TextView tvHeaderNombre;
    private TextView tvHeaderTipo;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabCamera;

    private RevisionTabsAdapter tabsAdapter;
    private AdminSQLiteOpenHelperRevisiones dbHelper;

    public static Fragment_form_revision newInstance(String ordenId) {
        Fragment_form_revision fragment = new Fragment_form_revision();
        Bundle args = new Bundle();
        args.putString("orden_id", ordenId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_form_revision, container, false);

        // Obtener ID de la orden
        if (getArguments() != null) {
            ordenId = getArguments().getString("orden_id");
        }

        dbHelper = new AdminSQLiteOpenHelperRevisiones(getContext());

        // Inicializar vistas
        tvHeaderMedidor = root.findViewById(R.id.tv_header_medidor);
        tvHeaderNombre = root.findViewById(R.id.tv_header_nombre);
        tvHeaderTipo = root.findViewById(R.id.tv_header_tipo);
        tabLayout = root.findViewById(R.id.tab_layout);
        viewPager = root.findViewById(R.id.view_pager);
        fabCamera = root.findViewById(R.id.fab_camera);

        cargarOrden();
        configurarTabs();
        setupListeners();

        return root;
    }

    private void cargarOrden() {
        if (ordenId != null) {
            // Buscar orden en BD
            orden = dbHelper.getRevisionById(ordenId);

            if (orden != null) {
                // Actualizar header
                tvHeaderMedidor.setText("Medidor: " + (orden.getRef_Medidor() != null ? orden.getRef_Medidor() : "N/A"));
                tvHeaderNombre.setText("Nombre: " + (orden.getNombre() != null ? orden.getNombre() : "Sin nombre"));

                String tipo = orden.getTipo_desviacion() != null ? orden.getTipo_desviacion() + " CONSUMO" : "N/A";
                tvHeaderTipo.setText("Tipo: " + tipo);

                // Registrar fecha de inicio si no existe
                if (orden.getFecha_inicio() == null || orden.getFecha_inicio().isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    orden.setFecha_inicio(sdf.format(new Date()));
                    dbHelper.insertOrUpdateRevision(orden, true);
                }
            }
        }
    }

    private void configurarTabs() {
        // Crear adapter
        tabsAdapter = new RevisionTabsAdapter(getActivity(), orden);
        viewPager.setAdapter(tabsAdapter);

        // Vincular TabLayout con ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("1. Lectura"); break;
                case 1: tab.setText("2. Residente"); break;
                case 2: tab.setText("3. Acometida"); break;
                case 3: tab.setText("4. Censos"); break;
                case 4: tab.setText("5. Clasificación"); break;
                case 5: tab.setText("6. Cierre"); break;
            }
        }).attach();

        // Listener para cambio de tabs
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Auto-guardar al cambiar de tab
                guardarProgreso();

                // Actualizar resumen en Tab 6
                if (position == 5 && tabsAdapter.getTab6() != null) {
                    tabsAdapter.getTab6().actualizarResumen();
                }
            }
        });
    }

    private void setupListeners() {
        // FAB de cámara (placeholder)
        fabCamera.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Función de cámara en desarrollo", Toast.LENGTH_SHORT).show();
        });

        // Esperar a que el Tab6 esté creado para agregar listeners
        viewPager.post(() -> {
            if (tabsAdapter.getTab6() != null) {
                tabsAdapter.getTab6().setOnCierreListener(new Tab6CierreFragment.OnCierreListener() {
                    @Override
                    public void onCerrarRevision() {
                        cerrarRevision();
                    }

                    @Override
                    public void onGenerarPDF() {
                        generarPDF();
                    }

                    @Override
                    public void onImprimir() {
                        imprimir();
                    }

                    @Override
                    public void onEnviarAPI() {
                        enviarAPI();
                    }
                });
            }
        });
    }

    private void guardarProgreso() {
        if (orden == null) return;

        // Validar y guardar datos del tab actual
        int currentTab = viewPager.getCurrentItem();

        try {
            switch (currentTab) {
                case 0:
                    if (tabsAdapter.getTab1() != null) {
                        tabsAdapter.getTab1().validar();
                    }
                    break;
                case 1:
                    if (tabsAdapter.getTab2() != null) {
                        tabsAdapter.getTab2().validar();
                    }
                    break;
                case 2:
                    if (tabsAdapter.getTab3() != null) {
                        tabsAdapter.getTab3().validar();
                    }
                    break;
                case 3:
                    if (tabsAdapter.getTab4() != null) {
                        tabsAdapter.getTab4().validar();
                    }
                    break;
                case 4:
                    if (tabsAdapter.getTab5() != null) {
                        tabsAdapter.getTab5().validar();
                    }
                    break;
                case 5:
                    if (tabsAdapter.getTab6() != null) {
                        tabsAdapter.getTab6().validar();
                    }
                    break;
            }

            // Guardar en BD
            dbHelper.insertOrUpdateRevision(orden, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cerrarRevision() {
        // Validar todos los tabs
        boolean valido = true;

        if (tabsAdapter.getTab1() == null || !tabsAdapter.getTab1().validar()) valido = false;
        if (tabsAdapter.getTab2() == null || !tabsAdapter.getTab2().validar()) valido = false;
        if (tabsAdapter.getTab3() == null || !tabsAdapter.getTab3().validar()) valido = false;
        if (tabsAdapter.getTab4() == null || !tabsAdapter.getTab4().validar()) valido = false;
        if (tabsAdapter.getTab5() == null || !tabsAdapter.getTab5().validar()) valido = false;
        if (tabsAdapter.getTab6() == null || !tabsAdapter.getTab6().validar()) valido = false;

        if (!valido) {
            Toast.makeText(getContext(), "Complete todos los campos requeridos", Toast.LENGTH_LONG).show();
            return;
        }

        // Marcar como ejecutada
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        orden.setFecha_cierre(sdf.format(new Date()));
        orden.setEstado("EJECUTADA");

        // Guardar censo hidráulico
        if (tabsAdapter.getTab4() != null && tabsAdapter.getTab4().getElementosHidraulicos() != null) {
            for (var elemento : tabsAdapter.getTab4().getElementosHidraulicos()) {
                dbHelper.insertCensoHidraulico(elemento);
            }
        }

        dbHelper.insertOrUpdateRevision(orden, true);

        Toast.makeText(getContext(), "✓ Revisión cerrada correctamente", Toast.LENGTH_SHORT).show();

        // Volver a la lista
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    private void generarPDF() {
        // TODO: Implementar generación de PDF
        Toast.makeText(getContext(), "Generación de PDF en desarrollo", Toast.LENGTH_SHORT).show();
    }

    private void imprimir() {
        // TODO: Implementar impresión Bluetooth
        Toast.makeText(getContext(), "Impresión en desarrollo", Toast.LENGTH_SHORT).show();
    }

    private void enviarAPI() {
        // TODO: Implementar envío a API
        Toast.makeText(getContext(), "Envío a API en desarrollo", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        guardarProgreso();
    }
}
