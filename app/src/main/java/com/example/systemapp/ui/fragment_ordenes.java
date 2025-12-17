package com.example.systemapp.ui;



import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.systemapp.AuthInterceptor;
import com.example.systemapp.R;
import com.example.systemapp.SystemAppAPI;
import com.example.systemapp.data.AdminSQLiteOpenHelper;
import com.example.systemapp.data.SessionPrefs;
import com.example.systemapp.data.VariablesSesion;
import com.example.systemapp.data.model.DBListas;
import com.example.systemapp.data.model.DBOrdenLecturas;
import com.example.systemapp.data.model.DBdefinicionOrdenes;
import com.example.systemapp.data.model.LoginEnvio;
import com.example.systemapp.data.model.LoginRespuesta;
import com.example.systemapp.data.model.OrdenesHijas;
import com.example.systemapp.data.model.OrdenesPares;
import com.example.systemapp.databinding.FragmentOrdenesBinding;
import com.example.systemapp.databinding.FragmentSyncBinding;
import com.example.systemapp.databinding.NavHeaderMainBinding;
import com.example.systemapp.ui.data.Fragment_form_lectura;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class fragment_ordenes extends Fragment implements RAsignadasFragment.ChangeListener  {

    //Retrofit y cliente API
    private SystemAppAPI systemappAPI;
    private Retrofit systemapp;
    private FragmentOrdenesBinding binding;

    //objeto para transacciones con BD
    private AdminSQLiteOpenHelper adminSQLiteOpenHelper;

    //Items del BottomNav
    private static MenuItem itmPend;

    private LinearLayout linearLayout;

    private Window window;


    private FragmentManager mFragmentManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        this.window = getActivity().getWindow();
        String primary = "#6200EE";

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayShowCustomEnabled(false);
            window.setStatusBarColor(Color.parseColor(primary));
            window.setNavigationBarColor(Color.parseColor(primary));
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(primary)));
            actionBar.setTitle(getString(R.string.app_name) + " - List Pendientes");
        }
        getActivity().setTitle(getString(R.string.app_name) + " - List Pendientes");

        binding = FragmentOrdenesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        BottomNavigationView bottomNav = root.findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        linearLayout = (LinearLayout) root.findViewById(R.id.linearl_load);

        showProgress(true);

        itmPend = bottomNav.getMenu().findItem(R.id.nav_pend);
        itmPend.setTitle(getString(R.string.bottom_nav_pendientes, 0));

        //Instanciamos el DBHelper
        adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(getContext());

        //abrir acceso a las preferencias
        final SharedPreferences mPrefs = getActivity().getSharedPreferences("SYSTEMAPP_PREFS", Context.MODE_PRIVATE);

        // ⭐ Crear OkHttpClient con el AuthInterceptor
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(getContext()))
                .build();

        // ⭐ Crear conexión al servicio REST con el cliente
        systemapp = new Retrofit.Builder()
                .baseUrl(SystemAppAPI.BASE_URL)
                .client(client) // Agregar el cliente con interceptor
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        systemappAPI = systemapp.create(SystemAppAPI.class);

        //verificar si las rutas ya fueron sincronizadas
        if(mPrefs.getBoolean("PREF_RUTAS_SYNC", false)){
            showProgress(false);
            RAsignadasFragment rAsignadasFragment = new RAsignadasFragment();
            mFragmentManager = getChildFragmentManager();
            changeFragment(rAsignadasFragment, true);
        } else {
            // ⭐ Ya NO necesitas usuario ni password
            // String usuario = mPrefs.getString("PREF_USER_NAME","");
            // String password = "2";

            // ⭐ Definir los servicios SIN parámetros (el token va automático en el header)
            final Call<List<DBOrdenLecturas>> getRoutes = systemappAPI.cargue();
            final Call<List<DBListas>> getListas = systemappAPI.listas();

            getRoutes.enqueue(new Callback<List<DBOrdenLecturas>>() {
                @Override
                public void onResponse(Call<List<DBOrdenLecturas>> call, Response<List<DBOrdenLecturas>> response) {

                    //sincronizar listas
                    getListas.clone().enqueue(new Callback<List<DBListas>>() {
                        @Override
                        public void onResponse(Call<List<DBListas>> call, Response<List<DBListas>> response) {

                            //Procesar errores
                            String error;
                            if (!response.isSuccessful()){
                                if (response.errorBody()
                                        .contentType()
                                        .subtype()
                                        .equals("application/json")) {
                                    error = response.message();
                                    Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                                    Log.e("SyncFragment", error);
                                    return;
                                }
                            }

                            List<DBListas> listElementosListaBD = response.body();

                            //insertar datos de las rutas en la base de datos
                            if (listElementosListaBD != null && listElementosListaBD.size() > 0){
                                for (DBListas elemento : listElementosListaBD) {
                                    adminSQLiteOpenHelper.insertElementoLista(elemento);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<List<DBListas>> call, Throwable t) {
                            Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
                            System.out.println(t.getMessage());
                        }
                    });

                    //Procesar errores
                    String error = "";
                    if (!response.isSuccessful()){
                        error = response.message();
                        System.out.println(error);
                        showProgress(false);
                        return;
                    }

                    Toast.makeText(getActivity(), "Almacenando registros, espere por favor...", Toast.LENGTH_LONG).show();

                    List<DBOrdenLecturas> asignadas = response.body();
                    //activar bandera para indicar que ya se sincronizaron las rutas
                    mPrefs.edit().putBoolean("PREF_RUTAS_SYNC", true).apply();

                    if (asignadas == null || asignadas.size() == 0){
                        showProgress(false);
                    } else {
                        showProgress(false);

                        for (DBOrdenLecturas dbOrdenLecturas : asignadas) {
                            adminSQLiteOpenHelper.insertOrden(dbOrdenLecturas, false);
                        }

                        Toast.makeText(getActivity(), "Sincronización exitosa!", Toast.LENGTH_LONG).show();

                        //setear el total de rutas asignadas
                        if (mPrefs.getInt("PREF_RUTAS_PENDIENTES", 0) != asignadas.size()){
                            SessionPrefs.get(getActivity()).setPrefRutasPendientes(asignadas.size());
                            Log.d("Home", "cambiando cantidad..." + getString(R.string.bottom_nav_pendientes,
                                    asignadas.size()));
                            itmPend.setTitle(getString(R.string.bottom_nav_pendientes,
                                    asignadas.size()));
                        }

                        //establecer fragment por defecto
                        RAsignadasFragment rAsignadasFragment = new RAsignadasFragment();
                        mFragmentManager = getChildFragmentManager();
                        changeFragment(rAsignadasFragment, true);
                    }
                }

                @Override
                public void onFailure(Call<List<DBOrdenLecturas>> call, Throwable t) {
                    showProgress(false);
                    System.out.println(t.getMessage());
                    Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        return root;
    }

    private void setInfoUInit(SharedPreferences mPrefs){

        int cantAsigadas = 0;
        int cantRelectura = 0;
        switch (VariablesSesion.filtro){
            case "ACUEDUCTO":
                cantAsigadas = adminSQLiteOpenHelper.getCount(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'ASIGNADAS' AND nservic='ACUEDUCTO'");
                cantRelectura = adminSQLiteOpenHelper.getCount(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA' AND nservic='ACUEDUCTO'");
                SessionPrefs.get(getActivity()).setPrefRutasPendientes(cantAsigadas);
                SessionPrefs.get(getActivity()).setPrefRutasProcesadas(cantRelectura);
                //setear la variable compartida en la sesión con la lista de rutas asgnadas según el filtro
                VariablesSesion.setRutasGobalAsignadas(adminSQLiteOpenHelper.getData(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'ASIGNADAS' AND nservic='ACUEDUCTO'"));
                Log.d("OrdenesFragment", "PROCESADAS "+cantRelectura);
                break;
            case "ENERGIA":
                cantAsigadas = adminSQLiteOpenHelper.getCount(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'ASIGNADAS' AND nservic='ENERGIA'");
                cantRelectura = adminSQLiteOpenHelper.getCount(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA' AND nservic='ENERGIA'");
                SessionPrefs.get(getActivity()).setPrefRutasPendientes(cantAsigadas);
                SessionPrefs.get(getActivity()).setPrefRutasProcesadas(cantRelectura);
                //setear la variable compartida en la sesión con la lista de rutas asgnadas según el filtro
                VariablesSesion.setRutasGobalAsignadas(adminSQLiteOpenHelper.getData(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'ASIGNADAS' AND nservic='ENERGIA'"));
                Log.d("OrdenesFragment", "PROCESADAS "+cantRelectura);
                break;
            case "TODO":
                cantAsigadas = adminSQLiteOpenHelper.getCount(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'ASIGNADAS'");
                cantRelectura = adminSQLiteOpenHelper.getCount(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA'");
                SessionPrefs.get(getActivity()).setPrefRutasPendientes(cantAsigadas);
                SessionPrefs.get(getActivity()).setPrefRutasProcesadas(cantRelectura);
                //setear la variable compartida en la sesión con la lista de rutas asgnadas según el filtro
                VariablesSesion.setRutasGobalAsignadas(adminSQLiteOpenHelper.getData(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'ASIGNADAS'"));
                Log.d("OrdenesFragment", "PROCESADAS "+cantRelectura);
                break;
        }

        itmPend.setTitle(getString(R.string.bottom_nav_pendientes,
                mPrefs.getInt("PREF_RUTAS_PENDIENTES", 0)));


        //establecer fragment por defecto
        RAsignadasFragment rAsignadasFragment = new RAsignadasFragment();

        mFragmentManager = getChildFragmentManager();
        changeFragment(rAsignadasFragment, true);
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId(); // Get the ID once

                if (itemId == R.id.nav_pend) {
                    // Change action bar title
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.app_name) + " - List Pendientes");

                    // Show the RAsignadasFragment
                    selectedFragment = new RAsignadasFragment();
                    changeFragment(selectedFragment, false);
                    return true; // Return true to show the item as selected

                } 

                return false; // Return false if the item is not handled
            };


    private void changeFragment(Fragment fragment, boolean needToAddBackstack) {
        mFragmentManager =  getActivity().getSupportFragmentManager();
        FragmentTransaction mFragmentTransaction =  mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.fragment_container1, fragment);
        mFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        if (needToAddBackstack)
            mFragmentTransaction.addToBackStack(null);
        mFragmentTransaction.commit();
    }



    private void showProgress(boolean show) {
        linearLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }



    public void setCantidades(String cantpendientes, String cantreasig, String cantproces) {
        itmPend.setTitle(getString(R.string.bottom_nav_pendientes,
                Integer.parseInt(cantpendientes)));

        Log.d("OrdenesFragment", "Actualizando cantidades");
    }

    public void handleOnBackPress() {

        Log.d("OrdenesFragment", "Handle new METHOD!!!!!!!");
        if (getChildFragmentManager().findFragmentById(R.id.fragment_container1) instanceof RAsignadasFragment) {
            ((RAsignadasFragment) getChildFragmentManager().findFragmentById(R.id.fragment_container1)).handleOnBackPress();
            return;
        }
        if (getChildFragmentManager().findFragmentById(R.id.fragment_container1) instanceof Fragment_form_lectura) {

            mFragmentManager.popBackStack();
            Log.d("OrdenesFragment", "Esta en LecturaFragment!!!!!"+
                    mFragmentManager.getBackStackEntryCount());
            return;
        }
    }


    @Override
    public void onCantChange(String cantpendientes, String cantreasig, String cantproces) {
        itmPend.setTitle(getString(R.string.bottom_nav_pendientes,
                Integer.parseInt(cantpendientes)));

        Log.d("OrdenesFragment", "PROCESADAS");
    }


}