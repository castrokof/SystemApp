package com.example.systemapp.data.factura;

import com.example.systemapp.data.model.DBOrdenLecturas;
import com.example.systemapp.data.model.TarifaVigenteResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Réplica local (offline) del motor de tarifas del backend — ver CONTEXTO_BACKEND.md sección
 * 4.1 (FacturacionService::calcular) y PLAN_FACTURACION_EN_SITIO.md Fase 7. Traslada esa lógica
 * literalmente, no la reinterpreta.
 *
 * Independiente de Validador.java (que usa 35%-165% y sigue gobernando fotos/confirmaciones de
 * la captura de lectura tal como hoy): esConsumoNormal() usa el ±50% real del backend
 * (Cliente::lecturaEsNormal) y solo decide si se ofrece o no facturar en sitio.
 */
public class FacturacionServiceLocal {

    private static final double TOLERANCIA_NORMAL = 0.5; // ±50%, igual que Cliente::lecturaEsNormal()

    public static boolean esConsumoNormal(int consumo, int promedio) {
        if (promedio <= 0) {
            // Sin promedio histórico no se puede evaluar desviación — no hay base para bloquear.
            return true;
        }
        double minimo = promedio * (1 - TOLERANCIA_NORMAL);
        double maximo = promedio * (1 + TOLERANCIA_NORMAL);
        return consumo >= minimo && consumo <= maximo;
    }

    public static FacturaCalculada calcular(DBOrdenLecturas orden, int consumoM3,
                                             List<TarifaVigenteResponse.CargoFijoDTO> cargosFijos,
                                             List<TarifaVigenteResponse.RangoDTO> rangos,
                                             TarifaVigenteResponse.EstratoDTO estrato,
                                             TarifaVigenteResponse.PeriodoLecturaDTO periodoLectura) {
        FacturaCalculada resultado = new FacturaCalculada();
        resultado.consumoM3 = consumoM3;
        if (periodoLectura != null) {
            resultado.fechaExpedicion = periodoLectura.fecha_expedicion;
            resultado.fechaVencimiento = periodoLectura.fecha_vencimiento;
        }

        String servicios = orden.getServiciosCliente() != null ? orden.getServiciosCliente() : "AG";
        boolean tieneAcueducto = servicios.contains("AG");
        boolean tieneAlcantarillado = servicios.contains("AL");
        boolean tieneAseo = Boolean.TRUE.equals(orden.getTieneAseo());

        if (tieneAcueducto) {
            resultado.acueducto = calcularServicio(consumoM3, "ACUEDUCTO", estrato, cargosFijos, rangos, true);
        }
        if (tieneAlcantarillado) {
            resultado.alcantarillado = calcularServicio(consumoM3, "ALCANTARILLADO", estrato, cargosFijos, rangos, true);
        }
        if (tieneAseo) {
            // ASEO no tiene subsidio en el modelo de Estrato (solo existen
            // subsidio_fijo_acueducto/alcantarillado) — nunca se le aplica descuento/sobretasa.
            resultado.aseo = calcularServicio(consumoM3, "ASEO", estrato, cargosFijos, rangos, false);
        }

        double total = 0;
        if (resultado.acueducto != null) total += resultado.acueducto.total;
        if (resultado.alcantarillado != null) total += resultado.alcantarillado.total;
        if (resultado.aseo != null) total += resultado.aseo.total;

        // Saldo anterior descargado con la ruta (medidoresout.SaldoAnterior) — a diferencia del
        // consumo/tarifa del período, este sí se conoce offline porque ya viene calculado por
        // el servidor en la última sincronización.
        resultado.saldoAnterior = orden.getSaldoAnterior() != null ? orden.getSaldoAnterior() : 0;
        resultado.totalAPagar = total + resultado.saldoAnterior;

        return resultado;
    }

    private static DesgloseServicio calcularServicio(int consumoM3, String servicio,
                                                       TarifaVigenteResponse.EstratoDTO estrato,
                                                       List<TarifaVigenteResponse.CargoFijoDTO> cargosFijos,
                                                       List<TarifaVigenteResponse.RangoDTO> rangos,
                                                       boolean aplicaSubsidio) {
        DesgloseServicio d = new DesgloseServicio();
        if (estrato == null) {
            return d; // sin estrato cacheado no se puede tarifar este servicio (fail-safe: queda en 0)
        }

        for (TarifaVigenteResponse.CargoFijoDTO cf : cargosFijos) {
            if (servicio.equals(cf.servicio) && estrato.id.equals(cf.estrato_id)) {
                d.cargoFijo = cf.cargo_fijo != null ? cf.cargo_fijo : 0;
                break;
            }
        }

        // Tramos ordenados por rango_desde — cobro por bloques acumulados reales (intersección
        // del consumo total con cada rango), no "todo al precio del tramo en que cae".
        List<TarifaVigenteResponse.RangoDTO> rangosServicio = new ArrayList<>();
        for (TarifaVigenteResponse.RangoDTO r : rangos) {
            if (servicio.equals(r.servicio) && estrato.id.equals(r.estrato_id)) {
                rangosServicio.add(r);
            }
        }
        rangosServicio.sort((a, b) -> a.rango_desde - b.rango_desde);

        for (TarifaVigenteResponse.RangoDTO r : rangosServicio) {
            int desde = r.rango_desde;
            int hasta = r.rango_hasta != null ? r.rango_hasta : Integer.MAX_VALUE;
            if (consumoM3 < desde) continue;

            int tope = Math.min(consumoM3, hasta);
            int m3EnTramo = tope - desde + 1;
            if (m3EnTramo <= 0) continue;

            double precio = r.precio_m3 != null ? r.precio_m3 : 0;
            double valorTramo = m3EnTramo * precio;

            if ("BASICO".equals(r.tipo)) {
                d.basicoM3 += m3EnTramo;
                d.basicoValor += valorTramo;
            } else if ("COMPLEMENTARIO".equals(r.tipo)) {
                d.complementarioM3 += m3EnTramo;
                d.complementarioValor += valorTramo;
            } else if ("SUNTUARIO".equals(r.tipo)) {
                d.suntuarioM3 += m3EnTramo;
                d.suntuarioValor += valorTramo;
            }
        }

        double subtotal = d.cargoFijo + d.basicoValor + d.complementarioValor + d.suntuarioValor;

        // Subsidio/sobretasa: aplica solo sobre basicoValor, condicionado a que el consumo
        // supere el umbral mínimo del estrato. Prioridad: monto fijo si existe, si no porcentaje.
        if (aplicaSubsidio) {
            double minimo = estrato.consumo_minimo_subsidio != null ? estrato.consumo_minimo_subsidio : 0;
            if (consumoM3 > minimo) {
                double subsidioFijo = "ACUEDUCTO".equals(servicio)
                        ? (estrato.subsidio_fijo_acueducto != null ? estrato.subsidio_fijo_acueducto : 0)
                        : (estrato.subsidio_fijo_alcantarillado != null ? estrato.subsidio_fijo_alcantarillado : 0);
                if (subsidioFijo != 0) {
                    d.subsidio = subsidioFijo;
                } else {
                    double porcentaje = estrato.porcentaje_subsidio != null ? estrato.porcentaje_subsidio : 0;
                    d.subsidio = d.basicoValor * porcentaje / 100.0;
                }
            }
        }

        d.total = subtotal - d.subsidio;
        return d;
    }
}
