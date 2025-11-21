package com.example.systemapp.data;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.systemapp.R;
import com.example.systemapp.data.model.DBOrdenLecturas;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorResumen {

    private Context context;
    private List<DBOrdenLecturas> arrayitems;

    public AdaptadorResumen(Context context, List<DBOrdenLecturas> arrayitems) {

        this.context = context;
        this.arrayitems = arrayitems;
    }

    public static class Fila{
        TextView tipo_serv;
        TextView lect_actut;
        TextView lect_medidor;
        TextView lect_consumo;
        TextView lect_causa;
        TextView lect_obsv;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        Fila view = new Fila();

        LayoutInflater inflator = LayoutInflater.from(context);
        DBOrdenLecturas itm = arrayitems.get(position);

        if (convertView==null){

            convertView = inflator.inflate(R.layout.listtemplate, parent,false);

            view.tipo_serv = convertView.findViewById(R.id.tipo_serv);
            view.lect_actut = convertView.findViewById(R.id.lect_actu);
            view.lect_medidor = convertView.findViewById(R.id.lect_medidor);
            //view.lect_consumo = convertView.findViewById(R.id.lect_consumo);
            view.lect_causa = convertView.findViewById(R.id.lect_causa);
            view.lect_obsv = convertView.findViewById(R.id.lect_obsv);
            convertView.setTag(view);
        }else{

            view = (Fila)convertView.getTag();
        }

        view.tipo_serv.setText(itm.getNservic());
        view.lect_actut.setText("Lectura actual: " + ((itm.getConsumo()!=null)?((itm.getConsumo()==0)?"":itm.getConsumo()):""));
        view.lect_medidor.setText("Medidor: " + itm.getRef_Medidor());
        view.lect_causa.setText("Causa: " + ((itm.getDescCausa()==null)?"":itm.getDescCausa()));
        view.lect_obsv.setText("Observación: " + ((itm.getDescObservacion()==null)?"":itm.getDescObservacion()));

        return convertView;
    }
}
