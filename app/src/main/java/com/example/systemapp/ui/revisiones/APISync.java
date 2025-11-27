package com.example.systemapp.ui.revisiones;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.example.systemapp.data.AdminSQLiteOpenHelperRevisiones;
import com.example.systemapp.data.ApiConfig;
import com.example.systemapp.data.SessionPrefs;
import com.example.systemapp.data.model.DBCensoHidraulico;
import com.example.systemapp.data.model.DBOrdenRevision;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Sincronización de revisiones con API
 */
public class APISync {

    private Context context;
    private AdminSQLiteOpenHelperRevisiones dbHelper;
    private String apiToken;

    public APISync(Context context) {
        this.context = context;
        this.dbHelper = new AdminSQLiteOpenHelperRevisiones(context);
        this.apiToken = SessionPrefs.get(context).getTokenApi();
    }

    /**
     * Descargar órdenes de revisión desde la API
     */
    public int descargarOrdenes() throws Exception {
        String endpoint = ApiConfig.buildUrl(context, "/revisiones/ordenes");

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiToken);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parsear respuesta JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
            boolean success = jsonResponse.getBoolean("success");

            if (success) {
                JSONArray ordenes = jsonResponse.getJSONArray("data");
                int contador = 0;

                for (int i = 0; i < ordenes.length(); i++) {
                    JSONObject ordenJson = ordenes.getJSONObject(i);

                    DBOrdenRevision orden = new DBOrdenRevision();
                    orden.setId(ordenJson.getString("id"));
                    orden.setCiclo(ordenJson.optString("ciclo"));
                    orden.setCategoria_orden(ordenJson.optString("categoria_orden"));
                    orden.setTipo_orden(ordenJson.optString("tipo_orden"));
                    orden.setPeriodo(ordenJson.optString("periodo"));
                    orden.setSuscriptor(ordenJson.optString("suscriptor"));
                    orden.setRef_Medidor(ordenJson.getString("medidor"));
                    orden.setDireccion(ordenJson.optString("direccion"));
                    orden.setNombre(ordenJson.optString("nombre"));
                    orden.setApell(ordenJson.optString("apellido"));
                    orden.setPromedio(ordenJson.optInt("consumo_promedio_6_meses"));
                    orden.setLA(ordenJson.optString("lectura_anterior"));
                    orden.setUsuario(SessionPrefs.get(context).getUsuario());
                    orden.setEstado("PENDIENTE");
                    orden.setTipo_desviacion(ordenJson.optString("tipo_desviacion"));
                    orden.setRuta(ordenJson.optString("ruta"));
                    orden.setConsecutivoRuta(ordenJson.optString("consecutivo_ruta"));
                    orden.setObservacion_inicial(ordenJson.optString("observacion_inicial"));
                    orden.setOrden_personalizado(i + 1); // Orden según viene de la API

                    // Guardar en BD
                    dbHelper.insertOrUpdateRevision(orden, false);
                    contador++;
                }

                // Descargar firma del técnico si no está en caché
                String usuarioTecnico = SessionPrefs.get(context).getUsuario();
                File firmaTecnicoLocal = getFirmaTecnicoLocal(usuarioTecnico);

                if (firmaTecnicoLocal == null) {
                    // Intentar descargar firma del técnico
                    try {
                        descargarFirmaTecnico(usuarioTecnico);
                    } catch (Exception e) {
                        // Si falla, continuar (la firma no es crítica)
                        e.printStackTrace();
                    }
                }

                return contador;
            } else {
                throw new Exception(jsonResponse.optString("message", "Error desconocido"));
            }
        } else {
            throw new Exception("Error HTTP: " + responseCode);
        }
    }

    /**
     * Enviar revisión completada a la API
     */
    public boolean enviarRevision(DBOrdenRevision orden) throws Exception {
        String endpoint = ApiConfig.buildUrl(context, "/revisiones/enviar");

        // Construir JSON
        JSONObject jsonRevision = new JSONObject();
        jsonRevision.put("id", orden.getId());
        jsonRevision.put("medidor", orden.getRef_Medidor());
        jsonRevision.put("lectura_actual", orden.getLectura_actual());
        jsonRevision.put("consumo", orden.getConsumo());
        jsonRevision.put("nombre_residente", orden.getNombre_residente());

        // Firma del cliente en Base64
        if (orden.getFirma_path() != null && !orden.getFirma_path().isEmpty()) {
            String firmaBase64 = convertirImagenABase64(orden.getFirma_path());
            jsonRevision.put("firma_cliente_base64", firmaBase64);
        }

        jsonRevision.put("estado_acometida", orden.getEstado_acometida());
        jsonRevision.put("estado_sellos", orden.getEstado_sellos());
        jsonRevision.put("que_surte", orden.getQue_surte());

        // Censos
        jsonRevision.put("censo_poblacional_familiar", orden.getCenso_poblacional_familiar());
        jsonRevision.put("censo_poblacional_personas", orden.getCenso_poblacional_personas());
        jsonRevision.put("censo_poblacional_adultos", orden.getCenso_poblacional_adultos());
        jsonRevision.put("censo_poblacional_ninos", orden.getCenso_poblacional_ninos());

        // Censo hidráulico
        List<DBCensoHidraulico> censos = dbHelper.getCensosByRevisionId(orden.getId());
        if (censos != null && !censos.isEmpty()) {
            JSONArray censosArray = new JSONArray();
            for (DBCensoHidraulico censo : censos) {
                JSONObject censoJson = new JSONObject();
                censoJson.put("elemento", censo.getElemento());
                censoJson.put("cantidad", censo.getCantidad());
                censoJson.put("estado", censo.getEstado());

                // Foto del elemento en Base64
                if (censo.getFoto_path() != null && !censo.getFoto_path().isEmpty()) {
                    String fotoBase64 = convertirImagenABase64(censo.getFoto_path());
                    censoJson.put("foto_base64", fotoBase64);
                }

                censosArray.put(censoJson);
            }
            jsonRevision.put("censo_hidraulico", censosArray);
        }

        // Clasificación
        jsonRevision.put("codigo_causa", orden.getCodigo_causa());
        jsonRevision.put("desc_causa", orden.getDesc_causa());
        jsonRevision.put("observacion_causa", orden.getObservacion_causa());

        // Observación general
        jsonRevision.put("observacion_general", orden.getObservacion_general());

        // Fechas
        jsonRevision.put("fecha_inicio", orden.getFecha_inicio());
        jsonRevision.put("fecha_cierre", orden.getFecha_cierre());

        // Ubicación GPS
        jsonRevision.put("latitud", orden.getLatitud());
        jsonRevision.put("longitud", orden.getLongitud());

        // PDF en Base64
        if (orden.getRuta_pdf() != null && !orden.getRuta_pdf().isEmpty()) {
            String pdfBase64 = convertirPDFABase64(orden.getRuta_pdf());
            jsonRevision.put("pdf_base64", pdfBase64);
        }

        // Enviar request
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiToken);
        conn.setDoOutput(true);
        conn.setConnectTimeout(60000); // 60 segundos para archivos grandes
        conn.setReadTimeout(60000);

        OutputStream os = conn.getOutputStream();
        os.write(jsonRevision.toString().getBytes("UTF-8"));
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            boolean success = jsonResponse.getBoolean("success");

            if (success) {
                // Marcar como enviada
                orden.setEnviado_api("SI");
                orden.setEstado("PROCESADA");
                dbHelper.insertOrUpdateRevision(orden, true);
                return true;
            } else {
                throw new Exception(jsonResponse.optString("message", "Error al enviar"));
            }
        } else {
            throw new Exception("Error HTTP: " + responseCode);
        }
    }

    /**
     * Convertir imagen a Base64
     */
    private String convertirImagenABase64(String imagePath) throws Exception {
        File imageFile = new File(imagePath);
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
        byte[] imageBytes = baos.toByteArray();

        bitmap.recycle();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    /**
     * Convertir PDF a Base64
     */
    private String convertirPDFABase64(String pdfPath) throws Exception {
        File pdfFile = new File(pdfPath);
        FileInputStream fis = new FileInputStream(pdfFile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }

        fis.close();
        byte[] pdfBytes = baos.toByteArray();

        return Base64.encodeToString(pdfBytes, Base64.DEFAULT);
    }

    /**
     * Descargar firma del técnico desde la API
     */
    public File descargarFirmaTecnico(String usuarioTecnico) throws Exception {
        String endpoint = ApiConfig.buildUrl(context, "/usuarios/" + usuarioTecnico + "/firma");

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + apiToken);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parsear respuesta JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
            boolean success = jsonResponse.getBoolean("success");

            if (success) {
                String firmaBase64 = jsonResponse.getString("firma_base64");

                // Decodificar Base64 y guardar como archivo
                byte[] firmaBytes = Base64.decode(firmaBase64, Base64.DEFAULT);

                // Crear directorio de firmas si no existe
                File firmasDir = new File(context.getFilesDir(), "firmas");
                if (!firmasDir.exists()) {
                    firmasDir.mkdirs();
                }

                // Guardar firma del técnico
                File firmaFile = new File(firmasDir, "firma_tecnico_" + usuarioTecnico + ".png");
                java.io.FileOutputStream fos = new java.io.FileOutputStream(firmaFile);
                fos.write(firmaBytes);
                fos.close();

                return firmaFile;
            } else {
                throw new Exception(jsonResponse.optString("message", "Firma no encontrada"));
            }
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            // No tiene firma registrada
            return null;
        } else {
            throw new Exception("Error HTTP: " + responseCode);
        }
    }

    /**
     * Verificar si existe firma del técnico en caché
     */
    public File getFirmaTecnicoLocal(String usuarioTecnico) {
        File firmaFile = new File(context.getFilesDir(), "firmas/firma_tecnico_" + usuarioTecnico + ".png");
        if (firmaFile.exists()) {
            return firmaFile;
        }
        return null;
    }
}
