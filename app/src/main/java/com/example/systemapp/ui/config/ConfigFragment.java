package com.example.systemapp.ui.config;

import static com.example.systemapp.data.PrinterCommands.ESC_ALIGN_CENTER;
import static com.example.systemapp.data.PrinterCommands.ESC_ALIGN_LEFT;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.systemapp.R;
import com.example.systemapp.data.SessionPrefs;
import com.example.systemapp.databinding.FragmentConfigBinding;
import com.example.systemapp.data.Utils;


import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ConfigFragment extends Fragment {

    private static final int REQUEST_WRITE_EXTERNAL = 2;

    private Button btn_mac_save;
    private TextView state_printer;
    private EditText printer_name;
    private Spinner list_impresoras;
    private Button btn_export;
    private FragmentConfigBinding binding;
    //abrir acceso a las preferencias
    public static SharedPreferences mPrefs;

    public static String PACKAGE_NAME;

    //impresion
    // android built in classes for bluetooth operations
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;
    boolean printExist = true;

    // needed for communication to bluetooth device / network
    OutputStream outputStream;
    InputStream inputStream;
    Thread thread;
    Resources resources;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        //obtener preferencias
        SharedPreferences mPrefs = this.getContext().getSharedPreferences("SYSTEMAPP_PREFS", Context.MODE_PRIVATE);
        String usuario = mPrefs.getString("PREF_USER_NAME", "");
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setTitle("logueado - " + usuario);
        }

        binding = FragmentConfigBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btn_mac_save = (Button) root.findViewById(R.id.btn_mac_save);
        printer_name = (EditText) root.findViewById(R.id.printer_name);
        state_printer = (TextView) root.findViewById(R.id.txtState_view);
        list_impresoras = (Spinner) root.findViewById(R.id.list_impresoras);


        PACKAGE_NAME = getContext().getPackageName();




        //create a list of items for the spinner.
        String[] items = new String[]{"Selecciona la marca de impresora", "YHD"};

        ArrayAdapter<String> adapter = getAdapter(items);
        list_impresoras.setAdapter(adapter);
        state_printer.setText("Sin conexión");
        String printerName = mPrefs.getString("PREF_PRINTER_NAME", "");
        String printerMac = mPrefs.getString("PREF_PRINTER_ADDRESS", "");

        if (!printerName.equals(""))
            printer_name.setText(printerName);
        if (!printerMac.equals("")) {
            state_printer.setText("Conectado a Mac"+"-"+mPrefs.getString("PREF_PRINTER_ADDRESS", ""));
        }else{
            state_printer.setText("Sin conexión");
        }

        btn_mac_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SessionPrefs.get(getActivity()).setPrefPrinterName(printer_name.getText().toString().trim());

                    FindBluetoothDevice();


                    Toast.makeText(getActivity(), "Datos de configuración actualizados con éxito!", Toast.LENGTH_SHORT).show();
                }catch (Exception ex){
                    if (bluetoothDevice == null) {
                        Toast.makeText(getContext(), "No fue posible conectar con la impresora intente de nuevo", Toast.LENGTH_LONG).show();
                    }

                }




            }
        });


        return root;
    }

    public ArrayAdapter<String> getAdapter(String[] datos) {
        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_list_item_1, datos) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

        };

        return spinnerArrayAdapter;
    }


    // this will find a bluetooth printer device
    public void FindBluetoothDevice() {

        mPrefs = getActivity().getSharedPreferences("SYSTEMAPP_PREFS", Context.MODE_PRIVATE);

        String impresora = mPrefs.getString("PREF_PRINTER_NAME", "");
        SessionPrefs.get(getActivity()).setPrefPrinterAddress("");
       /* if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT)== PackageManager.PERMISSION_DENIED)
        {
            if(Build.VERSION.SDK_INT>=27){
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.BLUETOOTH_CONNECT},100);
                return;
            }
        }*/



        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (bluetoothAdapter == null) {
                Log.d("Fragmet_form_lectura", "No bluetooth adapter available");
                return;
            }

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                getContext().startActivity(enableBT);

            }

            if (bluetoothAdapter.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                getContext().startActivity(enableBT);
            }



            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice pairedDev : pairedDevices) {
                    Log.d("Fragmet_form_lectura", "Nombre bluetooth " + pairedDev.getName()
                            + " MAC " + pairedDev.getAddress() + " device.getType() " + pairedDev.getType());

                    String printerName = mPrefs.getString("PREF_PRINTER_NAME", "Mobile Printer");
                    // RPP300 is the name of the bluetooth printer device
                    // we got this name from the list of paired devices YHD-5808
                    if (pairedDev.getName().equals(impresora)) {
                        bluetoothDevice = pairedDev;
                        SessionPrefs.get(getActivity()).setPrefPrinterAddress(bluetoothDevice.toString());
                        printExist = true;
                        openBT();

                        break;
                    }
                }
                if (bluetoothDevice == null) {
                    Toast.makeText(getContext(), "No fue posible conectar con la impresora intente de nuevo", Toast.LENGTH_LONG).show();
                }
            }



            Log.d("LecturaFragment", "Bluetooth device found.");

        } catch (Exception e) {
            e.printStackTrace();
            printExist = false;
        }
    }

    // tries to open a connection to the bluetooth printer device
    void openBT() throws IOException {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if(Build.VERSION.SDK_INT>=27){
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.BLUETOOTH_CONNECT},100);

            }
        }

        try {

            // Standard SerialPortService ID
            UUID uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");


            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuidSting);
            bluetoothSocket.connect();
            outputStream=bluetoothSocket.getOutputStream();
            inputStream=bluetoothSocket.getInputStream();
            beginListenForData();
            sendData();
            Log.d("LecturaFragment","Bluetooth Opened");

        } catch (Exception e) {
            e.printStackTrace();
            printExist = false;
        }

    }
    public boolean checkConnection(){
        if(bluetoothSocket!=null){
            if(bluetoothSocket.isConnected()){
                Toast.makeText(getContext(),"Estoy aqui esta conectado",Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return false;
    }

    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            thread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = inputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetByte = new byte[bytesAvailable];
                                inputStream.read(packetByte);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetByte[i];
                                    if (b == delimiter) {

                                        byte[] encodedByte = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedByte, 0,
                                                encodedByte.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedByte, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                Log.d("Fragment_form_lectura",data);
                                            }
                                        });
                                        sendData();
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this will send text data to be printed by the bluetooth printer
    void sendData() throws IOException{
        try {

            // the text typed by the user

            String msg = "CONSTANCIA DE LECTURA\r\n" +
                    "Decreto 1942 / 1991\r\n"+
                    "----------------------------\r\n"+
                    "SUSCRIPTOR: \r\n" +
                    "FECHA: 01-01-2022 \r\n" +
                    "NOMBRE: PRUEBAS\r\n" +
                    "RUTA: RUTA010203 \r\n" +
                    "CONS: 1010 \r\n" +
                    "DIRECCIÓN: CRA 262222 22\r\n" +
                    "MEDIDOR: 2675225222\r\n"+
                    "----------------------------\r\n";

            msg += "\n";
            msg += "\n";
            msg += "\n";
            msg += "\n";
            msg += "\n";

            //clear
            outputStream.flush();
            state_printer.setText("Imprimiendo prueba... de Mac " +"-"+mPrefs.getString("PREF_PRINTER_ADDRESS", ""));

            printPhoto1(R.drawable.logoprint);

            outputStream.write(msg.getBytes());

            // tell the user data were sent
            Log.d("Fragment_form_lectura","Data sent.");

            closeBT();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // close the connection to bluetooth printer.
    void closeBT() throws IOException {
        try {
            stopWorker = true;
            outputStream.close();
            inputStream.close();
            bluetoothSocket.close();
            Log.d("Fragment_form_lectura","Bluetooth Closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printPhoto1(int img){

        try {
            Bitmap bmp = BitmapFactory.decodeResource(resources, img);
            if (bmp != null){
                byte[] command = Utils.decodeBitmap(bmp);
                outputStream.write(ESC_ALIGN_CENTER);
                outputStream.write(command);
            }else{
                Log.e("Print photo error", "the file isn´t exists");
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e("Printtools", "the file isn´t exists");
            Toast.makeText(getContext(),"Ahora estoy acá en imagen"+e,Toast.LENGTH_LONG).show();
        }
    }


    }

