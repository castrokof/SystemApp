package com.example.systemapp.data;

import com.example.systemapp.data.model.DBOrdenLecturas;

public class Validador {


    public static String validaciones(DBOrdenLecturas ordenesDB){
        int consumo = ordenesDB.getLectura_actual() - Integer.parseInt(ordenesDB.getLA());

        String resultado = "";

        if (ordenesDB.getLectura_actual()==Integer.parseInt(ordenesDB.getLA())){
            return resultado = "LA=LANT";
        }

        if (ordenesDB.getLectura_actual()<Integer.parseInt(ordenesDB.getLA())){
            ordenesDB.setConsumo(consumo);
            return resultado = "LA<LANT";

        }



        ordenesDB.setConsumo(consumo);

        //obtener porcentajes del consumo promedio
        //165% consumo promedio
        long cpMaximo = Math.round(ordenesDB.getPromedio() * 1.65);

        ordenesDB.getConsumo();



            if (consumo>cpMaximo){
                return resultado = "CA>165CP";
            }
            if (consumo<Math.round(ordenesDB.getPromedio() * 0.35)){
                return resultado = "CA<35CP";
            }




        return resultado;

    }

}
