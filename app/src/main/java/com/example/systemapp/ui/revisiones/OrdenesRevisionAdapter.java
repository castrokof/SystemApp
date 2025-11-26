package com.example.systemapp.ui.revisiones;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systemapp.R;
import com.example.systemapp.data.model.DBOrdenRevision;

import java.util.List;

/**
 * Adaptador para lista de órdenes de revisión pendientes
 * Incluye funcionalidad de reordenamiento con drag and drop
 */
public class OrdenesRevisionAdapter extends RecyclerView.Adapter<OrdenesRevisionAdapter.ViewHolder> {

    private Context context;
    private List<DBOrdenRevision> ordenes;
    private OnItemClickListener clickListener;
    private OnStartDragListener dragListener;

    public interface OnItemClickListener {
        void onItemClick(DBOrdenRevision orden);
    }

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public OrdenesRevisionAdapter(Context context, List<DBOrdenRevision> ordenes) {
        this.context = context;
        this.ordenes = ordenes;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnStartDragListener(OnStartDragListener listener) {
        this.dragListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_orden_revision, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DBOrdenRevision orden = ordenes.get(position);

        // Orden personalizada
        holder.tvOrdenPersonalizada.setText("#" + orden.getOrden_personalizado());

        // Tipo de desviación con color
        String tipoDesviacion = orden.getTipo_desviacion() != null ? orden.getTipo_desviacion() : "";
        holder.tvTipoDesviacion.setText(tipoDesviacion);

        if ("ALTO".equalsIgnoreCase(tipoDesviacion)) {
            holder.tvTipoDesviacion.setBackgroundColor(Color.parseColor("#D32F2F")); // Rojo
        } else if ("BAJO".equalsIgnoreCase(tipoDesviacion)) {
            holder.tvTipoDesviacion.setBackgroundColor(Color.parseColor("#1976D2")); // Azul
        }

        // Medidor
        holder.tvMedidor.setText("Medidor: " + (orden.getRef_Medidor() != null ? orden.getRef_Medidor() : "N/A"));

        // Nombre
        holder.tvNombre.setText(orden.getNombre() != null ? orden.getNombre() : "Sin nombre");

        // Dirección
        holder.tvDireccion.setText(orden.getDireccion() != null ? orden.getDireccion() : "Sin dirección");

        // Consumo promedio
        Integer promedio = orden.getPromedio();
        holder.tvConsumoPromedio.setText((promedio != null ? promedio : 0) + " m³");

        // Consumo actual (del periodo)
        Integer actual = orden.getConsumo();
        holder.tvConsumoActual.setText((actual != null ? actual : 0) + " m³");

        // Color del consumo actual según desviación
        if ("ALTO".equalsIgnoreCase(tipoDesviacion)) {
            holder.tvConsumoActual.setTextColor(Color.parseColor("#D32F2F"));
        } else if ("BAJO".equalsIgnoreCase(tipoDesviacion)) {
            holder.tvConsumoActual.setTextColor(Color.parseColor("#1976D2"));
        }

        // Drag handle: iniciar arrastre al tocar el icono
        holder.ivDragHandle.setOnTouchListener((v, event) -> {
            if (dragListener != null) {
                dragListener.onStartDrag(holder);
            }
            return false;
        });

        // Click en el item completo
        holder.llContent.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(orden);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ordenes != null ? ordenes.size() : 0;
    }

    public void updateList(List<DBOrdenRevision> newOrdenes) {
        this.ordenes = newOrdenes;
        notifyDataSetChanged();
    }

    public void moveItem(int fromPosition, int toPosition) {
        DBOrdenRevision item = ordenes.remove(fromPosition);
        ordenes.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    public List<DBOrdenRevision> getOrdenes() {
        return ordenes;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llContent;
        TextView tvOrdenPersonalizada;
        TextView tvTipoDesviacion;
        TextView tvMedidor;
        TextView tvNombre;
        TextView tvDireccion;
        TextView tvConsumoPromedio;
        TextView tvConsumoActual;
        ImageView ivDragHandle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            llContent = itemView.findViewById(R.id.ll_content_revision);
            tvOrdenPersonalizada = itemView.findViewById(R.id.tv_orden_personalizada);
            tvTipoDesviacion = itemView.findViewById(R.id.tv_tipo_desviacion);
            tvMedidor = itemView.findViewById(R.id.tv_medidor_revision);
            tvNombre = itemView.findViewById(R.id.tv_nombre_revision);
            tvDireccion = itemView.findViewById(R.id.tv_direccion_revision);
            tvConsumoPromedio = itemView.findViewById(R.id.tv_consumo_promedio);
            tvConsumoActual = itemView.findViewById(R.id.tv_consumo_actual);
            ivDragHandle = itemView.findViewById(R.id.iv_drag_handle);
        }
    }
}
