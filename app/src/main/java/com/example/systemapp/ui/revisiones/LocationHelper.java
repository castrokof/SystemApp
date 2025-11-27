package com.example.systemapp.ui.revisiones;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Helper para captura de ubicación GPS
 * Permite registrar la ubicación exacta donde se realizó la revisión
 */
public class LocationHelper {

    public static final int REQUEST_LOCATION_PERMISSION = 200;

    private Context context;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastKnownLocation;

    public interface LocationCallback {
        void onLocationReceived(Location location);
        void onLocationError(String error);
    }

    public LocationHelper(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Verificar si tiene permisos de ubicación
     */
    public boolean hasPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Solicitar permisos de ubicación
     */
    public void requestPermission(Activity activity) {
        ActivityCompat.requestPermissions(
            activity,
            new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            },
            REQUEST_LOCATION_PERMISSION
        );
    }

    /**
     * Obtener ubicación actual
     */
    public void getCurrentLocation(LocationCallback callback) {
        if (!hasPermission()) {
            callback.onLocationError("Permiso de ubicación no concedido");
            return;
        }

        try {
            // Verificar si GPS está habilitado
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                callback.onLocationError("GPS y red deshabilitados. Active la ubicación en configuración.");
                return;
            }

            // Intentar obtener última ubicación conocida
            Location gpsLocation = null;
            Location networkLocation = null;

            if (isGPSEnabled) {
                gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            if (isNetworkEnabled) {
                networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            // Usar la más reciente y precisa
            if (gpsLocation != null && networkLocation != null) {
                lastKnownLocation = gpsLocation.getAccuracy() < networkLocation.getAccuracy()
                    ? gpsLocation : networkLocation;
            } else if (gpsLocation != null) {
                lastKnownLocation = gpsLocation;
            } else if (networkLocation != null) {
                lastKnownLocation = networkLocation;
            }

            if (lastKnownLocation != null) {
                callback.onLocationReceived(lastKnownLocation);
                return;
            }

            // Si no hay ubicación conocida, solicitar actualización
            requestLocationUpdate(callback);

        } catch (SecurityException e) {
            callback.onLocationError("Error de permisos: " + e.getMessage());
        }
    }

    /**
     * Solicitar actualización de ubicación
     */
    private void requestLocationUpdate(LocationCallback callback) {
        try {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    lastKnownLocation = location;
                    callback.onLocationReceived(location);
                    stopLocationUpdates();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {
                    callback.onLocationError("Proveedor de ubicación deshabilitado: " + provider);
                }
            };

            // Solicitar actualizaciones de ambos proveedores
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0,
                    locationListener
                );
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0,
                    0,
                    locationListener
                );
            }

            // Timeout de 10 segundos
            new android.os.Handler().postDelayed(() -> {
                if (lastKnownLocation == null) {
                    stopLocationUpdates();
                    callback.onLocationError("Tiempo de espera agotado. No se pudo obtener ubicación.");
                }
            }, 10000);

        } catch (SecurityException e) {
            callback.onLocationError("Error de permisos: " + e.getMessage());
        }
    }

    /**
     * Detener actualizaciones de ubicación
     */
    public void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Obtener última ubicación conocida
     */
    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    /**
     * Formatear coordenadas para mostrar
     */
    public static String formatCoordinates(Location location) {
        if (location == null) return "Sin ubicación";

        return String.format("Lat: %.6f, Lng: %.6f",
            location.getLatitude(),
            location.getLongitude());
    }

    /**
     * Formatear coordenadas con precisión
     */
    public static String formatCoordinatesWithAccuracy(Location location) {
        if (location == null) return "Sin ubicación";

        return String.format("Lat: %.6f, Lng: %.6f (±%.1fm)",
            location.getLatitude(),
            location.getLongitude(),
            location.getAccuracy());
    }

    /**
     * Obtener URL de Google Maps
     */
    public static String getGoogleMapsUrl(Location location) {
        if (location == null) return "";

        return String.format("https://www.google.com/maps?q=%.6f,%.6f",
            location.getLatitude(),
            location.getLongitude());
    }
}
