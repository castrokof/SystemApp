package com.example.systemapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.systemapp.data.model.DBdefinicionOrdenes;
import com.example.systemapp.data.model.DBListas;
import com.example.systemapp.data.model.DBOrdenLecturas;
import com.example.systemapp.data.model.FacturaLocal;
import com.example.systemapp.data.model.FacturaResueltaServidor;
import com.example.systemapp.data.model.HistoricoConsumoDTO;
import com.example.systemapp.data.model.RangoFacturacionResponse;
import com.example.systemapp.data.model.TarifaVigenteResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    public AdminSQLiteOpenHelper(Context context) {
        super(context, DBdefinicionOrdenes.DATABASE_NAME, null, DBdefinicionOrdenes.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String tabla_ordenes = "CREATE TABLE \"lecturas\" ( " +
                "`id` varchar ( 30 ) NOT NULL, `Ciclo` varchar ( 100 ) NOT NULL, " +
                "`Categoria_orden` varchar ( 30 ) , `Tipo_orden` varchar ( 30 ) , " +
                "`Periodo` varchar ( 100 ) NOT NULL, `Ref_Medidor` varchar ( 100 ) NOT NULL, " +
                "`Direccion` varchar ( 100 ) NOT NULL, `Nombre` varchar ( 100 ) NOT NULL, " +
                "`Apell` varchar ( 100 ) NOT NULL, `LA` varchar ( 100 ), " +
                "`Promedio` integer, `Año` varchar ( 10 ), `id_Ruta`  varchar ( 100 ) NOT NULL, " +
                "`Ruta` varchar ( 100 ) NOT NULL, `consecutivoRuta` varchar ( 100 ) NOT NULL, `Usuario` varchar ( 100 ) NOT NULL, " +
                "`Estado` varchar ( 50 ) NOT NULL, `Tope` varchar ( 20 ) NOT NULL, `Suscriptor` varchar ( 100 ) NOT NULL, " +
                "`cservic` varchar ( 15 ), `nservic` varchar ( 15 ), `ctipcon` varchar ( 15 ), `ntipcon` varchar ( 16 ), " +
                "`Lectura_actual` integer, `Estado_lectura` varchar (15), `Uploadlec` varchar ( 15 ), `finilec` TEXT, " +
                "`ffinlec` TEXT, `Consumo` integer, `Critica` varchar ( 100 ), Causa integer," +
                " DescCausa TEXT,  Observacion integer,  DescObservacion TEXT, ObservacionGral TEXT," +
                " latitud TEXT, longitud TEXT, ruta_foto TEXT, PRIMARY KEY(`id`) )";
        db.execSQL(DBdefinicionOrdenes.ORDENES_TABLE_CREATE);
        db.execSQL(DBdefinicionOrdenes.LISTAS_TABLE_CREATE);

        // Facturación en Sitio (ver PLAN_FACTURACION_EN_SITIO.md) — instalaciones nuevas
        // crean estas tablas directamente ya en version 4, sin pasar por onUpgrade.
        crearTablasFacturacionEnSitio(db);

    }

    private void crearTablasFacturacionEnSitio(SQLiteDatabase db) {
        db.execSQL(DBdefinicionOrdenes.TARIFA_PERIODO_TABLE_CREATE);
        db.execSQL(DBdefinicionOrdenes.PERIODO_LECTURA_TABLE_CREATE);
        db.execSQL(DBdefinicionOrdenes.TARIFA_CARGO_FIJO_TABLE_CREATE);
        db.execSQL(DBdefinicionOrdenes.TARIFA_RANGO_TABLE_CREATE);
        db.execSQL(DBdefinicionOrdenes.ESTRATO_CACHE_TABLE_CREATE);
        db.execSQL(DBdefinicionOrdenes.RANGO_FACTURACION_TABLE_CREATE);
        db.execSQL(DBdefinicionOrdenes.FACTURA_LOCAL_TABLE_CREATE);
        db.execSQL(DBdefinicionOrdenes.FACTURA_RESUELTA_SERVIDOR_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No borrar/recrear tablas aquí: eso destruye las lecturas capturadas en campo
        // que aún no se han sincronizado con el servidor.
        // Cada futuro incremento de DATABASE_VERSION debe agregar su propia migración
        // incremental (ALTER TABLE) sin tocar los datos existentes.
        if (oldVersion < 4) {
            db.execSQL(DBdefinicionOrdenes.LECTURAS_ALTER_ESTRATO_ID);
            db.execSQL(DBdefinicionOrdenes.LECTURAS_ALTER_SERVICIOS_CLIENTE);
            db.execSQL(DBdefinicionOrdenes.LECTURAS_ALTER_TIENE_ASEO);
            db.execSQL(DBdefinicionOrdenes.LECTURAS_ALTER_SALDO_ANTERIOR);
            db.execSQL(DBdefinicionOrdenes.LECTURAS_ALTER_NUMERO_FACTURA);
            db.execSQL(DBdefinicionOrdenes.LECTURAS_ALTER_HISTORICO_CONSUMOS);
            crearTablasFacturacionEnSitio(db);
        }
    }

    /*
     * OPERACIONES CRUD (Create, Read, Update, Delete)
     * A partir de aquí se definen los métodos para insertar, leer, actualizar y borrar registros de
     * la base de datos (BD)
     * */

    public void insertRecord(String tableName, ContentValues values){
        //Con este método insertamos las notas nuevas que el usuario vaya creando

        // 1. Obtenemos una reference de la BD con permisos de escritura
        SQLiteDatabase db = this.getWritableDatabase();
//
//        // 2. Creamos un obejto de tipo ContentValues para agregar los pares
//        // de Claves de Columna y Valor
//        ContentValues values = new ContentValues();
//        values.put(NotesDBDef.NOTES.TITLE_COL, book.getTitle()); // Titulo
//        values.put(NotesDBDef.NOTES.URL_COL, book.getUrl()); // Titulo
//        values.put(NotesDBDef.NOTES.DESCRIP_COL, book.getDescription()); // Descripción
//
//        db.
        // 3. Insertamos los datos en la tabla "notes"
        db.insert(tableName, null, values);

        // 4. Cerramos la conexión comn la BD
        db.close();
    }

    /**
     *VERIFICAR SI SE HACE NECESARIO VALIDAR SI YA UN REGISTRO ESTÁ INSERTADO
     */

    /**
     * Inserta los datos del elemento OrdenesDB, como un registro de la tabla ordenes
     * lo cuál solo deja en blanco los atributos de la tabla que pertenecen al proceso de la
     * lectura en un contador, dichos atributos en blanco serán llenados luego, cuando se realice la lectura
     * devuelve 0 si no actualizó registro o -1 si no pudo insertar
     * */
    public Long insertOrden(DBOrdenLecturas dbOrdenLecturas, boolean update){

        // 1. Obtenemos una reference de la BD con permisos de escritura
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        if (update){

            //crear un obejto de tipo ContentValues para agregar los pares
            // de Claves de Columna y Valor, la clave debe corresponder al nombre
            // de la columna en la bd
            values.put(DBdefinicionOrdenes.LECTURAS.Categoria_orden, dbOrdenLecturas.getCategoria_orden());
            values.put(DBdefinicionOrdenes.LECTURAS.Lectura_actual, dbOrdenLecturas.getLectura_actual());
            values.put(DBdefinicionOrdenes.LECTURAS.Consumo, dbOrdenLecturas.getConsumo());
            values.put(DBdefinicionOrdenes.LECTURAS.Critica, dbOrdenLecturas.getCritica());
            values.put(DBdefinicionOrdenes.LECTURAS.Observacion, dbOrdenLecturas.getObservacion());
            values.put(DBdefinicionOrdenes.LECTURAS.DescObservacion, dbOrdenLecturas.getDescObservacion());
            values.put(DBdefinicionOrdenes.LECTURAS.ObservacionGral, dbOrdenLecturas.getObservacionGral());
            values.put(DBdefinicionOrdenes.LECTURAS.Causa, dbOrdenLecturas.getCausa());
            values.put(DBdefinicionOrdenes.LECTURAS.DescCausa, dbOrdenLecturas.getDescCausa());
            values.put(DBdefinicionOrdenes.LECTURAS.ruta_foto, dbOrdenLecturas.getRuta_foto());
            values.put(DBdefinicionOrdenes.LECTURAS.finilec, dbOrdenLecturas.getFinilec());
            values.put(DBdefinicionOrdenes.LECTURAS.ffinlec, dbOrdenLecturas.getFfinlec());
            values.put(DBdefinicionOrdenes.LECTURAS.Promedio, dbOrdenLecturas.getPromedio());
            values.put(DBdefinicionOrdenes.LECTURAS.Estado_lectura, dbOrdenLecturas.getEstado_lectura());
            values.put(DBdefinicionOrdenes.LECTURAS.Uploadlec, dbOrdenLecturas.getUploadlec());
            values.put(DBdefinicionOrdenes.LECTURAS.latitud, dbOrdenLecturas.getLatitud());
            values.put(DBdefinicionOrdenes.LECTURAS.longitud, dbOrdenLecturas.getLongitud());
            if (dbOrdenLecturas.getEstratoId() != null) {
                values.put(DBdefinicionOrdenes.LECTURAS.EstratoId, dbOrdenLecturas.getEstratoId());
            }
            if (dbOrdenLecturas.getServiciosCliente() != null) {
                values.put(DBdefinicionOrdenes.LECTURAS.ServiciosCliente, dbOrdenLecturas.getServiciosCliente());
            }
            if (dbOrdenLecturas.getTieneAseo() != null) {
                values.put(DBdefinicionOrdenes.LECTURAS.TieneAseo, dbOrdenLecturas.getTieneAseo() ? 1 : 0);
            }
            if (dbOrdenLecturas.getSaldoAnterior() != null) {
                values.put(DBdefinicionOrdenes.LECTURAS.SaldoAnterior, dbOrdenLecturas.getSaldoAnterior());
            }

            // 3. Actualizar los datos en la tabla "ordenes"
            int i = db.update(DBdefinicionOrdenes.LECTURAS.TABLE_NAME, //table
                    values, // column/value
                    DBdefinicionOrdenes.LECTURAS.id + " = ?", // selections
                    new String[] { String.valueOf( dbOrdenLecturas.getId() ) }); //selection args

            // 4. Cerramos la conexión comn la BD
            db.close();

            return (long) i;

        }else{


            //crear un objeto de tipo ContentValues para agregar los pares
            // de Claves de Columna y Valor, la clave debe corresponder al nombre
            // de la columna en la bd
            values.put(DBdefinicionOrdenes.LECTURAS.Tipo_orden, "RUTAS");
            values.put(DBdefinicionOrdenes.LECTURAS.Categoria_orden, "ASIGNADAS");
            values.put(DBdefinicionOrdenes.LECTURAS.Periodo, dbOrdenLecturas.getPeriodo());
            values.put(DBdefinicionOrdenes.LECTURAS.Ciclo, dbOrdenLecturas.getCiclo());
            values.put(DBdefinicionOrdenes.LECTURAS.Ruta, dbOrdenLecturas.getRuta());
            values.put(DBdefinicionOrdenes.LECTURAS.Suscriptor, dbOrdenLecturas.getSuscriptor());
            // cservic/nservic: usar el valor real del servidor si viene, con el default histórico
            // como fallback (antes se hardcodeaba siempre, ignorando lo que mandara el backend).
            values.put(DBdefinicionOrdenes.LECTURAS.cservic,
                    dbOrdenLecturas.getCservic() != null ? dbOrdenLecturas.getCservic() : "0040");
            values.put(DBdefinicionOrdenes.LECTURAS.nservic,
                    dbOrdenLecturas.getNservic() != null ? dbOrdenLecturas.getNservic() : "ACUEDUCTO");
            values.put(DBdefinicionOrdenes.LECTURAS.ctipcon, "16");
            values.put(DBdefinicionOrdenes.LECTURAS.ntipcon, "ACUEDUCTO");
            values.put(DBdefinicionOrdenes.LECTURAS.consecutivoRuta, dbOrdenLecturas.getConsecutivoRuta());
            values.put(DBdefinicionOrdenes.LECTURAS.Direccion, dbOrdenLecturas.getDireccion());
            values.put(DBdefinicionOrdenes.LECTURAS.Nombre, dbOrdenLecturas.getNombre());
            values.put(DBdefinicionOrdenes.LECTURAS.Apell, dbOrdenLecturas.getApell());
            values.put(DBdefinicionOrdenes.LECTURAS.id, dbOrdenLecturas.getId());
            values.put(DBdefinicionOrdenes.LECTURAS.Ref_Medidor, dbOrdenLecturas.getRef_Medidor());
            values.put(DBdefinicionOrdenes.LECTURAS.LA, dbOrdenLecturas.getLA());
            values.put(DBdefinicionOrdenes.LECTURAS.Año, dbOrdenLecturas.getAño());
            values.put(DBdefinicionOrdenes.LECTURAS.id_Ruta, dbOrdenLecturas.getId_Ruta());
            values.put(DBdefinicionOrdenes.LECTURAS.Usuario, dbOrdenLecturas.getUsuario());
            values.put(DBdefinicionOrdenes.LECTURAS.Promedio, dbOrdenLecturas.getPromedio());
            values.put(DBdefinicionOrdenes.LECTURAS.Estado, dbOrdenLecturas.getEstado());
            values.put(DBdefinicionOrdenes.LECTURAS.Lectura_actual, dbOrdenLecturas.getLectura_actual());
            values.put(DBdefinicionOrdenes.LECTURAS.Tope, dbOrdenLecturas.getTope());
            values.put(DBdefinicionOrdenes.LECTURAS.EstratoId, dbOrdenLecturas.getEstratoId());
            values.put(DBdefinicionOrdenes.LECTURAS.ServiciosCliente,
                    dbOrdenLecturas.getServiciosCliente() != null ? dbOrdenLecturas.getServiciosCliente() : "AG");
            values.put(DBdefinicionOrdenes.LECTURAS.TieneAseo,
                    Boolean.TRUE.equals(dbOrdenLecturas.getTieneAseo()) ? 1 : 0);
            values.put(DBdefinicionOrdenes.LECTURAS.SaldoAnterior,
                    dbOrdenLecturas.getSaldoAnterior() != null ? dbOrdenLecturas.getSaldoAnterior() : 0);
            values.put(DBdefinicionOrdenes.LECTURAS.NumeroFactura, dbOrdenLecturas.getNumeroFactura());
            values.put(DBdefinicionOrdenes.LECTURAS.HistoricoConsumosJson,
                    new Gson().toJson(dbOrdenLecturas.getHistoricoConsumos()));


            // 3. Insertamos los datos en la tabla "ordenes"
            Long idInserted = db.insert(DBdefinicionOrdenes.LECTURAS.TABLE_NAME, null, values);

            // 4. Cerramos la conexión comn la BD
            db.close();

            return idInserted;

        }

    }

    /**
     * Inserta los datos del elemento OrdenesDB, como un registro de la tabla ordenes
     * lo cuál solo deja en blanco los atributos de la tabla que pertenecen al proceso de la
     * lectura en un contador, dichos atributos en blanco serán llenados luego, cuando se realice la lectura
     * */
    public Long insertElementoLista(DBListas dbListas){

        // 1. Obtenemos una reference de la BD con permisos de escritura
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        //crear un obejto de tipo ContentValues para agregar los pares
        // de Claves de Columna y Valor, la clave debe corresponder al nombre
        // de la columna en la bd
        values.put(DBdefinicionOrdenes.LISTAS.marca_id, dbListas.getMarca_id());
        values.put(DBdefinicionOrdenes.LISTAS.codigo, dbListas.getCodigo());
        values.put(DBdefinicionOrdenes.LISTAS.descripcion, dbListas.getDescripcion());

        // 3. Insertamos los datos en la tabla "listas"
        Long idInserted = db.insert(DBdefinicionOrdenes.LISTAS.TABLE_NAME, null, values);

        // 4. Cerramos la conexión comn la BD
        db.close();

        return idInserted;
    }

    /**
     * Obtener todos los registros de una tabla, se puede agregar una condición
     * @param tableName nombre de la tabla a consultar
     * @param condition condición para consultar los registros en la tabla dada, puede ser ""
     *                  la estructura debe ser "nombreColumna operador valor AND ..."
     * @return Lista de registros de la tabla
     */
    public List getData(String tableName, String condition) {
        //Instanciamos un Array para llenarlo con todos los objetos Notes que saquemos de la BD
        List dataList = new ArrayList();

        if (!condition.equals(""))
            condition = " WHERE " + condition;
        else
            condition = "";

        // 1. Armamos un String con el query a ejecutar
        String query = "SELECT * FROM " + tableName + condition;

        // Agregar ORDER BY solo para la tabla "lecturas"
        if ("lecturas".equals(tableName)) {
            query += " ORDER BY id_Ruta, CAST(consecutivoRuta AS INTEGER)";
        }

        // 2. Obtenemos una reference de la BD con permisos de escritura y ejecutamos el query
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Object data = null;
        switch (tableName){
            case "lecturas":

                if (cursor.moveToFirst()) {
                    do {
                        data = new DBOrdenLecturas();
                        ((DBOrdenLecturas) data).setId(cursor.getString(0));
                        ((DBOrdenLecturas) data).setCiclo(cursor.getString(1));
                        ((DBOrdenLecturas) data).setCategoria_orden(cursor.getString(2));
                        ((DBOrdenLecturas) data).setTipo_orden(cursor.getString(3));
                        ((DBOrdenLecturas) data).setPeriodo(cursor.getString(4));
                        ((DBOrdenLecturas) data).setRef_Medidor(cursor.getString(5));
                        ((DBOrdenLecturas) data).setDireccion(cursor.getString(6));
                        ((DBOrdenLecturas) data).setNombre(cursor.getString(7));
                        ((DBOrdenLecturas) data).setApell(cursor.getString(8));
                        ((DBOrdenLecturas) data).setLA(cursor.getString(9));
                        ((DBOrdenLecturas) data).setPromedio(cursor.getInt(10));
                        ((DBOrdenLecturas) data).setAño(cursor.getString(11));
                        ((DBOrdenLecturas) data).setId_Ruta(cursor.getString(12));
                        ((DBOrdenLecturas) data).setRuta(cursor.getString(13));
                        ((DBOrdenLecturas) data).setConsecutivoRuta(cursor.getString(14));
                        ((DBOrdenLecturas) data).setUsuario(cursor.getString(15));
                        ((DBOrdenLecturas) data).setEstado(cursor.getString(16));
                        ((DBOrdenLecturas) data).setTope(cursor.getString(17));
                        ((DBOrdenLecturas) data).setSuscriptor(cursor.getString(18));
                        ((DBOrdenLecturas) data).setCservic(cursor.getString(19));
                        ((DBOrdenLecturas) data).setNservic(cursor.getString(20));
                        ((DBOrdenLecturas) data).setCtipcon(cursor.getString(21));
                        ((DBOrdenLecturas) data).setNtipcon(cursor.getString(22));

                       // Campos nuevos al procesar la lectura

                        ((DBOrdenLecturas) data).setLectura_actual(cursor.getInt(23));
                        ((DBOrdenLecturas) data).setEstado_lectura(cursor.getString(24));
                        ((DBOrdenLecturas) data).setUploadlec(cursor.getString(25));
                        ((DBOrdenLecturas) data).setFinilec(cursor.getString(26));
                        ((DBOrdenLecturas) data).setFfinlec(cursor.getString(27));
                        ((DBOrdenLecturas) data).setConsumo(cursor.getInt(28));
                        ((DBOrdenLecturas) data).setCritica(cursor.getString(29));
                        ((DBOrdenLecturas) data).setCausa(cursor.getInt(30));
                        ((DBOrdenLecturas) data).setDescCausa(cursor.getString(31));
                        ((DBOrdenLecturas) data).setObservacion(cursor.getInt(32));
                        ((DBOrdenLecturas) data).setDescObservacion(cursor.getString(33));
                        ((DBOrdenLecturas) data).setObservacionGral(cursor.getString(34));
                        ((DBOrdenLecturas) data).setLatitud(cursor.getString(35));
                        ((DBOrdenLecturas) data).setLongitud(cursor.getString(36));
                        ((DBOrdenLecturas) data).setRuta_foto(cursor.getString(37));
                        // Campos nuevos de Facturación en Sitio (columnas 38-41, ver DATABASE_VERSION 4)
                        ((DBOrdenLecturas) data).setEstratoId(cursor.isNull(38) ? null : cursor.getInt(38));
                        ((DBOrdenLecturas) data).setServiciosCliente(cursor.getString(39));
                        ((DBOrdenLecturas) data).setTieneAseo(cursor.isNull(40) ? null : cursor.getInt(40) == 1);
                        ((DBOrdenLecturas) data).setSaldoAnterior(cursor.isNull(41) ? null : cursor.getDouble(41));
                        ((DBOrdenLecturas) data).setNumeroFactura(cursor.getString(42));
                        String historicoJson = cursor.getString(43);
                        if (historicoJson != null) {
                            Type tipoHistorico = new TypeToken<List<HistoricoConsumoDTO>>() {}.getType();
                            ((DBOrdenLecturas) data).setHistoricoConsumos(new Gson().fromJson(historicoJson, tipoHistorico));
                        }


                        dataList.add(data);
                    } while (cursor.moveToNext());
                }
                break;
            case "listas":
                if (cursor.moveToFirst()) {
                    do {
                        data = new DBListas();
                        ((DBListas) data).setMarca_id(cursor.getString(0));
                        ((DBListas) data).setCodigo(cursor.getString(1));
                        ((DBListas) data).setDescripcion(cursor.getString(2));

                        dataList.add(data);
                    } while (cursor.moveToNext());
                }
                break;
            case "tarifa_cargo_fijo":
                if (cursor.moveToFirst()) {
                    do {
                        TarifaVigenteResponse.CargoFijoDTO cf = new TarifaVigenteResponse.CargoFijoDTO();
                        // tarifa_periodo_id (0) no se mapea al DTO, solo se usó para filtrar/guardar
                        cf.servicio = cursor.getString(1);
                        cf.estrato_id = cursor.getInt(2);
                        cf.cargo_fijo = cursor.getDouble(3);
                        dataList.add(cf);
                    } while (cursor.moveToNext());
                }
                break;
            case "tarifa_rango":
                if (cursor.moveToFirst()) {
                    do {
                        TarifaVigenteResponse.RangoDTO r = new TarifaVigenteResponse.RangoDTO();
                        r.servicio = cursor.getString(1);
                        r.estrato_id = cursor.getInt(2);
                        r.tipo = cursor.getString(3);
                        r.rango_desde = cursor.getInt(4);
                        r.rango_hasta = cursor.isNull(5) ? null : cursor.getInt(5);
                        r.precio_m3 = cursor.getDouble(6);
                        dataList.add(r);
                    } while (cursor.moveToNext());
                }
                break;
            case "periodo_lectura":
                if (cursor.moveToFirst()) {
                    do {
                        TarifaVigenteResponse.PeriodoLecturaDTO pl = new TarifaVigenteResponse.PeriodoLecturaDTO();
                        pl.id = cursor.getInt(0);
                        pl.codigo = cursor.getString(1);
                        pl.nombre = cursor.getString(2);
                        pl.fecha_inicio_lectura = cursor.getString(3);
                        pl.fecha_fin_lectura = cursor.getString(4);
                        pl.fecha_expedicion = cursor.getString(5);
                        pl.fecha_vencimiento = cursor.getString(6);
                        pl.fecha_corte = cursor.getString(7);
                        dataList.add(pl);
                    } while (cursor.moveToNext());
                }
                break;
            case "estrato_cache":
                if (cursor.moveToFirst()) {
                    do {
                        TarifaVigenteResponse.EstratoDTO e = new TarifaVigenteResponse.EstratoDTO();
                        e.id = cursor.getInt(0);
                        e.numero = cursor.getInt(1);
                        e.nombre = cursor.getString(2);
                        e.porcentaje_subsidio = cursor.getDouble(3);
                        e.subsidio_fijo_acueducto = cursor.getDouble(4);
                        e.subsidio_fijo_alcantarillado = cursor.getDouble(5);
                        e.consumo_minimo_subsidio = cursor.getDouble(6);
                        e.activo = cursor.getInt(7) == 1;
                        dataList.add(e);
                    } while (cursor.moveToNext());
                }
                break;
            case "factura_local":
                if (cursor.moveToFirst()) {
                    do {
                        FacturaLocal f = new FacturaLocal();
                        f.setIdLocal(cursor.getString(0));
                        f.setNumeroFactura(cursor.getString(1));
                        f.setLecturaId(cursor.getString(2));
                        f.setSuscriptor(cursor.getString(3));
                        f.setPeriodo(cursor.getString(4));
                        f.setLecturaAnterior(cursor.isNull(5) ? null : cursor.getInt(5));
                        f.setLecturaActual(cursor.isNull(6) ? null : cursor.getInt(6));
                        f.setConsumoM3(cursor.isNull(7) ? null : cursor.getInt(7));
                        f.setEstratoIdUsado(cursor.isNull(8) ? null : cursor.getInt(8));
                        f.setTarifaPeriodoIdUsado(cursor.isNull(9) ? null : cursor.getInt(9));
                        f.setDesgloseJson(cursor.getString(10));
                        f.setTotalAPagar(cursor.isNull(11) ? null : cursor.getDouble(11));
                        f.setClasificacion(cursor.getString(12));
                        f.setFechaImpresion(cursor.getString(13));
                        f.setEstado(cursor.getString(14));
                        f.setAnulaAIdLocal(cursor.getString(15));
                        f.setFacturaIdServidor(cursor.isNull(16) ? null : cursor.getInt(16));
                        f.setSincronizado(cursor.getInt(17) == 1);
                        dataList.add(f);
                    } while (cursor.moveToNext());
                }
                break;
            case "factura_resuelta_servidor":
                if (cursor.moveToFirst()) {
                    do {
                        FacturaResueltaServidor fr = new FacturaResueltaServidor();
                        fr.setFacturaId(cursor.getInt(0));
                        fr.setNumeroFactura(cursor.getString(1));
                        fr.setLecturaId(cursor.getString(2));
                        fr.setSuscriptor(cursor.getString(3));
                        fr.setDesgloseJson(cursor.getString(4));
                        fr.setTotalAPagar(cursor.isNull(5) ? null : cursor.getDouble(5));
                        fr.setEstado(cursor.getString(6));
                        fr.setImpresa(cursor.getInt(7) == 1);
                        dataList.add(fr);
                    } while (cursor.moveToNext());
                }
                break;
            default:
                break;
        }

        //Cerramos el cursor
        cursor.close();

        //cerrar la conexión a bd
        db.close();

        // Devolvemos los registros encontrados o un array vacio en caso de que no se encuentre nada
        return dataList;
    }

    /**
     * Obtener la cantidad de filas retornadas a partir de una consulta
     * @param tableName nombre de la tabla a consultar
     * @param condition condición para consultar los registros en la tabla dada, puede ser ""
     *                  la estructura debe ser "nombreColumna operador valor AND ..."
     * @return Cantidad de registros de la tabla
     */
    public int getCount(String tableName, String condition) {

        int count = 0;

        if (!condition.equals(""))
            condition = " WHERE " + condition;
        else
            condition = "";

        // 1. Armamos un String con el query a ejecutar
        String query = "SELECT COUNT(*) AS count FROM " + tableName + condition;

        // 2. Obtenemos una reference de la BD con permisos de escritura y ejecutamos el query
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        //Cerramos el cursor
        cursor.close();

        //cerrar la conexión a bd
        db.close();

        // Devolvemos la cantidad de registros
        return count;
    }

    /*
     * ===== Facturación en Sitio — ver PLAN_FACTURACION_EN_SITIO.md =====
     */

    public void guardarTarifaVigente(TarifaVigenteResponse tarifa) {
        if (tarifa == null || tarifa.periodo == null) return;
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int periodoId = tarifa.periodo.id;

            ContentValues periodoValues = new ContentValues();
            periodoValues.put(DBdefinicionOrdenes.TARIFA_PERIODO.id, periodoId);
            periodoValues.put(DBdefinicionOrdenes.TARIFA_PERIODO.nombre, tarifa.periodo.nombre);
            periodoValues.put(DBdefinicionOrdenes.TARIFA_PERIODO.vigente_desde, tarifa.periodo.vigente_desde);
            periodoValues.put(DBdefinicionOrdenes.TARIFA_PERIODO.vigente_hasta, tarifa.periodo.vigente_hasta);
            db.insertWithOnConflict(DBdefinicionOrdenes.TARIFA_PERIODO.TABLE_NAME, null, periodoValues,
                    SQLiteDatabase.CONFLICT_REPLACE);

            // periodo_lectura es "el ciclo activo actual" (a diferencia de tarifa_periodo, que
            // puede acumular varias resoluciones históricas) — se reemplaza entero en cada sync
            // en vez de acumularse, para no imprimir fechas de un ciclo que ya no está vigente.
            db.delete(DBdefinicionOrdenes.PERIODO_LECTURA.TABLE_NAME, null, null);
            if (tarifa.periodo_lectura != null) {
                TarifaVigenteResponse.PeriodoLecturaDTO pl = tarifa.periodo_lectura;
                ContentValues plValues = new ContentValues();
                plValues.put(DBdefinicionOrdenes.PERIODO_LECTURA.id, pl.id);
                plValues.put(DBdefinicionOrdenes.PERIODO_LECTURA.codigo, pl.codigo);
                plValues.put(DBdefinicionOrdenes.PERIODO_LECTURA.nombre, pl.nombre);
                plValues.put(DBdefinicionOrdenes.PERIODO_LECTURA.fecha_inicio_lectura, pl.fecha_inicio_lectura);
                plValues.put(DBdefinicionOrdenes.PERIODO_LECTURA.fecha_fin_lectura, pl.fecha_fin_lectura);
                plValues.put(DBdefinicionOrdenes.PERIODO_LECTURA.fecha_expedicion, pl.fecha_expedicion);
                plValues.put(DBdefinicionOrdenes.PERIODO_LECTURA.fecha_vencimiento, pl.fecha_vencimiento);
                plValues.put(DBdefinicionOrdenes.PERIODO_LECTURA.fecha_corte, pl.fecha_corte);
                db.insertWithOnConflict(DBdefinicionOrdenes.PERIODO_LECTURA.TABLE_NAME, null, plValues,
                        SQLiteDatabase.CONFLICT_REPLACE);
            }

            if (tarifa.cargos_fijos != null) {
                for (TarifaVigenteResponse.CargoFijoDTO cf : tarifa.cargos_fijos) {
                    ContentValues v = new ContentValues();
                    v.put(DBdefinicionOrdenes.TARIFA_CARGO_FIJO.tarifa_periodo_id, periodoId);
                    v.put(DBdefinicionOrdenes.TARIFA_CARGO_FIJO.servicio, cf.servicio);
                    v.put(DBdefinicionOrdenes.TARIFA_CARGO_FIJO.estrato_id, cf.estrato_id);
                    v.put(DBdefinicionOrdenes.TARIFA_CARGO_FIJO.cargo_fijo, cf.cargo_fijo);
                    db.insertWithOnConflict(DBdefinicionOrdenes.TARIFA_CARGO_FIJO.TABLE_NAME, null, v,
                            SQLiteDatabase.CONFLICT_REPLACE);
                }
            }

            if (tarifa.rangos != null) {
                for (TarifaVigenteResponse.RangoDTO r : tarifa.rangos) {
                    ContentValues v = new ContentValues();
                    v.put(DBdefinicionOrdenes.TARIFA_RANGO.tarifa_periodo_id, periodoId);
                    v.put(DBdefinicionOrdenes.TARIFA_RANGO.servicio, r.servicio);
                    v.put(DBdefinicionOrdenes.TARIFA_RANGO.estrato_id, r.estrato_id);
                    v.put(DBdefinicionOrdenes.TARIFA_RANGO.tipo, r.tipo);
                    v.put(DBdefinicionOrdenes.TARIFA_RANGO.rango_desde, r.rango_desde);
                    if (r.rango_hasta != null) {
                        v.put(DBdefinicionOrdenes.TARIFA_RANGO.rango_hasta, r.rango_hasta);
                    } else {
                        v.putNull(DBdefinicionOrdenes.TARIFA_RANGO.rango_hasta);
                    }
                    v.put(DBdefinicionOrdenes.TARIFA_RANGO.precio_m3, r.precio_m3);
                    db.insertWithOnConflict(DBdefinicionOrdenes.TARIFA_RANGO.TABLE_NAME, null, v,
                            SQLiteDatabase.CONFLICT_REPLACE);
                }
            }

            if (tarifa.estratos != null) {
                for (TarifaVigenteResponse.EstratoDTO e : tarifa.estratos) {
                    ContentValues v = new ContentValues();
                    v.put(DBdefinicionOrdenes.ESTRATO_CACHE.id, e.id);
                    v.put(DBdefinicionOrdenes.ESTRATO_CACHE.numero, e.numero);
                    v.put(DBdefinicionOrdenes.ESTRATO_CACHE.nombre, e.nombre);
                    v.put(DBdefinicionOrdenes.ESTRATO_CACHE.porcentaje_subsidio, e.porcentaje_subsidio);
                    v.put(DBdefinicionOrdenes.ESTRATO_CACHE.subsidio_fijo_acueducto, e.subsidio_fijo_acueducto);
                    v.put(DBdefinicionOrdenes.ESTRATO_CACHE.subsidio_fijo_alcantarillado, e.subsidio_fijo_alcantarillado);
                    v.put(DBdefinicionOrdenes.ESTRATO_CACHE.consumo_minimo_subsidio, e.consumo_minimo_subsidio);
                    v.put(DBdefinicionOrdenes.ESTRATO_CACHE.activo, Boolean.TRUE.equals(e.activo) ? 1 : 0);
                    db.insertWithOnConflict(DBdefinicionOrdenes.ESTRATO_CACHE.TABLE_NAME, null, v,
                            SQLiteDatabase.CONFLICT_REPLACE);
                }
            }
        } finally {
            db.close();
        }
    }

    // Guarda el bloque de numeración asignado al sincronizar la ruta (decisión 5 del plan).
    // Reemplaza cualquier rango previo para el mismo período (PK = periodo).
    public void guardarRangoFacturacion(RangoFacturacionResponse rango) {
        if (rango == null || rango.periodo == null) return;
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues v = new ContentValues();
            v.put(DBdefinicionOrdenes.RANGO_FACTURACION.periodo, rango.periodo);
            v.put(DBdefinicionOrdenes.RANGO_FACTURACION.secuencia_desde, rango.secuencia_desde);
            v.put(DBdefinicionOrdenes.RANGO_FACTURACION.secuencia_hasta, rango.secuencia_hasta);
            v.put(DBdefinicionOrdenes.RANGO_FACTURACION.siguiente, rango.secuencia_desde);
            db.insertWithOnConflict(DBdefinicionOrdenes.RANGO_FACTURACION.TABLE_NAME, null, v,
                    SQLiteDatabase.CONFLICT_REPLACE);
        } finally {
            db.close();
        }
    }

    // true si ya hay un rango de numeración asignado para ese período y todavía no se agotó
    // (siguiente <= secuencia_hasta) — evita pedir un rango nuevo innecesariamente en cada sync.
    public boolean tieneRangoFacturacionVigente(String periodo) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT " + DBdefinicionOrdenes.RANGO_FACTURACION.siguiente + ", " +
                    DBdefinicionOrdenes.RANGO_FACTURACION.secuencia_hasta + " FROM " +
                    DBdefinicionOrdenes.RANGO_FACTURACION.TABLE_NAME + " WHERE " +
                    DBdefinicionOrdenes.RANGO_FACTURACION.periodo + " = ?", new String[]{periodo});
            boolean vigente = false;
            if (cursor.moveToFirst()) {
                vigente = cursor.getInt(0) <= cursor.getInt(1);
            }
            cursor.close();
            return vigente;
        } finally {
            db.close();
        }
    }

    /**
     * Consume y devuelve el siguiente número de secuencia disponible para el período dado,
     * incrementando "siguiente" de forma síncrona (lee+valida+incrementa+persiste en la misma
     * conexión) para que dos facturas seguidas nunca consuman el mismo número.
     * Devuelve null si no hay rango asignado para ese período, o si ya se agotó
     * (fail-safe: no inventa ni reutiliza números — ver Fase 10 del plan).
     */
    public synchronized Integer getYConsumirSiguienteNumeroFactura(String periodo) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT " + DBdefinicionOrdenes.RANGO_FACTURACION.siguiente + ", " +
                    DBdefinicionOrdenes.RANGO_FACTURACION.secuencia_hasta + " FROM " +
                    DBdefinicionOrdenes.RANGO_FACTURACION.TABLE_NAME + " WHERE " +
                    DBdefinicionOrdenes.RANGO_FACTURACION.periodo + " = ?", new String[]{periodo});

            Integer siguiente = null;
            if (cursor.moveToFirst()) {
                int candidato = cursor.getInt(0);
                int hasta = cursor.getInt(1);
                if (candidato <= hasta) {
                    siguiente = candidato;
                }
            }
            cursor.close();

            if (siguiente == null) {
                return null;
            }

            ContentValues v = new ContentValues();
            v.put(DBdefinicionOrdenes.RANGO_FACTURACION.siguiente, siguiente + 1);
            db.update(DBdefinicionOrdenes.RANGO_FACTURACION.TABLE_NAME, v,
                    DBdefinicionOrdenes.RANGO_FACTURACION.periodo + " = ?", new String[]{periodo});

            return siguiente;
        } finally {
            db.close();
        }
    }

    public Long insertFacturaLocal(FacturaLocal f, boolean update) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues v = new ContentValues();
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.numero_factura, f.getNumeroFactura());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.lectura_id, f.getLecturaId());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.suscriptor, f.getSuscriptor());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.periodo, f.getPeriodo());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.lectura_anterior, f.getLecturaAnterior());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.lectura_actual, f.getLecturaActual());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.consumo_m3, f.getConsumoM3());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.estrato_id_usado, f.getEstratoIdUsado());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.tarifa_periodo_id_usado, f.getTarifaPeriodoIdUsado());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.desglose_json, f.getDesgloseJson());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.total_a_pagar, f.getTotalAPagar());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.clasificacion, f.getClasificacion());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.fecha_impresion, f.getFechaImpresion());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.estado, f.getEstado());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.anula_a_id_local, f.getAnulaAIdLocal());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.factura_id_servidor, f.getFacturaIdServidor());
            v.put(DBdefinicionOrdenes.FACTURA_LOCAL.sincronizado, f.isSincronizado() ? 1 : 0);

            if (update) {
                return (long) db.update(DBdefinicionOrdenes.FACTURA_LOCAL.TABLE_NAME, v,
                        DBdefinicionOrdenes.FACTURA_LOCAL.id_local + " = ?", new String[]{f.getIdLocal()});
            } else {
                v.put(DBdefinicionOrdenes.FACTURA_LOCAL.id_local, f.getIdLocal());
                return db.insert(DBdefinicionOrdenes.FACTURA_LOCAL.TABLE_NAME, null, v);
            }
        } finally {
            db.close();
        }
    }

    public void insertFacturaResuelta(FacturaResueltaServidor fr) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues v = new ContentValues();
            v.put(DBdefinicionOrdenes.FACTURA_RESUELTA_SERVIDOR.factura_id, fr.getFacturaId());
            v.put(DBdefinicionOrdenes.FACTURA_RESUELTA_SERVIDOR.numero_factura, fr.getNumeroFactura());
            v.put(DBdefinicionOrdenes.FACTURA_RESUELTA_SERVIDOR.lectura_id, fr.getLecturaId());
            v.put(DBdefinicionOrdenes.FACTURA_RESUELTA_SERVIDOR.suscriptor, fr.getSuscriptor());
            v.put(DBdefinicionOrdenes.FACTURA_RESUELTA_SERVIDOR.desglose_json, fr.getDesgloseJson());
            v.put(DBdefinicionOrdenes.FACTURA_RESUELTA_SERVIDOR.total_a_pagar, fr.getTotalAPagar());
            v.put(DBdefinicionOrdenes.FACTURA_RESUELTA_SERVIDOR.estado, fr.getEstado());
            v.put(DBdefinicionOrdenes.FACTURA_RESUELTA_SERVIDOR.impresa, fr.isImpresa() ? 1 : 0);
            db.insertWithOnConflict(DBdefinicionOrdenes.FACTURA_RESUELTA_SERVIDOR.TABLE_NAME, null, v,
                    SQLiteDatabase.CONFLICT_REPLACE);
        } finally {
            db.close();
        }
    }

}
