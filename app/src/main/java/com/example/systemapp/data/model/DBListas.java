package com.example.systemapp.data.model;

public class DBListas {

    public String marca_id;
    public String codigo;
    public String descripcion;


    public DBListas(String marca_id, String codigo, String descripcion) {
        this.marca_id = marca_id;
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public DBListas() {

    }

    public String getMarca_id() {return  marca_id;}

    public void setMarca_id(String marca_id) {
        this.marca_id = marca_id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override

    //public String toString() {return codigo + "-"+ descripcion;}
    public String toString() {return  descripcion;}

}
