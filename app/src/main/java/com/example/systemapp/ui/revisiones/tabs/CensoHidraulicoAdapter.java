package com.example.systemapp.ui.revisiones.tabs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systemapp.R;
import com.example.systemapp.data.model.DBCensoHidraulico;

import java.util.List;

/**
 * Adapter para lista de elementos del censo hidráulico
 */
public class CensoHidraulicoAdapter extends RecyclerView.Adapter<CensoHidraulicoAdapter.ViewHolder> {

    private Context context;
    private List<DBCensoHidraulico> elementos;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onTomarFoto(DBCensoHidraulico elemento, int position);
        void onEliminar(DBCensoHidraulico elemento, int position);
    }

    public CensoHidraulicoAdapter(Context context, List<DBCensoHidraulico> elementos) {
        this.context = context;
        this.elementos = elementos;
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_censo_hidraulico, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DBCensoHidraulico elemento = elementos.get(position);

        holder.tvElemento.setText(elemento.getElemento());
        holder.tvCantidad.setText(String.valueOf(elemento.getCantidad()));
        holder.tvEstado.setText(elemento.getEstado());

        // Indicador de foto
        if (elemento.tieneFoto()) {
            holder.tvTieneFoto.setVisibility(View.VISIBLE);
        } else {
            holder.tvTieneFoto.setVisibility(View.GONE);
        }

        // Botón tomar foto
        holder.btnTomarFoto.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTomarFoto(elemento, position);
            }
        });

        // Botón eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminar(elemento, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return elementos != null ? elementos.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvElemento;
        TextView tvCantidad;
        TextView tvEstado;
        TextView tvTieneFoto;
        ImageButton btnTomarFoto;
        ImageButton btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvElemento = itemView.findViewById(R.id.tv_elemento);
            tvCantidad = itemView.findViewById(R.id.tv_cantidad);
            tvEstado = itemView.findViewById(R.id.tv_estado);
            tvTieneFoto = itemView.findViewById(R.id.tv_tiene_foto);
            btnTomarFoto = itemView.findViewById(R.id.btn_tomar_foto);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
        }
    }
}
