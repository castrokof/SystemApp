package com.example.systemapp.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginRespuesta {


    private String id;
    private String usuario;
    private String nombre;
    private String tipodeusuario;
    private String email;
    private String empresa;
    private String remenber_token;
    private String estado;
    private String created_at;
    @SerializedName("api_token")
    private String api_token; // Añade este campo
    public LoginRespuesta(String id, String usuario, String nombre, String tipodeusuario, String email, String empresa,
                     String remenber_token, String estado, String created_at, String api_token) {
        this.id = id;
        this.usuario = usuario;
        this.nombre = nombre;
        this.email = email;
        this.empresa = empresa;
        this.nombre = nombre;
        this.remenber_token = remenber_token;
        this.estado = estado;
        this.created_at = created_at;
        this.api_token = api_token;

    }

    // Getters y Setters
    public String getApiToken() {
        return api_token;
    }

    public void setApiToken(String api_token) {
        this.api_token = api_token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipodeusuario() {
        return tipodeusuario;
    }

    public void setTipodeusuario(String tipodeusuario) {
        this.tipodeusuario = tipodeusuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getRemenber_token() {
        return remenber_token;
    }

    public void setRemenber_token(String remenber_token) {
        this.remenber_token = remenber_token;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

}


