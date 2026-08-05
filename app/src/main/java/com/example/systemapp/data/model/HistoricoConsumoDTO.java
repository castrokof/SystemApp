package com.example.systemapp.data.model;

/**
 * Un mes del historial de consumo de un cliente — ver
 * SOLICITUD_HISTORICO_CONSUMOS.md (campo nuevo pedido al backend,
 * DBOrdenLecturas.HistoricoConsumos, array de estos en POST /api/medidoresout).
 * Nombres de campo calcados al JSON esperado del backend, para mapeo Gson directo.
 */
public class HistoricoConsumoDTO {
    public String periodo;      // YYYYMM
    public Integer consumo_m3;
}
