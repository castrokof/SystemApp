package com.example.systemapp.data.model;

import java.util.List;

public class OrdenesHijas {


    public List<DBOrdenLecturas> ASIGNADAS;
    public List<DBOrdenLecturas> REASIGNADAS;
    public List<DBOrdenLecturas> RELECTURA;

    public OrdenesHijas() {
    }

    public OrdenesHijas(List<DBOrdenLecturas> ASIGNADAS, List<DBOrdenLecturas> REASIGNADAS, List<DBOrdenLecturas> RELECTURA) {
        this.ASIGNADAS = ASIGNADAS;
        this.REASIGNADAS = REASIGNADAS;
        this.RELECTURA = RELECTURA;
    }

    public List<DBOrdenLecturas> getASIGNADAS() {
        return ASIGNADAS;
    }

    public void setASIGNADAS(List<DBOrdenLecturas> ASIGNADAS) {
        this.ASIGNADAS = ASIGNADAS;
    }

    public List<DBOrdenLecturas> getREASIGNADAS() {
        return REASIGNADAS;
    }

    public void setREASIGNADAS(List<DBOrdenLecturas> REASIGNADAS) {
        this.REASIGNADAS = REASIGNADAS;
    }

    public List<DBOrdenLecturas> getRELECTURA() {
        return RELECTURA;
    }

    public void setRELECTURA(List<DBOrdenLecturas> RELECTURA) {
        this.RELECTURA = RELECTURA;
    }
}
