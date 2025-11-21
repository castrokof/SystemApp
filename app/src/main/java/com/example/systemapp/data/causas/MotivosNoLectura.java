package com.example.systemapp.data.causas;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.systemapp.R;
import com.example.systemapp.data.AdminSQLiteOpenHelper;
import com.example.systemapp.data.model.DBListas;
import com.example.systemapp.data.model.DBdefinicionOrdenes;

import java.io.Serializable;
import java.util.List;

public class MotivosNoLectura extends AppCompatDialogFragment {

    //objeto para transacciones con BD
    private AdminSQLiteOpenHelper adminSQLiteOpenHelper;

    private Spinner list_motivos;

    //Elementos observacion o causa
    private DBListas motivonolect;

    //posición elemento elegido, para seleccionarlo por defecto
    public int posDefault = 0;

    public interface MotivosNoLecturaInterface extends Serializable {
        public void sendDataFromMotivosNoLectura(Object object, int posSelected);
    }

    private MotivosNoLecturaInterface callBackListener;

    /**
     * dialogInterface - instance of MyDialogInterface which will handle
     * callback events
     */
    public static MotivosNoLectura getInstance(MotivosNoLecturaInterface motivosNoLecturaInterface){

        MotivosNoLectura motivosNoLectura = new MotivosNoLectura();

        //set fragment arguments
        Bundle bundle = new Bundle();
        bundle.putSerializable("motivosNoLecturaInterface", motivosNoLecturaInterface);
        motivosNoLectura.setArguments(bundle);

        return motivosNoLectura;

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.motivo_layout, null);

        //con esta variable se manejará el callback para notificar cambios o realizar
        //la comunicación con otro fragment
        // get reference to MyDialogInterface instance from arguments
        Bundle bundle = getArguments();
        callBackListener = (MotivosNoLecturaInterface) bundle.getSerializable("motivosNoLecturaInterface");
        posDefault = bundle.getInt("posSelected");

        //get the spinner from the xml.
        list_motivos = view.findViewById(R.id.list_motivos);

        //Instanciamos el DBHelper
        adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(getContext());

        String condicion_motivos = "marca_id = 'CAUSAS'";

        //consultar las observaciones y las causas desde la base de datos
        //obtendremos un objeto de tipo ElementosListasDB
        final List motivos = adminSQLiteOpenHelper.getData(DBdefinicionOrdenes.LISTAS.TABLE_NAME, condicion_motivos);

        DBListas primerElementoHint = new DBListas("", "",
                "Seleccione Motivo...");
        motivos.add(0, primerElementoHint);
        ArrayAdapter<DBListas> adapter_motivos = getAdapter(motivos);

        //set the spinners adapter to the previously created one.
        list_motivos.setAdapter(adapter_motivos);
        list_motivos.setSelection(posDefault);

        list_motivos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0){
                    posDefault = position;
                    motivonolect = (DBListas) parent.getItemAtPosition(position);
                    Log.d("ObservCausaDialog", motivonolect.getCodigo() +"-"+ motivonolect.getDescripcion());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setView(view)
                .setTitle(getString(R.string.causa_title))
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Boolean canToCloseDialog = true;

                if (motivonolect!=null)
                    callBackListener.sendDataFromMotivosNoLectura(motivonolect, posDefault);
                else{
                    Toast.makeText(getActivity(),
                            getString(R.string.req_motivo),
                            Toast.LENGTH_LONG).show();
                    canToCloseDialog = false;
                }

                if(canToCloseDialog)
                    dialog.dismiss();

            }
        });

        return dialog;
    }

    public ArrayAdapter<DBListas> getAdapter(List datos){
        // Initializing an ArrayAdapter
        final ArrayAdapter<DBListas> spinnerArrayAdapter = new ArrayAdapter<DBListas>(
                getActivity(), android.R.layout.simple_list_item_1, datos){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }

        };

        return spinnerArrayAdapter;
    }


}
