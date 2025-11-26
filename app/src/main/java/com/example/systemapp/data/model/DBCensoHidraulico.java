package com.example.systemapp.data.model;

import java.io.Serializable;

/**
 * Modelo de datos para elemento del censo hidráulico
 * (SANITARIO, LAVAMANOS, DUCHA, etc.)
 */
public class DBCensoHidraulico implements Serializable {

    private Integer id;
    private String revision_id;
    private String elemento;
    private Integer cantidad;
    private String estado; // BUENO, MALO
    private String foto_path;

    // Constructor vacío
    public DBCensoHidraulico() {
    }

    // Constructor completo
    public DBCensoHidraulico(String revision_id, String elemento, Integer cantidad, String estado, String foto_path) {
        this.revision_id = revision_id;
        this.elemento = elemento;
        this.cantidad = cantidad;
        this.estado = estado;
        this.foto_path = foto_path;
    }

    // Getters y Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRevision_id() {
        return revision_id;
    }

    public void setRevision_id(String revision_id) {
        this.revision_id = revision_id;
    }

    public String getElemento() {
        return elemento;
    }

    public void setElemento(String elemento) {
        this.elemento = elemento;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFoto_path() {
        return foto_path;
    }

    public void setFoto_path(String foto_path) {
        this.foto_path = foto_path;
    }

    // Métodos auxiliares

    public boolean tieneFoto() {
        return foto_path != null && !foto_path.isEmpty();
    }

    public boolean esBueno() {
        return "BUENO".equals(estado);
    }

    public boolean esMalo() {
        return "MALO".equals(estado);
    }

    @Override
    public String toString() {
        return elemento + " (Cant: " + cantidad + ", Estado: " + estado + ")";
    }
}
