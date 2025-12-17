package com.example.systemapp;

import android.content.SharedPreferences;

import com.example.systemapp.data.Constants;
import com.example.systemapp.data.model.DBListas;
import com.example.systemapp.data.model.DBOrdenLecturas;
import com.example.systemapp.data.model.DBOrdenLecturasEnviar;
import com.example.systemapp.data.model.EnviarRespuesta;
import com.example.systemapp.data.model.LoginEnvio;
import com.example.systemapp.data.model.LoginRespuesta;
import com.example.systemapp.data.model.OrdenesPares;


import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;

import retrofit2.http.Header;
import retrofit2.http.Headers;

import retrofit2.http.POST;


public interface SystemAppAPI{

    public static final String BASE_URL = Constants.BASE_URL;;


    @POST("loginMovil1")
    Call<List<LoginRespuesta>> login(@Body LoginEnvio loginEnvio);



    @POST("medidoresout")
    Call<List<DBOrdenLecturas>> cargue();



    @POST("marcas")
    Call<List<DBListas>> listas();


    @POST("medidores")
     Call<Object> enviarordenes(@Body DBOrdenLecturasEnviar lecturas);

}
