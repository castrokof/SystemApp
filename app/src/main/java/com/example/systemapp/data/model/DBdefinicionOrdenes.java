package com.example.systemapp.data.model;

public class DBdefinicionOrdenes {
    //Nombre del esquema de Base de Datos
    public static final String DATABASE_NAME = "SystemApp";

    //Version de la Base de Datos
    public static final int DATABASE_VERSION = 4;

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

    // Campos nuevos para Facturación en Sitio (DATABASE_VERSION 4) — ver PLAN_FACTURACION_EN_SITIO.md
    public static final String EstratoId = "EstratoId";
    public static final String ServiciosCliente = "ServiciosCliente";
    public static final String TieneAseo = "TieneAseo";
    public static final String SaldoAnterior = "SaldoAnterior";
    // Consecutivo pre-asignado por el backend a esta lectura desde el 2026-07-29 (ver rutas API
    // actualizadas) — puede venir null para lecturas de períodos generados antes de esa fecha,
    // en cuyo caso se usa /api/rangoFacturacion como respaldo (ver Fragment_form_lectura).
    public static final String NumeroFactura = "NumeroFactura";
    // Historial de últimos meses de consumo, serializado como JSON (List<HistoricoConsumoDTO>) —
    // solo se usa para dibujar el gráfico de barras en 80mm, ver SOLICITUD_HISTORICO_CONSUMOS.md.
    public static final String HistoricoConsumosJson = "HistoricoConsumosJson";

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
                    LECTURAS.EstratoId + " INTEGER, " +
                    LECTURAS.ServiciosCliente + " VARCHAR ( 10 ) DEFAULT 'AG', " +
                    LECTURAS.TieneAseo + " INTEGER DEFAULT 0, " +
                    LECTURAS.SaldoAnterior + " REAL DEFAULT 0, " +
                    LECTURAS.NumeroFactura + " VARCHAR ( 20 ), " +
                    LECTURAS.HistoricoConsumosJson + " TEXT, " +

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

    // ================== Facturación en Sitio (DATABASE_VERSION 4) ==================
    // Ver PLAN_FACTURACION_EN_SITIO.md para el contrato de backend y el detalle de cada tabla.

    // Migración incremental de "lecturas" para instalaciones que venían en version 3.
    // Las instalaciones nuevas ya crean estas columnas vía ORDENES_TABLE_CREATE.
    public static final String LECTURAS_ALTER_ESTRATO_ID =
            "ALTER TABLE " + LECTURAS.TABLE_NAME + " ADD COLUMN " + LECTURAS.EstratoId + " INTEGER";
    public static final String LECTURAS_ALTER_SERVICIOS_CLIENTE =
            "ALTER TABLE " + LECTURAS.TABLE_NAME + " ADD COLUMN " + LECTURAS.ServiciosCliente + " VARCHAR(10) DEFAULT 'AG'";
    public static final String LECTURAS_ALTER_TIENE_ASEO =
            "ALTER TABLE " + LECTURAS.TABLE_NAME + " ADD COLUMN " + LECTURAS.TieneAseo + " INTEGER DEFAULT 0";
    public static final String LECTURAS_ALTER_SALDO_ANTERIOR =
            "ALTER TABLE " + LECTURAS.TABLE_NAME + " ADD COLUMN " + LECTURAS.SaldoAnterior + " REAL DEFAULT 0";
    public static final String LECTURAS_ALTER_NUMERO_FACTURA =
            "ALTER TABLE " + LECTURAS.TABLE_NAME + " ADD COLUMN " + LECTURAS.NumeroFactura + " VARCHAR(20)";
    public static final String LECTURAS_ALTER_HISTORICO_CONSUMOS =
            "ALTER TABLE " + LECTURAS.TABLE_NAME + " ADD COLUMN " + LECTURAS.HistoricoConsumosJson + " TEXT";

    public static class TARIFA_PERIODO {
        public static final String TABLE_NAME = "tarifa_periodo";
        public static final String id = "id";
        public static final String nombre = "nombre";
        public static final String vigente_desde = "vigente_desde";
        public static final String vigente_hasta = "vigente_hasta";
    }
    public static final String TARIFA_PERIODO_TABLE_CREATE =
            "CREATE TABLE " + TARIFA_PERIODO.TABLE_NAME + " (" +
                    TARIFA_PERIODO.id + " INTEGER NOT NULL, " +
                    TARIFA_PERIODO.nombre + " VARCHAR(150), " +
                    TARIFA_PERIODO.vigente_desde + " TEXT, " +
                    TARIFA_PERIODO.vigente_hasta + " TEXT, " +
                    " PRIMARY KEY(" + TARIFA_PERIODO.id + "));";
    public static final String TARIFA_PERIODO_TABLE_DROP = "DROP TABLE IF EXISTS " + TARIFA_PERIODO.TABLE_NAME;

    // Distinto de TARIFA_PERIODO (resolución tarifaria) — este es el ciclo de facturación en
    // curso (PeriodoLectura del backend), con las fechas clave a imprimir en la factura. Agregado
    // el 2026-07-29 junto con el campo periodo_lectura de POST /api/tarifaVigente. Se guarda un
    // único registro vigente por sync (ver AdminSQLiteOpenHelper.guardarTarifaVigente).
    public static class PERIODO_LECTURA {
        public static final String TABLE_NAME = "periodo_lectura";
        public static final String id = "id";
        public static final String codigo = "codigo";
        public static final String nombre = "nombre";
        public static final String fecha_inicio_lectura = "fecha_inicio_lectura";
        public static final String fecha_fin_lectura = "fecha_fin_lectura";
        public static final String fecha_expedicion = "fecha_expedicion";
        public static final String fecha_vencimiento = "fecha_vencimiento";
        public static final String fecha_corte = "fecha_corte";
    }
    public static final String PERIODO_LECTURA_TABLE_CREATE =
            "CREATE TABLE " + PERIODO_LECTURA.TABLE_NAME + " (" +
                    PERIODO_LECTURA.id + " INTEGER NOT NULL, " +
                    PERIODO_LECTURA.codigo + " VARCHAR(10), " +
                    PERIODO_LECTURA.nombre + " VARCHAR(150), " +
                    PERIODO_LECTURA.fecha_inicio_lectura + " TEXT, " +
                    PERIODO_LECTURA.fecha_fin_lectura + " TEXT, " +
                    PERIODO_LECTURA.fecha_expedicion + " TEXT, " +
                    PERIODO_LECTURA.fecha_vencimiento + " TEXT, " +
                    PERIODO_LECTURA.fecha_corte + " TEXT, " +
                    " PRIMARY KEY(" + PERIODO_LECTURA.id + "));";
    public static final String PERIODO_LECTURA_TABLE_DROP = "DROP TABLE IF EXISTS " + PERIODO_LECTURA.TABLE_NAME;

    public static class TARIFA_CARGO_FIJO {
        public static final String TABLE_NAME = "tarifa_cargo_fijo";
        public static final String tarifa_periodo_id = "tarifa_periodo_id";
        public static final String servicio = "servicio";
        public static final String estrato_id = "estrato_id";
        public static final String cargo_fijo = "cargo_fijo";
    }
    public static final String TARIFA_CARGO_FIJO_TABLE_CREATE =
            "CREATE TABLE " + TARIFA_CARGO_FIJO.TABLE_NAME + " (" +
                    TARIFA_CARGO_FIJO.tarifa_periodo_id + " INTEGER NOT NULL, " +
                    TARIFA_CARGO_FIJO.servicio + " VARCHAR(20) NOT NULL, " +
                    TARIFA_CARGO_FIJO.estrato_id + " INTEGER NOT NULL, " +
                    TARIFA_CARGO_FIJO.cargo_fijo + " REAL NOT NULL, " +
                    " PRIMARY KEY(" + TARIFA_CARGO_FIJO.tarifa_periodo_id + ", " + TARIFA_CARGO_FIJO.servicio + ", " + TARIFA_CARGO_FIJO.estrato_id + "));";
    public static final String TARIFA_CARGO_FIJO_TABLE_DROP = "DROP TABLE IF EXISTS " + TARIFA_CARGO_FIJO.TABLE_NAME;

    public static class TARIFA_RANGO {
        public static final String TABLE_NAME = "tarifa_rango";
        public static final String tarifa_periodo_id = "tarifa_periodo_id";
        public static final String servicio = "servicio";
        public static final String estrato_id = "estrato_id";
        public static final String tipo = "tipo";
        public static final String rango_desde = "rango_desde";
        public static final String rango_hasta = "rango_hasta";
        public static final String precio_m3 = "precio_m3";
    }
    public static final String TARIFA_RANGO_TABLE_CREATE =
            "CREATE TABLE " + TARIFA_RANGO.TABLE_NAME + " (" +
                    TARIFA_RANGO.tarifa_periodo_id + " INTEGER NOT NULL, " +
                    TARIFA_RANGO.servicio + " VARCHAR(20) NOT NULL, " +
                    TARIFA_RANGO.estrato_id + " INTEGER NOT NULL, " +
                    TARIFA_RANGO.tipo + " VARCHAR(20) NOT NULL, " +
                    TARIFA_RANGO.rango_desde + " INTEGER NOT NULL, " +
                    TARIFA_RANGO.rango_hasta + " INTEGER, " +
                    TARIFA_RANGO.precio_m3 + " REAL NOT NULL, " +
                    " PRIMARY KEY(" + TARIFA_RANGO.tarifa_periodo_id + ", " + TARIFA_RANGO.servicio + ", " + TARIFA_RANGO.estrato_id + ", " + TARIFA_RANGO.tipo + "));";
    public static final String TARIFA_RANGO_TABLE_DROP = "DROP TABLE IF EXISTS " + TARIFA_RANGO.TABLE_NAME;

    public static class ESTRATO_CACHE {
        public static final String TABLE_NAME = "estrato_cache";
        public static final String id = "id";
        public static final String numero = "numero";
        public static final String nombre = "nombre";
        public static final String porcentaje_subsidio = "porcentaje_subsidio";
        public static final String subsidio_fijo_acueducto = "subsidio_fijo_acueducto";
        public static final String subsidio_fijo_alcantarillado = "subsidio_fijo_alcantarillado";
        public static final String consumo_minimo_subsidio = "consumo_minimo_subsidio";
        public static final String activo = "activo";
    }
    public static final String ESTRATO_CACHE_TABLE_CREATE =
            "CREATE TABLE " + ESTRATO_CACHE.TABLE_NAME + " (" +
                    ESTRATO_CACHE.id + " INTEGER NOT NULL, " +
                    ESTRATO_CACHE.numero + " INTEGER, " +
                    ESTRATO_CACHE.nombre + " VARCHAR(50), " +
                    ESTRATO_CACHE.porcentaje_subsidio + " REAL, " +
                    ESTRATO_CACHE.subsidio_fijo_acueducto + " REAL, " +
                    ESTRATO_CACHE.subsidio_fijo_alcantarillado + " REAL, " +
                    ESTRATO_CACHE.consumo_minimo_subsidio + " REAL, " +
                    ESTRATO_CACHE.activo + " INTEGER, " +
                    " PRIMARY KEY(" + ESTRATO_CACHE.id + "));";
    public static final String ESTRATO_CACHE_TABLE_DROP = "DROP TABLE IF EXISTS " + ESTRATO_CACHE.TABLE_NAME;

    // Rango de numeración de facturas asignado al sincronizar la ruta (decisión 5 del plan):
    // evita colisiones de consecutivo entre dispositivos sin necesitar un round-trip al
    // servidor por cada factura generada en campo.
    public static class RANGO_FACTURACION {
        public static final String TABLE_NAME = "rango_facturacion";
        public static final String periodo = "periodo";
        public static final String secuencia_desde = "secuencia_desde";
        public static final String secuencia_hasta = "secuencia_hasta";
        public static final String siguiente = "siguiente";
    }
    public static final String RANGO_FACTURACION_TABLE_CREATE =
            "CREATE TABLE " + RANGO_FACTURACION.TABLE_NAME + " (" +
                    RANGO_FACTURACION.periodo + " VARCHAR(10) NOT NULL, " +
                    RANGO_FACTURACION.secuencia_desde + " INTEGER NOT NULL, " +
                    RANGO_FACTURACION.secuencia_hasta + " INTEGER NOT NULL, " +
                    RANGO_FACTURACION.siguiente + " INTEGER NOT NULL, " +
                    " PRIMARY KEY(" + RANGO_FACTURACION.periodo + "));";
    public static final String RANGO_FACTURACION_TABLE_DROP = "DROP TABLE IF EXISTS " + RANGO_FACTURACION.TABLE_NAME;

    public static class FACTURA_LOCAL {
        public static final String TABLE_NAME = "factura_local";
        public static final String id_local = "id_local";
        public static final String numero_factura = "numero_factura";
        public static final String lectura_id = "lectura_id";
        public static final String suscriptor = "suscriptor";
        public static final String periodo = "periodo";
        public static final String lectura_anterior = "lectura_anterior";
        public static final String lectura_actual = "lectura_actual";
        public static final String consumo_m3 = "consumo_m3";
        public static final String estrato_id_usado = "estrato_id_usado";
        public static final String tarifa_periodo_id_usado = "tarifa_periodo_id_usado";
        public static final String desglose_json = "desglose_json";
        public static final String total_a_pagar = "total_a_pagar";
        public static final String clasificacion = "clasificacion";
        public static final String fecha_impresion = "fecha_impresion";
        public static final String estado = "estado"; // PENDIENTE_SYNC | SINCRONIZADA | ANULADA
        public static final String anula_a_id_local = "anula_a_id_local";
        public static final String factura_id_servidor = "factura_id_servidor";
        public static final String sincronizado = "sincronizado";
    }
    public static final String FACTURA_LOCAL_TABLE_CREATE =
            "CREATE TABLE " + FACTURA_LOCAL.TABLE_NAME + " (" +
                    FACTURA_LOCAL.id_local + " VARCHAR(60) NOT NULL, " +
                    FACTURA_LOCAL.numero_factura + " VARCHAR(20) NOT NULL, " +
                    FACTURA_LOCAL.lectura_id + " VARCHAR(30) NOT NULL, " +
                    FACTURA_LOCAL.suscriptor + " VARCHAR(100) NOT NULL, " +
                    FACTURA_LOCAL.periodo + " VARCHAR(10), " +
                    FACTURA_LOCAL.lectura_anterior + " INTEGER, " +
                    FACTURA_LOCAL.lectura_actual + " INTEGER, " +
                    FACTURA_LOCAL.consumo_m3 + " INTEGER, " +
                    FACTURA_LOCAL.estrato_id_usado + " INTEGER, " +
                    FACTURA_LOCAL.tarifa_periodo_id_usado + " INTEGER, " +
                    FACTURA_LOCAL.desglose_json + " TEXT NOT NULL, " +
                    FACTURA_LOCAL.total_a_pagar + " REAL, " +
                    FACTURA_LOCAL.clasificacion + " VARCHAR(20), " +
                    FACTURA_LOCAL.fecha_impresion + " TEXT, " +
                    FACTURA_LOCAL.estado + " VARCHAR(20) NOT NULL, " +
                    FACTURA_LOCAL.anula_a_id_local + " VARCHAR(60), " +
                    FACTURA_LOCAL.factura_id_servidor + " INTEGER, " +
                    FACTURA_LOCAL.sincronizado + " INTEGER DEFAULT 0, " +
                    " PRIMARY KEY(" + FACTURA_LOCAL.id_local + "));";
    public static final String FACTURA_LOCAL_TABLE_DROP = "DROP TABLE IF EXISTS " + FACTURA_LOCAL.TABLE_NAME;

    public static class FACTURA_RESUELTA_SERVIDOR {
        public static final String TABLE_NAME = "factura_resuelta_servidor";
        public static final String factura_id = "factura_id";
        public static final String numero_factura = "numero_factura";
        public static final String lectura_id = "lectura_id";
        public static final String suscriptor = "suscriptor";
        public static final String desglose_json = "desglose_json";
        public static final String total_a_pagar = "total_a_pagar";
        public static final String estado = "estado";
        public static final String impresa = "impresa";
    }
    public static final String FACTURA_RESUELTA_SERVIDOR_TABLE_CREATE =
            "CREATE TABLE " + FACTURA_RESUELTA_SERVIDOR.TABLE_NAME + " (" +
                    FACTURA_RESUELTA_SERVIDOR.factura_id + " INTEGER NOT NULL, " +
                    FACTURA_RESUELTA_SERVIDOR.numero_factura + " VARCHAR(30), " +
                    FACTURA_RESUELTA_SERVIDOR.lectura_id + " VARCHAR(30) NOT NULL, " +
                    FACTURA_RESUELTA_SERVIDOR.suscriptor + " VARCHAR(100), " +
                    FACTURA_RESUELTA_SERVIDOR.desglose_json + " TEXT NOT NULL, " +
                    FACTURA_RESUELTA_SERVIDOR.total_a_pagar + " REAL, " +
                    FACTURA_RESUELTA_SERVIDOR.estado + " VARCHAR(20), " +
                    FACTURA_RESUELTA_SERVIDOR.impresa + " INTEGER DEFAULT 0, " +
                    " PRIMARY KEY(" + FACTURA_RESUELTA_SERVIDOR.factura_id + "));";
    public static final String FACTURA_RESUELTA_SERVIDOR_TABLE_DROP = "DROP TABLE IF EXISTS " + FACTURA_RESUELTA_SERVIDOR.TABLE_NAME;

}


