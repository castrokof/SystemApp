package com.example.systemapp.ui.revisiones;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.systemapp.R;
import com.example.systemapp.data.AdminSQLiteOpenHelperRevisiones;
import com.example.systemapp.data.model.DBFotoRevision;
import com.example.systemapp.data.model.DBOrdenRevision;
import com.example.systemapp.ui.revisiones.tabs.Tab6CierreFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
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
    private CameraHelper cameraHelper;
    private LocationHelper locationHelper;

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
        cameraHelper = new CameraHelper(getContext());
        locationHelper = new LocationHelper(getContext());

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

                // Registrar fecha de inicio y ubicación si no existe
                if (orden.getFecha_inicio() == null || orden.getFecha_inicio().isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    orden.setFecha_inicio(sdf.format(new Date()));

                    // Capturar ubicación GPS al inicio
                    capturarUbicacionInicio();
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
        // FAB de cámara
        fabCamera.setOnClickListener(v -> {
            abrirCameraGeneral();
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

        // Capturar ubicación GPS al cerrar (si no existe)
        if (orden.getLatitud() == null || orden.getLatitud().isEmpty()) {
            capturarUbicacionCierre();
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
        Toast.makeText(getContext(), "Generando PDF...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                PDFGenerator pdfGenerator = new PDFGenerator(getContext());
                File pdfFile = pdfGenerator.generarPDF(orden);

                // Guardar ruta en BD
                dbHelper.insertOrUpdateRevision(orden, true);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                            "✓ PDF generado: " + pdfFile.getName(),
                            Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                            "Error al generar PDF: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    });
                }
                e.printStackTrace();
            }
        }).start();
    }

    private void imprimir() {
        Toast.makeText(getContext(), "Impresión Bluetooth disponible cuando se conecte impresora", Toast.LENGTH_SHORT).show();
        // TODO: Implementar selección de impresora Bluetooth y envío
        // BluetoothPrinter printer = new BluetoothPrinter(getContext(), outputStream);
        // printer.imprimirRevision(orden);
    }

    private void enviarAPI() {
        Toast.makeText(getContext(), "Enviando a API...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                APISync apiSync = new APISync(getContext());
                boolean success = apiSync.enviarRevision(orden);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(getContext(),
                                "✓ Revisión enviada exitosamente a la API",
                                Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(),
                                "Error al enviar a la API",
                                Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    });
                }
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Abrir cámara para foto general
     */
    private void abrirCameraGeneral() {
        // Verificar permiso
        if (!cameraHelper.hasPermission()) {
            cameraHelper.requestPermission(getActivity());
            return;
        }

        try {
            int currentTab = viewPager.getCurrentItem() + 1; // 1-based
            File photoFile = cameraHelper.createRevisionPhotoFile(
                orden != null ? orden.getId() : "temp",
                currentTab
            );

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(
                    getContext(),
                    getContext().getPackageName() + ".fileprovider",
                    photoFile
                );

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CameraHelper.REQUEST_IMAGE_CAPTURE);
                } else {
                    Toast.makeText(getContext(), "No hay aplicación de cámara disponible", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al abrir cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraHelper.REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Foto capturada exitosamente
            String photoPath = cameraHelper.getCurrentPhotoPath();
            int currentTab = viewPager.getCurrentItem() + 1; // 1-based

            // Guardar foto en BD
            DBFotoRevision foto = new DBFotoRevision();
            foto.setRevision_id(orden != null ? orden.getId() : "");
            foto.setTab_numero(currentTab);
            foto.setRuta_foto(photoPath);
            foto.setDescripcion("Foto adicional del Tab " + currentTab);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            foto.setFecha_captura(sdf.format(new Date()));

            dbHelper.insertFotoRevision(foto);

            Toast.makeText(getContext(), "✓ Foto guardada (Tab " + currentTab + ")", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CameraHelper.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permiso concedido. Intente nuevamente.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LocationHelper.REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show();
                capturarUbicacionInicio();
            } else {
                Toast.makeText(getContext(), "Permiso de ubicación denegado. La ubicación no será registrada.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Capturar ubicación GPS al inicio de la revisión
     */
    private void capturarUbicacionInicio() {
        if (!locationHelper.hasPermission()) {
            locationHelper.requestPermission(getActivity());
            return;
        }

        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                if (orden != null) {
                    orden.setLatitud(String.valueOf(location.getLatitude()));
                    orden.setLongitud(String.valueOf(location.getLongitude()));
                    dbHelper.insertOrUpdateRevision(orden, true);

                    Toast.makeText(getContext(),
                        "📍 Ubicación registrada: " + LocationHelper.formatCoordinates(location),
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLocationError(String error) {
                Toast.makeText(getContext(),
                    "⚠ No se pudo obtener ubicación: " + error,
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Capturar ubicación GPS al cerrar la revisión
     */
    private void capturarUbicacionCierre() {
        if (!locationHelper.hasPermission()) {
            // No solicitar permiso al cerrar, solo registrar sin ubicación
            return;
        }

        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                if (orden != null) {
                    orden.setLatitud(String.valueOf(location.getLatitude()));
                    orden.setLongitud(String.valueOf(location.getLongitude()));
                    // Se guardará en el método cerrarRevision()
                }
            }

            @Override
            public void onLocationError(String error) {
                // Silencioso al cerrar, no molestamos al usuario
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        guardarProgreso();

        // Detener actualizaciones de ubicación
        if (locationHelper != null) {
            locationHelper.stopLocationUpdates();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Detener actualizaciones de ubicación
        if (locationHelper != null) {
            locationHelper.stopLocationUpdates();
        }
    }
}
