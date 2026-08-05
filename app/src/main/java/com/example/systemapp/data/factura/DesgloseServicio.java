package com.example.systemapp.data.factura;

/**
 * Desglose de valor de un servicio (ACUEDUCTO/ALCANTARILLADO/ASEO) dentro de una factura
 * calculada localmente — mismos campos que el contrato de backend (ver
 * PLAN_FACTURACION_EN_SITIO.md, contrato 1.3/1.4/1.5).
 */
public class DesgloseServicio {
    public double cargoFijo;
    public int basicoM3;
    public double basicoValor;
    public int complementarioM3;
    public double complementarioValor;
    public int suntuarioM3;
    public double suntuarioValor;
    public double subsidio; // positivo = descuento, negativo = sobretasa
    public double total;
}
