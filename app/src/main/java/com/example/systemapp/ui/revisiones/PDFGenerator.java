package com.example.systemapp.ui.revisiones;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.example.systemapp.data.AdminSQLiteOpenHelperRevisiones;
import com.example.systemapp.data.SessionPrefs;
import com.example.systemapp.data.model.DBCensoHidraulico;
import com.example.systemapp.data.model.DBOrdenRevision;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Generador de PDF para revisiones con ambas firmas
 */
public class PDFGenerator {

    private Context context;
    private static final int PAGE_WIDTH = 595; // A4 width in points
    private static final int PAGE_HEIGHT = 842; // A4 height in points
    private static final int MARGIN = 40;

    public PDFGenerator(Context context) {
        this.context = context;
    }

    /**
     * Generar PDF completo de la revisión
     */
    public File generarPDF(DBOrdenRevision orden) throws Exception {
        // Crear documento PDF
        PdfDocument document = new PdfDocument();

        // Página 1: Datos principales
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        int yPosition = MARGIN;

        // ========== ENCABEZADO ==========
        paint.setTextSize(20);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("REVISIÓN POR DESVIACIÓN DE CONSUMO", MARGIN, yPosition, paint);
        yPosition += 40;

        // Línea separadora
        paint.setStrokeWidth(2);
        canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, paint);
        yPosition += 30;

        // ========== INFORMACIÓN GENERAL ==========
        paint.setTextSize(12);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        canvas.drawText("MEDIDOR: " + orden.getRef_Medidor(), MARGIN, yPosition, paint);
        yPosition += 20;
        canvas.drawText("SUSCRIPTOR: " + orden.getNombre(), MARGIN, yPosition, paint);
        yPosition += 20;
        canvas.drawText("DIRECCIÓN: " + orden.getDireccion(), MARGIN, yPosition, paint);
        yPosition += 20;
        canvas.drawText("TIPO DESVIACIÓN: " + orden.getTipo_desviacion() + " CONSUMO", MARGIN, yPosition, paint);
        yPosition += 30;

        // ========== TAB 1: LECTURA ==========
        paint.setTextSize(14);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("1. TOMA DE LECTURA", MARGIN, yPosition, paint);
        yPosition += 25;

        paint.setTextSize(12);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Lectura Anterior: " + orden.getLA(), MARGIN + 20, yPosition, paint);
        yPosition += 20;
        canvas.drawText("Lectura Actual: " + orden.getLectura_actual(), MARGIN + 20, yPosition, paint);
        yPosition += 20;
        canvas.drawText("Consumo: " + orden.getConsumo() + " m³", MARGIN + 20, yPosition, paint);
        yPosition += 30;

        // ========== TAB 2: RESIDENTE ==========
        paint.setTextSize(14);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("2. DATOS DEL RESIDENTE", MARGIN, yPosition, paint);
        yPosition += 25;

        paint.setTextSize(12);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Nombre: " + orden.getNombre_residente(), MARGIN + 20, yPosition, paint);
        yPosition += 30;

        // Firma del cliente
        canvas.drawText("Firma del Cliente:", MARGIN + 20, yPosition, paint);
        yPosition += 10;

        if (orden.getFirma_path() != null && !orden.getFirma_path().isEmpty()) {
            try {
                File firmaFile = new File(orden.getFirma_path());
                if (firmaFile.exists()) {
                    Bitmap firmaCliente = BitmapFactory.decodeFile(firmaFile.getAbsolutePath());
                    if (firmaCliente != null) {
                        Bitmap scaledFirma = Bitmap.createScaledBitmap(firmaCliente, 200, 80, true);
                        canvas.drawBitmap(scaledFirma, MARGIN + 20, yPosition, paint);
                        firmaCliente.recycle();
                        scaledFirma.recycle();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        yPosition += 100;

        // ========== TAB 3: ACOMETIDA ==========
        paint.setTextSize(14);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("3. REVISIÓN DE ACOMETIDA", MARGIN, yPosition, paint);
        yPosition += 25;

        paint.setTextSize(12);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Estado Acometida: " + (orden.getEstado_acometida() != null ? orden.getEstado_acometida() : "N/A"), MARGIN + 20, yPosition, paint);
        yPosition += 20;
        canvas.drawText("Estado Sellos: " + (orden.getEstado_sellos() != null ? orden.getEstado_sellos() : "N/A"), MARGIN + 20, yPosition, paint);
        yPosition += 30;

        // ========== TAB 4: CENSOS ==========
        paint.setTextSize(14);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("4. CENSOS", MARGIN, yPosition, paint);
        yPosition += 25;

        paint.setTextSize(12);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Censo Poblacional:", MARGIN + 20, yPosition, paint);
        yPosition += 20;
        canvas.drawText("  - Núcleos familiares: " + orden.getCenso_poblacional_familiar(), MARGIN + 40, yPosition, paint);
        yPosition += 18;
        canvas.drawText("  - Total personas: " + orden.getCenso_poblacional_personas(), MARGIN + 40, yPosition, paint);
        yPosition += 18;
        canvas.drawText("  - Adultos: " + orden.getCenso_poblacional_adultos(), MARGIN + 40, yPosition, paint);
        yPosition += 18;
        canvas.drawText("  - Niños: " + orden.getCenso_poblacional_ninos(), MARGIN + 40, yPosition, paint);
        yPosition += 25;

        // Censo hidráulico
        AdminSQLiteOpenHelperRevisiones dbHelper = new AdminSQLiteOpenHelperRevisiones(context);
        List<DBCensoHidraulico> censos = dbHelper.getCensosByRevisionId(orden.getId());

        canvas.drawText("Censo Hidráulico:", MARGIN + 20, yPosition, paint);
        yPosition += 20;

        if (censos != null && !censos.isEmpty()) {
            for (DBCensoHidraulico censo : censos) {
                canvas.drawText("  - " + censo.getElemento() + ": " + censo.getCantidad() +
                        " (" + censo.getEstado() + ")", MARGIN + 40, yPosition, paint);
                yPosition += 18;
            }
        } else {
            canvas.drawText("  (Sin elementos)", MARGIN + 40, yPosition, paint);
            yPosition += 18;
        }
        yPosition += 20;

        // ========== TAB 5: CLASIFICACIÓN ==========
        paint.setTextSize(14);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("5. CLASIFICACIÓN", MARGIN, yPosition, paint);
        yPosition += 25;

        paint.setTextSize(12);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Causa: " + (orden.getDesc_causa() != null ? orden.getDesc_causa() : "N/A"), MARGIN + 20, yPosition, paint);
        yPosition += 20;

        if (orden.getObservacion_causa() != null && !orden.getObservacion_causa().isEmpty()) {
            canvas.drawText("Observación: " + orden.getObservacion_causa(), MARGIN + 20, yPosition, paint);
            yPosition += 20;
        }
        yPosition += 20;

        // ========== TAB 6: OBSERVACIÓN GENERAL ==========
        paint.setTextSize(14);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("6. OBSERVACIÓN GENERAL", MARGIN, yPosition, paint);
        yPosition += 25;

        paint.setTextSize(12);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        if (orden.getObservacion_general() != null && !orden.getObservacion_general().isEmpty()) {
            canvas.drawText(orden.getObservacion_general(), MARGIN + 20, yPosition, paint);
            yPosition += 20;
        }
        yPosition += 30;

        // ========== FIRMA DEL TÉCNICO ==========
        canvas.drawText("Firma del Técnico:", MARGIN + 20, yPosition, paint);
        yPosition += 10;

        // Obtener firma del técnico desde caché local
        String usuarioTecnico = SessionPrefs.get(context).getUsuario();
        APISync apiSync = new APISync(context);
        File firmaTecnicoFile = apiSync.getFirmaTecnicoLocal(usuarioTecnico);

        if (firmaTecnicoFile != null && firmaTecnicoFile.exists()) {
            try {
                Bitmap firmaTecnico = BitmapFactory.decodeFile(firmaTecnicoFile.getAbsolutePath());
                if (firmaTecnico != null) {
                    Bitmap scaledFirma = Bitmap.createScaledBitmap(firmaTecnico, 200, 80, true);
                    canvas.drawBitmap(scaledFirma, MARGIN + 20, yPosition, paint);
                    firmaTecnico.recycle();
                    scaledFirma.recycle();
                }
            } catch (Exception e) {
                e.printStackTrace();
                canvas.drawText("(Error al cargar firma)", MARGIN + 20, yPosition, paint);
            }
            yPosition += 90;
        } else {
            canvas.drawText("(Sin firma registrada)", MARGIN + 20, yPosition, paint);
            yPosition += 60;
        }

        // ========== PIE DE PÁGINA ==========
        yPosition = PAGE_HEIGHT - 80;
        paint.setTextSize(10);
        canvas.drawText("Fecha: " + orden.getFecha_cierre(), MARGIN, yPosition, paint);
        canvas.drawText("Técnico: " + SessionPrefs.get(context).getUsuario(), MARGIN, yPosition + 15, paint);

        document.finishPage(page);

        // Guardar PDF
        File pdfDir = new File(context.getFilesDir(), "pdfs");
        if (!pdfDir.exists()) {
            pdfDir.mkdirs();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String filename = "revision_" + orden.getRef_Medidor() + "_" + sdf.format(new Date()) + ".pdf";
        File pdfFile = new File(pdfDir, filename);

        FileOutputStream fos = new FileOutputStream(pdfFile);
        document.writeTo(fos);
        document.close();
        fos.close();

        // Actualizar ruta del PDF en la orden
        orden.setRuta_pdf(pdfFile.getAbsolutePath());

        return pdfFile;
    }
}
