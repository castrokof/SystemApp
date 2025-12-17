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
import android.os.Parcelable;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.systemapp.MainActivity;
import com.example.systemapp.R;
import com.example.systemapp.SystemAppAPI;
import com.example.systemapp.data.Adaptador;
import com.example.systemapp.data.AdminSQLiteOpenHelper;
import com.example.systemapp.data.SessionPrefs;
import com.example.systemapp.data.VariablesSesion;
import com.example.systemapp.data.model.DBOrdenLecturas;
import com.example.systemapp.data.model.DBdefinicionOrdenes;
import com.example.systemapp.databinding.FragmentREjecutadasBinding;
import com.example.systemapp.ui.data.Fragment_form_lectura;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;




public class REjecutadasFragment extends Fragment {



    //objeto para transacciones con BD
    private AdminSQLiteOpenHelper adminSQLiteOpenHelper;

    //contendrá la lista de rutas completa para mostrar
    private List allRutas;
    private int posicionRuta;//posición de la ruta elegida del listview
    private FragmentREjecutadasBinding binding;
    private Adaptador adaptador;
    private ListView listaItems;
    private TextView txt_nodata;
    private RelativeLayout relativeLayout;

    //Edittext for search
    private EditText input_search;

    //Listener para comunicar cambios entre fragments
    private fragment_ejecutadas parentFragment;

    public void handleOnBackPress() {
        Log.d("RAsignadasFragment", "Handle new METHOD!!!!!!!");

    }

    public interface ChangeListener extends Serializable {
        public void onCantChange(String cantpendientes, String cantreasig, String cantproces);
    }

    ChangeListener listener;

    public static REjecutadasFragment newInstace(ChangeListener changeListener){

        REjecutadasFragment rEjecutadasFragment = new REjecutadasFragment();

        //set fragment arguments
        Bundle bundle = new Bundle();
        bundle.putSerializable("changeListener", changeListener);
        rEjecutadasFragment.setArguments(bundle);

        return rEjecutadasFragment;

    }

        Parcelable state;



    @Override
    public void onResume() {
        Log.d("REjecutadasFragment", "onResume");

        if (listaItems!=null){
            listaItems.setSelection(VariablesSesion.posicionSelectProc);
            listaItems.setItemChecked(VariablesSesion.posicionSelectProc, true);
        }
        super.onResume();
    }

    @Override
    public void onDetach() {
        Log.d("REjecutadasFragment", "onDetach");
        super.onDetach();
    }

    @Override
    public void onStop() {
        Log.d("REjecutadasFragment", "onStop");
        super.onStop();
    }

    @Override
    public void onStart() {
        Log.d("REjecutadasFragment", "onStart");
        super.onStart();
    }

    @Override
    public void onDestroy() {
        Log.d("REjecutadasFragment", "onDestroy");
        super.onDestroy();
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentREjecutadasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setTitle(getString(R.string.app_name) + " - Ejecutadas");
        }
        getActivity().setTitle(getString(R.string.app_name) + " - Ejecutadas");

        Bundle bundle = getArguments();


        listaItems = root.findViewById(R.id.listItems1);
        txt_nodata = root.findViewById(R.id.txt_nodata1);
        relativeLayout = root.findViewById(R.id.rl_load1);
        input_search = root.findViewById(R.id.input_searchm1);

        showProgress(true);

        //Instanciamos el DBHelper
        adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(getContext());

        //abrir acceso a las preferencias
        final SharedPreferences mPrefs = getActivity().
                getSharedPreferences("SYSTEMAPP_PREFS", Context.MODE_PRIVATE);



        //acceder a items para cambiar cantidad de rutas
        View viewbtnav = inflater.inflate(R.layout.fragment_ejecutadas, null);
        BottomNavigationView bottomNav = viewbtnav.findViewById(R.id.bottom_navigation1);

        //set titles for options on nav


                   //obtener la cantidad de rutas asignadas para verificar que se estén mostrando todas
        switch (VariablesSesion.filtro){
            case "ACUEDUCTO":
                //Se consultan los registros usando filtro para traer solo rutas y no revisiones
                allRutas = adminSQLiteOpenHelper.getData(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "tipo_orden = 'RUTAS' AND categoria_orden = 'RELECTURA' AND nservic='ACUEDUCTO'");
                break;
            case "ENERGIA":
                //Se consultan los registros usando filtro para traer solo rutas y no revisiones
                allRutas = adminSQLiteOpenHelper.getData(DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "tipo_orden = 'RUTAS' AND categoria_orden = 'RELECTURA' AND nservic='ENERGIA'");
                break;
            case "TODO":
                //Se consultan los registros usando filtro para traer solo rutas y no revisiones
                allRutas = adminSQLiteOpenHelper.getData("lecturas",
                        "tipo_orden = 'RUTAS' AND categoria_orden = 'RELECTURA' ");
                break;
        }


        Comparator<DBOrdenLecturas> compareByField = new Comparator<DBOrdenLecturas>() {
            @Override
            public int compare(DBOrdenLecturas o1, DBOrdenLecturas o2) {
                try {
                    Date date1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(o1.getFfinlec());
                    Date date2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(o2.getFfinlec());
                    return date2.compareTo(date1);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return o2.getFinilec().compareTo(o1.getFfinlec());
                }
            }
        };

        Collections.sort(allRutas, compareByField);

        //setear el total de rutas procesadas
        if (mPrefs.getInt("PREF_RUTAS_PROCESADAS", 0) != allRutas.size()){

            SessionPrefs.get(getActivity()).setPrefRutasProcesadas(allRutas.size());
            if (parentFragment!=null){
                parentFragment.setCantidades(mPrefs.getInt("PREF_RUTAS_PENDIENTES", 0)+"",
                        mPrefs.getInt("PREF_RUTAS_REASIGNADAS", 0)+"",
                        allRutas.size()+"");
                Log.d("RProcesadasFragment", "Actualizando cantidades...");
            }
//                listener.onCantChange(allRutas.size()+"",
//                        mPrefs.getInt("PREF_RUTAS_REASIGNADAS", 0)+"",
//                        mPrefs.getInt("PREF_RUTAS_PROCESADAS", 0)+"");

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

        listaItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                posicionRuta = getAbsolutePosition(allRutas, adaptador.getItem(position).getId());
                openSelectedRoute(allRutas, posicionRuta);

            }
        });







        //action to search
        input_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //allRutas = adminSQLiteOpenHelper.getData("lecturas",
                  //      "tipo_orden = 'RUTAS' AND categoria_orden = 'RELECTURA' AND medidor like " +
                    //            "'%"+s.toString().trim().toUpperCase()+"%'");
                adaptador.getFilter().filter(s);
                //adaptador = new Adaptador(getActivity(), (ArrayList) allRutas);
                //listaItems.setAdapter(adaptador);
                //SessionPrefs.get(getActivity()).setPrefRutasProcesadas(allRutas.size());
                //if (parentFragment != null) {
                  //  parentFragment.setCantidades(mPrefs.getInt("PREF_RUTAS_PENDIENTES", 0) + "",
                    //        mPrefs.getInt("PREF_RUTAS_REASIGNADAS", 0) + "",
                      //      allRutas.size() + "");
                    //Log.d("RAsignadasFragment", "Actualizando cantidades...");
                //}
                //System.out.println("Nuevo tamaño"+adaptador.getCount());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return root;
    }





    private void openSelectedRoute(List<DBOrdenLecturas> allRutas, int posicionRuta){

        Fragment_form_lectura fragment_form_lectura = new Fragment_form_lectura();
        DBOrdenLecturas orden = allRutas.get(posicionRuta);
        VariablesSesion.posicionSelectProc = posicionRuta;

        List ordenesSuscriptor = getAllOrdeneswithContrato(allRutas,
                orden.getSuscriptor(), orden.getId());

        Bundle bundle =  new Bundle();
        bundle.putSerializable("orden", (Serializable) ordenesSuscriptor);
        bundle.putSerializable("allRutas", (Serializable) allRutas);
        bundle.putInt("posicion", posicionRuta);
        bundle.putBoolean("edit", true);

        fragment_form_lectura.setArguments(bundle);

        Log.d("RAsignadasFragment", orden.getSuscriptor() + " lecturas "+ordenesSuscriptor.size());


        getFragmentManager().beginTransaction().replace(R.id.fragment_container2,
                        fragment_form_lectura)
                .addToBackStack(null)
                .commit();
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
        for (int i = 0; i < ordenes.size(); i++) {
            if (ordenes.get(i).getId().equals(idreg)) {
                return i;
            }
        }
        return 0;
    }


}