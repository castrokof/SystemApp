package com.example.systemapp.data;


import com.example.systemapp.data.model.DBOrdenLecturas;

import java.util.List;
public class VariablesSesion {
    //contendrá todos las ordenes asginadas, reasignadas, procesadas
    //será principalmente usada para obtener las ordenes asociadas por contrato para cuando se nece
    //sitan subir al servidor
    public static List<DBOrdenLecturas> allRutasGobal = null;
    //contendrá solo las ordenes asignadas, se usará principalmente para compartir la lista
    //entre clases de forma rápida, se piensa que se podrá usar cuando se deba hacer un filtro
    //de las rutas asignadas
    public static List<DBOrdenLecturas> rutasGobalAsignadas = null;
    public static int posicionSelec = 0;
    public static int posicionSelectProc = 0;
    public static String filtro = "TODO";

    public static void setAllRutasGobal(List<DBOrdenLecturas> lista){
        allRutasGobal = lista;
    }

    public static void updateElementAtAllRutasGlobal(DBOrdenLecturas orden){
        int it = 0;
        DBOrdenLecturas ordenloop = null;
        for (int i=0; i<allRutasGobal.size(); i++) {
            if (allRutasGobal.get(i).getId().equals(orden.getId())){
                allRutasGobal.remove(i);
                allRutasGobal.add(i, orden);
            }
        }
    }

    public static void setRutasGobalAsignadas(List<DBOrdenLecturas> lista){
        rutasGobalAsignadas = lista;
    }


}
