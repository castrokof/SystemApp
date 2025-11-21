package com.example.systemapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.systemapp.data.model.LoginRespuesta;


public class SessionPrefs {

    private static SessionPrefs INSTANCE;
    public static final String PREFS_NAME = "SYSTEMAPP_PREFS";
    public static final String PREF_USER_ID = "PREF_USER_ID";
    public static final String PREF_USER_NAME = "PREF_USER_NAME";
    public static final String PREF_USER_FULLNAME = "PREF_USER_FULLNAME";
    public static final String PREF_USER_EMAIL = "PREF_USER_EMAIL";
    public static final String PREF_USER_ESTADO = "PREF_USER_ESTADO";
    public static final String PREF_USER_CLAVE = "PREF_USER_CLAVE";

    public static final String PREF_API_TOKEN = "PREF_API_TOKEN"; // ⭐ NUEVO

    public static final Boolean PREF_RUTAS_SYNC = false;//indica si ya fueron sincronizadas las rutas
    public static final int PREF_RUTAS_PENDIENTES = 0;
    public static final int PREF_RUTAS_REASIGNADAS = 0;
    public static final int PREF_RUTAS_PROCESADAS = 0;
    public static final String PREF_PRINTER_NAME = "PREF_PRINTER_NAME";

    public static final String PREF_PRINTER_ADDRESS= "PREF_PRINTER_ADDRESS";

    private final SharedPreferences mPrefs;
    private boolean mIsLoggedIn = false;

    public static SessionPrefs get(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SessionPrefs(context);
        }
        return INSTANCE;
    }

    private SessionPrefs(Context context) {
        mPrefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        mIsLoggedIn = !TextUtils.isEmpty(mPrefs.getString(PREF_USER_ESTADO, null));
    }

    public boolean isLoggedIn(){
        return mIsLoggedIn;
    }

    public void saveUserPref(LoginRespuesta loginResp, String usuario) {
        if (loginResp != null) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(PREF_USER_ID, loginResp.getId());
            editor.putString(PREF_USER_NAME, usuario);
            editor.putString(PREF_USER_FULLNAME, loginResp.getNombre());
            editor.putString(PREF_USER_EMAIL, loginResp.getEmail());
            editor.putString(PREF_USER_ESTADO, loginResp.getEstado());
            editor.putString(PREF_USER_CLAVE, loginResp.getEstado());
            editor.putString(PREF_API_TOKEN, loginResp.getApiToken()); // ⭐ GUARDAR TOKEN
            editor.apply();

            mIsLoggedIn = true;
        }
    }

    public void logOut(){
        mIsLoggedIn = false;
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_USER_ID, null);
        editor.putString(PREF_USER_NAME, null);
        editor.putString(PREF_USER_FULLNAME, null);
        editor.putString(PREF_USER_EMAIL, null);
        editor.putString(PREF_USER_ESTADO, null);
        editor.putString(PREF_API_TOKEN, null); // ⭐ LIMPIAR TOKEN
        editor.putBoolean("PREF_RUTAS_SYNC", false);
        editor.putString("PREF_RUTAS_PENDIENTES", null);
        editor.putString("PREF_RUTAS_REASIGNADAS", null);
        editor.putString("PREF_RUTAS_PROCESADAS", null);
        editor.putString("PREF_PRINTER_NAME", null);
        editor.putString("PREF_PRINTER_ADDRESS", null);
        editor.apply();
    }

    // ⭐ NUEVO: Método para obtener el token
    public String getApiToken(){
        return mPrefs.getString(PREF_API_TOKEN, null);
    }
    public void setPrefRutasPendientes(int cantrutaspend) {

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt("PREF_RUTAS_PENDIENTES", cantrutaspend);
        editor.apply();

    }

    public void setPrefRutasReasignadas(int cantrutasreasig) {

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt("PREF_RUTAS_REASIGNADAS", cantrutasreasig);
        editor.apply();

    }

    public void setPrefRutasProcesadas(int cantrutasproce) {

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt("PREF_RUTAS_PROCESADAS", cantrutasproce);
        editor.apply();

    }

    public String getUsuario(){
        return mPrefs.getString(PREF_USER_NAME, null);
    }

    public String getClave(){
        return mPrefs.getString(PREF_USER_CLAVE, null);
    }

    public void setPrefPrinterAddress(String mark) {

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("PREF_PRINTER_ADDRESS", mark);
        editor.apply();

    }

    public void setPrefPrinterName(String name) {

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("PREF_PRINTER_NAME", name);
        editor.apply();

    }
}
