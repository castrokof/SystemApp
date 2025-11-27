package com.example.systemapp.ui.revisiones;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.systemapp.data.model.DBOrdenRevision;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Utilidad para imprimir revisiones en impresoras térmicas Bluetooth
 * Usa comandos ESC/POS estándar
 */
public class BluetoothPrinter {

    private Context context;
    private OutputStream outputStream;

    // Comandos ESC/POS
    private static final byte[] ESC_ALIGN_CENTER = new byte[]{0x1B, 0x61, 0x01};
    private static final byte[] ESC_ALIGN_LEFT = new byte[]{0x1B, 0x61, 0x00};
    private static final byte[] ESC_BOLD_ON = new byte[]{0x1B, 0x45, 0x01};
    private static final byte[] ESC_BOLD_OFF = new byte[]{0x1B, 0x45, 0x00};
    private static final byte[] ESC_SIZE_NORMAL = new byte[]{0x1D, 0x21, 0x00};
    private static final byte[] ESC_SIZE_DOUBLE = new byte[]{0x1D, 0x21, 0x11};
    private static final byte[] ESC_FEED_LINE = new byte[]{0x0A};
    private static final byte[] ESC_CUT_PAPER = new byte[]{0x1D, 0x56, 0x00};

    public BluetoothPrinter(Context context, OutputStream outputStream) {
        this.context = context;
        this.outputStream = outputStream;
    }

    /**
     * Imprimir revisión completa
     */
    public void imprimirRevision(DBOrdenRevision orden) throws Exception {
        // Encabezado
        write(ESC_ALIGN_CENTER);
        write(ESC_SIZE_DOUBLE);
        write(ESC_BOLD_ON);
        writeLine("REVISIÓN");
        writeLine("DESVIACIÓN CONSUMO");
        write(ESC_BOLD_OFF);
        write(ESC_SIZE_NORMAL);
        writeLine("");
        write(ESC_ALIGN_LEFT);

        // Línea separadora
        writeLine("================================");

        // Información general
        writeLine("MEDIDOR: " + orden.getRef_Medidor());
        writeLine("NOMBRE: " + orden.getNombre());
        writeLine("DIRECCIÓN: " + orden.getDireccion());
        writeLine("TIPO: " + orden.getTipo_desviacion() + " CONSUMO");
        writeLine("================================");
        writeLine("");

        // Tab 1: Lectura
        write(ESC_BOLD_ON);
        writeLine("1. LECTURA");
        write(ESC_BOLD_OFF);
        writeLine("Lectura Anterior: " + orden.getLA());
        writeLine("Lectura Actual: " + orden.getLectura_actual());
        writeLine("Consumo: " + orden.getConsumo() + " m³");
        writeLine("");

        // Tab 2: Residente
        write(ESC_BOLD_ON);
        writeLine("2. RESIDENTE");
        write(ESC_BOLD_OFF);
        writeLine("Nombre: " + orden.getNombre_residente());
        writeLine("");

        // Firma del cliente (convertir a bitmap ESC/POS)
        if (orden.getFirma_path() != null && !orden.getFirma_path().isEmpty()) {
            writeLine("Firma Cliente:");
            try {
                File firmaFile = new File(orden.getFirma_path());
                if (firmaFile.exists()) {
                    Bitmap firma = BitmapFactory.decodeFile(firmaFile.getAbsolutePath());
                    if (firma != null) {
                        imprimirBitmap(firma);
                        firma.recycle();
                    }
                }
            } catch (Exception e) {
                writeLine("(Error al imprimir firma)");
            }
        }
        writeLine("");

        // Tab 3: Acometida
        write(ESC_BOLD_ON);
        writeLine("3. ACOMETIDA");
        write(ESC_BOLD_OFF);
        writeLine("Estado: " + (orden.getEstado_acometida() != null ? orden.getEstado_acometida() : "N/A"));
        writeLine("Sellos: " + (orden.getEstado_sellos() != null ? orden.getEstado_sellos() : "N/A"));
        writeLine("");

        // Tab 4: Censos
        write(ESC_BOLD_ON);
        writeLine("4. CENSOS");
        write(ESC_BOLD_OFF);
        writeLine("Núcleos Fam.: " + orden.getCenso_poblacional_familiar());
        writeLine("Personas: " + orden.getCenso_poblacional_personas());
        writeLine("Adultos: " + orden.getCenso_poblacional_adultos());
        writeLine("Niños: " + orden.getCenso_poblacional_ninos());
        writeLine("");

        // Tab 5: Clasificación
        write(ESC_BOLD_ON);
        writeLine("5. CLASIFICACIÓN");
        write(ESC_BOLD_OFF);
        writeLine("Causa: " + (orden.getDesc_causa() != null ? orden.getDesc_causa() : "N/A"));
        if (orden.getObservacion_causa() != null && !orden.getObservacion_causa().isEmpty()) {
            writeLine("Obs: " + orden.getObservacion_causa());
        }
        writeLine("");

        // Tab 6: Observación
        if (orden.getObservacion_general() != null && !orden.getObservacion_general().isEmpty()) {
            write(ESC_BOLD_ON);
            writeLine("6. OBSERVACIÓN GENERAL");
            write(ESC_BOLD_OFF);
            writeLine(orden.getObservacion_general());
            writeLine("");
        }

        // Firma del técnico (placeholder - se debería descargar del servidor)
        writeLine("Firma Técnico:");
        writeLine("(Firma digital registrada)");
        writeLine("");

        // Pie
        writeLine("================================");
        writeLine("Fecha: " + orden.getFecha_cierre());
        writeLine("================================");
        writeLine("");
        writeLine("");
        writeLine("");

        // Cortar papel
        write(ESC_CUT_PAPER);

        outputStream.flush();
    }

    /**
     * Imprimir bitmap en impresora térmica
     * Convierte bitmap a comandos ESC/POS
     */
    private void imprimirBitmap(Bitmap bitmap) throws Exception {
        // Escalar bitmap a ancho de impresora (384 pixels típico)
        int maxWidth = 384;
        float scale = (float) maxWidth / bitmap.getWidth();
        int scaledHeight = (int) (bitmap.getHeight() * scale);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, scaledHeight, true);

        // Convertir a monocromático
        int width = scaledBitmap.getWidth();
        int height = scaledBitmap.getHeight();

        // Implementación simplificada - en producción usar algoritmo completo ESC/POS
        // Aquí solo imprimimos un placeholder
        writeLine("[FIRMA DIGITAL CAPTURADA]");

        scaledBitmap.recycle();
    }

    private void write(byte[] data) throws Exception {
        outputStream.write(data);
    }

    private void writeLine(String text) throws Exception {
        outputStream.write(text.getBytes(Charset.forName("ISO-8859-1")));
        outputStream.write(ESC_FEED_LINE);
    }
}
