package com.example.systemapp.data.print;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

/**
 * Conexión RFCOMM por búsqueda de canal, extraída de la lógica duplicada que
 * existía en Fragment_form_lectura.printWithChannelSearch() y
 * ConfigFragment.testPrintWithChannelSearch() — mismo comportamiento exacto
 * (intenta el canal guardado en PREF_PRINTER_CHANNEL, si falla prueba 1-30),
 * solo que ahora vive en un único lugar.
 */
public class BluetoothPrinterClient {

    private static final String TAG = "BluetoothPrinterClient";
    private static final String PREF_PRINTER_CHANNEL = "PREF_PRINTER_CHANNEL";

    public interface ChannelProgressListener {
        void onProbingChannel(int channel);
    }

    public static class Connection {
        public final BluetoothSocket socket;
        public final OutputStream outputStream;
        public final int channelUsed;

        public Connection(BluetoothSocket socket, OutputStream outputStream, int channelUsed) {
            this.socket = socket;
            this.outputStream = outputStream;
            this.channelUsed = channelUsed;
        }
    }

    /**
     * Bloqueante — debe llamarse desde un hilo secundario, igual que antes.
     * Lanza IOException si no logra conectar tras probar los 30 canales.
     */
    public static Connection connect(BluetoothAdapter bluetoothAdapter, SharedPreferences prefs, String printerMac,
                                      ChannelProgressListener progressListener) throws IOException, InterruptedException {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "Discovery cancelado");
        }
        Thread.sleep(500);

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(printerMac);
        Log.d(TAG, "Conectando a MAC " + printerMac);

        int channelUsed = prefs.getInt(PREF_PRINTER_CHANNEL, 0);
        BluetoothSocket socket = null;
        boolean connected = false;

        if (channelUsed > 0) {
            Log.d(TAG, "Intentando con canal guardado: " + channelUsed);
            try {
                socket = openRfcommSocket(device, channelUsed);
                socket.connect();
                connected = true;
                Log.d(TAG, "Conectado con canal guardado: " + channelUsed);
            } catch (Exception e) {
                Log.w(TAG, "Canal guardado falló, buscando nuevo canal...");
                closeQuietly(socket);
                socket = null;
            }
        }

        if (!connected) {
            for (int channel = 1; channel <= 30 && !connected; channel++) {
                if (progressListener != null) {
                    progressListener.onProbingChannel(channel);
                }
                try {
                    Log.d(TAG, "Probando canal " + channel + "...");
                    socket = openRfcommSocket(device, channel);
                    socket.connect();
                    connected = true;
                    channelUsed = channel;
                    Log.d(TAG, "Éxito en canal " + channel);
                    prefs.edit().putInt(PREF_PRINTER_CHANNEL, channel).apply();
                } catch (Exception e) {
                    Log.w(TAG, "Canal " + channel + " falló");
                    closeQuietly(socket);
                    socket = null;
                }
            }
        }

        if (!connected || socket == null) {
            throw new IOException("No se pudo conectar después de probar 30 canales");
        }

        return new Connection(socket, socket.getOutputStream(), channelUsed);
    }

    public static void closeQuietly(Connection connection) {
        if (connection == null) return;
        try {
            if (connection.outputStream != null) connection.outputStream.close();
        } catch (Exception e) {
            Log.w(TAG, "Error cerrando outputStream");
        }
        closeQuietly(connection.socket);
    }

    private static void closeQuietly(BluetoothSocket socket) {
        try {
            if (socket != null) socket.close();
        } catch (Exception e) {
            Log.w(TAG, "Error cerrando socket");
        }
    }

    private static BluetoothSocket openRfcommSocket(BluetoothDevice device, int channel) throws Exception {
        Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
        return (BluetoothSocket) m.invoke(device, channel);
    }
}
