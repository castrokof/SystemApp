package com.example.systemapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.systemapp.data.model.DBdefinicionOrdenes;
import com.example.systemapp.data.model.DBListas;
import com.example.systemapp.data.model.DBOrdenLecturas;

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

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (newVersion > oldVersion) {
            //El método onUpgrade se ejecuta cada vez que recompilamos e instalamos la app con un
            //nuevo numero de version de base de datos (DataCollectorDBDef.DATABASE_VERSION), de tal mamera que en este
            // método lo que hacemos es:
            // 1. Con esta sentencia borramos la tabla "ordenes"
            db.execSQL(DBdefinicionOrdenes.ORDENES_TABLE_DROP);
            // 1.1 Con esta sentencia borramos la tabla "listas"
            db.execSQL(DBdefinicionOrdenes.LISTAS_TABLE_DROP);

            // 2. Con esta sentencia llamamos de nuevo al método onCreate para que se cree de nuevo
            //la tabla "ordenes" la cual seguramente al cambair de versión ha tenido modifciaciones
            // en cuanto a su estructura de columnas
            this.onCreate(db);
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
            values.put(DBdefinicionOrdenes.LECTURAS.cservic, "0040");
            values.put(DBdefinicionOrdenes.LECTURAS.nservic, "ACUEDUCTO");
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



}
