package com.example.systemapp.data.model;

public class DBdefinicionOrdenes {
    //Nombre del esquema de Base de Datos
    public static final String DATABASE_NAME = "SystemApp";

    //Version de la Base de Datos
    public static final int DATABASE_VERSION = 3;

public static class LECTURAS {
    public static final String TABLE_NAME = "lecturas";
    public static final String id = "id";
    public static final String Ciclo = "Ciclo";
    public static final String Categoria_orden = "Categoria_orden";
    public static final String Tipo_orden = "Tipo_orden";
    public static final String Periodo = "Periodo";
    public static final String Ref_Medidor = "Ref_Medidor";
    public static final String Direccion = "Direccion";
    public static final String Nombre = "Nombre";
    public static final String Apell = "Apell";
    public static final String LA = "LA";
    public static final String Promedio = "Promedio";
    public static final String Año = "Año";
    public static final String id_Ruta = "id_Ruta";
    public static final String Ruta = "Ruta";
    public static final String consecutivoRuta = "consecutivoRuta";
    public static final String Usuario = "Usuario";
    public static final String Estado = "Estado";
    public static final String Tope = "Tope";
    public static final String Suscriptor = "Suscriptor";
    public static final String cservic = "cservic";
    public static final String nservic = "nservic";
    public static final String ctipcon = "ctipcon";
    public static final String ntipcon = "ntipcon";

    // Campos nuevos al procesar la lectura
    public static final String Lectura_actual = "Lectura_actual";
    public static final String Estado_lectura = "estado_lectura";
    public static final String Uploadlec = "Uploadlec";
    public static final String Consumo = "Consumo";
    public static final String Critica = "Critica";
    public static final String Causa = "Causa";
    public static final String DescCausa = "DescCausa";
    public static final String Observacion = "Observacion";
    public static final String DescObservacion = "DescObservacion";
    public static final String ObservacionGral = "ObservacionGral";
    public static final String latitud = "latitud";
    public static final String longitud = "longitud";
    public static final String ruta_foto = "ruta_foto";
    public static final String finilec = "finilec";
    public static final String ffinlec = "ffinlec";


    }

    //Setencia SQL que permite crear la tabla ordenes
    public static final String ORDENES_TABLE_CREATE =
            "CREATE TABLE " + LECTURAS.TABLE_NAME + " (" +
                    LECTURAS.id + " varchar ( 30 ) NOT NULL, " +
                    LECTURAS.Ciclo + " varchar ( 100 ) NOT NULL, " +
                    LECTURAS.Categoria_orden + " varchar ( 30 ) , " +
                    LECTURAS.Tipo_orden + " varchar ( 30 ) , " +
                    LECTURAS.Periodo + " varchar ( 100 ) NOT NULL, " +
                    LECTURAS.Ref_Medidor + " varchar ( 100 ) NOT NULL, " +
                    LECTURAS.Direccion + " varchar ( 100 )  NOT NULL , " +
                    LECTURAS.Nombre + " varchar ( 100 ) NOT NULL, " +
                    LECTURAS.Apell + " varchar ( 100 ) NOT NULL, " +
                    LECTURAS.LA + " varchar ( 100 ), " +
                    LECTURAS.Promedio + " integer, " +
                    LECTURAS.Año + " varchar ( 10 ), " +
                    LECTURAS.id_Ruta + " varchar ( 100 ) NOT NULL, " +
                    LECTURAS.Ruta + " varchar ( 100 ) NOT NULL, " +
                    LECTURAS.consecutivoRuta + " varchar ( 100 ) NOT NULL, " +
                    LECTURAS.Usuario + " varchar ( 100 ) NOT NULL, " +
                    LECTURAS.Estado + " varchar ( 50 ) NOT NULL, " +
                    LECTURAS.Tope + " varchar ( 20 ) NOT NULL, " +
                    LECTURAS.Suscriptor + " varchar ( 100 ) NOT NULL, " +
                    LECTURAS.cservic + " varchar ( 15 ), " +
                    LECTURAS.nservic + " varchar ( 15 ), " +
                    LECTURAS.ctipcon + " varchar ( 15 ), " +
                    LECTURAS.ntipcon + " varchar ( 16 ), " +
                    LECTURAS.Lectura_actual + " integer, " +
                    LECTURAS.Estado_lectura + " varchar ( 15 ), " +
                    LECTURAS.Uploadlec + " varchar ( 15 ), " +
                    LECTURAS.finilec + " TEXT, " +
                    LECTURAS.ffinlec + " TEXT, " +
                    LECTURAS.Consumo + " integer, " +
                    LECTURAS.Critica + " varchar ( 100 ), " +
                    LECTURAS.Causa + " integer, " +
                    LECTURAS.DescCausa + " TEXT, " +
                    LECTURAS.Observacion + " integer, " +
                    LECTURAS.DescObservacion + " TEXT, " +
                    LECTURAS.ObservacionGral + " TEXT, " +
                    LECTURAS.latitud + " TEXT, " +
                    LECTURAS.longitud + " TEXT, " +
                    LECTURAS.ruta_foto + " TEXT, " +

                     " PRIMARY KEY( " + LECTURAS.id + ") );";

    //Setencia SQL que permite eliminar la tabla ordenes
    public static final String ORDENES_TABLE_DROP = "DROP TABLE IF EXISTS " + LECTURAS.TABLE_NAME;

    //Clase estatica en la que se definen las propiedaes que determinan la tabla listas
    public static class LISTAS {
        //Nombre de la tabla
        public static final String TABLE_NAME = "listas";
        //Nombre de las Columnas que contiene la tabla
        public static final String marca_id = "marca_id"; // Hace de grupo
        public static final String codigo = "codigo";
        public static final String descripcion = "descripcion";

    }

    //Setencia SQL que permite crear la tabla listas
    public static final String LISTAS_TABLE_CREATE =
            "CREATE TABLE " + LISTAS.TABLE_NAME + " (" +
                    LISTAS.marca_id + " varchar ( 100 ) NOT NULL, " +
                    LISTAS.codigo + " varchar ( 30 ) NOT NULL, " +
                    LISTAS.descripcion + " varchar ( 100 ) NOT NULL, " +
                    " PRIMARY KEY( " + LISTAS.marca_id + ", " + LISTAS.codigo + " ) );";

    //Setencia SQL que permite eliminar la tabla ordenes
    public static final String LISTAS_TABLE_DROP = "DROP TABLE IF EXISTS " + LISTAS.TABLE_NAME;

}


