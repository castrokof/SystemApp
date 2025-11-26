package com.example.systemapp.data.model;

import java.io.Serializable;

/**
 * Modelo de datos para fotos adicionales de la revisión
 * Permite tomar fotos en cualquier tab
 */
public class DBFotoRevision implements Serializable {

    private Integer id;
    private String revision_id;
    private Integer tab_numero; // 1-6
    private String descripcion;
    private String ruta_foto;
    private String fecha_captura;

    // Constructor vacío
    public DBFotoRevision() {
    }

    // Constructor completo
    public DBFotoRevision(String revision_id, Integer tab_numero, String descripcion, String ruta_foto, String fecha_captura) {
        this.revision_id = revision_id;
        this.tab_numero = tab_numero;
        this.descripcion = descripcion;
        this.ruta_foto = ruta_foto;
        this.fecha_captura = fecha_captura;
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

    public Integer getTab_numero() {
        return tab_numero;
    }

    public void setTab_numero(Integer tab_numero) {
        this.tab_numero = tab_numero;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRuta_foto() {
        return ruta_foto;
    }

    public void setRuta_foto(String ruta_foto) {
        this.ruta_foto = ruta_foto;
    }

    public String getFecha_captura() {
        return fecha_captura;
    }

    public void setFecha_captura(String fecha_captura) {
        this.fecha_captura = fecha_captura;
    }

    // Métodos auxiliares

    public String getTabNombre() {
        switch (tab_numero) {
            case 1:
                return "Lectura";
            case 2:
                return "Residente";
            case 3:
                return "Acometida";
            case 4:
                return "Censos";
            case 5:
                return "Clasificación";
            case 6:
                return "Observación";
            default:
                return "Tab " + tab_numero;
        }
    }

    @Override
    public String toString() {
        return getTabNombre() + " - " + (descripcion != null ? descripcion : "Sin descripción");
    }
}
