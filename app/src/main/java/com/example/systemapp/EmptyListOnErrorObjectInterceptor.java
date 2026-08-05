package com.example.systemapp;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

// El backend, cuando "medidoresout" no tiene resultados, responde HTTP 200 con un body
// {"error": "..."} (objeto) en vez del array vacío que Retrofit/Gson espera para
// Call<List<DBOrdenLecturas>> (ver CONTEXTO_BACKEND.md / CLAUDE.md: "respuestas sin
// resultados llegan como HTTP 200 con {\"error\": ...}"). Gson revienta al convertir ese
// objeto a lista ("Expected BEGIN_ARRAY but was BEGIN_OBJECT"), y el error crudo terminaba
// en el Toast del usuario. Este interceptor normaliza esa respuesta puntual a un array
// vacío antes de que el converter de Gson la vea.
public class EmptyListOnErrorObjectInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        String path = request.url().encodedPath();
        boolean esperaArray = path.contains("medidoresout");
        if (!esperaArray || !response.isSuccessful() || response.body() == null) {
            return response;
        }

        String bodyString = response.body().string();
        MediaType contentType = response.body().contentType();
        String trimmed = bodyString.trim();

        String nuevoContenido = trimmed.startsWith("{") ? "[]" : bodyString;
        return response.newBuilder()
                .body(ResponseBody.create(contentType, nuevoContenido))
                .build();
    }
}
