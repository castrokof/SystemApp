package com.example.systemapp.data.model;

public class DBOrdenLecturasEnviar {



    public String id;
    public String tipo;
    public String usuario;
    public Integer lectact;
    public String critica;
    public Integer causal;
    public Integer observ;
    public String observg;
    public String latitud;
    public String longitud;
    public String ruta_foto;
    public String finilec;
    public String ffinlec;
    // String (una sola foto base64, contrato viejo) u Object List<String> (array de fotos
    // base64, contrato nuevo con FOTOS_MULTIPLES_SOPORTADA) — Gson serializa según el tipo
    // real del valor asignado en tiempo de ejecución, no del tipo declarado del campo.
    public Object campoFoto;
    public Integer consumo;
    public String suscriptor;
    public String texobser;
    public String texcausa;



    public DBOrdenLecturasEnviar(Object campoFoto, String id, String tipo, String finilec, String ffinlec, Integer lectact, String critica, Integer causal, Integer observ, String observg, String latitud, String longitud, Integer consumo, String suscriptor, String usuario, String texobser, String texcausa) {

        this.id = id;
        this.tipo = tipo;
        this.usuario = usuario;
        this.lectact = lectact;
        this.critica = critica;
        this.causal = causal;
        this.observ = observ;
        this.observg = observg;
        this.latitud = latitud;
        this.longitud = longitud;
        this.ruta_foto = ruta_foto;
        this.finilec = finilec;
        this.ffinlec = ffinlec;
        this.campoFoto = campoFoto;
        this.consumo = consumo;
        this.suscriptor = suscriptor;
        this.texobser = texobser;
        this.texcausa = texcausa;

    }
}
