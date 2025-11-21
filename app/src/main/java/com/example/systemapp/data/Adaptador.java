package com.example.systemapp.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.systemapp.R;
import com.example.systemapp.data.model.DBOrdenLecturas;

import java.util.ArrayList;

public class Adaptador extends ArrayAdapter<DBOrdenLecturas>  implements Filterable {

    private Context context;
    private ArrayList<DBOrdenLecturas> arrayitems;
    private ItemFilter mFilter = new ItemFilter();
    private ArrayList<DBOrdenLecturas> filteredData;
    public Adaptador(Context context, ArrayList<DBOrdenLecturas> arrayitems) {
        super(context, 0, arrayitems);
        this.context = context;
        this.arrayitems = arrayitems;
        this.filteredData = arrayitems;
    }

    public static class Fila{
        TextView txtMedidor;
        TextView txtRuta;
        TextView txtDireccion;
        TextView txtNombre;
        ImageView img;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Fila view = new Fila();

        LayoutInflater inflator = LayoutInflater.from(context);
        DBOrdenLecturas itm = filteredData.get(position);

        if (convertView==null){

            convertView = inflator.inflate(R.layout.itemruta, parent,false);
            view.txtMedidor = convertView.findViewById(R.id.txtMedidor);
            view.txtRuta = convertView.findViewById(R.id.txtRuta);
            view.txtDireccion = convertView.findViewById(R.id.txtDireccion);
            view.txtNombre= convertView.findViewById(R.id.txtNombre);
            view.img = convertView.findViewById(R.id.imgItem);
            convertView.setTag(view);
        }else{

            view = (Fila)convertView.getTag();
        }

        view.txtMedidor.setText("Medidor: " + itm.getRef_Medidor());
        view.txtRuta.setText(" " + itm.getRuta() + " | " + itm.getSuscriptor() + " | " + itm.getConsecutivoRuta());
        view.txtDireccion.setText(" " + itm.getDireccion());
        view.txtNombre.setText(" " + itm.getNombre());
        if (itm.getNservic().equals("ACUEDUCTO")){
            view.img.setImageResource(R.drawable.ic_water);
        }else{
            view.img.setImageResource(R.drawable.energia);
        }


        return convertView;
    }


    public int getCount() {
        return filteredData.size();
    }

    public DBOrdenLecturas getItem(int position) {
        return filteredData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        if(mFilter == null)
            mFilter = new ItemFilter();
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase().trim();

            FilterResults results = new FilterResults();

            final ArrayList<DBOrdenLecturas> list = arrayitems;

            int count = list.size();
            final ArrayList<DBOrdenLecturas> nlist = new ArrayList<DBOrdenLecturas>(count);

            DBOrdenLecturas filterableString ;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.getRef_Medidor().toLowerCase().trim().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<DBOrdenLecturas>) results.values;
            notifyDataSetChanged();
        }

    }

}
