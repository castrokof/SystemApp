package com.example.systemapp.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Parcelable;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.systemapp.MainActivity;
import com.example.systemapp.R;
import com.example.systemapp.SystemAppAPI;
import com.example.systemapp.data.Adaptador;
import com.example.systemapp.data.AdminSQLiteOpenHelper;
import com.example.systemapp.data.SessionPrefs;
import com.example.systemapp.data.VariablesSesion;
import com.example.systemapp.data.model.DBOrdenLecturas;
import com.example.systemapp.data.model.DBdefinicionOrdenes;
import com.example.systemapp.databinding.FragmentRAsignadasBinding;
import com.example.systemapp.databinding.FragmentSyncBinding;
import com.example.systemapp.ui.data.Fragment_form_lectura;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RAsignadasFragment extends Fragment {

    private static final int REQUEST_LOCATION = 1;

    //Retrofit y cliente API
    private SystemAppAPI systemappAPI;
    private Retrofit systemapp;


    //objeto para transacciones con BD
    private AdminSQLiteOpenHelper adminSQLiteOpenHelper;

    //contendrá la lista de rutas completa para mostrar
    private List allRutas;
    private int posicionRuta;//posición de la ruta elegida del listview
    private FragmentRAsignadasBinding binding;
    private Adaptador adaptador;
    private ListView listaItems;
    private TextView txt_nodata;
    private RelativeLayout relativeLayout;

    //Edittext for search
    private EditText input_search;

    //Listener para comunicar cambios entre fragments
    private fragment_ordenes parentFragment;

    public void handleOnBackPress() {
        Log.d("RAsignadasFragment", "Handle new METHOD!!!!!!!");

    }

    public interface ChangeListener extends Serializable {
        public void onCantChange(String cantpendientes, String cantreasig, String cantproces);
    }

    ChangeListener listener;

    public static RAsignadasFragment newInstace(ChangeListener changeListener){

        RAsignadasFragment rAsignadasFragment = new RAsignadasFragment();

        //set fragment arguments
        Bundle bundle = new Bundle();
        bundle.putSerializable("changeListener", changeListener);
        rAsignadasFragment.setArguments(bundle);

        return rAsignadasFragment;

    }

    Parcelable state;



    @Override
    public void onResume() {
        Log.d("RAsignadasFragment", "onResume");

        if (listaItems!=null){
            listaItems.setSelection(VariablesSesion.posicionSelec);
            listaItems.setItemChecked(VariablesSesion.posicionSelec, true);
        }
        super.onResume();
    }

    @Override
    public void onDetach() {
        Log.d("RAsignadasFragment", "onDetach");
        super.onDetach();
    }

    @Override
    public void onStop() {
        Log.d("RAsignadasFragment", "onStop");
        super.onStop();
    }

    @Override
    public void onStart() {
        Log.d("RAsignadasFragment", "onStart");
        super.onStart();
    }

    @Override
    public void onDestroy() {
        Log.d("RAsignadasFragment", "onDestroy");
        super.onDestroy();
    }




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentRAsignadasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setTitle(getString(R.string.app_name) + " - Pendientes");
        }
        getActivity().setTitle(getString(R.string.app_name) + " - Pendientes");

        Bundle bundle = getArguments();


        listaItems = root.findViewById(R.id.listItems);
        txt_nodata = root.findViewById(R.id.txt_nodata);
        relativeLayout = root.findViewById(R.id.rl_load);
        input_search = root.findViewById(R.id.input_searchm);

        showProgress(true);

        //Instanciamos el DBHelper
        adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(getContext());

        //abrir acceso a las preferencias
        final SharedPreferences mPrefs = getActivity().
                getSharedPreferences("SYSTEMAPP_PREFS", Context.MODE_PRIVATE);

        // Crear conexión al servicio REST
        systemapp = new Retrofit.Builder()
                .baseUrl(SystemAppAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        systemappAPI = systemapp.create(SystemAppAPI.class);

        //acceder a items para cambiar cantidad de rutas
        View viewbtnav = inflater.inflate(R.layout.fragment_ordenes, null);
        BottomNavigationView bottomNav = viewbtnav.findViewById(R.id.bottom_navigation);

        //set titles for options on nav


        //verificar si las  rutas ya fueron sincronizadas
        //if(mPrefs.getBoolean("PREF_RUTAS_SYNC", false)){
        //verificar si el objeto que se va a compartir entre clases que contiene la lista de
        //rutas ya está seteado
        if(VariablesSesion.rutasGobalAsignadas ==null) {

            Log.d("RAsignadasFragment", "sincroniza desde BD");
            //Se consultan los registros usando filtro para traer solo rutas y no revisiones
            allRutas = adminSQLiteOpenHelper.getData("lecturas",
                    "" +
                            "Tipo_orden = 'RUTAS' AND Categoria_orden = 'ASIGNADAS' ");
            VariablesSesion.setRutasGobalAsignadas(allRutas);
        }else {
            //obtener la cantidad de rutas asignadas para verificar que se estén mostrando todas

            int count = 0;
            switch (VariablesSesion.filtro){
                case "ACUEDUCTO":
                    count = adminSQLiteOpenHelper.getCount(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                            "Tipo_orden = 'RUTAS' AND Categoria_orden = 'ASIGNADAS' AND nservic='ACUEDUCTO'");
                    if (count>VariablesSesion.rutasGobalAsignadas.size()){
                        //Se consultan los registros usando filtro para traer solo rutas y no revisiones
                        allRutas = adminSQLiteOpenHelper.getData("lecturas",
                                "Tipo_orden = 'RUTAS' AND Categoria_orden = 'ASIGNADAS' AND nservic='ACUEDUCTO'");
                        VariablesSesion.setRutasGobalAsignadas(allRutas);
                    }else{
                        allRutas = VariablesSesion.rutasGobalAsignadas;
                    }
                    break;
                case "ENERGIA":
                    count = adminSQLiteOpenHelper.getCount("lecturas",
                            "Tipo_orden = 'RUTAS' AND Categoria_orden = 'ASIGNADAS' AND nservic='ENERGIA'");
                    if (count>VariablesSesion.rutasGobalAsignadas.size()){
                        //Se consultan los registros usando filtro para traer solo rutas y no revisiones
                        allRutas = adminSQLiteOpenHelper.getData("lecturas",
                                "Tipo_orden = 'RUTAS' AND Categoria_orden = 'ASIGNADAS' AND nservic='ENERGIA'");
                        VariablesSesion.setRutasGobalAsignadas(allRutas);
                    }else{
                        allRutas = VariablesSesion.rutasGobalAsignadas;
                    }
                    break;
                case "TODO":
                    count = adminSQLiteOpenHelper.getCount("lecturas",
                            "Tipo_orden = 'RUTAS' AND Categoria_orden = 'ASIGNADAS' ");
                    if (count>VariablesSesion.rutasGobalAsignadas.size()){
                        //Se consultan los registros usando filtro para traer solo rutas y no revisiones
                        allRutas = adminSQLiteOpenHelper.getData("lecturas",
                                "Tipo_orden = 'RUTAS' AND Categoria_orden = 'ASIGNADAS' ");
                        VariablesSesion.setRutasGobalAsignadas(allRutas);
                    }else{
                        allRutas = VariablesSesion.rutasGobalAsignadas;
                    }
                    break;
            }

        }
        //setear el total de rutas asignadas
        if (mPrefs.getInt("PREF_RUTAS_PENDIENTES", 0) != allRutas.size()) {

            SessionPrefs.get(getActivity()).setPrefRutasPendientes(allRutas.size());
            if (parentFragment != null) {
                parentFragment.setCantidades(allRutas.size() + "",
                        mPrefs.getInt("PREF_RUTAS_REASIGNADAS", 0) + "",
                        mPrefs.getInt("PREF_RUTAS_PROCESADAS", 0) + "");
                Log.d("RAsignadasFragment", "Actualizando cantidades...");
            }


        }


        //verificar si hay registros
        if (allRutas.size() == 0) {
            showProgress(false);
            txt_nodata.setVisibility(View.VISIBLE);
        } else {

            // Restore previous state (including selected item index and scroll position)
            //if is called from popBackStackImmediate
            adaptador = new Adaptador(getActivity(), (ArrayList) allRutas);
            listaItems.setAdapter(adaptador);
            showProgress(false);


        }

        Log.d("RAsignadasFragment", "posicionSelec " + VariablesSesion.posicionSelec);
        listaItems.setSelector(R.drawable.selector_item);

        listaItems.setSelection(VariablesSesion.posicionSelec);

        listaItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {




                posicionRuta = getAbsolutePosition(allRutas, adaptador.getItem(position).getId());
                checkPermissionGPSandOpenRoute();

            }
        });


        //action to search
        input_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                adaptador.getFilter().filter(s);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return root;
    }

    private void checkPermissionGPSandOpenRoute(){

        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        //comprobar que se tenga gps encendido
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Pedir que se active el gps
            MainActivity.displayPromptForEnablingGPS(getActivity());

        }else{

            //comprobar que se tengan permisos para acceder a la posición gps
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                Log.d("RAsignadasFragmen", "NO marcó no volver a preguntar");
                requestPermissions(
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);


            }else{
                openSelectedRoute(allRutas, posicionRuta);
            }

        }

    }



    private void openSelectedRoute(List<DBOrdenLecturas> allRutas, int posicionRuta){

        Fragment_form_lectura fragment_form_lectura = new Fragment_form_lectura();
        DBOrdenLecturas orden = allRutas.get(posicionRuta);
        VariablesSesion.posicionSelec = posicionRuta;

        List ordenesSuscriptor = getAllOrdeneswithContrato(allRutas,
                orden.getSuscriptor(), orden.getId());

        Bundle bundle =  new Bundle();
        bundle.putSerializable("orden", (Serializable) ordenesSuscriptor);
        bundle.putSerializable("allRutas", (Serializable) allRutas);
        bundle.putInt("posicion", posicionRuta);

        fragment_form_lectura.setArguments(bundle);

        Log.d("RAsignadasFragment", orden.getSuscriptor() + " lecturas "+ordenesSuscriptor.size());


        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container1,fragment_form_lectura);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {



        Log.d("RAsignadasFragmen", "PackageManager.PERMISSION_GRANTED "+ PackageManager.PERMISSION_GRANTED);
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[grantResults.length-1] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("RAsignadasFragmen", "Ysy Granted!");
                    openSelectedRoute(allRutas, posicionRuta);


                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //verifica si se seleccionó "no volver a preguntar"
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                            &&!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        Log.d("RAsignadasFragmen", "Marcó no volver a preguntar");

                        new AlertDialog.Builder(getActivity()).setMessage(
                                        "Se necesitan permisos para obtener la ubicación, " +
                                                "por favor, entra a la configuración del dispositivo para otorgarle permisos a la aplicación")
                                .setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                Intent intent = new Intent();
                                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                                intent.setData(uri);
                                                getActivity().startActivity(intent);
                                                dialog.dismiss();

                                            }
                                        }).create().show();

                    }else{
                        Log.d("RAsignadasFragmen", "NO marcó no volver a preguntar");
                        requestPermissions(
                                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_LOCATION);
                    }
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void showProgress(boolean show) {
        relativeLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Devuelve una lista de ordenes o rutas relacionadas a un mismo contrato
     * @param ordenes lista completa de ordenes
     * @param contrato número de contrato
     * @param idreg1 id del registro o ruta elegida, que debe quedar como primer elmento de la lista devuelta
     * @return lista de ordenes
     */
    public static List getAllOrdeneswithContrato(List<DBOrdenLecturas> ordenes, String contrato, String idreg1) {

        List ordenesContrato = new ArrayList();

        for (DBOrdenLecturas orden : ordenes) {
            if (orden.getId().equals(idreg1)){
                ordenesContrato.add(0, orden);
            }else if (orden.getSuscriptor().equals(contrato)) {
                ordenesContrato.add(orden);
            }
        }
        return ordenesContrato;
    }

    public static int getAbsolutePosition(List<DBOrdenLecturas> ordenes, String idreg) {

        for (int i=0; i < ordenes.size(); i++){
            if (ordenes.get(i).getId().equals(idreg)){
                return i;
            }
        }

        return 0;

    }
}