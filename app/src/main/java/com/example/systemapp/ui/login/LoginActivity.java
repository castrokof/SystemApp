package com.example.systemapp.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.systemapp.MainActivity;
import com.example.systemapp.R;
import com.example.systemapp.SystemAppAPI;
import com.example.systemapp.data.SessionPrefs;
import com.example.systemapp.data.model.DBListas;
import com.example.systemapp.data.model.LoginEnvio;
import com.example.systemapp.data.model.LoginRespuesta;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {


    private Retrofit systemapp;
    private SystemAppAPI systemappAPI;


    private Button loginButton;
    private EditText usernameEditText, passwordEditText;
    private View loadingProgressBar;
    private RadioButton Rbsesion;

    private  boolean isActivateRadioButton;

    public static SharedPreferences mPrefs;

    public static final String STRING_PREFERENCES = "com.example.systemapp";
    private static final String PREFERENCE_ESTADO_BUTTON_SESION = "estado.button.sesion";
    public static final String PREFERENCE_USUARIO = "PREFERENCE_USUARIO";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);

        if(obtenerEstadoButton()){

            showMainPanelActivity();

        }

        loginButton = findViewById(R.id.login);
        loadingProgressBar = findViewById(R.id.loading);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Rbsesion = findViewById(R.id.Rbsesion);
        isActivateRadioButton = Rbsesion.isChecked();
        loginButton.setEnabled(false);


        taskOnBegining();

        Rbsesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isActivateRadioButton){
                    Rbsesion.setChecked(false);
                }
                isActivateRadioButton = Rbsesion.isChecked();

            }
        });



        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });


    }

    public void guardarEstadoButton(){

        SharedPreferences preferences = getSharedPreferences(STRING_PREFERENCES, MODE_PRIVATE);
        preferences.edit().putBoolean(PREFERENCE_ESTADO_BUTTON_SESION,Rbsesion.isChecked()).apply();
    }

    public boolean obtenerEstadoButton(){

        SharedPreferences preferences = getSharedPreferences(STRING_PREFERENCES, MODE_PRIVATE);
         preferences.getBoolean(PREFERENCE_ESTADO_BUTTON_SESION,false);
        return false;
    }


    private void taskOnBegining() {

        loginButton.setEnabled(true);
        // Crear conexión al servicio REST
        systemapp = new Retrofit.Builder()
                .baseUrl(SystemAppAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        systemappAPI = systemapp.create(SystemAppAPI.class);

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


               if (!isOnline()) {
                    showLoginError(getString(R.string.error_network));
                    return;
                }

                attemptLogin();

            }
        });
    }

    private void showLoginFailed(String errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void attemptLogin() {

        // Reset errors.
        usernameEditText.setError(null);
        passwordEditText.setError(null);

        // Store values at the time of the login attempt.
        String usuario = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_field_required));
            focusView = passwordEditText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(usuario)) {
            usernameEditText.setError(getString(R.string.error_field_required));
            focusView = usernameEditText;
            cancel = true;
        }

        showProgress(true);

        Call<List<LoginRespuesta>> getUsers = systemappAPI.login(new LoginEnvio(usuario,password));

        getUsers.enqueue(new Callback<List<LoginRespuesta>>() {
            @Override
            public void onResponse(Call<List<LoginRespuesta>> call, Response<List<LoginRespuesta>> response) {

                guardarEstadoButton();

                showProgress(false);

                //Procesar errores
                String error = "";
                if (!response.isSuccessful()){

                    if (response.errorBody()
                            .contentType()
                            .subtype()
                            .equals("application/json")){
                       error = response.message();
                    }else{
                        error = response.message();
                    }
                    showLoginFailed(error);
                    return;
                }

                List<LoginRespuesta> loginResp = response.body();
                // Guardar afiliado en preferencias


                if (loginResp.size()>0){

                    for (LoginRespuesta elemento : loginResp) {
                        SharedPreferences mPrefs = getSharedPreferences("SYSTEMAPP_PREFS", Context.MODE_PRIVATE);
                        SessionPrefs.get(LoginActivity.this).saveUserPref(elemento, usuario);
                        SharedPreferences mPrefs1 = getSharedPreferences(STRING_PREFERENCES, MODE_PRIVATE);
                        mPrefs1.edit().putString(PREFERENCE_USUARIO,elemento.getUsuario()).apply();
                    }

                }



                showMainPanelActivity();

            }

            @Override
            public void onFailure(Call<List<LoginRespuesta>> call, Throwable t) {
                showProgress(false);
                showLoginError(t.getMessage());
            }
        });

    }

    private void showProgress(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);


    }

    private void showLoginError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    //función para verificar la disponibilidad de la red
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void showMainPanelActivity(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }



}