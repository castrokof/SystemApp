package com.example.systemapp.ui.revisiones;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Helper para manejo de cámara y fotos
 */
public class CameraHelper {

    public static final int REQUEST_CAMERA_PERMISSION = 100;
    public static final int REQUEST_IMAGE_CAPTURE = 101;

    private Context context;
    private String currentPhotoPath;

    public CameraHelper(Context context) {
        this.context = context;
    }

    /**
     * Verificar si tiene permiso de cámara
     */
    public boolean hasPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Solicitar permiso de cámara
     */
    public void requestPermission(Activity activity) {
        ActivityCompat.requestPermissions(
            activity,
            new String[]{Manifest.permission.CAMERA},
            REQUEST_CAMERA_PERMISSION
        );
    }

    /**
     * Abrir cámara para capturar foto
     */
    public Intent createCameraIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Crear archivo para guardar la foto
        File photoFile = createImageFile();

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                photoFile
            );
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        }

        return takePictureIntent;
    }

    /**
     * Crear archivo de imagen temporal
     */
    private File createImageFile() throws IOException {
        // Timestamp para nombre único
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Directorio de fotos de la app
        File storageDir = new File(context.getFilesDir(), "fotos");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        );

        // Guardar path para uso posterior
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Obtener path de la última foto capturada
     */
    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    /**
     * Guardar bitmap como archivo
     */
    public String saveBitmap(Bitmap bitmap, String prefix) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = prefix + "_" + timeStamp + ".jpg";

        File storageDir = new File(context.getFilesDir(), "fotos");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File photoFile = new File(storageDir, fileName);
        FileOutputStream fos = new FileOutputStream(photoFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        fos.close();

        return photoFile.getAbsolutePath();
    }

    /**
     * Crear archivo para foto de elemento de censo
     */
    public File createCensoPhotoFile(String revisionId, String elemento) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "censo_" + revisionId + "_" + elemento + "_" + timeStamp + ".jpg";

        File storageDir = new File(context.getFilesDir(), "fotos");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File photoFile = new File(storageDir, fileName);
        currentPhotoPath = photoFile.getAbsolutePath();

        return photoFile;
    }

    /**
     * Crear archivo para foto general de revisión
     */
    public File createRevisionPhotoFile(String revisionId, int tabNumber) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "revision_" + revisionId + "_tab" + tabNumber + "_" + timeStamp + ".jpg";

        File storageDir = new File(context.getFilesDir(), "fotos");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File photoFile = new File(storageDir, fileName);
        currentPhotoPath = photoFile.getAbsolutePath();

        return photoFile;
    }
}
