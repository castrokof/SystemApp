package com.example.systemapp.data.factura;

/**
 * Resultado del cálculo local de tarifa para una lectura (FacturacionServiceLocal.calcular()).
 * saldo_anterior siempre queda en 0 — requiere historial de facturas del backend, no disponible
 * offline (limitación conocida, documentada en PLAN_FACTURACION_EN_SITIO.md Fase 7).
 */
public class FacturaCalculada {
    public int consumoM3;
    public DesgloseServicio acueducto;
    public DesgloseServicio alcantarillado;
    public DesgloseServicio aseo;
    // Descargado con la ruta (medidoresout.SaldoAnterior) — deuda real de períodos anteriores,
    // ya incluida en totalAPagar. Se muestra/imprime por separado para que el cliente entienda
    // el desglose (cargo del período vs. deuda vieja), no solo el total combinado.
    public double saldoAnterior;
    public double totalAPagar;
    // Del periodo_lectura activo descargado en tarifaVigente (agregado 2026-07-29) — null si no
    // hay ningún PeriodoLectura ACTIVO cacheado (ver FacturacionServiceLocal.calcular()).
    public String fechaExpedicion;
    public String fechaVencimiento;
}
