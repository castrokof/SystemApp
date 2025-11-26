package com.example.systemapp.data.model;

import java.io.Serializable;

/**
 * Modelo de datos para una orden de revisión
 * Contiene todos los campos de los 6 tabs
 */
public class DBOrdenRevision implements Serializable {

    // Campos principales
    private String id;
    private String Ciclo;
    private String Categoria_orden;
    private String Tipo_orden;
    private String Periodo;
    private String Suscriptor;
    private String Ref_Medidor;
    private String Direccion;
    private String Nombre;
    private String Apell;
    private Integer Promedio;
    private String LA; // Lectura Anterior
    private String Usuario;
    private String Estado; // PENDIENTE, EN_EJECUCION, EJECUTADA
    private String Tipo_desviacion; // ALTO, BAJO
    private String Ruta;
    private String consecutivoRuta;
    private String observacion_inicial;

    // Tab 1: Lectura
    private Integer lectura_actual;
    private Integer consumo;

    // Tab 2: Residente
    private String nombre_residente;
    private String firma_path;

    // Tab 3: Acometida
    private String estado_acometida;
    private String estado_sellos;
    private String que_surte;

    // Tab 4: Censos
    private Integer censo_poblacional_familiar;
    private Integer censo_poblacional_personas;
    private Integer censo_poblacional_adultos;
    private Integer censo_poblacional_ninos;

    // Tab 5: Clasificación
    private Integer codigo_causa;
    private String desc_causa;
    private String observacion_causa;

    // Tab 6: Observación General
    private String observacion_general;

    // Control
    private String fecha_inicio;
    private String fecha_cierre;
    private Integer cantidad_modificaciones;
    private Integer orden_personalizado;
    private String latitud;
    private String longitud;
    private String ruta_pdf;
    private String enviado_api; // SI, NO

    // Constructor vacío
    public DBOrdenRevision() {
        this.cantidad_modificaciones = 0;
        this.orden_personalizado = 0;
        this.enviado_api = "NO";
    }

    // Getters y Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCiclo() {
        return Ciclo;
    }

    public void setCiclo(String ciclo) {
        Ciclo = ciclo;
    }

    public String getCategoria_orden() {
        return Categoria_orden;
    }

    public void setCategoria_orden(String categoria_orden) {
        Categoria_orden = categoria_orden;
    }

    public String getTipo_orden() {
        return Tipo_orden;
    }

    public void setTipo_orden(String tipo_orden) {
        Tipo_orden = tipo_orden;
    }

    public String getPeriodo() {
        return Periodo;
    }

    public void setPeriodo(String periodo) {
        Periodo = periodo;
    }

    public String getSuscriptor() {
        return Suscriptor;
    }

    public void setSuscriptor(String suscriptor) {
        Suscriptor = suscriptor;
    }

    public String getRef_Medidor() {
        return Ref_Medidor;
    }

    public void setRef_Medidor(String ref_Medidor) {
        Ref_Medidor = ref_Medidor;
    }

    public String getDireccion() {
        return Direccion;
    }

    public void setDireccion(String direccion) {
        Direccion = direccion;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getApell() {
        return Apell;
    }

    public void setApell(String apell) {
        Apell = apell;
    }

    public Integer getPromedio() {
        return Promedio;
    }

    public void setPromedio(Integer promedio) {
        Promedio = promedio;
    }

    public String getLA() {
        return LA;
    }

    public void setLA(String LA) {
        this.LA = LA;
    }

    public String getUsuario() {
        return Usuario;
    }

    public void setUsuario(String usuario) {
        Usuario = usuario;
    }

    public String getEstado() {
        return Estado;
    }

    public void setEstado(String estado) {
        Estado = estado;
    }

    public String getTipo_desviacion() {
        return Tipo_desviacion;
    }

    public void setTipo_desviacion(String tipo_desviacion) {
        Tipo_desviacion = tipo_desviacion;
    }

    public String getRuta() {
        return Ruta;
    }

    public void setRuta(String ruta) {
        Ruta = ruta;
    }

    public String getConsecutivoRuta() {
        return consecutivoRuta;
    }

    public void setConsecutivoRuta(String consecutivoRuta) {
        this.consecutivoRuta = consecutivoRuta;
    }

    public String getObservacion_inicial() {
        return observacion_inicial;
    }

    public void setObservacion_inicial(String observacion_inicial) {
        this.observacion_inicial = observacion_inicial;
    }

    public Integer getLectura_actual() {
        return lectura_actual;
    }

    public void setLectura_actual(Integer lectura_actual) {
        this.lectura_actual = lectura_actual;
    }

    public Integer getConsumo() {
        return consumo;
    }

    public void setConsumo(Integer consumo) {
        this.consumo = consumo;
    }

    public String getNombre_residente() {
        return nombre_residente;
    }

    public void setNombre_residente(String nombre_residente) {
        this.nombre_residente = nombre_residente;
    }

    public String getFirma_path() {
        return firma_path;
    }

    public void setFirma_path(String firma_path) {
        this.firma_path = firma_path;
    }

    public String getEstado_acometida() {
        return estado_acometida;
    }

    public void setEstado_acometida(String estado_acometida) {
        this.estado_acometida = estado_acometida;
    }

    public String getEstado_sellos() {
        return estado_sellos;
    }

    public void setEstado_sellos(String estado_sellos) {
        this.estado_sellos = estado_sellos;
    }

    public String getQue_surte() {
        return que_surte;
    }

    public void setQue_surte(String que_surte) {
        this.que_surte = que_surte;
    }

    public Integer getCenso_poblacional_familiar() {
        return censo_poblacional_familiar;
    }

    public void setCenso_poblacional_familiar(Integer censo_poblacional_familiar) {
        this.censo_poblacional_familiar = censo_poblacional_familiar;
    }

    public Integer getCenso_poblacional_personas() {
        return censo_poblacional_personas;
    }

    public void setCenso_poblacional_personas(Integer censo_poblacional_personas) {
        this.censo_poblacional_personas = censo_poblacional_personas;
    }

    public Integer getCenso_poblacional_adultos() {
        return censo_poblacional_adultos;
    }

    public void setCenso_poblacional_adultos(Integer censo_poblacional_adultos) {
        this.censo_poblacional_adultos = censo_poblacional_adultos;
    }

    public Integer getCenso_poblacional_ninos() {
        return censo_poblacional_ninos;
    }

    public void setCenso_poblacional_ninos(Integer censo_poblacional_ninos) {
        this.censo_poblacional_ninos = censo_poblacional_ninos;
    }

    public Integer getCodigo_causa() {
        return codigo_causa;
    }

    public void setCodigo_causa(Integer codigo_causa) {
        this.codigo_causa = codigo_causa;
    }

    public String getDesc_causa() {
        return desc_causa;
    }

    public void setDesc_causa(String desc_causa) {
        this.desc_causa = desc_causa;
    }

    public String getObservacion_causa() {
        return observacion_causa;
    }

    public void setObservacion_causa(String observacion_causa) {
        this.observacion_causa = observacion_causa;
    }

    public String getObservacion_general() {
        return observacion_general;
    }

    public void setObservacion_general(String observacion_general) {
        this.observacion_general = observacion_general;
    }

    public String getFecha_inicio() {
        return fecha_inicio;
    }

    public void setFecha_inicio(String fecha_inicio) {
        this.fecha_inicio = fecha_inicio;
    }

    public String getFecha_cierre() {
        return fecha_cierre;
    }

    public void setFecha_cierre(String fecha_cierre) {
        this.fecha_cierre = fecha_cierre;
    }

    public Integer getCantidad_modificaciones() {
        return cantidad_modificaciones;
    }

    public void setCantidad_modificaciones(Integer cantidad_modificaciones) {
        this.cantidad_modificaciones = cantidad_modificaciones;
    }

    public Integer getOrden_personalizado() {
        return orden_personalizado;
    }

    public void setOrden_personalizado(Integer orden_personalizado) {
        this.orden_personalizado = orden_personalizado;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getRuta_pdf() {
        return ruta_pdf;
    }

    public void setRuta_pdf(String ruta_pdf) {
        this.ruta_pdf = ruta_pdf;
    }

    public String getEnviado_api() {
        return enviado_api;
    }

    public void setEnviado_api(String enviado_api) {
        this.enviado_api = enviado_api;
    }

    // Métodos auxiliares

    public boolean puedeSerModificada() {
        return cantidad_modificaciones < 3;
    }

    public boolean isPendiente() {
        return "PENDIENTE".equals(Estado);
    }

    public boolean isEjecutada() {
        return "EJECUTADA".equals(Estado);
    }

    public boolean isEnviada() {
        return "SI".equals(enviado_api);
    }
}
