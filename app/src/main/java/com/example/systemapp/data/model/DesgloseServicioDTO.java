package com.example.systemapp.data.model;

/**
 * Desglose de un servicio en el formato JSON del contrato de backend (snake_case) — ver
 * PLAN_FACTURACION_EN_SITIO.md, contratos 1.3/1.4/1.5. Distinto de
 * com.example.systemapp.data.factura.DesgloseServicio (camelCase, objeto de negocio interno);
 * este es solo para serializar/deserializar el JSON que viaja por la red.
 */
public class DesgloseServicioDTO {
    public Double cargo_fijo;
    public Integer basico_m3;
    public Double basico_valor;
    public Integer complementario_m3;
    public Double complementario_valor;
    public Integer suntuario_m3;
    public Double suntuario_valor;
    public Double subsidio;
    public Double total;

    public static DesgloseServicioDTO desde(com.example.systemapp.data.factura.DesgloseServicio d) {
        if (d == null) return null;
        DesgloseServicioDTO dto = new DesgloseServicioDTO();
        dto.cargo_fijo = d.cargoFijo;
        dto.basico_m3 = d.basicoM3;
        dto.basico_valor = d.basicoValor;
        dto.complementario_m3 = d.complementarioM3;
        dto.complementario_valor = d.complementarioValor;
        dto.suntuario_m3 = d.suntuarioM3;
        dto.suntuario_valor = d.suntuarioValor;
        dto.subsidio = d.subsidio;
        dto.total = d.total;
        return dto;
    }
}
