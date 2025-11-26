package com.example.systemapp.ui.revisiones;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.systemapp.R;

/**
 * Fragment principal para el módulo de REVISIONES
 * Muestra lista de órdenes pendientes y ejecutadas
 */
public class Fragment_ordenes_revision extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Por ahora un layout temporal
        View root = inflater.inflate(R.layout.fragment_ordenes, container, false);

        // TODO: Implementar lista de revisiones con reordenamiento

        return root;
    }
}
