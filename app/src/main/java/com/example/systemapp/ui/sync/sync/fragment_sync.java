package com.example.systemapp.ui.sync.sync;




import static androidx.fragment.app.FragmentManager.TAG;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.systemapp.AuthInterceptor;
import com.example.systemapp.R;
import com.example.systemapp.SystemAppAPI;
import com.example.systemapp.data.AdminSQLiteOpenHelper;
import com.example.systemapp.data.SessionPrefs;
import com.example.systemapp.data.Utilidades;
import com.example.systemapp.data.model.DBListas;
import com.example.systemapp.data.model.DBOrdenLecturas;
import com.example.systemapp.data.model.DBOrdenLecturasEnviar;
import com.example.systemapp.data.model.EnviarRespuesta;
import com.example.systemapp.data.model.LoginEnvio;
import com.example.systemapp.databinding.FragmentSyncBinding;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;


public class fragment_sync extends Fragment {


    private AdminSQLiteOpenHelper adminSQLiteOpenHelper;

    public  static String jsonLectura = ""; //Json para enviar al servidor

    //abrir acceso a las preferencias
    public static SharedPreferences mPrefs;
    //public static SessionPrefs mPrefs;

   // public static Retrofit systemapp1;
    private Retrofit systemapp;
    private SystemAppAPI systemAppAPI;
    private FragmentSyncBinding binding;
    private ProgressBar progresB_sync;
    private View back_disables;
    private MaterialCardView cardSyncListas;
    private MaterialCardView cardSyncOrdenes;
    private MaterialCardView cardUpload;

    private TextView textViewStatus;

    private ProgressDialog progressDialog;



    /*public fragment_sync() {
        // Required empty public constructor
    }*/



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setTitle(getString(R.string.app_name) + " Sync Ordenes");
        }

        binding = FragmentSyncBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ✅ NUEVAS VISTAS
        cardSyncListas = root.findViewById(R.id.card_sync_listas);
        cardSyncOrdenes = root.findViewById(R.id.card_sync_ordenes);
        cardUpload = root.findViewById(R.id.card_upload);
        progresB_sync = root.findViewById(R.id.progresB_sync);
        textViewStatus = root.findViewById(R.id.textViewStatus);
        back_disables = root.findViewById(R.id.back_disabled);

        // ⭐ Inicialización de base de datos y preferencias
        adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(getContext());
        mPrefs = getActivity().getSharedPreferences("SYSTEMAPP_PREFS", Context.MODE_PRIVATE);

        String usuario = mPrefs.getString("PREF_USER_NAME", "");

        // ✅ Cambié textViewName por textViewStatus (ya que textViewName no existe)
        textViewStatus.setText("Sincroniza las órdenes asignadas. Usuario: " + usuario);

        // ⭐ Cliente con interceptor
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(getContext()))
                .build();

        systemapp = new Retrofit.Builder()
                .baseUrl(SystemAppAPI.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        systemAppAPI = systemapp.create(SystemAppAPI.class);

        // ⭐ Servicio para órdenes
        Call<List<DBOrdenLecturas>> getRoutes = systemAppAPI.cargue();

        // 🔹 cardSyncOrdenes reemplaza a btn_sync
        cardSyncOrdenes.setOnClickListener(v -> {
            showProgress(true);
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            mPrefs.edit().putBoolean("PREF_RUTAS_SYNC", true).apply();

            getRoutes.clone().enqueue(new Callback<List<DBOrdenLecturas>>() {
                @Override
                public void onResponse(Call<List<DBOrdenLecturas>> call, Response<List<DBOrdenLecturas>> response) {
                    showProgress(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    if (!response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
                        Log.e("SyncFragment", response.message());
                        return;
                    }

                    if (response.body() != null) {
                        List<DBOrdenLecturas> asignadas = response.body();
                        Toast.makeText(getActivity(), "Sincronización exitosa", Toast.LENGTH_LONG).show();
                        mPrefs.edit().putBoolean("PREF_RUTAS_SYNC", true).apply();

                        if (asignadas.size() > 0) {
                            for (DBOrdenLecturas dbOrdenLecturas : asignadas) {
                                adminSQLiteOpenHelper.insertOrden(dbOrdenLecturas, false);
                            }

                            SessionPrefs.get(getActivity()).setPrefRutasPendientes(
                                    mPrefs.getInt("PREF_RUTAS_PENDIENTES", 0) + asignadas.size());
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<DBOrdenLecturas>> call, Throwable t) {
                    showProgress(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("SyncFragment", t.getMessage());
                }
            });
        });

        // ⭐ Servicio para listas
        Call<List<DBListas>> getListas = systemAppAPI.listas();

        // 🔹 cardSyncListas reemplaza a btn_sync_listas
        cardSyncListas.setOnClickListener(v -> {
            showProgress(true);
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            getListas.clone().enqueue(new Callback<List<DBListas>>() {
                @Override
                public void onResponse(Call<List<DBListas>> call, Response<List<DBListas>> response) {
                    showProgress(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    if (!response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
                        Log.e("SyncFragment", response.message());
                        return;
                    }

                    if (response.body() != null) {
                        List<DBListas> listElementosListaBD = response.body();
                        Toast.makeText(getActivity(), "Sincronización exitosa", Toast.LENGTH_LONG).show();

                        for (DBListas elemento : listElementosListaBD) {
                            adminSQLiteOpenHelper.insertElementoLista(elemento);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<DBListas>> call, Throwable t) {
                    showProgress(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("SyncFragment", t.getMessage());
                }
            });
        });

        // 🔹 cardUpload reemplaza a btn_upload
        cardUpload.setOnClickListener(v -> {
            showProgress(true);
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Se enviarán las lecturas realizadas. ¿Desea continuar?")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> initUpload())
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        showProgress(false);
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    });

            try {
                builder.create().show();
            } catch (Exception e) {
                Log.e("SyncFragment", "No fue posible crear el diálogo: " + e.getMessage());
            }
        });

        return root;
    }



    public void initUpload(){

        final String usuario = mPrefs.getString("PREF_USER_NAME", "");

        //verificar si existe el usuario y está activo
        if (!usuario.equals("")){

                       List<DBOrdenLecturas> allRutastoUp = adminSQLiteOpenHelper.getData("lecturas",
                                    "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA' " +
                                            " AND (Uploadlec IS NULL OR Uploadlec = 'false') ");

                            progressDialog = new ProgressDialog(getActivity());
                            progressDialog.setMax(allRutastoUp.size()); // Progress Dialog Max Value
                            progressDialog.setMessage("Subiendo al servidor, por favor espere..."); // Setting Message
                            progressDialog.setTitle("Envío de lecturas"); // Setting Title
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // Progress Dialog Style Horizontal
                            progressDialog.show(); // Display Progress Dialog
                            progressDialog.setCancelable(false);

                            sendDataToServerOnetoOne(allRutastoUp,1, allRutastoUp.size());



                    }



        }


    public void sendDataToServerOnetoOne(final List<DBOrdenLecturas> ordenestoupload, final int ciclo, final int total){

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
                    Log.d("Envio con respuesta","onResponse");

                    String error;
                    if (!response.isSuccessful()){
                        if (response.errorBody()
                                .contentType()
                                .subtype()
                                .equals("application/json")){
                            error = response.message();
                            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                            Log.e("Respues de servidor", error);
                        }else if(response.code() == 401){
                            error = response.message();
                            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                            Log.e("Respues de servidor codigo", error);
                        }
                    }else{
                        response.body().toString();
                        Log.e("SyncFragment_respuestaok", response.body().toString());
                    }

                    if (response.body() != null){
                        Toast.makeText(getActivity(), "Cargue exitoso", Toast.LENGTH_LONG).show();
                    }else{
                        error = response.message();
                        Log.e("LecturaFragment",error);
                        ordenestoupload.remove(0);
                        sendDataToServerOnetoOne(ordenestoupload, (ciclo+1), total);
                        return;
                    }

                    Log.d("LecturaFragment", "Resultado subida " + response.body().equals(true));
                    ordentoupload.setUploadlec("true");
                    if(adminSQLiteOpenHelper.insertOrden(ordentoupload, true)>0){
                        Log.i("LecturaFragment","Registro subido y actualizado en BD con éxito");
                        progressDialog.incrementProgressBy(1);
                    }

                    ordenestoupload.remove(0);
                    sendDataToServerOnetoOne(ordenestoupload, (ciclo+1), total);
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Log.d("LecturaFragment","onFailure");
                    Log.e("LecturaFragment",t.getMessage()+" "+t.getCause());
                    if (getActivity()!=null){
                        showProgress(false);
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Toast.makeText(getActivity(), "Hubo una falla durante el envío, es posible que aún queden lecturas pendientes.", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });

        }else{
            if (getActivity()!=null){
                showProgress(false);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Se han procesado y enviando todas las lecturas al servidor.", Toast.LENGTH_LONG).show();
            }
        }
    }




    private void showProgress(boolean show) {
        progresB_sync.setVisibility(show ? View.VISIBLE : View.GONE);
        back_disables.setVisibility(show ? View.VISIBLE : View.GONE);
        cardUpload.setEnabled(!show);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}

