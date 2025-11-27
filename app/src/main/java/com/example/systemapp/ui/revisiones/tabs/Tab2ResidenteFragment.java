package com.example.systemapp.ui.revisiones.tabs;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.systemapp.R;
import com.example.systemapp.data.model.DBOrdenRevision;
import com.example.systemapp.ui.revisiones.SignaturePadView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Tab 2: Datos del Residente + Firma
 */
public class Tab2ResidenteFragment extends Fragment {

    private DBOrdenRevision orden;
    private TextInputEditText etNombreResidente;
    private SignaturePadView signaturePad;
    private Button btnLimpiar;
    private Button btnGuardar;
    private TextView tvFirmaGuardada;
    private String firmaPath;

    public static Tab2ResidenteFragment newInstance(DBOrdenRevision orden) {
        Tab2ResidenteFragment fragment = new Tab2ResidenteFragment();
        fragment.orden = orden;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.tab2_residente, container, false);

        etNombreResidente = root.findViewById(R.id.et_nombre_residente);
        FrameLayout signatureContainer = root.findViewById(R.id.signature_container);
        btnLimpiar = root.findViewById(R.id.btn_limpiar_firma);
        btnGuardar = root.findViewById(R.id.btn_guardar_firma);
        tvFirmaGuardada = root.findViewById(R.id.tv_firma_guardada);

        // Crear SignaturePad programáticamente
        signaturePad = new SignaturePadView(getContext(), null);
        signatureContainer.addView(signaturePad);

        cargarDatos();
        setupListeners();

        return root;
    }

    private void cargarDatos() {
        if (orden != null) {
            // Nombre del residente
            if (orden.getNombre_residente() != null) {
                etNombreResidente.setText(orden.getNombre_residente());
            }

            // Verificar si ya tiene firma guardada
            if (orden.getFirma_path() != null && !orden.getFirma_path().isEmpty()) {
                firmaPath = orden.getFirma_path();
                tvFirmaGuardada.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupListeners() {
        btnLimpiar.setOnClickListener(v -> {
            signaturePad.clear();
            tvFirmaGuardada.setVisibility(View.GONE);
        });

        btnGuardar.setOnClickListener(v -> guardarFirma());
    }

    private void guardarFirma() {
        if (signaturePad.isEmpty()) {
            Toast.makeText(getContext(), "Por favor firme primero", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Bitmap firma = signaturePad.getSignatureBitmap();
            if (firma != null) {
                // Guardar en almacenamiento interno
                File dir = new File(getContext().getFilesDir(), "firmas");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String filename = "firma_" + orden.getId() + "_" + System.currentTimeMillis() + ".png";
                File file = new File(dir, filename);

                FileOutputStream fos = new FileOutputStream(file);
                firma.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.close();

                firmaPath = file.getAbsolutePath();

                // Actualizar modelo
                if (orden != null) {
                    orden.setFirma_path(firmaPath);
                }

                tvFirmaGuardada.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Firma guardada correctamente", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al guardar firma: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public boolean validar() {
        // Nombre del residente
        String nombre = etNombreResidente.getText().toString().trim();
        if (nombre.isEmpty()) {
            etNombreResidente.setError("Nombre del residente requerido");
            return false;
        }

        // Actualizar modelo
        if (orden != null) {
            orden.setNombre_residente(nombre);
        }

        // Firma
        if (firmaPath == null || firmaPath.isEmpty()) {
            Toast.makeText(getContext(), "Debe capturar la firma del cliente", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
