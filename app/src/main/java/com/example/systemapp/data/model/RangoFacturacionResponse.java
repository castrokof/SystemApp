package com.example.systemapp.data.model;

/**
 * Respuesta de POST /api/rangoFacturacion — ver PLAN_FACTURACION_EN_SITIO.md, contrato 1.2.
 * El servidor asigna un bloque exclusivo de correlativos para este dispositivo/día; cada
 * llamada asigna un bloque nuevo (huecos posibles si se resincroniza varias veces el mismo
 * día, nunca colisión — no es facturación electrónica DIAN, huecos no son un problema legal).
 */
public class RangoFacturacionResponse {

    public String periodo;
    public Integer secuencia_desde;
    public Integer secuencia_hasta;
    public String asignado_en;
}
