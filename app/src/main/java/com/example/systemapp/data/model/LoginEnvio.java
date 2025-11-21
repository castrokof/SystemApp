package com.example.systemapp.data.model;

public class LoginEnvio {

    private String usuario;
    private String password;



    public LoginEnvio(String usuario, String password) {
        this.usuario = usuario;
        this.password = password;

    }

    public String getUsr() {
        return usuario;
    }

    public void setUsr(String username) {
        this.usuario = usuario;
    }

    public String getPwd() {
        return password;
    }

    public void setPwd(String password) {
        this.password = password;
    }



}
