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
import com.example.systemapp.data.factura.FacturaCalculada;
import com.example.systemapp.data.model.DBListas;
import com.example.systemapp.data.model.DBOrdenLecturas;
import com.example.systemapp.data.model.DBOrdenLecturasEnviar;
import com.example.systemapp.data.model.DesgloseServicioDTO;
import com.example.systemapp.data.model.EnviarRespuesta;
import com.example.systemapp.data.model.FacturaLocal;
import com.example.systemapp.data.model.FacturaLocalEnviarDTO;
import com.example.systemapp.data.model.FacturaResueltaDTO;
import com.example.systemapp.data.model.FacturaResueltaServidor;
import com.example.systemapp.data.model.FacturaSubidaResponse;
import com.example.systemapp.data.model.LoginEnvio;
import com.example.systemapp.data.model.RangoFacturacionRequest;
import com.example.systemapp.data.model.RangoFacturacionResponse;
import com.example.systemapp.data.model.TarifaVigenteResponse;
import com.google.gson.Gson;
import com.example.systemapp.databinding.FragmentSyncBinding;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
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
    private MaterialCardView cardSyncTarifas;
    private MaterialCardView cardSyncFacturasResueltas;
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
        cardSyncTarifas = root.findViewById(R.id.card_sync_tarifas);
        cardSyncFacturasResueltas = root.findViewById(R.id.card_sync_facturas_resueltas);
        cardUpload = root.findViewById(R.id.card_upload);

        // Facturación en Sitio: el backend de la mayoría de flavors todavía no expone estos
        // endpoints (tarifaVigente/rangoFacturacion/facturasResueltas/facturas) — ocultar los
        // cards nuevos evita que un lecturista de un cliente sin esta feature los toque y
        // reciba errores de un endpoint que no existe. Ver com.example.systemapp.BuildConfig.
        if (!com.example.systemapp.BuildConfig.FACTURACION_SITIO_SOPORTADA) {
            cardSyncTarifas.setVisibility(View.GONE);
            cardSyncFacturasResueltas.setVisibility(View.GONE);
        }
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
                .addInterceptor(new com.example.systemapp.EmptyListOnErrorObjectInterceptor())
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

                        // Facturación en Sitio: pedir rango de numeración de facturas para hoy,
                        // solo si no queda uno vigente (evita gastar bloques innecesariamente en
                        // cada resync del mismo día) — ver PLAN_FACTURACION_EN_SITIO.md, decisión 5.
                        // Solo aplica al flavor que soporta la feature; los demás backends no
                        // tienen el endpoint "rangoFacturacion" todavía.
                        if (com.example.systemapp.BuildConfig.FACTURACION_SITIO_SOPORTADA) {
                            solicitarRangoFacturacionSiHaceFalta(asignadas.size());
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

        // 🔹 Descargar tarifas (Facturación en Sitio) — ver PLAN_FACTURACION_EN_SITIO.md, contrato 1.3
        cardSyncTarifas.setOnClickListener(v -> {
            showProgress(true);
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            systemAppAPI.tarifaVigente().clone().enqueue(new Callback<TarifaVigenteResponse>() {
                @Override
                public void onResponse(Call<TarifaVigenteResponse> call, Response<TarifaVigenteResponse> response) {
                    showProgress(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    if (!response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
                        Log.e("SyncFragment", response.message());
                        return;
                    }

                    TarifaVigenteResponse tarifa = response.body();
                    if (tarifa != null) {
                        adminSQLiteOpenHelper.guardarTarifaVigente(tarifa);

                        if (tarifa.config_facturacion_sitio != null) {
                            TarifaVigenteResponse.ConfigFacturacionSitioDTO cfg = tarifa.config_facturacion_sitio;
                            if (cfg.habilitar_normal != null) {
                                SessionPrefs.get(getActivity()).setPermiteFacturarNormal(cfg.habilitar_normal);
                            }
                            if (cfg.habilitar_alto != null) {
                                SessionPrefs.get(getActivity()).setPermiteFacturarAlto(cfg.habilitar_alto);
                            }
                            if (cfg.habilitar_bajo != null) {
                                SessionPrefs.get(getActivity()).setPermiteFacturarBajo(cfg.habilitar_bajo);
                            }
                            // habilitar_negativo se ignora a propósito: Caso B nunca se factura en
                            // sitio, es una regla fija de negocio, no configurable desde el backend.
                        }

                        Toast.makeText(getActivity(), "Tarifas sincronizadas", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<TarifaVigenteResponse> call, Throwable t) {
                    showProgress(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("SyncFragment", t.getMessage());
                }
            });
        });

        // 🔹 Descargar facturas resueltas — ver PLAN_FACTURACION_EN_SITIO.md, contrato 1.4 y Fase 12
        cardSyncFacturasResueltas.setOnClickListener(v -> {
            showProgress(true);
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            systemAppAPI.facturasResueltas().clone().enqueue(new Callback<List<FacturaResueltaDTO>>() {
                @Override
                public void onResponse(Call<List<FacturaResueltaDTO>> call, Response<List<FacturaResueltaDTO>> response) {
                    showProgress(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    if (!response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
                        Log.e("SyncFragment", response.message());
                        return;
                    }

                    List<FacturaResueltaDTO> resueltas = response.body();
                    if (resueltas != null) {
                        Gson gson = new Gson();
                        for (FacturaResueltaDTO dto : resueltas) {
                            FacturaResueltaServidor fr = new FacturaResueltaServidor();
                            fr.setFacturaId(dto.factura_id);
                            fr.setNumeroFactura(dto.numero_factura);
                            fr.setLecturaId(dto.lectura_id);
                            fr.setSuscriptor(dto.suscriptor);
                            fr.setDesgloseJson(gson.toJson(dto));
                            fr.setTotalAPagar(dto.total_a_pagar);
                            fr.setEstado(dto.estado);
                            fr.setImpresa(false);
                            adminSQLiteOpenHelper.insertFacturaResuelta(fr);
                        }
                        Toast.makeText(getActivity(), "Facturas resueltas sincronizadas: " + resueltas.size(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<List<FacturaResueltaDTO>> call, Throwable t) {
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
                        // El backend responde 403 {"error": "No puede sincronizar listas"}
                        // cuando el usuario no tiene órdenes pendientes (ver CLAUDE.md) — se
                        // muestra ese texto en vez del genérico "Forbidden" de HTTP.
                        String mensaje = extraerMensajeError(response.errorBody(), response.message());
                        Toast.makeText(getActivity(), mensaje, Toast.LENGTH_LONG).show();
                        Log.e("SyncFragment", mensaje);
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
                    .setPositiveButton("OK", (dialog, id) -> {
                        initUpload();
                        if (com.example.systemapp.BuildConfig.FACTURACION_SITIO_SOPORTADA) {
                            initUploadFacturas(); // Facturación en Sitio — ver Fase 12
                        }
                    })
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

    // El backend manda los errores "sin resultados" como {"error": "..."} en el body (ver
    // CLAUDE.md) — esto extrae ese texto para mostrarlo en vez del genérico de HTTP
    // (ej. "Forbidden" para un 403), con fallback si el body no viene o no es el JSON esperado.
    private String extraerMensajeError(ResponseBody errorBody, String fallbackHttp) {
        if (errorBody == null) {
            return fallbackHttp;
        }
        try {
            String texto = errorBody.string();
            com.google.gson.JsonObject json = new com.google.gson.JsonParser().parse(texto).getAsJsonObject();
            if (json.has("error")) {
                return json.get("error").getAsString();
            }
            return texto;
        } catch (Exception e) {
            return fallbackHttp;
        }
    }

    // Facturación en Sitio — ver PLAN_FACTURACION_EN_SITIO.md, decisión 5 y contrato 1.2.
    private void solicitarRangoFacturacionSiHaceFalta(int cantidadOrdenesRuta) {
        String periodoActual = new SimpleDateFormat("yyyyMM", Locale.getDefault()).format(new Date());

        if (adminSQLiteOpenHelper.tieneRangoFacturacionVigente(periodoActual)) {
            return; // ya hay números disponibles para hoy, no gastar un bloque nuevo
        }

        systemAppAPI.rangoFacturacion(new RangoFacturacionRequest(Math.max(cantidadOrdenesRuta, 1)))
                .clone().enqueue(new Callback<RangoFacturacionResponse>() {
            @Override
            public void onResponse(Call<RangoFacturacionResponse> call, Response<RangoFacturacionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adminSQLiteOpenHelper.guardarRangoFacturacion(response.body());
                    Log.d("SyncFragment", "Rango de facturación asignado: " +
                            response.body().secuencia_desde + "-" + response.body().secuencia_hasta);
                } else {
                    Log.w("SyncFragment", "No se pudo obtener rango de facturación: " +
                            (response.message() != null ? response.message() : "sin body"));
                }
            }

            @Override
            public void onFailure(Call<RangoFacturacionResponse> call, Throwable t) {
                // No bloquea la sincronización de órdenes por esto — solo significa que
                // "Facturación en sitio" no podrá emitir números hasta el próximo sync exitoso.
                Log.w("SyncFragment", "Fallo al pedir rango de facturación: " + t.getMessage());
            }
        });
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

            Object campoFoto;
            if (com.example.systemapp.BuildConfig.FOTOS_MULTIPLES_SOPORTADA) {
                List<String> fotosBase64 = new ArrayList<>();
                for (String path : ordentoupload.getFotosList()) {
                    fotosBase64.add(Utilidades.encodeImage(path));
                }
                campoFoto = fotosBase64;
            } else {
                String campoFoto1 = "";
                if (ordentoupload.getRuta_foto() != null) {
                    campoFoto1 = Utilidades.encodeImage(ordentoupload.getRuta_foto());
                }
                campoFoto = campoFoto1;
            }
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




    // ===== Facturación en Sitio — subida de facturas locales pendientes (Fase 12) =====
    // Mismo patrón que initUpload()/sendDataToServerOnetoOne(): cola pull-based basada en un
    // estado persistido (aquí "factura_local.estado", allá "Uploadlec"), recorrido secuencial
    // uno-a-uno, sin reintento automático en caso de fallo de red (se reintenta en el próximo
    // "Subir Órdenes").
    private void initUploadFacturas() {
        List facturasLocales = adminSQLiteOpenHelper.getData("factura_local",
                "estado = '" + FacturaLocal.ESTADO_PENDIENTE_SYNC + "'");
        if (facturasLocales.size() > 0) {
            sendFacturasLocalesOnetoOne(facturasLocales, 1, facturasLocales.size());
        }
    }

    private void sendFacturasLocalesOnetoOne(final List facturasLocales, final int ciclo, final int total) {
        if (facturasLocales.size() > 0) {
            final FacturaLocal facturaLocal = (FacturaLocal) facturasLocales.get(0);

            Gson gson = new Gson();
            FacturaCalculada factura = gson.fromJson(facturaLocal.getDesgloseJson(), FacturaCalculada.class);

            FacturaLocalEnviarDTO dto = new FacturaLocalEnviarDTO();
            dto.id_local = facturaLocal.getIdLocal();
            dto.numero_factura = facturaLocal.getNumeroFactura();
            dto.lectura_id = facturaLocal.getLecturaId();
            dto.suscriptor = facturaLocal.getSuscriptor();
            dto.periodo = facturaLocal.getPeriodo();
            dto.lectura_anterior = facturaLocal.getLecturaAnterior();
            dto.lectura_actual = facturaLocal.getLecturaActual();
            dto.consumo_m3 = facturaLocal.getConsumoM3();
            dto.estrato_id_usado = facturaLocal.getEstratoIdUsado();
            dto.tarifa_periodo_id_usado = facturaLocal.getTarifaPeriodoIdUsado();
            if (factura != null) {
                dto.acueducto = DesgloseServicioDTO.desde(factura.acueducto);
                dto.alcantarillado = DesgloseServicioDTO.desde(factura.alcantarillado);
                dto.aseo = DesgloseServicioDTO.desde(factura.aseo);
                dto.saldo_anterior = factura.saldoAnterior;
            }
            dto.total_a_pagar = facturaLocal.getTotalAPagar();
            dto.fecha_impresion = facturaLocal.getFechaImpresion();
            dto.clasificacion = facturaLocal.getClasificacion();

            if (facturaLocal.getAnulaAIdLocal() != null) {
                List anulada = adminSQLiteOpenHelper.getData("factura_local",
                        "id_local = '" + facturaLocal.getAnulaAIdLocal() + "'");
                if (!anulada.isEmpty()) {
                    dto.anula_numero_factura = ((FacturaLocal) anulada.get(0)).getNumeroFactura();
                }
            }

            systemAppAPI.subirFactura(dto).clone().enqueue(new Callback<FacturaSubidaResponse>() {
                @Override
                public void onResponse(Call<FacturaSubidaResponse> call, Response<FacturaSubidaResponse> response) {
                    if (response.isSuccessful() && response.body() != null && Boolean.TRUE.equals(response.body().success)) {
                        // No se sobrescribe numero_factura ni total_a_pagar con la respuesta del
                        // servidor: prevalece el valor ya impreso (decisión de negocio del plan).
                        facturaLocal.setFacturaIdServidor(response.body().factura_id);
                        facturaLocal.setSincronizado(true);
                        facturaLocal.setEstado(FacturaLocal.ESTADO_SINCRONIZADA);
                        adminSQLiteOpenHelper.insertFacturaLocal(facturaLocal, true);
                    } else {
                        Log.w("SyncFragment", "No se pudo subir factura " + facturaLocal.getNumeroFactura() + ": " +
                                (response.message() != null ? response.message() : "sin body"));
                    }

                    facturasLocales.remove(0);
                    sendFacturasLocalesOnetoOne(facturasLocales, ciclo + 1, total);
                }

                @Override
                public void onFailure(Call<FacturaSubidaResponse> call, Throwable t) {
                    Log.w("SyncFragment", "Fallo al subir facturas locales: " + t.getMessage());
                    // No se corta la subida de lecturas por esto; las facturas pendientes se
                    // reintentan en el próximo "Subir Órdenes".
                }
            });
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

