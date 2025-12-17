package com.example.systemapp.ui.config;

import static com.example.systemapp.data.PrinterCommands.ESC_ALIGN_CENTER;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.systemapp.R;
import com.example.systemapp.data.SessionPrefs;
import com.example.systemapp.databinding.FragmentConfigBinding;
import com.example.systemapp.data.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfigFragment extends Fragment {

    private static final String TAG = "ConfigFragment";
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 100;
    private static final int REQUEST_LOCATION_PERMISSIONS = 101;

    private Button btnSearchBluetooth;
    private Button btnSaveConfig;
    private Button btnTestPrint;
    private TextView txtStateView;
    private TextView txtSelectedPrinter;
    private TextView txtSearchStatus;
    private ImageView iconStatus;
    private Spinner spinnerBluetoothDevices;
    private ProgressBar progressSearch;
    private FragmentConfigBinding binding;

    private SharedPreferences mPrefs;

    // Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice selectedDevice;

    // Lista de dispositivos
    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private ArrayAdapter<String> deviceAdapter;
    private List<String> deviceNames = new ArrayList<>();

    // BroadcastReceiver para discovery
    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Dispositivo encontrado
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device != null && !bluetoothDeviceList.contains(device)) {
                    bluetoothDeviceList.add(device);

                    if (hasBluetoothConnectPermission()) {
                        try {
                            String deviceName = device.getName() != null ? device.getName() : "Dispositivo Desconocido";
                            String deviceAddress = device.getAddress();
                            String bondState = device.getBondState() == BluetoothDevice.BOND_BONDED ? " ✅" : "";

                            deviceNames.add(deviceName + bondState + "\n" + deviceAddress);
                            deviceAdapter.notifyDataSetChanged();

                            Log.d(TAG, "Dispositivo encontrado: " + deviceName + " (" + deviceAddress + ")");

                            txtSearchStatus.setText("Encontrados: " + bluetoothDeviceList.size());

                        } catch (SecurityException e) {
                            Log.e(TAG, "Error obteniendo info del dispositivo", e);
                        }
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "Búsqueda iniciada");
                progressSearch.setVisibility(View.VISIBLE);
                btnSearchBluetooth.setEnabled(false);
                txtSearchStatus.setText("Buscando dispositivos...");

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Búsqueda finalizada");
                progressSearch.setVisibility(View.GONE);
                btnSearchBluetooth.setEnabled(true);

                if (bluetoothDeviceList.isEmpty()) {
                    txtSearchStatus.setText("No se encontraron dispositivos");
                    Toast.makeText(getContext(), "No se encontraron dispositivos Bluetooth cercanos", Toast.LENGTH_LONG).show();
                } else {
                    txtSearchStatus.setText("Encontrados: " + bluetoothDeviceList.size() + " dispositivos");
                }
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConfigBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar preferencias
        mPrefs = getContext().getSharedPreferences("SYSTEMAPP_PREFS", Context.MODE_PRIVATE);

        // Configurar ActionBar
        String usuario = mPrefs.getString("PREF_USER_NAME", "");
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setTitle("Configuración - " + usuario);
        }

        // Inicializar vistas
        btnSearchBluetooth = root.findViewById(R.id.btn_search_bluetooth);
        btnSaveConfig = root.findViewById(R.id.btn_mac_save);
        btnTestPrint = root.findViewById(R.id.btn_test_print);
        txtStateView = root.findViewById(R.id.txtState_view);
        txtSelectedPrinter = root.findViewById(R.id.txt_selected_printer);
        txtSearchStatus = root.findViewById(R.id.txt_search_status);
        iconStatus = root.findViewById(R.id.icon_status);
        spinnerBluetoothDevices = root.findViewById(R.id.spinner_bluetooth_devices);
        progressSearch = root.findViewById(R.id.progress_search);

        // Inicializar Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Registrar BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(discoveryReceiver, filter);

        // Verificar si hay impresora guardada
        loadSavedPrinter();

        // Configurar listeners
        setupListeners();

        return root;
    }

    private void setupListeners() {
        // Botón buscar dispositivos
        btnSearchBluetooth.setOnClickListener(v -> {
            if (checkAllPermissions()) {
                startBluetoothDiscovery();
            }
        });

        // Spinner de dispositivos
        spinnerBluetoothDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position <= bluetoothDeviceList.size()) {
                    selectedDevice = bluetoothDeviceList.get(position - 1);

                    if (hasBluetoothConnectPermission()) {
                        try {
                            txtSelectedPrinter.setText("Impresora: " + selectedDevice.getName());

                            // Verificar si está emparejado
                            if (selectedDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                                btnSaveConfig.setEnabled(true);
                                iconStatus.setColorFilter(0xFF0052FF); // Azul
                            } else {
                                // Iniciar emparejamiento
                                pairDevice(selectedDevice);
                            }

                        } catch (SecurityException e) {
                            Log.e(TAG, "Error al seleccionar dispositivo", e);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Botón guardar
        btnSaveConfig.setOnClickListener(v -> savePrinterConfig());

        // Botón probar impresión
        btnTestPrint.setOnClickListener(v -> {
            diagnoseBluetooth();
            testPrintWithChannelSearch();
        });
    }

    private void loadSavedPrinter() {
        String printerName = mPrefs.getString("PREF_PRINTER_NAME", "");
        String printerMac = mPrefs.getString("PREF_PRINTER_ADDRESS", "");

        if (!printerName.isEmpty() && !printerMac.isEmpty()) {
            txtSelectedPrinter.setText("Impresora: " + printerName);
            txtStateView.setText("Configurado: " + printerMac);
            iconStatus.setColorFilter(0xFF34C759); // Verde
            btnTestPrint.setVisibility(View.VISIBLE);
        } else {
            txtSelectedPrinter.setText("Impresora: Ninguna");
            txtStateView.setText("Sin configurar");
            iconStatus.setColorFilter(0xFF8E8E93); // Gris
        }
    }

    private boolean hasBluetoothConnectPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private boolean hasBluetoothScanPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkAllPermissions() {
        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Este dispositivo no tiene Bluetooth", Toast.LENGTH_LONG).show();
            return false;
        }

        List<String> permissionsNeeded = new ArrayList<>();

        // Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasBluetoothConnectPermission()) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (!hasBluetoothScanPermission()) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
        } else {
            // Android 11 y anteriores necesitan ubicación para discovery
            if (!hasLocationPermission()) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            requestPermissions(
                    permissionsNeeded.toArray(new String[0]),
                    REQUEST_BLUETOOTH_PERMISSIONS
            );
            return false;
        }

        // Verificar si Bluetooth está encendido
        if (!hasBluetoothConnectPermission()) {
            return false;
        }

        try {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBT);
                return false;
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error verificando Bluetooth", e);
            return false;
        }

        return true;
    }

    private void startBluetoothDiscovery() {
        if (!hasBluetoothScanPermission() || !hasBluetoothConnectPermission()) {
            Toast.makeText(getContext(), "Faltan permisos de Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Limpiar lista anterior
            bluetoothDeviceList.clear();
            deviceNames.clear();
            deviceNames.add("Selecciona un dispositivo");

            // Configurar adaptador
            deviceAdapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_spinner_item, deviceNames) {
                @Override
                public boolean isEnabled(int position) {
                    return position != 0;
                }
            };
            deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerBluetoothDevices.setAdapter(deviceAdapter);
            spinnerBluetoothDevices.setVisibility(View.VISIBLE);

            // Cancelar discovery anterior si está activo
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            // Iniciar nueva búsqueda
            boolean started = bluetoothAdapter.startDiscovery();

            if (started) {
                Log.d(TAG, "Discovery iniciado exitosamente");
                Toast.makeText(getContext(), "Buscando dispositivos Bluetooth cercanos...", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "No se pudo iniciar discovery");
                Toast.makeText(getContext(), "Error al iniciar búsqueda", Toast.LENGTH_SHORT).show();
            }

        } catch (SecurityException e) {
            Log.e(TAG, "Error de permisos al iniciar discovery", e);
            Toast.makeText(getContext(), "Error de permisos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void pairDevice(BluetoothDevice device) {
        if (!hasBluetoothConnectPermission()) {
            return;
        }

        try {
            Toast.makeText(getContext(), "Emparejando dispositivo...", Toast.LENGTH_SHORT).show();

            boolean paired = device.createBond();

            if (paired) {
                Log.d(TAG, "Emparejamiento iniciado");

                // Esperar a que termine el emparejamiento
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        getActivity().runOnUiThread(() -> {
                            btnSaveConfig.setEnabled(true);
                            Toast.makeText(getContext(), "Dispositivo emparejado", Toast.LENGTH_SHORT).show();
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (SecurityException e) {
            Log.e(TAG, "Error al emparejar", e);
            Toast.makeText(getContext(), "Error al emparejar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void savePrinterConfig() {
        if (selectedDevice == null) {
            Toast.makeText(getContext(), "Selecciona un dispositivo primero", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasBluetoothConnectPermission()) {
            Toast.makeText(getContext(), "Se necesitan permisos de Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String deviceName = selectedDevice.getName();
            String deviceAddress = selectedDevice.getAddress();

            SessionPrefs.get(getActivity()).setPrefPrinterName(deviceName);
            SessionPrefs.get(getActivity()).setPrefPrinterAddress(deviceAddress);

            txtSelectedPrinter.setText("Impresora: " + deviceName);
            txtStateView.setText("Guardado: " + deviceAddress);
            iconStatus.setColorFilter(0xFF34C759); // Verde
            btnTestPrint.setVisibility(View.VISIBLE);

            Toast.makeText(getContext(), "✅ Configuración guardada exitosamente", Toast.LENGTH_SHORT).show();

        } catch (SecurityException e) {
            Log.e(TAG, "Error guardando configuración", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                startBluetoothDiscovery();
            } else {
                Toast.makeText(getContext(), "Se necesitan todos los permisos para buscar dispositivos", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Cancelar discovery si está activo
        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                if (hasBluetoothScanPermission()) {
                    bluetoothAdapter.cancelDiscovery();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelando discovery", e);
        }

        // Desregistrar receiver
        try {
            getActivity().unregisterReceiver(discoveryReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Error desregistrando receiver", e);
        }

        // Cerrar socket
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cerrando socket", e);
        }

        binding = null;
    }

    private void diagnoseBluetooth() {
        if (!hasBluetoothConnectPermission()) {
            Log.e(TAG, "❌ Sin permisos de Bluetooth");
            return;
        }

        try {
            String printerMac = mPrefs.getString("PREF_PRINTER_ADDRESS", "");

            if (printerMac.isEmpty()) {
                Log.e(TAG, "❌ No hay MAC configurada");
                return;
            }

            Log.d(TAG, "🔍 Diagnóstico de Bluetooth:");
            Log.d(TAG, "  - Adaptador Bluetooth: " + (bluetoothAdapter != null ? "✅" : "❌"));

            if (bluetoothAdapter != null) {
                Log.d(TAG, "  - Bluetooth encendido: " + (bluetoothAdapter.isEnabled() ? "✅" : "❌"));
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Log.d(TAG, "  - Discovery activo: " + (bluetoothAdapter.isDiscovering() ? "SÍ" : "NO"));
            }

            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(printerMac);
            Log.d(TAG, "  - Dispositivo encontrado: ✅");

            if (hasBluetoothConnectPermission()) {
                try {
                    Log.d(TAG, "  - Nombre: " + device.getName());
                    Log.d(TAG, "  - MAC: " + device.getAddress());
                    Log.d(TAG, "  - Bond State: " + getBondStateString(device.getBondState()));
                } catch (SecurityException e) {
                    Log.e(TAG, "Error obteniendo datos del dispositivo", e);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error en diagnóstico", e);
        }
    }

    private String getBondStateString(int bondState) {
        switch (bondState) {
            case BluetoothDevice.BOND_BONDED: return "EMPAREJADO ✅";
            case BluetoothDevice.BOND_BONDING: return "EMPAREJANDO...";
            case BluetoothDevice.BOND_NONE: return "NO EMPAREJADO ❌";
            default: return "DESCONOCIDO";
        }
    }

    private void testPrintWithChannelSearch() {
        String printerName = mPrefs.getString("PREF_PRINTER_NAME", "");
        String printerMac = mPrefs.getString("PREF_PRINTER_ADDRESS", "");

        if (printerName.isEmpty() || printerMac.isEmpty()) {
            Toast.makeText(getContext(), "❌ Primero configura una impresora", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasBluetoothConnectPermission()) {
            Toast.makeText(getContext(), "❌ Se necesitan permisos de Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "🔍 Conectando a impresora...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            BluetoothSocket tempSocket = null;
            java.io.OutputStream tempOutputStream = null;
            boolean connected = false;

            try {
                // 🔹 PASO 1: Cancelar discovery
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "Discovery cancelado");
                }
                Thread.sleep(500);

                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(printerMac);
                Log.d(TAG, "📱 Intentando conectar a: " + printerName + " (" + printerMac + ")");

                // 🔹 PASO 2: Verificar si ya hay un canal guardado
                int savedChannel = mPrefs.getInt("PREF_PRINTER_CHANNEL", 0);

                if (savedChannel > 0) {
                    Log.d(TAG, "🎯 Intentando con canal guardado: " + savedChannel);
                    try {
                        java.lang.reflect.Method m = device.getClass().getMethod(
                                "createRfcommSocket",
                                new Class[]{int.class}
                        );
                        tempSocket = (BluetoothSocket) m.invoke(device, savedChannel);
                        tempSocket.connect();
                        connected = true;

                        Log.d(TAG, "✅ Conectado con canal guardado: " + savedChannel);

                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "✅ Conectado (canal " + savedChannel + ")", Toast.LENGTH_SHORT).show()
                        );

                    } catch (Exception e) {
                        Log.w(TAG, "⚠️ Canal guardado falló, buscando nuevo canal...");
                        try {
                            if (tempSocket != null) tempSocket.close();
                        } catch (Exception ex) {}
                    }
                }

                // 🔹 PASO 3: Si no conectó, buscar canal correcto
                if (!connected) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "🔍 Buscando canal correcto...", Toast.LENGTH_SHORT).show()
                    );

                    for (int channel = 1; channel <= 30 && !connected; channel++) {
                        try {
                            Log.d(TAG, "Probando canal " + channel + "...");

                            java.lang.reflect.Method m = device.getClass().getMethod(
                                    "createRfcommSocket",
                                    new Class[]{int.class}
                            );

                            tempSocket = (BluetoothSocket) m.invoke(device, channel);
                            tempSocket.connect();

                            // ✅ Conexión exitosa
                            connected = true;
                            final int successChannel = channel;

                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(),
                                            "✅ Conectado en canal " + successChannel,
                                            Toast.LENGTH_SHORT).show()
                            );

                            Log.d(TAG, "✅✅✅ ÉXITO EN CANAL " + channel + " ✅✅✅");

                            // 🔥 GUARDAR EL CANAL
                            mPrefs.edit().putInt("PREF_PRINTER_CHANNEL", channel).apply();

                            break;

                        } catch (Exception e) {
                            Log.w(TAG, "Canal " + channel + " falló");
                            try {
                                if (tempSocket != null) {
                                    tempSocket.close();
                                    tempSocket = null;
                                }
                            } catch (Exception ex) {}

                            // Actualizar progreso cada 5 canales
                            if (channel % 5 == 0) {
                                final int currentChannel = channel;
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(),
                                                "Probando canal " + currentChannel + "/30...",
                                                Toast.LENGTH_SHORT).show()
                                );
                                Thread.sleep(100);
                            }
                        }
                    }
                }

                // 🔹 PASO 4: Verificar si se conectó
                if (!connected || tempSocket == null) {
                    throw new java.io.IOException("❌ No se pudo conectar después de probar 30 canales");
                }

                // 🔹 PASO 5: Obtener OutputStream
                tempOutputStream = tempSocket.getOutputStream();
                final java.io.OutputStream finalOutputStream = tempOutputStream;
                final BluetoothSocket finalSocket = tempSocket;

                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "🖨️ Imprimiendo...", Toast.LENGTH_SHORT).show()
                );

                // 🔹 PASO 6: Preparar datos de impresión
                int canal = mPrefs.getInt("PREF_PRINTER_CHANNEL", 0);
                String testMsg = "╔════════════════════════════╗\r\n" +
                        "║  PRUEBA DE IMPRESIÓN      ║\r\n" +
                        "╠════════════════════════════╣\r\n" +
                        "  Impresora: " + printerName + "\r\n" +
                        "  MAC: " + printerMac + "\r\n" +
                        "  Canal BT: " + canal + "\r\n" +
                        "  Fecha: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                        .format(new java.util.Date()) + "\r\n" +
                        "╚════════════════════════════╝\r\n" +
                        "\r\n\r\n\r\n";

                // 🔹 PASO 7: Imprimir
                finalOutputStream.write(new byte[]{0x1B, 0x40}); // ESC @ (Inicializar)

                // Logo (opcional)
                try {
                    Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.logoprint);
                    if (bmp != null) {
                        byte[] command = Utils.decodeBitmap(bmp);
                        if (command != null) {
                            finalOutputStream.write(ESC_ALIGN_CENTER);
                            finalOutputStream.write(command);
                        }
                        bmp.recycle();
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Logo omitido: " + e.getMessage());
                }

                // Contenido
                finalOutputStream.write(testMsg.getBytes("GBK"));
                finalOutputStream.flush();

                Log.d(TAG, "📄 Datos enviados a impresora");

                // 🔹 PASO 8: Esperar impresión
                Thread.sleep(2000);

                // 🔹 PASO 9: Cerrar conexión
                finalOutputStream.close();
                finalSocket.close();

                // 🔹 PASO 10: Notificar éxito
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(),
                                "✅ Impresión exitosa\n" +
                                        "Canal guardado: " + mPrefs.getInt("PREF_PRINTER_CHANNEL", 0),
                                Toast.LENGTH_LONG).show()
                );

                Log.d(TAG, "✅ Impresión de prueba completada");

            } catch (SecurityException e) {
                Log.e(TAG, "❌ Error de permisos", e);
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "❌ Error de permisos", Toast.LENGTH_LONG).show()
                );
            } catch (Exception e) {
                Log.e(TAG, "❌ Error en impresión de prueba", e);
                final String errorMsg = e.getMessage();
                getActivity().runOnUiThread(() -> {
                    new android.app.AlertDialog.Builder(getContext())
                            .setTitle("❌ Error de Conexión")
                            .setMessage(
                                    "No se pudo conectar a la impresora\n\n" +
                                            "Error: " + errorMsg + "\n\n" +
                                            "Verifica:\n" +
                                            "✓ Impresora encendida\n" +
                                            "✓ Cerca del dispositivo (< 5m)\n" +
                                            "✓ No conectada a otro equipo\n" +
                                            "✓ Papel suficiente\n" +
                                            "✓ Batería cargada\n\n" +
                                            "Intenta:\n" +
                                            "• Reiniciar la impresora\n" +
                                            "• Desemparejar y volver a buscar\n" +
                                            "• Activar/desactivar Bluetooth"
                            )
                            .setPositiveButton("OK", null)
                            .setNegativeButton("Ver Log", (dialog, which) -> {
                                diagnoseBluetooth();
                                Toast.makeText(getContext(), "Revisa el Logcat", Toast.LENGTH_SHORT).show();
                            })
                            .show();
                });
            } finally {
                // 🔹 PASO 11: Limpiar recursos
                try {
                    if (tempOutputStream != null) tempOutputStream.close();
                    if (tempSocket != null) tempSocket.close();
                } catch (Exception e) {
                    Log.w(TAG, "Error cerrando recursos");
                }
            }
        }).start();
    }
}