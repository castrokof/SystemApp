package com.example.systemapp.data;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.systemapp.SystemAppAPI;
import com.example.systemapp.data.model.DBOrdenLecturas;
import com.example.systemapp.data.model.DBOrdenLecturasEnviar;
import com.example.systemapp.data.model.DBdefinicionOrdenes;
import com.example.systemapp.data.model.EnviarRespuesta;
import com.example.systemapp.data.model.LoginEnvio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Utilidades {

    //objeto para transacciones con BD
    public static AdminSQLiteOpenHelper adminSQLiteOpenHelper;
    public static Retrofit systemapp1;
    public static SystemAppAPI systemAppAPI;
    public static String jsonLectura = "";//json para enviar al servidor
    public static Call<Boolean> enviarordenes;
    //abrir acceso a las preferencias
    public static SharedPreferences mPrefs;

    public static void sendDataToServer(final DBOrdenLecturas ordentoupload){

    Integer lectura = null ;

        if (ordentoupload.getLectura_actual()==null){
        if (ordentoupload.getCausa()==null||ordentoupload.getCausa()<=0)//si no hay lectura y tampoco causa, no envía lectura
            return;
    }else{
        if (ordentoupload.getCausa()==null||ordentoupload.getCausa()==0)
            lectura = Integer.valueOf(ordentoupload.getLectura_actual()+"");
    }

    String campoFoto =  "\"fotos\": [";
        if (ordentoupload.getRuta_foto()!=null) {
        String[] rutasFotos = ordentoupload.getRuta_foto().split(",");
        int numeroFoto = 0;
        for (String rutaFoto : rutasFotos) {

            //se asegura que solo se agregaran diez fotos en el request
            if (numeroFoto < 5) {
                rutaFoto = rutaFoto.trim();
                if (numeroFoto == 0) {//si es el primer registro no llevará coma al inicio
                    campoFoto += "\"" + encodeImage(rutaFoto) + "\"";
                } else {
                    campoFoto += ", \"" + encodeImage(rutaFoto) + "\"";
                }
                numeroFoto++;
            }

        }
    }
        campoFoto += "]";
        String id = ordentoupload.getId();
        String tipo = "4";
        String finilec = ordentoupload.getFinilec();
        String ffinlec = ordentoupload.getFfinlec();
        Integer lectact = lectura;
        String critica = ((ordentoupload.getCritica()==null)?"49":ordentoupload.getCritica());
        Integer causal = ((ordentoupload.getCausa()==null)?null:ordentoupload.getCausa());
        Integer observ = ((ordentoupload.getObservacion()==null)?null:ordentoupload.getObservacion());
        String observg = ((ordentoupload.getObservacionGral()==null)?"":ordentoupload.getObservacionGral());
        String latitud = ((ordentoupload.getLatitud()==null)?"":ordentoupload.getLatitud());
        String longitud = ((ordentoupload.getLongitud()==null)?"":ordentoupload.getLongitud());
        Integer consumo =  ((ordentoupload.getConsumo()==null)?null:ordentoupload.getConsumo());
        String suscriptor = ordentoupload.getSuscriptor();
        String usuario = ordentoupload.getUsuario();
        String texobser = ordentoupload.getDescObservacion();
        String texcausa = ordentoupload.getDescCausa();

        // ⭐ ELIMINADO: No vuelvas a crear Retrofit aquí, usa el que ya creaste en onCreateView

        // ⭐ Usar el systemAppAPI que ya tiene el interceptor configurado
        Call<Object> enviarordenes = systemAppAPI.enviarordenes(
                new DBOrdenLecturasEnviar(campoFoto, id, tipo, finilec, ffinlec, lectact,
                        critica, causal, observ, observg, latitud, longitud, consumo,
                        suscriptor, usuario, texobser, texcausa)
        );

        Log.d("LecturaFragment","va al servicio");
        Log.d("LecturasSync", String.valueOf(enviarordenes));

        enviarordenes.clone().enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Log.d("LecturaFragment","onResponse");
                //Procesar errores
                String error;
                if (!response.isSuccessful()){
                    error = response.message();

                    if (response.errorBody()
                            .contentType()
                            .subtype()
                            .equals("application/json")){
                        error = response.message();
                    }else{
                        error = response.message();
                    }



                    Log.e("LecturaFragment",error);
                    return;
                }

                if (response.body().equals(false)){
                    error = response.message();
                //Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                Log.e("LecturaFragment",error);
                return;
            }

            //si se pudo subir el registro, se notifica en la base de datos
            Log.d("LecturaFragment", "Resultado subida " + response.body().equals(true));
            ordentoupload.setUploadlec("true");
            if(adminSQLiteOpenHelper.insertOrden(ordentoupload, true)>0){
                Log.i("LecturaFragment","Registro subido y actualizado en BD con éxito");
            }

        }

        @Override
        public void onFailure(Call<Object> call, Throwable t) {
            Log.d("LecturaFragment","onFailure");
            Log.e("LecturaFragment",t.getMessage()+" "+t.getCause());
        }
    });
}

      public static void sendPendingData(){
        Log.i("LecturaFragment","Buscando registros pendientes de subir...");
        List<DBOrdenLecturas> allRutastoUp = adminSQLiteOpenHelper.getData("lecturas",
                "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA' " +
                        " AND (Uploadlec IS NULL OR Uploadlec = 'false') ");
        Log.i("LecturaFragment","Encontrados "+allRutastoUp.size()+" registros pendientes de subir...");
        sendDataToServerOnetoOne(allRutastoUp);
    }

    public static void sendDataToServerOnetoOne(final List<DBOrdenLecturas> ordenestoupload){

        if (ordenestoupload.size()>0){

            final DBOrdenLecturas ordentoupload = ordenestoupload.get(0);

            Integer lectura = null;
            if (ordentoupload.getLectura_actual()==null){
                if (ordentoupload.getCausa()==null||ordentoupload.getCausa()<=0)
                    return;
            }else{
                if (ordentoupload.getCausa()==null||ordentoupload.getCausa()==0)
                    lectura = Integer.valueOf(ordentoupload.getLectura_actual()+"");
            }

            String campoFoto1 =  "";
            if (ordentoupload.getRuta_foto()!=null) {
                String rutaFoto = ordentoupload.getRuta_foto();
                campoFoto1 = Utilidades.encodeImage(rutaFoto);
            }

            String campoFoto = campoFoto1;
            String id = ordentoupload.getId();
            String tipo = "4";
            String finilec = ordentoupload.getFinilec();
            String ffinlec = ordentoupload.getFfinlec();
            Integer lectact = lectura;
            String critica = (ordentoupload.getCritica());
            Integer causal = ((ordentoupload.getCausa()==null)?0:ordentoupload.getCausa());
            Integer observ = ((ordentoupload.getObservacion()==null)?0:ordentoupload.getObservacion());
            String observg = ((ordentoupload.getObservacionGral()==null)?"":ordentoupload.getObservacionGral());
            String latitud = ((ordentoupload.getLatitud()==null)?"":ordentoupload.getLatitud());
            String longitud = ((ordentoupload.getLongitud()==null)?"":ordentoupload.getLongitud());
            Integer consumo = ((ordentoupload.getConsumo()==null)?null:ordentoupload.getConsumo());
            String suscriptor = ordentoupload.getSuscriptor();
            String usuario = ordentoupload.getUsuario();
            String texobser = ordentoupload.getDescObservacion();
            String texcausa = ordentoupload.getDescCausa();

            // ⭐ ELIMINADO: No vuelvas a crear Retrofit aquí, usa el que ya creaste en onCreateView

            // ⭐ Usar el systemAppAPI que ya tiene el interceptor configurado
            Call<Object> enviarordenes = systemAppAPI.enviarordenes(
                    new DBOrdenLecturasEnviar(campoFoto, id, tipo, finilec, ffinlec, lectact,
                            critica, causal, observ, observg, latitud, longitud, consumo,
                            suscriptor, usuario, texobser, texcausa)
            );

            Log.d("LecturaFragment","va al servicio");
            Log.d("LecturasSync", String.valueOf(enviarordenes));

            enviarordenes.clone().enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    Log.d("LecturaFragment","onResponse");
                    //Procesar errores

                    String error;
                    if (!response.isSuccessful()){
                        if (response.errorBody()
                                .contentType()
                                .subtype()
                                .equals("application/json")){
                            error = response.message();
                            //Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                            Log.e("Respues de servidor", error);
                        }else if(response.code() == 401){
                            error = response.message();
                            //Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                            Log.e("Respues de servidor codigo", error);
                        }
                    }else{
                        response.body().toString();
                        Log.e("SyncFragment_respuestaok", response.body().toString());
                    }

                    if (response.body() != null){
                       // Toast.makeText(getActivity(), "Cargue exitoso", Toast.LENGTH_LONG).show();
                    }else{
                        error = response.message();
                        Log.e("LecturaFragment",error);
                        ordenestoupload.remove(0);
                        sendDataToServerOnetoOne(ordenestoupload);
                        return;
                    }


                    //si se pudo subir el registro, se notifica en la base de datos
                    Log.d("LecturaFragment", "Resultado subida " + response.body().equals("ok"));
                    ordentoupload.setUploadlec("true");
                    if(adminSQLiteOpenHelper.insertOrden(ordentoupload, true)>0){
                        Log.i("LecturaFragment","Registro subido y actualizado en BD con éxito");
                    }

                    ordenestoupload.remove(0);
                    sendDataToServerOnetoOne(ordenestoupload);

                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Log.d("LecturaFragment","onFailure");
                    Log.e("LecturaFragment",t.getMessage()+" "+t.getCause());
                }


            });

        }
    }

    public static String encodeImage(String path)
    {
        File imagefile = new File(path);
        FileInputStream fis = null;
        String encImage = "";
        try{
            fis = new FileInputStream(imagefile);

            Bitmap bm = BitmapFactory.decodeStream(fis);

            //redimensionar imagen a 640x640 para subir al servidor
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, 640, 480, true);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            byte[] b = outputStream.toByteArray();
            encImage = Base64.encodeToString(b, Base64.DEFAULT);
            resizedBitmap.recycle();

        }catch(Exception e){
            e.printStackTrace();
            return encImage;
        }


        return encImage;

    }

    public static boolean ultimaLecturadeContrato(String idreg, String contrato){
        boolean result = false;

        List<DBOrdenLecturas> ordenesDBS = adminSQLiteOpenHelper.getData(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                "Suscriptor = '"+contrato+"'");

        for (DBOrdenLecturas orden: ordenesDBS) {
            if (orden.getCategoria_orden().equals("ASIGNADAS")){
                if (orden.getId().equals(idreg)){
                    result = true;
                }else {
                    return false;
                }
            }
        }

        return result;
    }

}
