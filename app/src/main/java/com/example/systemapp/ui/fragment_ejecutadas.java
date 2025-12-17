package com.example.systemapp.ui;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.systemapp.R;
import com.example.systemapp.SystemAppAPI;
import com.example.systemapp.data.AdminSQLiteOpenHelper;
import com.example.systemapp.data.SessionPrefs;
import com.example.systemapp.data.VariablesSesion;
import com.example.systemapp.data.model.DBListas;
import com.example.systemapp.data.model.DBOrdenLecturas;
import com.example.systemapp.data.model.DBdefinicionOrdenes;
import com.example.systemapp.data.model.LoginEnvio;
import com.example.systemapp.databinding.FragmentEjecutadasBinding;
import com.example.systemapp.ui.data.Fragment_form_lectura;
import com.google.android.material.bottomnavigation.BottomNavigationView;





public class fragment_ejecutadas extends Fragment implements REjecutadasFragment.ChangeListener  {


    private FragmentEjecutadasBinding binding;

    //objeto para transacciones con BD
    private AdminSQLiteOpenHelper adminSQLiteOpenHelper;

    //Items del BottomNav

    private static MenuItem itmProces;

    private LinearLayout linearLayout;

    private Window window;


    private FragmentManager mFragmentManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        this.window = getActivity().getWindow();
        String primary = "#e04a09";

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayShowCustomEnabled(false);
            window.setStatusBarColor(Color.parseColor(primary));
            //window.setBackgroundDrawable(new ColorDrawable(Color.parseColor(primary)));
            window.setNavigationBarColor(Color.parseColor(primary));
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(primary)));
            actionBar.setTitle(getString(R.string.app_name) + " - List Ejecutadas");

        }


        getActivity().setTitle(getString(R.string.app_name) + " - List Ejecutadas");


        binding = FragmentEjecutadasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();




        BottomNavigationView bottomNav = root.findViewById(R.id.bottom_navigation1);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        linearLayout = (LinearLayout) root.findViewById(R.id.linearl_load1);

        //showProgress(true);




        itmProces =  bottomNav.getMenu().findItem(R.id.nav_proces);

       // itmPend.setTitle(getString(R.string.bottom_nav_pendientes, 0));
       // itmreasig.setTitle(getString(R.string.bottom_nav_reasignadas, 0));
        itmProces.setTitle(getString(R.string.bottom_nav_procesadas, 0));

        //Cargar o sincronizar datos de ordenes
        //Instanciamos el DBHelper
        adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(getContext());

        //establecer fragment por defecto
        REjecutadasFragment rEjecutadasFragment = new REjecutadasFragment();

        mFragmentManager = getChildFragmentManager();
        changeFragment(rEjecutadasFragment, true);




        return root;
    }

    private void setInfoUInit(SharedPreferences mPrefs){

        int cantAsigadas = 0;
        int cantRelectura = 0;
        switch (VariablesSesion.filtro){
            case "ACUEDUCTO":
                cantAsigadas = adminSQLiteOpenHelper.getCount(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA' AND nservic='ACUEDUCTO'");
                cantRelectura = adminSQLiteOpenHelper.getCount(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA' AND nservic='ACUEDUCTO'");
                SessionPrefs.get(getActivity()).setPrefRutasPendientes(cantAsigadas);
                SessionPrefs.get(getActivity()).setPrefRutasProcesadas(cantRelectura);
                //setear la variable compartida en la sesión con la lista de rutas asgnadas según el filtro
                VariablesSesion.setRutasGobalAsignadas(adminSQLiteOpenHelper.getData(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA' AND nservic='ACUEDUCTO'"));
                Log.d("OrdenesFragment", "RELECTURA "+cantRelectura);
                break;
            case "ENERGIA":
                cantAsigadas = adminSQLiteOpenHelper.getCount(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA' AND nservic='ENERGIA'");
                cantRelectura = adminSQLiteOpenHelper.getCount(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA' AND nservic='ENERGIA'");
                SessionPrefs.get(getActivity()).setPrefRutasPendientes(cantAsigadas);
                SessionPrefs.get(getActivity()).setPrefRutasProcesadas(cantRelectura);
                //setear la variable compartida en la sesión con la lista de rutas asgnadas según el filtro
                VariablesSesion.setRutasGobalAsignadas(adminSQLiteOpenHelper.getData(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA' AND nservic='ENERGIA'"));
                Log.d("OrdenesFragment", "PROCESADAS "+cantRelectura);
                break;
            case "TODO":
                cantAsigadas = adminSQLiteOpenHelper.getCount(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA'");
                cantRelectura = adminSQLiteOpenHelper.getCount(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA'");
                SessionPrefs.get(getActivity()).setPrefRutasPendientes(cantAsigadas);
                SessionPrefs.get(getActivity()).setPrefRutasProcesadas(cantRelectura);
                //setear la variable compartida en la sesión con la lista de rutas asgnadas según el filtro
                VariablesSesion.setRutasGobalAsignadas(adminSQLiteOpenHelper.getData(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA'"));
                Log.d("OrdenesFragment", "PROCESADAS "+cantRelectura);
                break;
        }


        itmProces.setTitle(getString(R.string.bottom_nav_procesadas,
                mPrefs.getInt("PREF_RUTAS_PROCESADAS", 0)));

        //establecer fragment por defecto
        REjecutadasFragment rEjecutadasFragment = new REjecutadasFragment();

        mFragmentManager = getChildFragmentManager();
        changeFragment(rEjecutadasFragment, true);
    }


    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId(); // Get the ID once

                if (itemId == R.id.nav_proces) {
                    // Change action bar title
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.app_name) + " - List Ejecutadas");

                    // Show the RAsignadasFragment
                    selectedFragment = new REjecutadasFragment();
                    changeFragment(selectedFragment, false);
                    return true; // Return true to show the item as selected

                }

                return false; // Return false if the item is not handled
            };

    private void changeFragment(Fragment fragment, boolean needToAddBackstack) {
        mFragmentManager =  getActivity().getSupportFragmentManager();
        FragmentTransaction mFragmentTransaction =  mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.fragment_container2, fragment);
        mFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        if (needToAddBackstack)
            mFragmentTransaction.addToBackStack(null);
        mFragmentTransaction.commit();
    }







    public void setCantidades(String cantpendientes, String cantreasig, String cantproces) {

        itmProces.setTitle(getString(R.string.bottom_nav_procesadas,
         Integer.parseInt(cantproces)));
        Log.d("OrdenesFragment", "Actualizando cantidades");
    }

    public void handleOnBackPress() {

        Log.d("OrdenesFragment", "Handle new METHOD!!!!!!!");
        if (getChildFragmentManager().findFragmentById(R.id.fragment_container2) instanceof RAsignadasFragment) {
            ((RAsignadasFragment) getChildFragmentManager().findFragmentById(R.id.fragment_container2)).handleOnBackPress();
            return;
        }
        if (getChildFragmentManager().findFragmentById(R.id.fragment_container2) instanceof Fragment_form_lectura) {

            mFragmentManager.popBackStack();
            Log.d("OrdenesFragment", "Esta en LecturaFragment!!!!!"+
                    mFragmentManager.getBackStackEntryCount());
            return;
        }
    }


    @Override
    public void onCantChange(String cantpendientes, String cantreasig, String cantproces) {

        itmProces.setTitle(getString(R.string.bottom_nav_procesadas,
                Integer.parseInt(cantproces)));
        Log.d("OrdenesFragment", "PROCESADAS");
    }


}