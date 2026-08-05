package com.example.systemapp.data.print;

import com.example.systemapp.data.factura.DesgloseServicio;
import com.example.systemapp.data.factura.FacturaCalculada;
import com.example.systemapp.data.model.DBOrdenLecturas;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Arma el contenido de la factura impresa en campo, en texto plano ESC/POS, adaptado a 58mm
 * (32 columnas, compacto) u 80mm de papel — 72mm de area imprimible real, 48 columnas con
 * fuente estandar (12 dots/char a 203dpi), con mas detalle — ver
 * PLAN_FACTURACION_EN_SITIO.md Fase 9. Replica la estructura de una factura de servicios
 * públicos de referencia (ver fac_muesta.jpeg) en la medida de lo que cabe en cada ancho
 * térmico, priorizando siempre los datos financieros y de lectura sobre lo decorativo
 * (se omiten a propósito: gráfico de historial de consumo, tabla de indicadores de calidad,
 * textos informativos largos, información de financiación/cartera).
 *
 * Fecha de vencimiento: agregado 2026-07-29 — POST /api/tarifaVigente ahora expone
 * periodo_lectura.fecha_vencimiento (el ciclo de lectura ACTIVO, distinto de "periodo" que es la
 * resolución tarifaria), cacheado en la tabla local periodo_lectura y propagado a
 * FacturaCalculada.fechaVencimiento vía FacturacionServiceLocal.calcular(). Solo se imprime si
 * factura.fechaVencimiento viene no vacío — puede faltar si el dispositivo no ha vuelto a
 * sincronizar tarifas desde ese cambio, o si el backend no tiene ningún PeriodoLectura ACTIVO.
 *
 * QR/código de barras: no implementado — no se confirmó que los SDKs vendored
 * (SDKLib.jar/posprinterconnectandsendsdk.jar) expongan un comando nativo de impresión de QR;
 * queda documentado como limitación conocida en vez de asumir soporte.
 */
public class FacturaPrintTemplateBuilder {

    public static String build(FacturaCalculada factura, DBOrdenLecturas orden, String numeroFactura,
                                int anchoMM, String nombreEmpresa) {
        boolean detallado = anchoMM >= 80;
        int columnas = detallado ? 48 : 32;
        StringBuilder sb = new StringBuilder();

        sb.append(centrar(safe(nombreEmpresa).isEmpty() ? "FACTURA DE SERVICIOS" : nombreEmpresa.toUpperCase(Locale.getDefault()), columnas)).append("\r\n");
        sb.append(repetir('-', columnas)).append("\r\n");
        sb.append("Cuenta/Suscriptor: ").append(orden.getSuscriptor()).append("\r\n");
        sb.append("Factura N: ").append(numeroFactura).append("\r\n");
        if (safe(orden.getPeriodo()).length() > 0) {
            sb.append("Mes de servicio: ").append(formatearPeriodo(orden.getPeriodo())).append("\r\n");
        }
        sb.append(truncar("Nombre: " + safe(orden.getNombre()) + " " + safe(orden.getApell()), columnas)).append("\r\n");
        if (detallado) {
            sb.append(truncar("Direccion: " + safe(orden.getDireccion()), columnas)).append("\r\n");
        }
        sb.append(repetir('-', columnas)).append("\r\n");
        sb.append("Lectura anterior: ").append(safe(orden.getLA())).append(" m3\r\n");
        sb.append("Lectura actual: ").append(orden.getLectura_actual()).append(" m3\r\n");
        sb.append("Consumo: ").append(factura.consumoM3).append(" m3\r\n");
        if (orden.getPromedio() != null) {
            sb.append("Promedio: ").append(orden.getPromedio()).append(" m3\r\n");
        }
        if (detallado) {
            if (orden.getEstratoId() != null) {
                sb.append("Estrato: ").append(orden.getEstratoId()).append("\r\n");
            }
            if (safe(orden.getRuta()).length() > 0) {
                sb.append("Ruta: ").append(orden.getRuta()).append("\r\n");
            }
        }
        sb.append(repetir('-', columnas)).append("\r\n");

        agregarServicio(sb, "ACUEDUCTO", factura.acueducto, columnas, detallado);
        agregarServicio(sb, "ALCANTARILLADO", factura.alcantarillado, columnas, detallado);
        agregarServicio(sb, "ASEO", factura.aseo, columnas, detallado);

        sb.append(repetir('-', columnas)).append("\r\n");
        if (factura.saldoAnterior > 0) {
            sb.append(linea("Saldo anterior", formatoMoneda(factura.saldoAnterior), columnas)).append("\r\n");
        }
        sb.append(linea("TOTAL A PAGAR", formatoMoneda(factura.totalAPagar), columnas)).append("\r\n");
        sb.append(repetir('-', columnas)).append("\r\n");
        sb.append("Fecha: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date())).append("\r\n");
        if (safe(factura.fechaVencimiento).length() > 0) {
            sb.append("Vence: ").append(formatearFecha(factura.fechaVencimiento)).append("\r\n");
        }
        sb.append("Estado: PENDIENTE DE PAGO\r\n");
        sb.append(centrar("Conserve este recibo", columnas)).append("\r\n");

        return sb.toString();
    }

    private static void agregarServicio(StringBuilder sb, String nombre, DesgloseServicio d, int columnas, boolean detallado) {
        if (d == null) return;
        sb.append(nombre).append(":\r\n");
        sb.append(linea("  Cargo fijo", formatoMoneda(d.cargoFijo), columnas)).append("\r\n");
        if (d.basicoM3 > 0) {
            sb.append(linea("  Basico (" + d.basicoM3 + "m3" + valorUnitario(detallado, d.basicoValor, d.basicoM3) + ")", formatoMoneda(d.basicoValor), columnas)).append("\r\n");
        }
        if (detallado && d.complementarioM3 > 0) {
            sb.append(linea("  Complement.(" + d.complementarioM3 + "m3" + valorUnitario(true, d.complementarioValor, d.complementarioM3) + ")", formatoMoneda(d.complementarioValor), columnas)).append("\r\n");
        }
        if (detallado && d.suntuarioM3 > 0) {
            sb.append(linea("  Suntuario (" + d.suntuarioM3 + "m3" + valorUnitario(true, d.suntuarioValor, d.suntuarioM3) + ")", formatoMoneda(d.suntuarioValor), columnas)).append("\r\n");
        }
        if (d.subsidio != 0) {
            sb.append(linea("  Subsidio", formatoMoneda(-d.subsidio), columnas)).append("\r\n");
        }
        sb.append(linea("  Subtotal", formatoMoneda(d.total), columnas)).append("\r\n");
    }

    // Valor unitario ($/m3) del tramo, derivado del propio desglose (valor / m3) en vez de
    // requerir un campo nuevo en el contrato — solo se muestra en 80mm por espacio.
    private static String valorUnitario(boolean detallado, double valor, int m3) {
        if (!detallado || m3 <= 0) return "";
        return " @" + formatoMoneda(valor / m3);
    }

    // Periodo llega como "YYYYMM" (ver contrato de backend) — se muestra más legible.
    private static String formatearPeriodo(String periodo) {
        if (periodo == null || periodo.length() != 6) return safe(periodo);
        String[] meses = {"ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"};
        try {
            int mes = Integer.parseInt(periodo.substring(4, 6));
            if (mes >= 1 && mes <= 12) {
                return meses[mes - 1] + "/" + periodo.substring(0, 4);
            }
        } catch (NumberFormatException ignored) {
        }
        return periodo;
    }

    // Fechas del backend llegan "yyyy-MM-dd" (ver periodo_lectura.fecha_vencimiento) — se
    // muestran en formato local; si no matchea ese formato, se imprime tal cual llegó.
    private static String formatearFecha(String fechaIso) {
        try {
            Date fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fechaIso);
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fecha);
        } catch (Exception e) {
            return fechaIso;
        }
    }

    private static String formatoMoneda(double valor) {
        return "$" + String.format(Locale.getDefault(), "%,.0f", valor);
    }

    private static String safe(String texto) {
        return texto != null ? texto : "";
    }

    private static String centrar(String texto, int columnas) {
        if (texto.length() >= columnas) return texto.substring(0, columnas);
        int espacios = (columnas - texto.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < espacios; i++) sb.append(' ');
        sb.append(texto);
        return sb.toString();
    }

    private static String repetir(char c, int columnas) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columnas; i++) sb.append(c);
        return sb.toString();
    }

    private static String truncar(String texto, int maxLen) {
        if (texto == null) return "";
        return texto.length() > maxLen ? texto.substring(0, maxLen) : texto;
    }

    // Etiqueta a la izquierda, valor a la derecha, con relleno de espacios entre medio —
    // si no alcanza el ancho, simplemente concatena sin forzar el layout (mejor que cortar $).
    private static String linea(String etiqueta, String valor, int columnas) {
        int espacio = columnas - etiqueta.length() - valor.length();
        StringBuilder sb = new StringBuilder(etiqueta);
        for (int i = 0; i < Math.max(espacio, 1); i++) sb.append(' ');
        sb.append(valor);
        return sb.toString();
    }
}
