package com.example.systemapp.data.model;
import com.google.gson.annotations.JsonAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class DBOrdenLecturas {

    public String id;
    public String Ciclo;
    public String Categoria_orden;
    public String Tipo_orden;
    public String Periodo;
    public String Ref_Medidor;
    public String Direccion;
    public String Nombre;
    public String Apell;
    public String LA;
    public Integer Promedio;
    public String Año;
    public String id_Ruta;
    public String Ruta;
    public String consecutivoRuta;
    public String Usuario;
    public String Estado;
    public String Tope;
    public String Suscriptor;
    public String cservic;



    public String nservic;
    public String ctipcon;
    public String ntipcon;
    public Integer Lectura_actual;
    public String Estado_lectura;
    public String Uploadlec;
    public Integer Consumo;
    public String Critica;
    public Integer Causa;
    public String DescCausa;
    public Integer Observacion;
    public String DescObservacion;
    public String ObservacionGral;
    public String latitud;
    public String longitud;
    public String ruta_foto;
    public String finilec;
    public String ffinlec;

    // Campos nuevos para Facturación en Sitio (ver PLAN_FACTURACION_EN_SITIO.md, contrato 1.1)
    public Integer EstratoId;
    public String ServiciosCliente;
    @JsonAdapter(LenientBooleanAdapter.class)
    public Boolean TieneAseo;
    public Double SaldoAnterior;
    // Consecutivo ya pre-asignado por el backend a esta lectura desde el 2026-07-29 — puede venir
    // null para lecturas de períodos generados antes de esa fecha (ver rutas API actualizadas).
    public String NumeroFactura;
    // Últimos meses de consumo (ver SOLICITUD_HISTORICO_CONSUMOS.md, campo pedido al
    // backend) — solo para dibujar el gráfico de barras en la factura impresa en 80mm. Puede
    // venir null/vacío si el backend aún no lo manda o no hay historial para el cliente.
    public List<HistoricoConsumoDTO> HistoricoConsumos;


    public DBOrdenLecturas() {

    }


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

    public String getLA() {
        return LA;
    }

    public void setLA(String LA) {
        this.LA = LA;
    }

    public Integer getPromedio() {
        return Promedio;
    }

    public void setPromedio(Integer promedio) {
        Promedio = promedio;
    }

    public String getAño() {
        return Año;
    }

    public void setAño(String año) {
        Año = año;
    }

    public String getId_Ruta() {
        return id_Ruta;
    }

    public void setId_Ruta(String id_Ruta) {
        this.id_Ruta = id_Ruta;
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

    public String getTope() {
        return Tope;
    }

    public void setTope(String tope) {
        Tope = tope;
    }

    public String getSuscriptor() {
        return Suscriptor;
    }

    public void setSuscriptor(String suscriptor) {
        Suscriptor = suscriptor;
    }

    public Integer getLectura_actual() {
        return Lectura_actual;
    }

    public void setLectura_actual(Integer lectura_actual) {
        Lectura_actual = lectura_actual;
    }

    public String getEstado_lectura() {
        return Estado_lectura;
    }

    public void setEstado_lectura(String estado_lectura) {
        Estado_lectura = estado_lectura;
    }

    public String getUploadlec() {
        return Uploadlec;
    }

    public void setUploadlec(String uploadlec) {
        Uploadlec = uploadlec;
    }

    public Integer getConsumo() {
        return Consumo;
    }

    public void setConsumo(Integer consumo) {
        Consumo = consumo;
    }

    public String getCritica() {
        return Critica;
    }

    public void setCritica(String critica) {
        Critica = critica;
    }

    public Integer getCausa() {
        return Causa;
    }

    public void setCausa(Integer causa) {
        Causa = causa;
    }

    public String getDescCausa() {
        return DescCausa;
    }

    public void setDescCausa(String descCausa) {
        DescCausa = descCausa;
    }

    public Integer getObservacion() {
        return Observacion;
    }

    public void setObservacion(Integer observacion) {
        Observacion = observacion;
    }

    public String getDescObservacion() {
        return DescObservacion;
    }

    public void setDescObservacion(String descObservacion) {
        DescObservacion = descObservacion;
    }

    public String getObservacionGral() {
        return ObservacionGral;
    }

    public void setObservacionGral(String observacionGral) {
        ObservacionGral = observacionGral;
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

    public String getRuta_foto() {
        return ruta_foto;
    }

    public void setRuta_foto(String ruta_foto) {
        this.ruta_foto = ruta_foto;
    }

    // Helpers sobre ruta_foto (String con rutas separadas por ", ") para el flujo de
    // captura de fotos vía el ícono de cámara — ver PLAN de fotos por orden.
    public List<String> getFotosList() {
        List<String> fotos = new ArrayList<>();
        if (ruta_foto == null || ruta_foto.trim().isEmpty()) {
            return fotos;
        }
        for (String path : ruta_foto.split(",")) {
            String trimmed = path.trim();
            if (!trimmed.isEmpty()) {
                fotos.add(trimmed);
            }
        }
        return fotos;
    }

    public void agregarFoto(String path) {
        List<String> fotos = getFotosList();
        fotos.add(path);
        ruta_foto = String.join(", ", fotos);
    }

    public void quitarFoto(String path) {
        List<String> fotos = getFotosList();
        fotos.remove(path);
        ruta_foto = fotos.isEmpty() ? null : String.join(", ", fotos);
    }

    public String getFinilec() {
        return finilec;
    }

    public void setFinilec(String finilec) {
        this.finilec = finilec;
    }

    public String getFfinlec() {
        return ffinlec;
    }

    public void setFfinlec(String ffinlec) {
        this.ffinlec = ffinlec;
    }

    public String getCservic() {
        return cservic;
    }

    public void setCservic(String cservic) {
        this.cservic = cservic;
    }

    public String getNservic() {
        return nservic;
    }

    public void setNservic(String nservic) {
        this.nservic = nservic;
    }

    public String getCtipcon() {
        return ctipcon;
    }

    public void setCtipcon(String ctipcon) {
        this.ctipcon = ctipcon;
    }

    public String getNtipcon() {
        return ntipcon;
    }

    public void setNtipcon(String ntipcon) {
        this.ntipcon = ntipcon;
    }

    public Integer getEstratoId() {
        return EstratoId;
    }

    public void setEstratoId(Integer estratoId) {
        EstratoId = estratoId;
    }

    public String getServiciosCliente() {
        return ServiciosCliente;
    }

    public void setServiciosCliente(String serviciosCliente) {
        ServiciosCliente = serviciosCliente;
    }

    public Boolean getTieneAseo() {
        return TieneAseo;
    }

    public void setTieneAseo(Boolean tieneAseo) {
        TieneAseo = tieneAseo;
    }

    public Double getSaldoAnterior() {
        return SaldoAnterior;
    }

    public void setSaldoAnterior(Double saldoAnterior) {
        SaldoAnterior = saldoAnterior;
    }

    public String getNumeroFactura() {
        return NumeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        NumeroFactura = numeroFactura;
    }

    public List<HistoricoConsumoDTO> getHistoricoConsumos() {
        return HistoricoConsumos;
    }

    public void setHistoricoConsumos(List<HistoricoConsumoDTO> historicoConsumos) {
        HistoricoConsumos = historicoConsumos;
    }

}
