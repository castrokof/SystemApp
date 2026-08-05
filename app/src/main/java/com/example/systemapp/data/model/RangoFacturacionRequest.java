package com.example.systemapp.data.model;

/**
 * Request de POST /api/rangoFacturacion — ver PLAN_FACTURACION_EN_SITIO.md, contrato 1.2.
 */
public class RangoFacturacionRequest {

    public int cantidad_ordenes;

    public RangoFacturacionRequest(int cantidadOrdenes) {
        this.cantidad_ordenes = cantidadOrdenes;
    }
}
