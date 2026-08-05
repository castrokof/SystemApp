package com.example.systemapp.data.model;

import java.util.List;

/**
 * Respuesta de POST /api/tarifaVigente — ver PLAN_FACTURACION_EN_SITIO.md, contrato 1.3.
 * POJO plano para mapeo directo con Gson (nombres de campo calcados al JSON del backend).
 */
public class TarifaVigenteResponse {

    public TarifaPeriodoDTO periodo;
    // Agregado 2026-07-29 — distinto de "periodo" (TarifaPeriodo = resolución tarifaria): este es
    // el ciclo de facturación en curso (PeriodoLectura), con las fechas a imprimir en la factura
    // generada en sitio. Puede venir null si no hay ningún PeriodoLectura en estado ACTIVO.
    public PeriodoLecturaDTO periodo_lectura;
    public List<CargoFijoDTO> cargos_fijos;
    public List<RangoDTO> rangos;
    public List<EstratoDTO> estratos;
    public ConfigFacturacionSitioDTO config_facturacion_sitio;

    public static class TarifaPeriodoDTO {
        public Integer id;
        public String nombre;
        public String vigente_desde;
        public String vigente_hasta;
    }

    public static class PeriodoLecturaDTO {
        public Integer id;
        public String codigo;
        public String nombre;
        public String fecha_inicio_lectura;
        public String fecha_fin_lectura;
        public String fecha_expedicion;
        public String fecha_vencimiento;
        public String fecha_corte;
    }

    public static class CargoFijoDTO {
        public String servicio; // ACUEDUCTO | ALCANTARILLADO | ASEO
        public Integer estrato_id;
        public Double cargo_fijo;
    }

    public static class RangoDTO {
        public String servicio;
        public Integer estrato_id;
        public String tipo; // BASICO | COMPLEMENTARIO | SUNTUARIO
        public Integer rango_desde;
        public Integer rango_hasta; // null = ilimitado
        public Double precio_m3;
    }

    public static class EstratoDTO {
        public Integer id;
        public Integer numero;
        public String nombre;
        public Double porcentaje_subsidio;
        public Double subsidio_fijo_acueducto;
        public Double subsidio_fijo_alcantarillado;
        public Double consumo_minimo_subsidio;
        public Boolean activo;
    }

    public static class ConfigFacturacionSitioDTO {
        public Boolean habilitar_normal;
        public Boolean habilitar_alto;
        public Boolean habilitar_bajo;
        public Boolean habilitar_negativo;
    }
}
