package com.example.systemapp.data.causas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.systemapp.R;
import com.example.systemapp.data.AdminSQLiteOpenHelper;
import com.example.systemapp.data.model.DBListas;

import java.io.Serializable;
import java.util.List;

public class CustomDialog extends AppCompatDialogFragment {

    //objeto para transacciones con BD
    private AdminSQLiteOpenHelper adminSQLiteOpenHelper;

    private Spinner list;
    private EditText txt_comentario;

    //Elementos observacion
    private DBListas observacion;

    //posición elemento elegido, para seleccionarlo por defecto
    public int posDefault = 0;
    public String comentario = "";
    public String titulo = "";
    public String opcion_selec = "";

    //Elementos para elegir
    public List<String> elementos;

    public interface CustomDialogInterface extends Serializable {
        public void sendDataFromCustomDialog(Object object, int posSelected, String comentario);
    }

    private CustomDialogInterface callBackListener;

    /**
     * dialogInterface - instance of MyDialogInterface which will handle
     * callback events
     */
    public static CustomDialog getInstance(CustomDialogInterface customDialogInterface){

        CustomDialog customDialog = new CustomDialog();

        //set fragment arguments
        Bundle bundle = new Bundle();
        bundle.putSerializable("customDialogInterface", customDialogInterface);
        customDialog.setArguments(bundle);

        return customDialog;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.comentario_layout, null);

        //get the spinner from the xml.
        list = view.findViewById(R.id.listcomentario);

        //con esta variable se manejará el callback para notificar cambios o realizar
        //la comunicación con otro fragment
        // get reference to MyDialogInterface instance from arguments
        Bundle bundle = getArguments();
        callBackListener = (CustomDialogInterface) bundle.getSerializable("customDialogInterface");
        elementos = bundle.getStringArrayList("list");
        titulo = ((bundle.getString("titulo")) != null ? bundle.getString("titulo") : "");

        if (elementos==null){

            txt_comentario = view.findViewById(R.id.txt_comentario);
            list.setVisibility(View.GONE);
            txt_comentario.setVisibility(View.VISIBLE);

            builder.setView(view)
                    .setTitle((titulo.equals("") ? "Diligencia el campo" : titulo))
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean canToCloseDialog = true;


                    comentario = txt_comentario.getText().toString();

                    if((comentario.equals("")||comentario==null)) {
                        Toast.makeText(getActivity(),
                                getString(R.string.req_campo),
                                Toast.LENGTH_LONG).show();
                        canToCloseDialog = false;
                    }else {//sino se envía solo el comentario para ser almacenada en la lectura
                        callBackListener.sendDataFromCustomDialog(observacion, posDefault, comentario);
                    }

                    if (canToCloseDialog)
                        dialog.dismiss();

                }
            });
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);

            return dialog;

        }else {



            DBListas primerElementoHint = new DBListas("", "",
                    "Selecciona Observación...");

            //inicializar los adapters para poner la informacion de observaciones y causas
            //observaciones.add(0, primerElementoHint);
            ArrayAdapter<String> adapter_obs = getAdapter(elementos);

            //set the spinners adapter to the previously created one.
            list.setAdapter(adapter_obs);
            //list_observaciones.setSelection(posDefault);

            list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedItemText = (String) parent.getItemAtPosition(position);
                    // If user change the default selection
                    // First item is disable and it is used for hint
                    if (position > 0) {
                        posDefault = position;
                        opcion_selec = (String) parent.getItemAtPosition(position);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            builder.setView(view)
                    .setTitle((titulo.equals("") ? "Elige una opción" : titulo))
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });



            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean canToCloseDialog = true;

                    if (!opcion_selec.equals("")) {
                        callBackListener.sendDataFromCustomDialog(observacion, posDefault, opcion_selec);
                    } else {
                        Toast.makeText(getActivity(),
                                getString(R.string.req_opcion),
                                Toast.LENGTH_LONG).show();
                        canToCloseDialog = false;
                    }


                    if (canToCloseDialog)
                        dialog.dismiss();

                }
            });
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);

            return dialog;
        }

    }

    public ArrayAdapter<String> getAdapter(List datos){
        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
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
