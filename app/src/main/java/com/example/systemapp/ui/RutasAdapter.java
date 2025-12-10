package com.example.systemapp.ui;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.systemapp.R;
import com.example.systemapp.data.model.DBOrdenLecturas;

import java.util.ArrayList;
import java.util.List;

public class RutasAdapter extends RecyclerView.Adapter<RutasAdapter.RutaViewHolder> implements Filterable {

    private List<DBOrdenLecturas> rutasList;
    private List<DBOrdenLecturas> rutasListFull; // Para el filtrado
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DBOrdenLecturas ruta, int position);
    }

    public RutasAdapter(List<DBOrdenLecturas> rutasList, OnItemClickListener listener) {
        this.rutasList = rutasList;
        this.rutasListFull = new ArrayList<>(rutasList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public RutaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemruta, parent, false);
        return new RutaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutaViewHolder holder, int position) {
        DBOrdenLecturas ruta = rutasList.get(position);
        holder.bind(ruta, listener);
    }

    @Override
    public int getItemCount() {
        return rutasList.size();
    }

    @Override
    public Filter getFilter() {
        return rutasFilter;
    }

    private Filter rutasFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<DBOrdenLecturas> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(rutasListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (DBOrdenLecturas item : rutasListFull) {
                    if (item.getId().toLowerCase().contains(filterPattern) ||
                            item.getDireccion().toLowerCase().contains(filterPattern) ||
                            item.getRuta().toLowerCase().contains(filterPattern) ||
                            item.getNombre().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            rutasList.clear();
            rutasList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public void updateData(List<DBOrdenLecturas> newRutas) {
        this.rutasList = newRutas;
        this.rutasListFull = new ArrayList<>(newRutas);
        notifyDataSetChanged();
    }

    static class RutaViewHolder extends RecyclerView.ViewHolder {
        TextView txtMedidor, txtDireccion, txtRuta, txtNombre;
        ImageView imgItem;
        View itemContainer;

        public RutaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMedidor = itemView.findViewById(R.id.txtMedidor);
            txtDireccion = itemView.findViewById(R.id.txtDireccion);
            txtRuta = itemView.findViewById(R.id.txtRuta);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            imgItem = itemView.findViewById(R.id.imgItem);
            itemContainer = itemView;
        }

        public void bind(final DBOrdenLecturas ruta, final OnItemClickListener listener) {
            txtMedidor.setText(ruta.getId());
            txtDireccion.setText(ruta.getDireccion());
            txtRuta.setText("Ruta " + ruta.getRuta());
            txtNombre.setText(ruta.getNombre());

            // Ocultar imagen si no hay datos
            // imgItem.setVisibility(View.GONE);

            // ✨ ANIMACIÓN ESTILO iOS AL TOCAR
            itemContainer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // Animar hacia abajo (presionado)
                            v.animate()
                                    .scaleX(0.96f)
                                    .scaleY(0.96f)
                                    .alpha(0.9f)
                                    .setDuration(100)
                                    .start();
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            // Restaurar tamaño original
                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .alpha(1f)
                                    .setDuration(100)
                                    .withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                                int position = getAdapterPosition();
                                                if (position != RecyclerView.NO_POSITION && listener != null) {
                                                    listener.onItemClick(ruta, position);
                                                }
                                            }
                                        }
                                    })
                                    .start();
                            break;
                    }
                    return true;
                }
            });
        }
    }
}