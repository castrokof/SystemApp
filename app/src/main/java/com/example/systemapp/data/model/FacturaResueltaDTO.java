package com.example.systemapp.data.model;

/**
 * Respuesta de GET/POST /api/facturasResueltas — ver PLAN_FACTURACION_EN_SITIO.md, contrato 1.4.
 * Facturas ya calculadas/resueltas desde el panel web (FacturacionEspecialController),
 * descargadas para imprimir en campo sin recalcular nada en el dispositivo.
 */
public class FacturaResueltaDTO {
    public Integer factura_id;
    public String numero_factura;
    public String lectura_id;
    public String suscriptor;
    public String periodo;
    public String fecha_expedicion;
    public String fecha_vencimiento;
    public Integer lectura_anterior;
    public Integer lectura_actual;
    public Integer consumo_m3;
    public Integer estrato_snapshot;
    public DesgloseServicioDTO acueducto;
    public DesgloseServicioDTO alcantarillado;
    public DesgloseServicioDTO aseo;
    public Double saldo_anterior;
    public Double total_a_pagar;
    public String estado;
}
