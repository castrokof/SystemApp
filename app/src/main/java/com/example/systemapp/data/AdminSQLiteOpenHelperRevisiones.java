package com.example.systemapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.systemapp.data.model.DBCensoHidraulico;
import com.example.systemapp.data.model.DBCausaDesviacion;
import com.example.systemapp.data.model.DBFotoRevision;
import com.example.systemapp.data.model.DBOrdenRevision;
import com.example.systemapp.data.model.DBdefinicionRevisiones;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper de base de datos SQLite para el módulo de REVISIONES
 * Gestiona: revisiones, censo_hidraulico, fotos_revision, causas_desviacion
 */
public class AdminSQLiteOpenHelperRevisiones extends SQLiteOpenHelper {

    public AdminSQLiteOpenHelperRevisiones(Context context) {
        super(context, DBdefinicionRevisiones.DATABASE_NAME, null, DBdefinicionRevisiones.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear todas las tablas del módulo de revisiones
        db.execSQL(DBdefinicionRevisiones.REVISIONES.TABLE_CREATE);
        db.execSQL(DBdefinicionRevisiones.CENSO_HIDRAULICO.TABLE_CREATE);
        db.execSQL(DBdefinicionRevisiones.FOTOS_REVISION.TABLE_CREATE);
        db.execSQL(DBdefinicionRevisiones.CAUSAS_DESVIACION.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL(DBdefinicionRevisiones.REVISIONES.TABLE_DROP);
            db.execSQL(DBdefinicionRevisiones.CENSO_HIDRAULICO.TABLE_DROP);
            db.execSQL(DBdefinicionRevisiones.FOTOS_REVISION.TABLE_DROP);
            db.execSQL(DBdefinicionRevisiones.CAUSAS_DESVIACION.TABLE_DROP);
            this.onCreate(db);
        }
    }

    // ========== OPERACIONES CRUD PARA REVISIONES ==========

    /**
     * Insertar o actualizar revisión
     */
    public Long insertOrUpdateRevision(DBOrdenRevision revision, boolean update) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBdefinicionRevisiones.REVISIONES.id, revision.getId());
        values.put(DBdefinicionRevisiones.REVISIONES.Ciclo, revision.getCiclo());
        values.put(DBdefinicionRevisiones.REVISIONES.Categoria_orden, revision.getCategoria_orden());
        values.put(DBdefinicionRevisiones.REVISIONES.Tipo_orden, revision.getTipo_orden());
        values.put(DBdefinicionRevisiones.REVISIONES.Periodo, revision.getPeriodo());
        values.put(DBdefinicionRevisiones.REVISIONES.Suscriptor, revision.getSuscriptor());
        values.put(DBdefinicionRevisiones.REVISIONES.Ref_Medidor, revision.getRef_Medidor());
        values.put(DBdefinicionRevisiones.REVISIONES.Direccion, revision.getDireccion());
        values.put(DBdefinicionRevisiones.REVISIONES.Nombre, revision.getNombre());
        values.put(DBdefinicionRevisiones.REVISIONES.Apell, revision.getApell());
        values.put(DBdefinicionRevisiones.REVISIONES.Promedio, revision.getPromedio());
        values.put(DBdefinicionRevisiones.REVISIONES.LA, revision.getLA());
        values.put(DBdefinicionRevisiones.REVISIONES.Usuario, revision.getUsuario());
        values.put(DBdefinicionRevisiones.REVISIONES.Estado, revision.getEstado());
        values.put(DBdefinicionRevisiones.REVISIONES.Tipo_desviacion, revision.getTipo_desviacion());
        values.put(DBdefinicionRevisiones.REVISIONES.Ruta, revision.getRuta());
        values.put(DBdefinicionRevisiones.REVISIONES.consecutivoRuta, revision.getConsecutivoRuta());
        values.put(DBdefinicionRevisiones.REVISIONES.observacion_inicial, revision.getObservacion_inicial());
        values.put(DBdefinicionRevisiones.REVISIONES.lectura_actual, revision.getLectura_actual());
        values.put(DBdefinicionRevisiones.REVISIONES.consumo, revision.getConsumo());
        values.put(DBdefinicionRevisiones.REVISIONES.nombre_residente, revision.getNombre_residente());
        values.put(DBdefinicionRevisiones.REVISIONES.firma_path, revision.getFirma_path());
        values.put(DBdefinicionRevisiones.REVISIONES.estado_acometida, revision.getEstado_acometida());
        values.put(DBdefinicionRevisiones.REVISIONES.estado_sellos, revision.getEstado_sellos());
        values.put(DBdefinicionRevisiones.REVISIONES.que_surte, revision.getQue_surte());
        values.put(DBdefinicionRevisiones.REVISIONES.censo_poblacional_familiar, revision.getCenso_poblacional_familiar());
        values.put(DBdefinicionRevisiones.REVISIONES.censo_poblacional_personas, revision.getCenso_poblacional_personas());
        values.put(DBdefinicionRevisiones.REVISIONES.censo_poblacional_adultos, revision.getCenso_poblacional_adultos());
        values.put(DBdefinicionRevisiones.REVISIONES.censo_poblacional_ninos, revision.getCenso_poblacional_ninos());
        values.put(DBdefinicionRevisiones.REVISIONES.codigo_causa, revision.getCodigo_causa());
        values.put(DBdefinicionRevisiones.REVISIONES.desc_causa, revision.getDesc_causa());
        values.put(DBdefinicionRevisiones.REVISIONES.observacion_causa, revision.getObservacion_causa());
        values.put(DBdefinicionRevisiones.REVISIONES.observacion_general, revision.getObservacion_general());
        values.put(DBdefinicionRevisiones.REVISIONES.fecha_inicio, revision.getFecha_inicio());
        values.put(DBdefinicionRevisiones.REVISIONES.fecha_cierre, revision.getFecha_cierre());
        values.put(DBdefinicionRevisiones.REVISIONES.cantidad_modificaciones, revision.getCantidad_modificaciones());
        values.put(DBdefinicionRevisiones.REVISIONES.orden_personalizado, revision.getOrden_personalizado());
        values.put(DBdefinicionRevisiones.REVISIONES.latitud, revision.getLatitud());
        values.put(DBdefinicionRevisiones.REVISIONES.longitud, revision.getLongitud());
        values.put(DBdefinicionRevisiones.REVISIONES.ruta_pdf, revision.getRuta_pdf());
        values.put(DBdefinicionRevisiones.REVISIONES.enviado_api, revision.getEnviado_api());

        Long result;
        if (update) {
            result = (long) db.update(DBdefinicionRevisiones.REVISIONES.TABLE_NAME,
                    values,
                    DBdefinicionRevisiones.REVISIONES.id + " = ?",
                    new String[]{revision.getId()});
        } else {
            result = db.insert(DBdefinicionRevisiones.REVISIONES.TABLE_NAME, null, values);
        }

        db.close();
        return result;
    }

    /**
     * Obtener revisiones con condición
     */
    public List<DBOrdenRevision> getRevisiones(String condition) {
        List<DBOrdenRevision> revisiones = new ArrayList<>();

        String query = "SELECT * FROM " + DBdefinicionRevisiones.REVISIONES.TABLE_NAME;
        if (condition != null && !condition.isEmpty()) {
            query += " WHERE " + condition;
        }
        query += " ORDER BY " + DBdefinicionRevisiones.REVISIONES.orden_personalizado + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                DBOrdenRevision revision = new DBOrdenRevision();
                revision.setId(cursor.getString(0));
                revision.setCiclo(cursor.getString(1));
                revision.setCategoria_orden(cursor.getString(2));
                revision.setTipo_orden(cursor.getString(3));
                revision.setPeriodo(cursor.getString(4));
                revision.setSuscriptor(cursor.getString(5));
                revision.setRef_Medidor(cursor.getString(6));
                revision.setDireccion(cursor.getString(7));
                revision.setNombre(cursor.getString(8));
                revision.setApell(cursor.getString(9));
                revision.setPromedio(cursor.getInt(10));
                revision.setLA(cursor.getString(11));
                revision.setUsuario(cursor.getString(12));
                revision.setEstado(cursor.getString(13));
                revision.setTipo_desviacion(cursor.getString(14));
                revision.setRuta(cursor.getString(15));
                revision.setConsecutivoRuta(cursor.getString(16));
                revision.setObservacion_inicial(cursor.getString(17));
                revision.setLectura_actual(cursor.getInt(18));
                revision.setConsumo(cursor.getInt(19));
                revision.setNombre_residente(cursor.getString(20));
                revision.setFirma_path(cursor.getString(21));
                revision.setEstado_acometida(cursor.getString(22));
                revision.setEstado_sellos(cursor.getString(23));
                revision.setQue_surte(cursor.getString(24));
                revision.setCenso_poblacional_familiar(cursor.getInt(25));
                revision.setCenso_poblacional_personas(cursor.getInt(26));
                revision.setCenso_poblacional_adultos(cursor.getInt(27));
                revision.setCenso_poblacional_ninos(cursor.getInt(28));
                revision.setCodigo_causa(cursor.getInt(29));
                revision.setDesc_causa(cursor.getString(30));
                revision.setObservacion_causa(cursor.getString(31));
                revision.setObservacion_general(cursor.getString(32));
                revision.setFecha_inicio(cursor.getString(33));
                revision.setFecha_cierre(cursor.getString(34));
                revision.setCantidad_modificaciones(cursor.getInt(35));
                revision.setOrden_personalizado(cursor.getInt(36));
                revision.setLatitud(cursor.getString(37));
                revision.setLongitud(cursor.getString(38));
                revision.setRuta_pdf(cursor.getString(39));
                revision.setEnviado_api(cursor.getString(40));

                revisiones.add(revision);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return revisiones;
    }

    /**
     * Obtener revisiones por estado
     */
    public List<DBOrdenRevision> getRevisionesByEstado(String estado) {
        String condition = DBdefinicionRevisiones.REVISIONES.Estado + " = '" + estado + "'";
        return getRevisiones(condition);
    }

    /**
     * Contar revisiones con condición
     */
    public int getCountRevisiones(String condition) {
        String query = "SELECT COUNT(*) FROM " + DBdefinicionRevisiones.REVISIONES.TABLE_NAME;
        if (condition != null && !condition.isEmpty()) {
            query += " WHERE " + condition;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();

        return count;
    }

    /**
     * Borrar todas las revisiones
     */
    public void deleteAllRevisiones() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DBdefinicionRevisiones.REVISIONES.TABLE_NAME, null, null);
        db.delete(DBdefinicionRevisiones.CENSO_HIDRAULICO.TABLE_NAME, null, null);
        db.delete(DBdefinicionRevisiones.FOTOS_REVISION.TABLE_NAME, null, null);
        db.close();
    }

    // ========== OPERACIONES PARA CENSO HIDRÁULICO ==========

    public Long insertCensoHidraulico(DBCensoHidraulico censo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBdefinicionRevisiones.CENSO_HIDRAULICO.revision_id, censo.getRevision_id());
        values.put(DBdefinicionRevisiones.CENSO_HIDRAULICO.elemento, censo.getElemento());
        values.put(DBdefinicionRevisiones.CENSO_HIDRAULICO.cantidad, censo.getCantidad());
        values.put(DBdefinicionRevisiones.CENSO_HIDRAULICO.estado, censo.getEstado());
        values.put(DBdefinicionRevisiones.CENSO_HIDRAULICO.foto_path, censo.getFoto_path());

        Long result = db.insert(DBdefinicionRevisiones.CENSO_HIDRAULICO.TABLE_NAME, null, values);
        db.close();
        return result;
    }

    public List<DBCensoHidraulico> getCensosByRevisionId(String revisionId) {
        List<DBCensoHidraulico> censos = new ArrayList<>();

        String query = "SELECT * FROM " + DBdefinicionRevisiones.CENSO_HIDRAULICO.TABLE_NAME +
                " WHERE " + DBdefinicionRevisiones.CENSO_HIDRAULICO.revision_id + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{revisionId});

        if (cursor.moveToFirst()) {
            do {
                DBCensoHidraulico censo = new DBCensoHidraulico();
                censo.setId(cursor.getInt(0));
                censo.setRevision_id(cursor.getString(1));
                censo.setElemento(cursor.getString(2));
                censo.setCantidad(cursor.getInt(3));
                censo.setEstado(cursor.getString(4));
                censo.setFoto_path(cursor.getString(5));
                censos.add(censo);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return censos;
    }

    // ========== OPERACIONES PARA CAUSAS DE DESVIACIÓN ==========

    public Long insertCausaDesviacion(DBCausaDesviacion causa) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBdefinicionRevisiones.CAUSAS_DESVIACION.codigo, causa.getCodigo());
        values.put(DBdefinicionRevisiones.CAUSAS_DESVIACION.tipo, causa.getTipo());
        values.put(DBdefinicionRevisiones.CAUSAS_DESVIACION.descripcion, causa.getDescripcion());

        Long result = db.insert(DBdefinicionRevisiones.CAUSAS_DESVIACION.TABLE_NAME, null, values);
        db.close();
        return result;
    }

    public List<DBCausaDesviacion> getCausas(String tipo) {
        List<DBCausaDesviacion> causas = new ArrayList<>();

        String query = "SELECT * FROM " + DBdefinicionRevisiones.CAUSAS_DESVIACION.TABLE_NAME;
        if (tipo != null && !tipo.isEmpty()) {
            query += " WHERE " + DBdefinicionRevisiones.CAUSAS_DESVIACION.tipo + " = ?";
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        if (tipo != null && !tipo.isEmpty()) {
            cursor = db.rawQuery(query, new String[]{tipo});
        } else {
            cursor = db.rawQuery(query, null);
        }

        if (cursor.moveToFirst()) {
            do {
                DBCausaDesviacion causa = new DBCausaDesviacion();
                causa.setCodigo(cursor.getInt(0));
                causa.setTipo(cursor.getString(1));
                causa.setDescripcion(cursor.getString(2));
                causas.add(causa);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return causas;
    }
}
