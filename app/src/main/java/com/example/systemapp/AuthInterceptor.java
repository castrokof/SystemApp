package com.example.systemapp;

import android.content.Context;

import com.example.systemapp.data.SessionPrefs;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Obtener el token guardado en SharedPreferences
        String token = SessionPrefs.get(context).getApiToken();

        // Si hay token, agregarlo al header Authorization
        if (token != null && !token.isEmpty()) {
            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(newRequest);
        }

        // Si no hay token, continuar con la petición original
        return chain.proceed(originalRequest);
    }
}