package com.example.systemapp.data.model;

/**
 * Definición de estructura de base de datos para el módulo de REVISIONES
 * Completamente separado del módulo de LECTURAS
 */
public class DBdefinicionRevisiones {

    public static final String DATABASE_NAME = "systemapp_revisiones.db";
    public static final int DATABASE_VERSION = 1;

    /**
     * Tabla: revisiones
     * Almacena las órdenes de revisión por desviaciones de consumo
     */
    public static class REVISIONES {
        public static final String TABLE_NAME = "revisiones";

        // Campos principales
        public static final String id = "id";
        public static final String Ciclo = "Ciclo";
        public static final String Categoria_orden = "Categoria_orden";
        public static final String Tipo_orden = "Tipo_orden";
        public static final String Periodo = "Periodo";
        public static final String Suscriptor = "Suscriptor";
        public static final String Ref_Medidor = "Ref_Medidor";
        public static final String Direccion = "Direccion";
        public static final String Nombre = "Nombre";
        public static final String Apell = "Apell";
        public static final String Promedio = "Promedio";
        public static final String LA = "LA"; // Lectura Anterior
        public static final String Usuario = "Usuario";
        public static final String Estado = "Estado"; // PENDIENTE, EN_EJECUCION, EJECUTADA
        public static final String Tipo_desviacion = "Tipo_desviacion"; // ALTO, BAJO
        public static final String Ruta = "Ruta";
        public static final String consecutivoRuta = "consecutivoRuta";
        public static final String observacion_inicial = "observacion_inicial";

        // Tab 1: Lectura
        public static final String lectura_actual = "lectura_actual";
        public static final String consumo = "consumo";

        // Tab 2: Residente
        public static final String nombre_residente = "nombre_residente";
        public static final String firma_path = "firma_path";

        // Tab 3: Acometida
        public static final String estado_acometida = "estado_acometida";
        public static final String estado_sellos = "estado_sellos";
        public static final String que_surte = "que_surte";

        // Tab 4: Censos
        public static final String censo_poblacional_familiar = "censo_poblacional_familiar";
        public static final String censo_poblacional_personas = "censo_poblacional_personas";
        public static final String censo_poblacional_adultos = "censo_poblacional_adultos";
        public static final String censo_poblacional_ninos = "censo_poblacional_ninos";

        // Tab 5: Clasificación
        public static final String codigo_causa = "codigo_causa";
        public static final String desc_causa = "desc_causa";
        public static final String observacion_causa = "observacion_causa";

        // Tab 6: Observación General
        public static final String observacion_general = "observacion_general";

        // Control
        public static final String fecha_inicio = "fecha_inicio";
        public static final String fecha_cierre = "fecha_cierre";
        public static final String cantidad_modificaciones = "cantidad_modificaciones";
        public static final String orden_personalizado = "orden_personalizado";
        public static final String latitud = "latitud";
        public static final String longitud = "longitud";
        public static final String ruta_pdf = "ruta_pdf";
        public static final String enviado_api = "enviado_api"; // SI, NO

        // SQL Create Table
        public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
                id + " VARCHAR(30) PRIMARY KEY, " +
                Ciclo + " VARCHAR(100), " +
                Categoria_orden + " VARCHAR(30), " +
                Tipo_orden + " VARCHAR(30), " +
                Periodo + " VARCHAR(100), " +
                Suscriptor + " VARCHAR(100), " +
                Ref_Medidor + " VARCHAR(100), " +
                Direccion + " VARCHAR(100), " +
                Nombre + " VARCHAR(100), " +
                Apell + " VARCHAR(100), " +
                Promedio + " INTEGER, " +
                LA + " VARCHAR(100), " +
                Usuario + " VARCHAR(100), " +
                Estado + " VARCHAR(50), " +
                Tipo_desviacion + " VARCHAR(20), " +
                Ruta + " VARCHAR(100), " +
                consecutivoRuta + " VARCHAR(100), " +
                observacion_inicial + " TEXT, " +
                lectura_actual + " INTEGER, " +
                consumo + " INTEGER, " +
                nombre_residente + " VARCHAR(200), " +
                firma_path + " TEXT, " +
                estado_acometida + " VARCHAR(50), " +
                estado_sellos + " VARCHAR(50), " +
                que_surte + " VARCHAR(200), " +
                censo_poblacional_familiar + " INTEGER, " +
                censo_poblacional_personas + " INTEGER, " +
                censo_poblacional_adultos + " INTEGER, " +
                censo_poblacional_ninos + " INTEGER, " +
                codigo_causa + " INTEGER, " +
                desc_causa + " TEXT, " +
                observacion_causa + " TEXT, " +
                observacion_general + " TEXT, " +
                fecha_inicio + " TEXT, " +
                fecha_cierre + " TEXT, " +
                cantidad_modificaciones + " INTEGER DEFAULT 0, " +
                orden_personalizado + " INTEGER DEFAULT 0, " +
                latitud + " TEXT, " +
                longitud + " TEXT, " +
                ruta_pdf + " TEXT, " +
                enviado_api + " VARCHAR(10) DEFAULT 'NO')";

        // SQL Drop Table
        public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    /**
     * Tabla: censo_hidraulico
     * Almacena los elementos del censo hidráulico (sanitarios, lavamanos, etc.)
     */
    public static class CENSO_HIDRAULICO {
        public static final String TABLE_NAME = "censo_hidraulico";

        public static final String id = "id";
        public static final String revision_id = "revision_id";
        public static final String elemento = "elemento";
        public static final String cantidad = "cantidad";
        public static final String estado = "estado"; // BUENO, MALO
        public static final String foto_path = "foto_path";

        // SQL Create Table
        public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
                id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                revision_id + " VARCHAR(30), " +
                elemento + " VARCHAR(100), " +
                cantidad + " INTEGER, " +
                estado + " VARCHAR(20), " +
                foto_path + " TEXT, " +
                "FOREIGN KEY(" + revision_id + ") REFERENCES " +
                REVISIONES.TABLE_NAME + "(" + REVISIONES.id + ") ON DELETE CASCADE)";

        // SQL Drop Table
        public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    /**
     * Tabla: fotos_revision
     * Almacena las fotos adicionales tomadas en cada tab
     */
    public static class FOTOS_REVISION {
        public static final String TABLE_NAME = "fotos_revision";

        public static final String id = "id";
        public static final String revision_id = "revision_id";
        public static final String tab_numero = "tab_numero";
        public static final String descripcion = "descripcion";
        public static final String ruta_foto = "ruta_foto";
        public static final String fecha_captura = "fecha_captura";

        // SQL Create Table
        public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
                id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                revision_id + " VARCHAR(30), " +
                tab_numero + " INTEGER, " +
                descripcion + " VARCHAR(200), " +
                ruta_foto + " TEXT, " +
                fecha_captura + " TEXT, " +
                "FOREIGN KEY(" + revision_id + ") REFERENCES " +
                REVISIONES.TABLE_NAME + "(" + REVISIONES.id + ") ON DELETE CASCADE)";

        // SQL Drop Table
        public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    /**
     * Tabla: causas_desviacion
     * Catálogo de causas de desviación (precargado desde API)
     */
    public static class CAUSAS_DESVIACION {
        public static final String TABLE_NAME = "causas_desviacion";

        public static final String codigo = "codigo";
        public static final String tipo = "tipo"; // ALTO, BAJO
        public static final String descripcion = "descripcion";

        // SQL Create Table
        public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
                codigo + " INTEGER PRIMARY KEY, " +
                tipo + " VARCHAR(10), " +
                descripcion + " TEXT)";

        // SQL Drop Table
        public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
