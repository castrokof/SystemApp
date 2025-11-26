package com.example.systemapp.ui.revisiones;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systemapp.R;
import com.example.systemapp.data.model.DBOrdenRevision;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para lista de revisiones ejecutadas
 * Muestra estado y permite reapertura (máximo 3 veces)
 */
public class EjecutadasRevisionAdapter extends RecyclerView.Adapter<EjecutadasRevisionAdapter.ViewHolder> {

    private Context context;
    private List<DBOrdenRevision> ordenes;
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(DBOrdenRevision orden);
    }

    public EjecutadasRevisionAdapter(Context context, List<DBOrdenRevision> ordenes) {
        this.context = context;
        this.ordenes = ordenes;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ejecutada_revision, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DBOrdenRevision orden = ordenes.get(position);

        // Estado
        String estado = orden.getEstado() != null ? orden.getEstado() : "EJECUTADA";
        holder.tvEstado.setText(estado);

        // Color según estado
        if ("EJECUTADA".equals(estado)) {
            holder.tvEstado.setBackgroundColor(Color.parseColor("#388E3C")); // Verde
        } else if ("PROCESADA".equals(estado)) {
            holder.tvEstado.setBackgroundColor(Color.parseColor("#1976D2")); // Azul
        }

        // Modificaciones
        Integer modificaciones = orden.getCantidad_modificaciones() != null ? orden.getCantidad_modificaciones() : 0;
        holder.tvModificaciones.setText("Modificaciones: " + modificaciones + "/3");

        // Tipo de desviación
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

        // Fecha ejecución (usar fecha_cierre)
        String fechaEjecucion = orden.getFecha_cierre() != null ? orden.getFecha_cierre() : "N/A";
        holder.tvFechaEjecucion.setText("Ejecutada: " + fechaEjecucion);

        // Estado de reapertura
        boolean puedeReabrir = orden.puedeSerModificada();
        if (puedeReabrir) {
            holder.tvEstadoReapertura.setText("✓ Puede reabrir");
            holder.tvEstadoReapertura.setTextColor(Color.parseColor("#388E3C"));
        } else {
            holder.tvEstadoReapertura.setText("✗ Límite de modificaciones alcanzado");
            holder.tvEstadoReapertura.setTextColor(Color.parseColor("#D32F2F"));
        }

        // Si ya está enviada a la API, mostrar indicador
        if ("SI".equals(orden.getEnviado_api())) {
            holder.tvEstadoReapertura.setText("✓ Enviada a API");
            holder.tvEstadoReapertura.setTextColor(Color.parseColor("#1976D2"));
        }

        // Click en el item
        holder.itemView.setOnClickListener(v -> {
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEstado;
        TextView tvModificaciones;
        TextView tvTipoDesviacion;
        TextView tvMedidor;
        TextView tvNombre;
        TextView tvDireccion;
        TextView tvFechaEjecucion;
        TextView tvEstadoReapertura;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEstado = itemView.findViewById(R.id.tv_estado_revision);
            tvModificaciones = itemView.findViewById(R.id.tv_modificaciones);
            tvTipoDesviacion = itemView.findViewById(R.id.tv_tipo_desviacion_ejecutada);
            tvMedidor = itemView.findViewById(R.id.tv_medidor_ejecutada);
            tvNombre = itemView.findViewById(R.id.tv_nombre_ejecutada);
            tvDireccion = itemView.findViewById(R.id.tv_direccion_ejecutada);
            tvFechaEjecucion = itemView.findViewById(R.id.tv_fecha_ejecucion);
            tvEstadoReapertura = itemView.findViewById(R.id.tv_estado_reapertura);
        }
    }
}
