package com.example.systemapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.systemapp.data.SessionPrefs;
import com.example.systemapp.databinding.NavHeaderMainBinding;
import com.example.systemapp.ui.fragment_ordenes;
import com.example.systemapp.ui.login.LoginActivity;
import com.example.systemapp.ui.sync.sync.fragment_sync;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.systemapp.databinding.ActivityMainBinding;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {



    private DrawerLayout drawer;
    private SwitchMaterial switchCerrarSesion;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FragmentManager mFragmentManager;
    public static SharedPreferences mPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        if (!SessionPrefs.get(this).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        //obtener preferencias
     mPrefs = MainActivity.this.getSharedPreferences("SYSTEMAPP_PREFS", Context.MODE_PRIVATE);


        String usuario = mPrefs.getString("PREF_USER_NAME", "");
        ActionBar actionBar = ((AppCompatActivity)MainActivity.this).getSupportActionBar();

        if (actionBar!=null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setTitle(usuario + " - logueado");
        }
        MainActivity.this.setTitle(usuario + " - logueado");



        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);


        // se elimino boton floating
       /*binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.


        mAppBarConfiguration = new AppBarConfiguration.Builder(

                R.id.nav_ordenes,
                R.id.nav_ejecutadas,
                R.id.nav_borrarruta,
                R.id.nav_sync,
                R.id.nav_config

        )
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);


        return true;
    }



    // In C:/Documentos/Castro/Webaguas/SystemApp_nueva/SystemApp/app/src/main/java/com/example/systemapp/MainActivity.java

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Use if-else if instead of switch
        if (id == R.id.action_settings) {
            Toast.makeText(MainActivity.this, "Cerrando usuario: " + mPrefs.getString("PREF_USER_NAME", ""), Toast.LENGTH_LONG).show();

            SessionPrefs.get(this).logOut();
            startActivity(new Intent(this, MainActivity.class));

            finish();

            // It's good practice to return true when you've handled the item click.
            return true;
        }
        // You could add more 'else if' blocks here for other menu items.
    /*
    else if (id == R.id.another_action) {
        // Handle another action
        return true;
    }
    */

        return super.onOptionsItemSelected(item);
    }





    private void changeFragment(Fragment fragment, boolean needToAddBackstack) {
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.nav_host_fragment_content_main, fragment);
        mFragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        if (needToAddBackstack)
            mFragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        mFragmentTransaction.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            Log.d("RAsignadasFragment", "Esta aquí  if (drawer.isDrawerOpen(GravityCompat.START)) {");
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        if (getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main) instanceof fragment_ordenes) {
            ((fragment_ordenes) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main)).handleOnBackPress();
            return;
        }

        if ((getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main) instanceof fragment_sync)) {
            return;
        }else
            super.onBackPressed();

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void displayPromptForComfirmLogout(final Activity activity, int cantpendingroutes){

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final String message = activity.getString(R.string.comfirm_logout);

        builder.setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                SessionPrefs.get(activity).logOut();
                                startActivity(new Intent(activity, MainActivity.class));
                                //activity.deleteDatabase(DataCollectorDBDef.DATABASE_NAME);
                                finish();
                            }
                        })
                .setNegativeButton("Cancelar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

        try {
            builder.create().show();
        }catch (Exception e){
            System.out.println("No se cerro el screen. " + e.getMessage());
        }

    }


    public static void displayPromptForEnablingGPS(final Activity activity){

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = activity.getString(R.string.req_gps_on);

        builder.setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                activity.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        });

        try {
            builder.create().show();
        }catch (Exception e){
            System.out.println("No fue posible crear mensaje en pantalla. " + e.getMessage());
        }

    }



}