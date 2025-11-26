package com.example.systemapp.data.model;

import java.io.Serializable;

/**
 * Modelo de datos para causas de desviación de consumo
 * Catálogo precargado desde la API
 */
public class DBCausaDesviacion implements Serializable {

    private Integer codigo;
    private String tipo; // ALTO, BAJO
    private String descripcion;

    // Constructor vacío
    public DBCausaDesviacion() {
    }

    // Constructor completo
    public DBCausaDesviacion(Integer codigo, String tipo, String descripcion) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    // Getters y Setters

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Métodos auxiliares

    public boolean esAltoConsumo() {
        return "ALTO".equals(tipo);
    }

    public boolean esBajoConsumo() {
        return "BAJO".equals(tipo);
    }

    /**
     * Para usar en Spinners/Adapters
     */
    @Override
    public String toString() {
        return descripcion;
    }

    /**
     * Para comparar objetos
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DBCausaDesviacion that = (DBCausaDesviacion) obj;
        return codigo.equals(that.codigo);
    }

    @Override
    public int hashCode() {
        return codigo != null ? codigo.hashCode() : 0;
    }
}
