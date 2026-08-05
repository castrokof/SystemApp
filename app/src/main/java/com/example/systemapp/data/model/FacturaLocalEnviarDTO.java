package com.example.systemapp.data.model;

/**
 * Request de POST /api/facturas (subir factura generada en sitio) — ver
 * PLAN_FACTURACION_EN_SITIO.md, contrato 1.5. numero_factura ya es el número final (consumido
 * del rango asignado en sync, decisión 5) — el backend solo persiste, no reasigna número.
 * anula_numero_factura es la factura anulada por corrección de lectura (Fase 11), o null.
 */
public class FacturaLocalEnviarDTO {
    public String id_local;
    public String numero_factura;
    public String lectura_id;
    public String suscriptor;
    public String periodo;
    public Integer lectura_anterior;
    public Integer lectura_actual;
    public Integer consumo_m3;
    public Integer estrato_id_usado;
    public Integer tarifa_periodo_id_usado;
    public DesgloseServicioDTO acueducto;
    public DesgloseServicioDTO alcantarillado;
    public DesgloseServicioDTO aseo;
    // Ya descargado con la ruta (medidoresout.SaldoAnterior) e incluido en total_a_pagar antes
    // de imprimir — el backend NO lo vuelve a sumar, solo lo persiste (ver decisión 2 del plan
    // extendida a la deuda previa).
    public Double saldo_anterior;
    public Double total_a_pagar;
    public String fecha_impresion;
    public String clasificacion;
    public String anula_numero_factura;
}
