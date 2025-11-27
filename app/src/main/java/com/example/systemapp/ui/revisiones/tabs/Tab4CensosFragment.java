package com.example.systemapp.ui.revisiones.tabs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systemapp.R;
import com.example.systemapp.data.model.DBCensoHidraulico;
import com.example.systemapp.data.model.DBOrdenRevision;
import com.example.systemapp.ui.revisiones.CameraHelper;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tab 4: Censos (Poblacional + Hidráulico)
 */
public class Tab4CensosFragment extends Fragment {

    private DBOrdenRevision orden;
    private TextInputEditText etCensoFamiliar;
    private TextInputEditText etCensoPersonas;
    private TextInputEditText etCensoAdultos;
    private TextInputEditText etCensoNinos;
    private RecyclerView rvCensoHidraulico;
    private TextView tvSinElementos;
    private Button btnAgregar;

    private List<DBCensoHidraulico> elementosHidraulicos;
    private CensoHidraulicoAdapter adapter;
    private CameraHelper cameraHelper;
    private int currentPhotoElementPosition = -1;

    public static Tab4CensosFragment newInstance(DBOrdenRevision orden) {
        Tab4CensosFragment fragment = new Tab4CensosFragment();
        fragment.orden = orden;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.tab4_censos, container, false);

        // Censo poblacional
        etCensoFamiliar = root.findViewById(R.id.et_censo_familiar);
        etCensoPersonas = root.findViewById(R.id.et_censo_personas);
        etCensoAdultos = root.findViewById(R.id.et_censo_adultos);
        etCensoNinos = root.findViewById(R.id.et_censo_ninos);

        // Censo hidráulico
        rvCensoHidraulico = root.findViewById(R.id.rv_censo_hidraulico);
        tvSinElementos = root.findViewById(R.id.tv_sin_elementos);
        btnAgregar = root.findViewById(R.id.btn_agregar_elemento);

        elementosHidraulicos = new ArrayList<>();
        adapter = new CensoHidraulicoAdapter(getContext(), elementosHidraulicos);
        rvCensoHidraulico.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCensoHidraulico.setAdapter(adapter);

        // Inicializar camera helper
        cameraHelper = new CameraHelper(getContext());

        cargarDatos();
        setupListeners();
        actualizarVistaLista();

        return root;
    }

    private void cargarDatos() {
        if (orden != null) {
            // Censo poblacional
            etCensoFamiliar.setText(String.valueOf(orden.getCenso_poblacional_familiar() != null ? orden.getCenso_poblacional_familiar() : 0));
            etCensoPersonas.setText(String.valueOf(orden.getCenso_poblacional_personas() != null ? orden.getCenso_poblacional_personas() : 0));
            etCensoAdultos.setText(String.valueOf(orden.getCenso_poblacional_adultos() != null ? orden.getCenso_poblacional_adultos() : 0));
            etCensoNinos.setText(String.valueOf(orden.getCenso_poblacional_ninos() != null ? orden.getCenso_poblacional_ninos() : 0));

            // Censo hidráulico - se carga desde BD en otra clase
        }
    }

    private void setupListeners() {
        btnAgregar.setOnClickListener(v -> mostrarDialogAgregar());

        adapter.setOnItemActionListener(new CensoHidraulicoAdapter.OnItemActionListener() {
            @Override
            public void onTomarFoto(DBCensoHidraulico elemento, int position) {
                currentPhotoElementPosition = position;
                abrirCamara(elemento);
            }

            @Override
            public void onEliminar(DBCensoHidraulico elemento, int position) {
                elementosHidraulicos.remove(position);
                adapter.notifyItemRemoved(position);
                actualizarVistaLista();
            }
        });
    }

    private void mostrarDialogAgregar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_agregar_elemento, null);

        Spinner spinnerElemento = dialogView.findViewById(R.id.spinner_elemento);
        TextInputEditText etCantidad = dialogView.findViewById(R.id.et_cantidad);
        Spinner spinnerEstado = dialogView.findViewById(R.id.spinner_estado);
        Button btnCancelar = dialogView.findViewById(R.id.btn_cancelar);
        Button btnAgregarDialog = dialogView.findViewById(R.id.btn_agregar);

        // Configurar spinner elementos
        String[] elementos = {"SANITARIO", "LAVAMANOS", "DUCHA", "LAVADERO", "LLAVE_COCINA", "TANQUE", "PISCINA", "OTRO"};
        ArrayAdapter<String> adapterElementos = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, elementos);
        adapterElementos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerElemento.setAdapter(adapterElementos);

        // Configurar spinner estado
        String[] estados = {"BUENO", "MALO"};
        ArrayAdapter<String> adapterEstados = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, estados);
        adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterEstados);

        AlertDialog dialog = builder.setView(dialogView).create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnAgregarDialog.setOnClickListener(v -> {
            String cantidadStr = etCantidad.getText().toString().trim();
            if (cantidadStr.isEmpty()) {
                etCantidad.setError("Requerido");
                return;
            }

            DBCensoHidraulico nuevo = new DBCensoHidraulico();
            nuevo.setRevision_id(orden != null ? orden.getId() : "");
            nuevo.setElemento(spinnerElemento.getSelectedItem().toString());
            nuevo.setCantidad(Integer.parseInt(cantidadStr));
            nuevo.setEstado(spinnerEstado.getSelectedItem().toString());

            elementosHidraulicos.add(nuevo);
            adapter.notifyItemInserted(elementosHidraulicos.size() - 1);
            actualizarVistaLista();

            dialog.dismiss();
        });

        dialog.show();
    }

    private void actualizarVistaLista() {
        if (elementosHidraulicos.isEmpty()) {
            tvSinElementos.setVisibility(View.VISIBLE);
            rvCensoHidraulico.setVisibility(View.GONE);
        } else {
            tvSinElementos.setVisibility(View.GONE);
            rvCensoHidraulico.setVisibility(View.VISIBLE);
        }
    }

    public boolean validar() {
        try {
            if (orden != null) {
                orden.setCenso_poblacional_familiar(Integer.parseInt(etCensoFamiliar.getText().toString()));
                orden.setCenso_poblacional_personas(Integer.parseInt(etCensoPersonas.getText().toString()));
                orden.setCenso_poblacional_adultos(Integer.parseInt(etCensoAdultos.getText().toString()));
                orden.setCenso_poblacional_ninos(Integer.parseInt(etCensoNinos.getText().toString()));
            }
            return true;
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error en datos del censo poblacional", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public List<DBCensoHidraulico> getElementosHidraulicos() {
        return elementosHidraulicos;
    }

    /**
     * Abrir cámara para foto de elemento
     */
    private void abrirCamara(DBCensoHidraulico elemento) {
        // Verificar permiso
        if (!cameraHelper.hasPermission()) {
            cameraHelper.requestPermission(getActivity());
            return;
        }

        try {
            File photoFile = cameraHelper.createCensoPhotoFile(
                orden != null ? orden.getId() : "temp",
                elemento.getElemento()
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

            if (currentPhotoElementPosition >= 0 && currentPhotoElementPosition < elementosHidraulicos.size()) {
                DBCensoHidraulico elemento = elementosHidraulicos.get(currentPhotoElementPosition);
                elemento.setFoto_path(photoPath);
                adapter.notifyItemChanged(currentPhotoElementPosition);

                Toast.makeText(getContext(), "Foto guardada", Toast.LENGTH_SHORT).show();
            }

            currentPhotoElementPosition = -1;
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
        }
    }
}
