package com.example.systemapp.data.model;

/**
 * Factura generada en sitio (offline), persistida en la tabla factura_local.
 * Ver PLAN_FACTURACION_EN_SITIO.md, esquema SQLite y Fase 10.
 */
public class FacturaLocal {

    public static final String ESTADO_PENDIENTE_SYNC = "PENDIENTE_SYNC";
    public static final String ESTADO_SINCRONIZADA = "SINCRONIZADA";
    public static final String ESTADO_ANULADA = "ANULADA";

    private String idLocal;          // UUID, PK interna del dispositivo
    private String numeroFactura;    // ya final: periodo + 5 dígitos, consumido de rango_facturacion
    private String lecturaId;
    private String suscriptor;
    private String periodo;
    private Integer lecturaAnterior;
    private Integer lecturaActual;
    private Integer consumoM3;
    private Integer estratoIdUsado;
    private Integer tarifaPeriodoIdUsado;
    private String desgloseJson;
    private Double totalAPagar;
    private String clasificacion; // NORMAL | ALTO | BAJO
    private String fechaImpresion;
    private String estado;        // PENDIENTE_SYNC | SINCRONIZADA | ANULADA
    private String anulaAIdLocal;
    private Integer facturaIdServidor;
    private boolean sincronizado;

    public FacturaLocal() {
    }

    public FacturaLocal(String idLocal, String numeroFactura, String lecturaId, String suscriptor,
                         String desgloseJson, Double totalAPagar, String clasificacion,
                         String fechaImpresion, String estado) {
        this.idLocal = idLocal;
        this.numeroFactura = numeroFactura;
        this.lecturaId = lecturaId;
        this.suscriptor = suscriptor;
        this.desgloseJson = desgloseJson;
        this.totalAPagar = totalAPagar;
        this.clasificacion = clasificacion;
        this.fechaImpresion = fechaImpresion;
        this.estado = estado;
    }

    public String getIdLocal() { return idLocal; }
    public void setIdLocal(String idLocal) { this.idLocal = idLocal; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public String getLecturaId() { return lecturaId; }
    public void setLecturaId(String lecturaId) { this.lecturaId = lecturaId; }

    public String getSuscriptor() { return suscriptor; }
    public void setSuscriptor(String suscriptor) { this.suscriptor = suscriptor; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public Integer getLecturaAnterior() { return lecturaAnterior; }
    public void setLecturaAnterior(Integer lecturaAnterior) { this.lecturaAnterior = lecturaAnterior; }

    public Integer getLecturaActual() { return lecturaActual; }
    public void setLecturaActual(Integer lecturaActual) { this.lecturaActual = lecturaActual; }

    public Integer getConsumoM3() { return consumoM3; }
    public void setConsumoM3(Integer consumoM3) { this.consumoM3 = consumoM3; }

    public Integer getEstratoIdUsado() { return estratoIdUsado; }
    public void setEstratoIdUsado(Integer estratoIdUsado) { this.estratoIdUsado = estratoIdUsado; }

    public Integer getTarifaPeriodoIdUsado() { return tarifaPeriodoIdUsado; }
    public void setTarifaPeriodoIdUsado(Integer tarifaPeriodoIdUsado) { this.tarifaPeriodoIdUsado = tarifaPeriodoIdUsado; }

    public String getDesgloseJson() { return desgloseJson; }
    public void setDesgloseJson(String desgloseJson) { this.desgloseJson = desgloseJson; }

    public Double getTotalAPagar() { return totalAPagar; }
    public void setTotalAPagar(Double totalAPagar) { this.totalAPagar = totalAPagar; }

    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String clasificacion) { this.clasificacion = clasificacion; }

    public String getFechaImpresion() { return fechaImpresion; }
    public void setFechaImpresion(String fechaImpresion) { this.fechaImpresion = fechaImpresion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getAnulaAIdLocal() { return anulaAIdLocal; }
    public void setAnulaAIdLocal(String anulaAIdLocal) { this.anulaAIdLocal = anulaAIdLocal; }

    public Integer getFacturaIdServidor() { return facturaIdServidor; }
    public void setFacturaIdServidor(Integer facturaIdServidor) { this.facturaIdServidor = facturaIdServidor; }

    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
}
