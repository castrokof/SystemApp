package com.example.systemapp.data.model;

/**
 * Factura ya calculada/resuelta desde el panel web (FacturacionEspecialController), descargada
 * en sync y lista para imprimir en campo sin recalcular nada en el dispositivo.
 * Ver PLAN_FACTURACION_EN_SITIO.md, contrato 1.4 y Fase 12.
 */
public class FacturaResueltaServidor {

    private Integer facturaId;
    private String numeroFactura;
    private String lecturaId;
    private String suscriptor;
    private String desgloseJson;
    private Double totalAPagar;
    private String estado;
    private boolean impresa;

    public Integer getFacturaId() { return facturaId; }
    public void setFacturaId(Integer facturaId) { this.facturaId = facturaId; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public String getLecturaId() { return lecturaId; }
    public void setLecturaId(String lecturaId) { this.lecturaId = lecturaId; }

    public String getSuscriptor() { return suscriptor; }
    public void setSuscriptor(String suscriptor) { this.suscriptor = suscriptor; }

    public String getDesgloseJson() { return desgloseJson; }
    public void setDesgloseJson(String desgloseJson) { this.desgloseJson = desgloseJson; }

    public Double getTotalAPagar() { return totalAPagar; }
    public void setTotalAPagar(Double totalAPagar) { this.totalAPagar = totalAPagar; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isImpresa() { return impresa; }
    public void setImpresa(boolean impresa) { this.impresa = impresa; }
}
