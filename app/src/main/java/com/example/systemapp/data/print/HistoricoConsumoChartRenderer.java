package com.example.systemapp.data.print;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.systemapp.data.Utils;
import com.example.systemapp.data.model.HistoricoConsumoDTO;

import java.util.List;

/**
 * Dibuja el historial de consumo de los últimos meses como un bitmap de barras (no texto ni
 * gráfico de librería), replicando el gráfico de historial de la factura de referencia
 * (fac_muesta.jpeg) — ver PLAN_FACTURACION_EN_SITIO.md. Solo tiene sentido para impresoras de
 * 80mm (72mm de área imprimible real = 576px a 203dpi, mismo ancho que usa el logo en ese
 * formato) — en 58mm no hay espacio y el llamador debe omitirlo directamente.
 *
 * Requiere DBOrdenLecturas.HistoricoConsumos (campo pedido al backend, ver
 * SOLICITUD_HISTORICO_CONSUMOS.md) — si viene null/vacío, build()/buildComando()
 * devuelven null y el llamador simplemente no imprime el gráfico (fail-safe).
 *
 * No reutiliza Fragment_form_lectura.printPhoto() a propósito: ese método aplica
 * BuildConfig.INVERT_LOGO (pensado para compensar el asset del logo de cada flavor), que no debe
 * aplicarse a un bitmap ya dibujado en blanco/negro puro por esta clase.
 */
public class HistoricoConsumoChartRenderer {

    private static final int WIDTH = 576; // 72mm @ 203dpi (80mm de papel), igual que el logo
    private static final int ALTO_BARRAS = 100;
    private static final int MARGEN_SUPERIOR_VALOR = 18;
    private static final int ALTO_ETIQUETA = 22;
    private static final int HEIGHT = MARGEN_SUPERIOR_VALOR + ALTO_BARRAS + ALTO_ETIQUETA;

    public static Bitmap build(List<HistoricoConsumoDTO> historico) {
        if (historico == null || historico.isEmpty()) return null;

        int max = 1;
        for (HistoricoConsumoDTO h : historico) {
            if (h.consumo_m3 != null && h.consumo_m3 > max) max = h.consumo_m3;
        }

        Bitmap bmp = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.WHITE);

        Paint barPaint = new Paint();
        barPaint.setAntiAlias(false);
        barPaint.setColor(Color.BLACK);
        barPaint.setStyle(Paint.Style.FILL);

        Paint outlinePaint = new Paint();
        outlinePaint.setAntiAlias(false);
        outlinePaint.setColor(Color.BLACK);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3);

        Paint labelPaint = new Paint();
        labelPaint.setAntiAlias(false);
        labelPaint.setColor(Color.BLACK);
        labelPaint.setTextSize(18);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        Paint valuePaint = new Paint(labelPaint);
        valuePaint.setTextSize(15);

        int count = historico.size();
        int slot = WIDTH / count;
        int barWidth = (int) (slot * 0.5f);
        int baseline = MARGEN_SUPERIOR_VALOR + ALTO_BARRAS;

        for (int i = 0; i < count; i++) {
            HistoricoConsumoDTO h = historico.get(i);
            int consumo = h.consumo_m3 != null ? h.consumo_m3 : 0;
            int barHeight = Math.max(2, Math.round(((float) consumo / max) * ALTO_BARRAS));

            int centerX = slot * i + slot / 2;
            int left = centerX - barWidth / 2;
            int right = centerX + barWidth / 2;
            int top = baseline - barHeight;

            // El último mes (período actual) se dibuja hueco en vez de sólido, para
            // diferenciarlo del historial sin depender de escala de grises (1-bit ESC/POS).
            boolean esActual = (i == count - 1);
            canvas.drawRect(left, top, right, baseline, esActual ? outlinePaint : barPaint);

            canvas.drawText(String.valueOf(consumo), centerX, Math.max(12, top - 4), valuePaint);
            canvas.drawText(formatearMes(h.periodo), centerX, baseline + ALTO_ETIQUETA - 6, labelPaint);
        }

        canvas.drawLine(0, baseline, WIDTH, baseline, barPaint);

        return bmp;
    }

    // Bitmap ya convertido a comando ESC/POS crudo (mismo formato que Utils.decodeBitmap produce
    // para el logo) — listo para out.write() directo, sin pasar por printPhoto().
    public static byte[] buildComando(List<HistoricoConsumoDTO> historico) {
        Bitmap bmp = build(historico);
        if (bmp == null) return null;
        try {
            return Utils.decodeBitmap(bmp);
        } finally {
            bmp.recycle();
        }
    }

    private static String formatearMes(String periodo) {
        if (periodo == null || periodo.length() != 6) return "";
        String[] meses = {"ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"};
        try {
            int mes = Integer.parseInt(periodo.substring(4, 6));
            if (mes >= 1 && mes <= 12) return meses[mes - 1];
        } catch (NumberFormatException ignored) {
        }
        return periodo;
    }
}
