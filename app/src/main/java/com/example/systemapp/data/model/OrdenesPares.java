package com.example.systemapp.data.model;


import java.util.List;

public class OrdenesPares {


    public OrdenesHijas RUTAS;
    public OrdenesHijas REVISIONES;
    public List<Object> ELIMINAR;

    public OrdenesPares() {
    }

    public OrdenesPares(OrdenesHijas rutas, OrdenesHijas revisiones, List<Object> eliminar) {
        this.RUTAS = rutas;
        this.REVISIONES = revisiones;
        this.ELIMINAR = eliminar;
    }

    public OrdenesHijas getRutas() {
        return RUTAS;
    }

    public void setRutas(OrdenesHijas rutas) {
        this.RUTAS = rutas;
    }

    public OrdenesHijas getRevisiones() {
        return REVISIONES;
    }

    public void setRevisiones(OrdenesHijas revisiones) {
        this.REVISIONES = revisiones;
    }

    public List<Object> getEliminar() {
        return ELIMINAR;
    }

    public void setEliminar(List<Object> eliminar) {
        this.ELIMINAR = eliminar;
    }
}
