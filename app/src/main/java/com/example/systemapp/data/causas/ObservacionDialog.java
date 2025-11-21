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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.systemapp.R;
import com.example.systemapp.data.AdminSQLiteOpenHelper;
import com.example.systemapp.data.model.DBListas;
import com.example.systemapp.data.model.DBdefinicionOrdenes;

import java.io.Serializable;
import java.util.List;

public class ObservacionDialog extends AppCompatDialogFragment {

    //objeto para transacciones con BD
    private AdminSQLiteOpenHelper adminSQLiteOpenHelper;

    private Spinner list_observaciones;
    private TextView txt_comentario;

    //Elementos observacion
    private DBListas observacion;

    //posición elemento elegido, para seleccionarlo por defecto
    public int posDefault = 0;
    public String comentario = "";
    public boolean bloq = false;

    public interface ObservacionDialogInterface extends Serializable {
        public void sendDataFromObservacionDialog(Object object, int posSelected, String comentario);
    }

    private ObservacionDialogInterface callBackListener;

    /**
     * dialogInterface - instance of MyDialogInterface which will handle
     * callback events
     */
    public static ObservacionDialog getInstance(ObservacionDialogInterface observacionDialogInterface){

        ObservacionDialog observacionDialog = new ObservacionDialog();

        //set fragment arguments
        Bundle bundle = new Bundle();
        bundle.putSerializable("observacionDialogInterface", observacionDialogInterface);
        observacionDialog.setArguments(bundle);

        return observacionDialog;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.observacion_layout, null);

        //con esta variable se manejará el callback para notificar cambios o realizar
        //la comunicación con otro fragment
        // get reference to MyDialogInterface instance from arguments
        Bundle bundle = getArguments();
        callBackListener = (ObservacionDialogInterface) bundle.getSerializable("observacionDialogInterface");
        posDefault = bundle.getInt("posSelected");
        comentario = bundle.getString("Comentario");
        bloq = bundle.getBoolean("bloq_comentario");
        //obtener lista de id de observaciones asociadas si antes fue seleccionada una causa
        final String idObserv = bundle.getString("idObserv");

        //get the spinner from the xml.
        list_observaciones = view.findViewById(R.id.list_observaciones);
        txt_comentario = view.findViewById(R.id.txt_comentario);

        //set comentario si ya se había escrito algo
        txt_comentario.setText(comentario);

        txt_comentario.setEnabled(!bloq);

        //Instanciamos el DBHelper
        adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(getContext());

        String condicion_observ = "marca_id = 'OBSERVACIONES'";
        if(idObserv!=null){
            condicion_observ += " AND value IN ("+idObserv+")";
        }

        //consultar las observaciones y las causas desde la base de datos
        //obtendremos un objeto de tipo ElementosListasDB
        final List observaciones = adminSQLiteOpenHelper.getData(DBdefinicionOrdenes.LISTAS.TABLE_NAME, condicion_observ);

        DBListas primerElementoHint = new DBListas("", "",
                "Selecciona Observación...");

        //inicializar los adapters para poner la informacion de observaciones y causas
        observaciones.add(0, primerElementoHint);
        ArrayAdapter<DBListas> adapter_obs = getAdapter(observaciones);

        //set the spinners adapter to the previously created one.
        list_observaciones.setAdapter(adapter_obs);
        list_observaciones.setSelection(posDefault);

        list_observaciones.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    posDefault = position;
                    observacion = (DBListas) parent.getItemAtPosition(position);
                    Log.d("ObservacionDialog", observacion.getCodigo() + "-" + observacion.getDescripcion());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Toast.makeText(getActivity(), "La observacion es obligatoria",
                 //       Toast.LENGTH_LONG).show();

            }
        });

        builder.setView(view)
                .setTitle(getString(R.string.observacion_title))
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        //obtener el id de causa
        final String idCausal = bundle.getString("idCausal");


        //si no viene seteado idObserv, quiere decir que no es obligatorio tomar observación o comentario
        //específicamente esto se está mannejando solo para cuando se llama este dialog por haber elegido la causa
        //37, en la que se obliga a tomar unas observaciones específicas
        if (idObserv==null&&idCausal==null){//!idCausal.equals("13")
            builder.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
        }

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Boolean canToCloseDialog = true;

                comentario = txt_comentario.getText().toString();

                if (observacion!=null&&idCausal==null) {
                    callBackListener.sendDataFromObservacionDialog(observacion, posDefault, comentario);
                }else{
                   if (idCausal!=null){
                        if (idCausal.equals("10"))
                            if((comentario.equals("")||comentario==null)) {
                                Toast.makeText(getActivity(),
                                        getString(R.string.req_coment),
                                        Toast.LENGTH_LONG).show();
                                canToCloseDialog = false;
                            }else {//sino se envía solo el comentario para ser almacenada en la lectura
                                callBackListener.sendDataFromObservacionDialog(observacion, posDefault, comentario);
                            }
                    }else if (observacion==null){
                       Toast.makeText(getActivity(), "La observacion es obligatoria",
                               Toast.LENGTH_LONG).show();
                       canToCloseDialog = false;


                    }else{
                       //sino se envía solo el comentario para ser almacenada en la lectura
                       callBackListener.sendDataFromObservacionDialog(observacion, posDefault, comentario);
                   }
                }

                if(canToCloseDialog)
                    dialog.dismiss();

            }
        });
        dialog.setCanceledOnTouchOutside(false);

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
