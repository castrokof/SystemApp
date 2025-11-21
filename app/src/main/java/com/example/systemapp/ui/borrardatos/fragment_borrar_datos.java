package com.example.systemapp.ui.borrardatos;

import android.app.Activity;
import android.app.AlertDialog;
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

import com.example.systemapp.R;
import com.example.systemapp.SystemAppAPI;
import com.example.systemapp.data.AdminSQLiteOpenHelper;
import com.example.systemapp.data.SessionPrefs;
import com.example.systemapp.data.VariablesSesion;
import com.example.systemapp.data.model.DBdefinicionOrdenes;
import com.example.systemapp.databinding.FragmentBorrarDatosBinding;
import com.example.systemapp.databinding.FragmentHomeBinding;


public class fragment_borrar_datos extends Fragment {



    //objeto para transacciones con BD
    private AdminSQLiteOpenHelper adminSQLiteOpenHelper;
    private FragmentBorrarDatosBinding binding;
    private View view;
    private ProgressBar pgBScirre;
    private LinearLayout back_disabled;
    private Button btn_cierre;
    private TextView txtTotalAsignadas;
    private TextView txtProcesadas;
    private TextView txtPendientes;
    private TextView txtPend_envio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentBorrarDatosBinding.inflate(inflater, container, false);
        View root   = binding.getRoot();




        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setTitle(getString(R.string.app_name));
        }

        pgBScirre = (ProgressBar) root.findViewById(R.id.pgB_cierre);
        btn_cierre = (Button) root.findViewById(R.id.btn_cierre);
        back_disabled = (LinearLayout) root.findViewById(R.id.back_disabled);

        txtTotalAsignadas = (TextView) root.findViewById(R.id.txtTotalAsignadas);
        txtProcesadas = (TextView) root.findViewById(R.id.txtProcesadas);
        txtPendientes = (TextView) root.findViewById(R.id.txtPendientes);
        txtPend_envio = (TextView) root.findViewById(R.id.txtPend_envio);


        //abrir acceso a las preferencias
        final SharedPreferences mPrefs = getActivity().
                getSharedPreferences("SYSTEMAPP_PREFS", Context.MODE_PRIVATE);

        //Instanciamos el DBHelper
        adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(getContext());
        //obtener la cantidad de
        int cantidad = adminSQLiteOpenHelper.getCount("lecturas",
                "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA'" +
                        " AND (Uploadlec IS NULL OR Uploadlec = '') ");

        //obtener la cantidad de
        int cantidadpro = adminSQLiteOpenHelper.getCount("lecturas",
                "Tipo_orden = 'RUTAS' AND Categoria_orden = 'RELECTURA'");

        //obtener la cantidad de
        int cantidadasig = adminSQLiteOpenHelper.getCount("lecturas",
                "Tipo_orden = 'RUTAS' ");

        txtTotalAsignadas.setText(getString(R.string.txt_cierre_asignadas,
                (
                        cantidadasig
                )));
        txtProcesadas.setText(getString(R.string.txt_cierre_realizadas,
                cantidadpro));
        txtPendientes.setText(getString(R.string.txt_cierre_pendientes,
                (
                        mPrefs.getInt("PREF_RUTAS_PENDIENTES", 0) +
                                mPrefs.getInt("PREF_RUTAS_REASIGNADAS", 0)
                )));
        txtPend_envio.setText(getString(R.string.txt_cierre_pend_envio,
                cantidad));

           btn_cierre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                displayPromptForComfirmLogout(getActivity(),
                        mPrefs.getInt("PREF_RUTAS_PENDIENTES", 0),cantidad);

            }
        });

        return  root;
    }

    private void showProgress(boolean show) {
        pgBScirre.setVisibility(show ? View.VISIBLE : View.GONE);
        back_disabled.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void displayPromptForComfirmLogout(final Activity activity, int cantpendingroutes, int cantidad
                                             ){

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final String message = activity.getString(R.string.req_comfirm_cierre_ruta, cantpendingroutes);

        if(cantidad>0){
            Toast.makeText(getActivity(), "Primero envié los ejecutados", Toast.LENGTH_LONG).show();
        //}else {
            builder.setMessage(message)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    cierreRuta(activity);
                                }
                            })
                    .setNegativeButton("Cancelar",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
        }else{
            builder.setMessage(message)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    cierreRuta(activity);
                                }
                            })
                    .setNegativeButton("Cancelar",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });


        }
        try {
            builder.create().show();
        }catch (Exception e){
            System.out.println("No fue posible crear mensaje en pantalla. " + e.getMessage());
        }

    }

    public void cierreRuta(Activity activity){

        final Activity activity1 = activity;
        showProgress(true);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                //habilitar interacción
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


                Toast.makeText(getActivity(), "Datos eliminados", Toast.LENGTH_LONG).show();

        //clarear los contadores
                SessionPrefs.get(activity1).setPrefRutasPendientes(0);
                SessionPrefs.get(activity1).setPrefRutasReasignadas(0);
                SessionPrefs.get(activity1).setPrefRutasProcesadas(0);
                //borar la base de datos
                activity1.deleteDatabase(DBdefinicionOrdenes.DATABASE_NAME);
                //borrar datos de sesionobj
                VariablesSesion.setAllRutasGobal(null);
                VariablesSesion.setRutasGobalAsignadas(null);
                VariablesSesion.posicionSelec = 0;
                //clarear contadores UI
                txtTotalAsignadas.setText(getString(R.string.txt_cierre_asignadas, 0));
                txtProcesadas.setText(getString(R.string.txt_cierre_realizadas, 0));
                txtPendientes.setText(getString(R.string.txt_cierre_pendientes, 0));
                txtPend_envio.setText(getString(R.string.txt_cierre_pend_envio, 0));

                showProgress(false);

            }





}