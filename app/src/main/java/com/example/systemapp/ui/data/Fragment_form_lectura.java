package com.example.systemapp.ui.data;

import static android.app.Activity.RESULT_OK;

import static com.example.systemapp.data.PrinterCommands.FEED_LINE;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.systemapp.AuthInterceptor;
import com.example.systemapp.MainActivity;
import com.example.systemapp.R;
import com.example.systemapp.SystemAppAPI;
import com.example.systemapp.data.AdminSQLiteOpenHelper;
import com.example.systemapp.data.Constants;
import com.example.systemapp.data.SessionPrefs;
import com.example.systemapp.data.Validador;
import com.example.systemapp.data.GuardarFotos;
import com.example.systemapp.data.Utilidades;
import com.example.systemapp.data.VariablesSesion;
import com.example.systemapp.data.causas.CustomDialog;
import com.example.systemapp.data.causas.MotivosNoLectura;
import com.example.systemapp.data.causas.ObservacionDialog;
import com.example.systemapp.data.model.DBListas;
import com.example.systemapp.data.model.DBOrdenLecturas;
import com.example.systemapp.data.model.DBOrdenLecturasEnviar;
import com.example.systemapp.data.model.DBdefinicionOrdenes;
import com.example.systemapp.data.model.EnviarRespuesta;
import com.example.systemapp.databinding.FragmentFormLecturaBinding;
import com.example.systemapp.ui.RAsignadasFragment;
import com.example.systemapp.ui.fragment_ordenes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.systemapp.data.Utils;
import com.example.systemapp.data.PrinterCommands;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import okhttp3.Connection;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.animation.ObjectAnimator;
import android.view.animation.DecelerateInterpolator;


public class Fragment_form_lectura extends Fragment implements MotivosNoLectura.MotivosNoLecturaInterface, CustomDialog.CustomDialogInterface, ObservacionDialog.ObservacionDialogInterface {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_LOCATION = 1;
    private static final String TAG = "Fragment_form_lectura";
    private FragmentFormLecturaBinding binding;
    public boolean bloquear_comentario = false;

    public DBOrdenLecturas orden = null;
    public int cantidadFotos = 0;
    public int verificacion = 1;
    public boolean fotoAdicional = false;
    public List<String> mensajesFotos;
    public boolean imprimir_linea = false;

    private TextView textVresumen;
    private TextView textV_direccion;
    private TextView textVnombre;
    private TextView textVsuscriptor;
    private TextView textVmedidor;
    private TextView textVruta;
    private TextView textVconsecutivo;
    private TextView textVtipo;
    private EditText editTextLectura;
    private EditText editTextObservacionG;
    private ImageButton btnBack;
    private ImageButton btnNext;

    private FloatingActionButton btnSave;
    private SwitchMaterial switchMotivo;
    private TextView textVmotivov;
    private FloatingActionButton btnLecturaMotivoClose;

    private FloatingActionButton btnLecturaImprimir;

    //Listener para comunicar cambios entre fragments
    private fragment_ordenes parentFragment;

    private List ordenes;

    //Lista con las ordenes disponibles
    private List allRutas;

    //Posicion del item seleccionado
    private int posicion;

    //posicion del item a mostrar de la lista de ordenes del mismo contrato
    private int position = 0;

    //objeto para transacciones con BD
    private AdminSQLiteOpenHelper adminSQLiteOpenHelper;

    //Retrofit y API
    private SystemAppAPI systemAppAPI;

    private Retrofit systemapp;
    private String jsonOrden = "";//json para enviar al servidor

    private Call<EnviarRespuesta> enviarordenes;

    private LocationManager mLocationManager = null;

    private double longitude;
    private double latitude;
    private double lastlongitude;
    private double lastlatitude;

    //abrir acceso a las preferencias
    public SharedPreferences mPrefs;


    //valida causal
    public String validaCausal = "";

    //posición elemento elegido, para seleccionarlo por defecto
    public int posDefaultCausa = 0;
    public int posDefaultObs = 0;

    //Bundle para almacenar argumentos obtenidos desde otros fragment
    public Bundle bundle;
    public boolean edit = false;


    //conexión a la impresora
    private Connection thePrinterConn;

    //impresion
    // android built in classes for bluetooth operations
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;
    boolean printExist = false;

    // needed for communication to bluetooth device / network
    OutputStream outputStream;
    InputStream inputStream;
    Thread thread;
    Resources resources;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    String dataprint;

    public static final byte[] ESC_ALIGN_LEFT = new byte[]{0x1b, 'a', 0x00};
    public static final byte[] ESC_ALIGN_RIGHT = new byte[]{0x1b, 'a', 0x02};
    public static final byte[] ESC_ALIGN_CENTER = new byte[]{0x1b, 'a', 0x01};
    public static final byte[] ESC_CANCEL_BOLD = new byte[]{0x1B, 0x45, 0};
    public static byte[] format = {27, 33, 0};
    public static byte[] arrayOfByte1 = {27, 33, 0};

    InputMethodManager imm;


    // ✅ NUEVAS VARIABLES PARA SWIPE MEJORADO
    private GestureDetector gestureDetector;
    private boolean isProcessing = false;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    // Variables para tracking en tiempo real del swipe
    private float initialX = 0f;
    private float currentTranslationX = 0f;
    private boolean isSwiping = false;
    private static final float SWIPE_SENSITIVITY = 0.6f; // 60% del desplazamiento del dedo
    private static final float SWIPE_SNAP_THRESHOLD = 0.3f; // 30% del ancho de pantalla

    // Variables para efecto carousel (vista previa)
    private View nextPreviewCard;
    private View prevPreviewCard;
    private TextView nextPreviewText;
    private TextView prevPreviewText;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentFormLecturaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        EditText lecturaInput = binding.editTextLectura;

        // 🔹 Inicializa Bluetooth una sola vez
        inicializarBluetooth();

        Animation scaleIn = AnimationUtils.loadAnimation(getContext(), R.anim.edittext_focus_scale);
        Animation scaleOut = AnimationUtils.loadAnimation(getContext(), R.anim.edittext_focus_scale_reverse);

        lecturaInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.startAnimation(scaleIn);
            } else {
                v.startAnimation(scaleOut);
            }
        });



        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        //comprobar que se tenga gps encendido
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Pedir que se active el gps
            MainActivity.displayPromptForEnablingGPS(getActivity());

        } else {

            bundle = getArguments();

            if (bundle != null) {


                try {

                    Location location = getLastKnownLocation();
                    if (location != null) {
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                        lastlongitude = location.getLongitude();
                        lastlatitude = location.getLatitude();
                    }

                    final LocationListener locationListener = new LocationListener() {
                        public void onLocationChanged(Location location) {
                            Log.d("Fragment_form_lectura", "onLocationChanged " +
                                    location.getLatitude() + " " + location.getLongitude());
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
                            lastlongitude = location.getLongitude();
                            lastlatitude = location.getLatitude();
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {

                        }

                        @Override
                        public void onProviderDisabled(String provider) {

                        }
                    };

                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000, 100, locationListener);

                } catch (SecurityException e) {

                    Log.e("Fragment_form_lectura", "Sin coordenadas" +
                            e.getMessage());

                } catch (Exception e) {
                    Log.e("Fragment_form_lectura", "Revise impresora" +
                            e.getMessage());
                }
                //Instanciamos el DBHelper
                adminSQLiteOpenHelper = new AdminSQLiteOpenHelper(getContext());

                mPrefs = getActivity().
                        getSharedPreferences("SYSTEMAPP_PREFS", Context.MODE_PRIVATE);

                // ⭐ Crear OkHttpClient con el Interceptor
                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(new AuthInterceptor(getContext()))
                        .build();
                // Crear conexión al servicio REST
                systemapp = new Retrofit.Builder()
                        .baseUrl(SystemAppAPI.BASE_URL)
                        .client(client) //
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                systemAppAPI = systemapp.create(SystemAppAPI.class);

                //obtener padre
                //parentFragment = (OrdenesFragment) getParentFragment();

                posicion = bundle.getInt("posicion");

                //variable para manejar mostrar o no el teclado
                imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                //obtener mac de impresora


                ordenes = (List<DBOrdenLecturas>) bundle.getSerializable("orden");

                //obtener todas las ordenes disponibles para avanzar o retroceder entre registros
                if (bundle.getBoolean("edit")) {
                    allRutas = (List<DBOrdenLecturas>) bundle.getSerializable("allRutas");
                    orden = (DBOrdenLecturas) allRutas.get(posicion);
                } else {
                    if (VariablesSesion.rutasGobalAsignadas == null) {
                        allRutas = (List<DBOrdenLecturas>) bundle.getSerializable("allRutas");
                    } else {
                        allRutas = VariablesSesion.rutasGobalAsignadas;
                    }
                    //validar en caso de que den al boton atrás de android, que las ordenes asociadas no estén
                    //vacias, ya que está generando excepción
                    if (ordenes != null)
                        orden = (DBOrdenLecturas) ordenes.get(position);
                    else {
                        orden = (DBOrdenLecturas) allRutas.get(VariablesSesion.posicionSelec);
                        ordenes = RAsignadasFragment.getAllOrdeneswithContrato(allRutas,
                                orden.getSuscriptor(),
                                orden.getId());
                    }
                }


                Log.d("Fragment_form_lectura", orden.getId() + " " + orden.getConsumo());
                Log.d("Fragment_form_lectura", ordenes.size() + " " + orden.getSuscriptor());


                textVresumen = binding.textVresumen;
                textV_direccion = binding.textVDireccion;
                textVnombre = binding.textVnombre;
                textVsuscriptor = binding.textVsuscriptor;
                textVmedidor = binding.textVmedidor;
                textVruta = binding.textVruta;
                textVconsecutivo = binding.textVconsecutivo;
                textVtipo = binding.textVtipo;
                btnNext = binding.btnNext;
                btnBack = binding.btnBack;
                btnSave = binding.btnSave;
                switchMotivo = binding.switchMotivo;
                editTextObservacionG = binding.editTextObservacionG;
                btnLecturaImprimir = binding.btnLecturaImprimir;
                editTextLectura = binding.editTextLectura;
                textVmotivov = binding.textVmotivov;
                btnLecturaMotivoClose = binding.btnLecturaMotivoClose;
                //txt_lectura_obsval = view.findViewById(R.id.txt_lectura_obsval);
                //btn_lectura_obsclose = view.findViewById(R.id.btn_lectura_obsclose);


                if (bundle.getBoolean("edit")) {
                    edit = true;
                    btnLecturaImprimir.show();
                    btnLecturaMotivoClose.show();
                }

                reIniFragment();

                //Obtener criticas
                String condicion_criticas = "marca_id = 'CRITICA'";

                //consultar las criticas desde la base de datos
                //obtendremos un objeto de tipo ElementosListasDB
                final List criticas = adminSQLiteOpenHelper.getData(DBdefinicionOrdenes.LISTAS.TABLE_NAME, condicion_criticas);

                /*
                 listas de critica según el código
                "value": "50",
                "name": "CONSUMO NEGATIVO"

                "value": "51",
                "name": "ALTO CONSUMO"

                "value": "52",
                "name": "BAJO CONSUMO"

                "value": "53",
                "name": "LECTURA IGUAL QUE LA ANTERIOR"
                 */


                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (allRutas.size() > 0 && posicion > 0) {
                            posicion--;
                            orden = (DBOrdenLecturas) allRutas.get(posicion);
                            if (edit) {
                                VariablesSesion.posicionSelectProc = posicion;
                            } else {
                                VariablesSesion.posicionSelec = posicion;
                            }
                            ordenes = RAsignadasFragment.
                                    getAllOrdeneswithContrato((List<DBOrdenLecturas>) allRutas,
                                            orden.getSuscriptor(), orden.getId());
                            reIniFragment();
                        }
                    }
                });

                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (allRutas.size() > 0 && posicion < (allRutas.size() - 1)) {
                            posicion++;
                            orden = (DBOrdenLecturas) allRutas.get(posicion);
                            if (edit) {
                                VariablesSesion.posicionSelectProc = posicion;
                            } else {
                                VariablesSesion.posicionSelec = posicion;
                            }
                            ordenes = RAsignadasFragment.
                                    getAllOrdeneswithContrato((List<DBOrdenLecturas>) allRutas,
                                            orden.getSuscriptor(), orden.getId());
                            reIniFragment();
                        }
                    }
                });


                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("Fragment_form_lectura", "Guardar...");

                        if (posDefaultCausa == 0) {//si se cumple esta condiciòn quiere decir que no hay
                            // ninguna causa elegida, por tanto debe validarse que el campo lectura esté seteado
                            //por tanto no se realiza ninguna validación, ya que el campo de lectura es vacío
                            if (editTextLectura.getText().toString().equals("")) {
                                editTextLectura.setError(getString(R.string.error_field_required));
                                displayPrompt(getActivity(), getActivity().getString(R.string.lectura_input_req),
                                        "", null);
                                return;
                            }

                            try {
                                orden.setLectura_actual(Integer.parseInt(editTextLectura.getText().toString()));
                            } catch (NumberFormatException e) {
                                editTextLectura.setText("");
                                Toast.makeText(getActivity(), "El valor de lectura es inválido o demasiado grande, por favor verifique",
                                        Toast.LENGTH_LONG).show();
                            }

                            String validacion = Validador.validaciones(orden);

                            switch (validacion) {
                                case Constants.VALIDACION1:
                                    cantidadFotos = 1;
                                    orden.setCritica(String.valueOf(Integer.parseInt(
                                            ((DBListas) criticas.get(3)).getCodigo()))+"-"+
                                            ((DBListas) criticas.get(3)).getDescripcion());
                                    displayPrompt(getActivity(), "<h3><b>-- " + criticas.get(3).toString() +
                                                    " --</b><h3><br>" + getString(R.string.mensaje_critica),
                                            "openDialogObservacion", orden);
                                    break;
                                case Constants.VALIDACION2:
                                    //verificación == 1 indica que es el primer intento de lectura
                                    //en esta validación se deben hacer dos
                                    if (verificacion == 1) {
                                        editTextLectura.setText("");
                                        editTextLectura.setHint("Revisa lectura");
                                        displayPrompt(getActivity(), "<h3><b>--" + criticas.get(0).toString() +
                                                        "--</b><h3><br>" + getString(R.string.mensaje_critica),
                                                "", null);
                                        verificacion++;
                                    } else {
                                        cantidadFotos = 1;
                                        orden.setCritica(String.valueOf(Integer.parseInt(
                                                ((DBListas) criticas.get(0)).getCodigo())+"-"+
                                                ((DBListas) criticas.get(0)).getDescripcion()));
                                        displayPrompt(getActivity(), "<h3><b>--" + criticas.get(0).toString() +
                                                        " --</b><h3><br>" + getString(R.string.mensaje_critica),
                                                "dispatchTakePictureIntent", orden);
                                        verificacion = 1;
                                    }
                                    break;
                                case Constants.VALIDACION3:
                                    //verificación == 1 indica que es el primer intento de lectura
                                    //en esta validación se deben hacer dos
                                    if (verificacion == 1) {
                                        editTextLectura.setText("");
                                        editTextLectura.setHint("Valida la lectura");
                                        displayPrompt(getActivity(), "<h3><b>-- " + criticas.get(1).toString() +
                                                        " --</b><h3><br>" + getString(R.string.mensaje_critica),
                                                "", null);
                                        verificacion++;
                                    } else {
                                        cantidadFotos = 1;
                                        orden.setCritica(String.valueOf(Integer.parseInt(
                                                ((DBListas) criticas.get(1)).getCodigo()))+"-"+((DBListas) criticas.get(1)).getDescripcion());
                                        displayPrompt(getActivity(), "<h3><b>-- " + criticas.get(1).toString() +
                                                        " --</b><h3><br>" + getString(R.string.mensaje_critica),
                                                "dispatchTakePictureIntent", orden);
                                        verificacion = 1;
                                    }
                                    break;
                                case Constants.VALIDACION4:
                                    if (verificacion == 1) {
                                        editTextLectura.setText("");
                                        editTextLectura.setHint("Valida la lectura");
                                        displayPrompt(getActivity(), "<h3><b>-- " + criticas.get(2).toString() +
                                                        " --</b><h3><br>" + getString(R.string.mensaje_critica),
                                                "", null);
                                        verificacion++;
                                    } else {
                                        cantidadFotos = 1;
                                        orden.setCritica(String.valueOf(Integer.parseInt(
                                                ((DBListas) criticas.get(2)).getCodigo()))+"-"+
                                                ((DBListas) criticas.get(2)).getDescripcion());
                                        displayPrompt(getActivity(), "<h3><b>-- " + criticas.get(2).toString() +
                                                        " --</b><h3><br>" + getString(R.string.mensaje_critica),
                                                "dispatchTakePictureIntent", orden);
                                        verificacion = 1;
                                    }
                                    break;
                                case Constants.VALIDACION5:
                                    if (verificacion == 1) {
                                        editTextLectura.setText("");
                                        editTextLectura.setHint("Valida la lectura");
                                        displayPrompt(getActivity(), "<h3><b>-- " + criticas.get(2).toString() +
                                                        " --</b><h3><br>" + getString(R.string.mensaje_critica),
                                                "", null);
                                        verificacion++;
                                    } else {
                                        cantidadFotos = 1;
                                        orden.setCritica(String.valueOf(Integer.parseInt(
                                                ((DBListas) criticas.get(2)).getCodigo()))+"-"+
                                                ((DBListas) criticas.get(2)).getDescripcion());
                                        displayPrompt(getActivity(), "<h3><b>-- " + criticas.get(2).toString() +
                                                        " --</b><h3><br>" + getString(R.string.mensaje_critica),
                                                "dispatchTakePictureIntent", orden);
                                        verificacion = 1;
                                    }
                                    break;
                                default:
                                    if (!edit && orden.getCausa()!= null || orden.getCausa()!= 0 ) {
                                        //manda a imprimir verificando si es el último registro asociado por contrato
                                        orden.setCritica(String.valueOf(Integer.parseInt(
                                                ((DBListas) criticas.get(4)).getCodigo())+"-"+
                                                ((DBListas) criticas.get(4)).getDescripcion()));

                                                preparetoprint(orden);

                                    }

                                    //Habilitar o deshabilitar la foto obligatoria en consumo normal
                                    dispatchTakePictureIntent(orden.getId());
                                    break;
                            }

                        } else {//si tiene una causa elegida y da al botón guardar...
                            //SE TOMAN LAS COORDENASSSS!!!!  y se toma la foto
                            //si se ha elegido la observación en caso de que la causa sea 37

                            if (orden.getCausa() == 37) {
                                if (orden.getObservacion() == null) {
                                    displayPrompt(getActivity(), "Debes seleccionar una observación",
                                            "", null);
                                } else if (orden.getObservacion() == 0) {
                                    displayPrompt(getActivity(), "Debes seleccionar una observación",
                                            "", null);
                                }
                            } else {
                                if (!edit ) {
                                    //manda a imprimir verificando si es el último registro asociado por contrato
                                    preparetoprint(orden);
                                }
                                dispatchTakePictureIntent(orden.getId());
                            }

                        }
                    }
                });

                btnLecturaImprimir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        preparetoprint(orden);
                    }
                });

                switchMotivo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                        if (b) {
                            MotivosNoLectura motivosNoLectura = MotivosNoLectura.getInstance(Fragment_form_lectura.this);
                            Bundle bundleCausa = motivosNoLectura.getArguments();
                            bundleCausa.putInt("posSelected", posDefaultCausa);
                            motivosNoLectura.setArguments(bundleCausa);

                            motivosNoLectura.show(getActivity().getSupportFragmentManager(), "example dialog");

                        }
                    }
                });


                btnLecturaMotivoClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        textVmotivov.setText("");
                        orden.setCausa(null);
                        mensajesFotos = null;
                        posDefaultCausa = 0;
                        editTextLectura.setEnabled(true);
                        btnLecturaMotivoClose.hide();
                    }
                });


                bundle.clear();

                editTextLectura.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        Log.d("Fragment_form_lectura", "FOCUSSSS!!");
                        imm.showSoftInput(editTextLectura, InputMethodManager.SHOW_FORCED);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        ((InputMethodManager) (getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
                });

                //}//fin revisar si se tienen permisos para acceder a la ubicación

            } else {
                Log.d("Fragment_form_lectura", "fragment sin argumentos");
            }


        }//fin de comprobación de GPS encendido

        // ✅ CONFIGURAR GESTOS DE SWIPE (agregar antes de return root;)
        setupSwipeGesture(root);

        // ✅ CONFIGURAR VISTAS DE PREVIEW PARA EFECTO CAROUSEL
        setupCarouselPreview(root);

        return root;
    }


    ActivityResultLauncher<Intent> dispatchTakePictureIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            if (result.getResultCode() == RESULT_OK) {
                Bundle extras = result.getData().getExtras();
                Bitmap imgBitmap = (Bitmap) extras.get("data");
            }

        }
    });


    private void dispatchTakePictureIntent(String idOrden) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {

        //startActivityForResult(takePictureIntent,1);
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = GuardarFotos.createImageFile(getActivity(), idOrden);
        } catch (IOException ex) {
            Log.e("Error", ex.toString());

        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(getActivity(),
                    "com.example.systemapp.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            //mostrar mensaje si existe
            if (mensajesFotos != null) {
                Toast.makeText(getActivity(), mensajesFotos.get(0), Toast.LENGTH_SHORT).show();
            }

            Log.d("Fragment_form_lectura", GuardarFotos.currentPhotoPath + " aquí estaría");
            Log.d("Fragment_form_lectura", photoURI.getPath() + " aquí estaría");
        }

        //}
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            //imageView.setImageBitmap(imageBitmap);

            //almacenar la ruta de la foto
            orden.setRuta_foto((orden.getRuta_foto() == null) ? GuardarFotos.currentPhotoPath : orden.getRuta_foto() + ", " + GuardarFotos.currentPhotoPath);
            Log.d("Fragment_form_lectura", "ruta_foto " + orden.getRuta_foto());

            cantidadFotos--;
            if (cantidadFotos > 0) {
                if (mensajesFotos != null) {
                    mensajesFotos.remove(0);
                    Toast.makeText(getActivity(), mensajesFotos.get(0), Toast.LENGTH_SHORT).show();
                }
                dispatchTakePictureIntent(orden.getId());
            } else {
                if (!fotoAdicional)
                    finalizarRegistroLectura();
                else
                    fotoAdicional = false;
            }
        } else {

            if (requestCode == REQUEST_ENABLE_BT) {
                //indica que se pidió permitir habilitar el bluetooth

            } else {
                if (!fotoAdicional) {
                    //sino indica que no se tomó foto y se obliga a que se tome
                    Toast.makeText(getActivity(), getString(R.string.foto_req), Toast.LENGTH_SHORT).show();
                    dispatchTakePictureIntent(orden.getId());
                } else
                    fotoAdicional = false;
            }

        }
    }


    public void finalizarRegistroLectura() {
        // ✅ Evitar doble clic rápido
        btnSave.setEnabled(false);
        new Handler(Looper.getMainLooper()).postDelayed(() -> btnSave.setEnabled(true), 2000);

        // ✅ VALIDACIÓN 1: Evitar múltiples ejecuciones
        if (isProcessing) {
            Log.d(TAG, "⚠️ Ya se está procesando un registro");
            Toast.makeText(getActivity(), "Procesando, por favor espere...", Toast.LENGTH_SHORT).show();
            return;
        }




        // ✅ Bloquear swipe mientras procesa
        isProcessing = true;
        btnSave.setEnabled(false); // Deshabilitar botón temporalmente


        // ✅ VALIDACIÓN 2: Verificar que tenga lectura O causa válida
        boolean tieneLectura = (orden.getLectura_actual() != null);
        boolean tieneCausa = (orden.getCausa() != null && orden.getCausa() > 0);
        boolean tieneCritica = (orden.getCritica() != null && !orden.getCritica().trim().isEmpty());

        if (!tieneLectura && !tieneCausa) {
            Log.e(TAG, "❌ Registro inválido: sin lectura, (lectura 0 sin critica), ni causa");
            Toast.makeText(getActivity(), "Debe ingresar una lectura valida o seleccionar un motivo", Toast.LENGTH_LONG).show();

            // Rehabilitar botón
            isProcessing = false;
            btnSave.setEnabled(true);
            return;
        }

        // ✅ VALIDACIÓN 3: Tiene lectura 0 sin crítica → error
        if (tieneLectura) {
            int lectura = Integer.parseInt(orden.getLectura_actual().toString());
            if (lectura == 0 && !tieneCritica) {
                Log.e(TAG, "❌ Registro inválido: lectura 0 sin crítica");
                Toast.makeText(getActivity(), "Si la lectura es 0 debe tener una crítica obligatoriamente", Toast.LENGTH_LONG).show();
                isProcessing = false;
                btnSave.setEnabled(true);
                return;
            }
        }

        // ✅ VALIDACIÓN 4: Verificar que tenga foto (ruta_foto)
        if (orden.getRuta_foto() == null || orden.getRuta_foto().isEmpty()) {
            Log.e(TAG, "❌ Registro sin foto");
            Toast.makeText(getActivity(), "Debe tomar al menos una foto", Toast.LENGTH_LONG).show();

            isProcessing = false;
            btnSave.setEnabled(true);
            return;
        }

        orden.setEstado_lectura("RELECTURA");//RELECTURA indicará lectura realizada, es decir que
        //va a aparecer en la pestaña de ordenes o lecturas "Procesadas"
        orden.setCategoria_orden("RELECTURA");
        orden.setFfinlec(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));


        orden.setObservacionGral((orden.getObservacionGral()!=null?orden.getObservacionGral():"")+"-"+(editTextObservacionG.getText().toString()));

        /*if(orden.getObservacionGral()==null) {
            orden.setObservacionGral(editTextObservacionG.getText().toString()); // guardar comentario general
        }*/



        if (orden.getCausa() != null)
            if (orden.getCausa() > 0) {
                orden.setCritica("55-CAUSADO");
                orden.setLectura_actual(null);
            }

        if (latitude == 0 || longitude == 0) {
            Location location = getLastKnownLocation();
            if (location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                lastlongitude = location.getLongitude();
                lastlatitude = location.getLatitude();
            } else {
                longitude = lastlongitude;
                latitude = lastlatitude;
            }
        }


        orden.setLatitud(latitude + "");
        orden.setLongitud(longitude + "");



        //verifica que se haya insertado o actualizado el registro
        if (adminSQLiteOpenHelper.insertOrden(orden, true) > 0) {

            //actualizar la variable lista allRutasGobal, para actualizar el registro almacenado
            if (VariablesSesion.allRutasGobal == null) {
                VariablesSesion.setAllRutasGobal(adminSQLiteOpenHelper.getData(
                        DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                        "Tipo_orden = 'RUTAS'"
                ));
            }
            //insertar o reemplazar el elemento en la variable allRutasGobal
            // que fue recién almacenado o actualizado en bd
            VariablesSesion.updateElementAtAllRutasGlobal(orden);

//            Log.d("LecturaFragment", orden.getObserv()+"observ");

            //se valida si el procesamiento de la lectura fue una edición o una lectura nueva
            if (edit) {

                posicion++;
                if (posicion >= allRutas.size()) {
                    ordenes.removeAll(ordenes);
                    posicion--;
                    Log.d(TAG, "elementos ordenes " + ordenes.size());
                }
                Log.d(TAG, "fuera del if elementos ordenes " + ordenes.size());

            } else {

                //actualiza las cantidades, de las lecturas u órdenes procesadas
                SessionPrefs.get(getActivity()).setPrefRutasProcesadas(
                        mPrefs.getInt("PREF_RUTAS_PROCESADAS", 0) + 1
                );

                //actualizar la variable lista rutasGobalAsignadas
                VariablesSesion.rutasGobalAsignadas.remove(posicion);
                allRutas = VariablesSesion.rutasGobalAsignadas;
                if (posicion >= VariablesSesion.rutasGobalAsignadas.size())
                    posicion = VariablesSesion.rutasGobalAsignadas.size() - 1;
                //obtener las demás ordenes que pertenecen al contrato del último registro guardado
                ordenes = RAsignadasFragment.getAllOrdeneswithContrato(
                        VariablesSesion.rutasGobalAsignadas,
                        orden.getSuscriptor(), orden.getId());

                //actualizar contadores del bottom nav
                if (parentFragment != null) {
                    parentFragment.setCantidades(allRutas.size() + "",
                            mPrefs.getInt("PREF_RUTAS_REASIGNADAS", 0) + "",
                            mPrefs.getInt("PREF_RUTAS_PROCESADAS", 0) + "");
                    Log.d("RAsignadasFragment", "Actualizando cantidades...");
                }

            }


            //Hacer el envío de la lectura al servidor
              sendDataToServer(orden);

            position++;
            if (ordenes.size() > 0) {
                orden = (DBOrdenLecturas) allRutas.get(posicion);
                reIniFragment();
                Log.d("Fragment_form_lectura", "Reinicia los componentes");

            } else {


                if (edit) {
                    if (posicion < allRutas.size())
                        orden = (DBOrdenLecturas) allRutas.get(posicion);
                    else
                        orden = (DBOrdenLecturas) allRutas.get(
                                allRutas.size() - 1
                        );
                    ordenes = RAsignadasFragment.
                            getAllOrdeneswithContrato(allRutas,
                                    orden.getSuscriptor(), orden.getId());
                } else {
                    if (posicion < VariablesSesion.rutasGobalAsignadas.size() && posicion >= 0)
                        orden = (DBOrdenLecturas) VariablesSesion.rutasGobalAsignadas.get(posicion);
                    else if (posicion >= 0)
                        orden = (DBOrdenLecturas) VariablesSesion.rutasGobalAsignadas.get(
                                VariablesSesion.rutasGobalAsignadas.size() - 1
                        );
                    else if (VariablesSesion.rutasGobalAsignadas.size() == 0) {
                        VariablesSesion.rutasGobalAsignadas = null;
                        getFragmentManager().beginTransaction().replace(R.id.fragment_container1,
                                        new RAsignadasFragment())
                                .addToBackStack(null)
                                .commit();
                        return;
                    }

                    ordenes = RAsignadasFragment.
                            getAllOrdeneswithContrato(VariablesSesion.rutasGobalAsignadas,
                                    orden.getSuscriptor(), orden.getId());
                }
                reIniFragment();
                Log.d("Fragment_form_lectura", "Reinicia los componentes en el siguiente contrato");

            }
        } else {//si no se actualizó el registro se queda en la actual lectura y se envia toast notificando
            Log.d("Fragment_form_lectura", "No se pudo almacenar la lectura. Intente nuevamente.");
            Toast.makeText(getActivity(), "No se pudo almacenar la lectura. Intente nuevamente.", Toast.LENGTH_LONG).show();
        }

        // ✅ Desbloquear botón y swipe
        isProcessing = false;
        btnSave.setEnabled(true);
    }


    // Dialog myDialog;


    public  void sendDataToServer(final DBOrdenLecturas ordentoupload) {

        Integer lectura = null;

        if (ordentoupload.getLectura_actual() == null) {
            if (ordentoupload.getCausa() == null || ordentoupload.getCausa() <= 0)//si no hay lectura y tampoco causa, no envía lectura
                return;
        } else {
            if (ordentoupload.getCausa() == null || ordentoupload.getCausa() == 0)
                lectura = Integer.valueOf(ordentoupload.getLectura_actual() + "");
        }

        String campoFoto1 =  "";
        if (ordentoupload.getRuta_foto()!=null) {
            String rutaFoto = ordentoupload.getRuta_foto();
            campoFoto1 = Utilidades.encodeImage(rutaFoto);
        }

        String campoFoto = campoFoto1;
        String id = ordentoupload.getId();
        String tipo = "4";
        String finilec = ordentoupload.getFinilec();
        String ffinlec = ordentoupload.getFfinlec();
        Integer lectact = lectura;
        String critica = (ordentoupload.getCritica());
        Integer causal = ((ordentoupload.getCausa()==null)?0:ordentoupload.getCausa());
        Integer observ = ((ordentoupload.getObservacion()==null)?0:ordentoupload.getObservacion());
        String observg = ((ordentoupload.getObservacionGral()==null)?"":ordentoupload.getObservacionGral());
        String latitud = ((ordentoupload.getLatitud()==null)?"":ordentoupload.getLatitud());
        String longitud = ((ordentoupload.getLongitud()==null)?"":ordentoupload.getLongitud());
        Integer consumo = ((ordentoupload.getConsumo()==null)?null:ordentoupload.getConsumo());
        String suscriptor = ordentoupload.getSuscriptor();
        String usuario = ordentoupload.getUsuario();
        String texobser = ordentoupload.getDescObservacion();
        String texcausa = ordentoupload.getDescCausa();

        // ⭐ ELIMINADO: No vuelvas a crear Retrofit aquí, usa el que ya creaste en onCreateView

        // ⭐ Usar el systemAppAPI que ya tiene el interceptor configurado
        Call<Object> enviarordenes = systemAppAPI.enviarordenes(
                new DBOrdenLecturasEnviar(campoFoto, id, tipo, finilec, ffinlec, lectact,
                        critica, causal, observ, observg, latitud, longitud, consumo,
                        suscriptor, usuario, texobser, texcausa)
        );

        Log.d("LecturaFragment", "va al servicio");
        Log.d("LecturasSync", String.valueOf(enviarordenes));

        enviarordenes.clone().enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Log.d("LecturaFragment", "onResponse");
                //Procesar errores
                String error;
                if (!response.isSuccessful()) {
                    error = response.message();

                    if (response.errorBody()
                            .contentType()
                            .subtype()
                            .equals("application/json")) {
                        error = response.message();
                    } else {
                        error = response.message();
                    }


                    Log.e("LecturaFragment", error);
                    return;
                }

                if (response.body().equals(false)) {
                    error = response.message();
                    //Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                    Log.e("LecturaFragment", error);
                    return;
                }

                //si se pudo subir el registro, se notifica en la base de datos
                Log.d("LecturaFragment", "Resultado subida " + response.body().equals(true));
                ordentoupload.setUploadlec("true");
                if (adminSQLiteOpenHelper.insertOrden(ordentoupload, true) > 0) {
                    Log.i("LecturaFragment", "Registro subido y actualizado en BD con éxito");
                }

                //si hay éxito en la subida del registro, se realiza proceso para subir
                //registros pendientes por subir
                Utilidades.mPrefs = mPrefs;
                Utilidades.systemAppAPI = systemAppAPI;
                Utilidades.adminSQLiteOpenHelper = adminSQLiteOpenHelper;
                Utilidades.sendPendingData();

            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.d("LecturaFragment", "onFailure");
                Log.e("LecturaFragment", t.getMessage() + " " + t.getCause());
            }
        });
    }
    // ✅ Reinicia los componentes del fragmento con seguridad
    private void reIniFragment() {
        try {
            // 🔹 Reinicio de variables internas
            cantidadFotos = 0;
            verificacion = 1;
            validaCausal = "";
            posDefaultCausa = 0;
            posDefaultObs = 0;

            // 🔹 Reiniciar ActionBar con layout personalizado
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowCustomEnabled(true);
                LayoutInflater inflator = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = inflator.inflate(R.layout.title_actionbar_lectura, null);
                actionBar.setCustomView(v);
            } else {
                Log.d("Fragment_form_lectura", "ActionBar es null");
            }

            // 🔹 Actualizar datos del objeto orden
            if (orden != null) {
                orden.setFinilec(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));

                binding.textVresumen.setText((posicion + 1) + " de " + allRutas.size());
                binding.textVnombre.setText(getString(R.string.textV_nombre) + "     " + orden.getNombre());
                binding.textVmedidor.setText(getString(R.string.textV_medidor) + " " + orden.getRef_Medidor());
                binding.textVDireccion.setText(getString(R.string.textV_direccion) + "   " + orden.getDireccion());
                binding.textVruta.setText(getString(R.string.textV_ruta) + "           " + orden.getRuta());
                binding.textVsuscriptor.setText(getString(R.string.textV_suscriptor) + "    " + orden.getSuscriptor());
                binding.textVconsecutivo.setText(getString(R.string.textV_consecutivo) + " " + orden.getConsecutivoRuta());
                binding.textVtipo.setText(getString(R.string.textV_tipoconsumo) + " " + orden.getNservic());
            }

            // 🔹 Reinicio de campos de texto
            binding.editTextLectura.setText("");
            binding.editTextObservacionG.setText("");
            binding.textVmotivov.setText("");
            binding.editTextLectura.setEnabled(true);
            binding.btnLecturaMotivoClose.hide();
            binding.switchMotivo.setChecked(false);

            // 🔹 Colocar foco y mostrar teclado
            boolean fo = binding.editTextLectura.requestFocus();
            if (fo && imm != null) {
                imm.showSoftInput(binding.editTextLectura, InputMethodManager.SHOW_FORCED);
            }

            // 🔹 Si está en modo edición
            if (edit && orden != null) {
                if (orden.getConsumo() != null && orden.getConsumo() != 0) {
                    binding.editTextLectura.setText(String.valueOf(orden.getLectura_actual()));
                    binding.editTextLectura.setEnabled(false);
                }
                if (orden.getCausa() != null && orden.getCausa() != 0) {
                    binding.textVmotivov.setText(orden.getDescCausa());
                    binding.textVmotivov.setVisibility(View.VISIBLE);
                    binding.editTextLectura.setEnabled(false);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(binding.editTextLectura.getWindowToken(), 0);
                    }
                    binding.btnLecturaMotivoClose.show();
                }
            }

            Log.d(TAG, "✅ Fragmento reiniciado correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "⚠️ Error al reiniciar fragmento", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error en reIniFragment: " + e.getMessage());
        }
    }



    public void displayPrompt(final Activity activity, final String message,
                              final String accionOK, final Object object) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(Html.fromHtml(message))
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                if (accionOK.equals("openDialogObservacion")) {

                                    if (validateObserv30()) {

                                        AlertDialog.Builder buildersub = new AlertDialog.Builder(activity);

                                        buildersub.setMessage(Html.fromHtml(
                                                        "En lecturas pasadas se eligió la observación 70"
                                                ))
                                                .setPositiveButton("OK",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface d, int id) {

                                                                DBOrdenLecturas ordenesDB = (DBOrdenLecturas) object;
                                                                ObservacionDialog observDialog = ObservacionDialog.getInstance(Fragment_form_lectura.this);

                                                                Bundle bundle = observDialog.getArguments();
                                                                bundle.putString("tipo_conexion", ordenesDB.getCtipcon());
                                                                observDialog.setArguments(bundle);

                                                                observDialog.show(getActivity().getSupportFragmentManager(), "example dialog");

                                                            }
                                                        });

                                        try {
                                            buildersub.create().show();
                                        } catch (Exception e) {
                                            System.out.println("No fue posible crear mensaje en pantalla. " + e.getMessage());
                                        }

                                    } else {


                                        Log.d("LecturaFragment", "else no hay 70");
                                        ObservacionDialog observacionDialog = ObservacionDialog.getInstance(Fragment_form_lectura.this);

                                        Bundle bundleObs = observacionDialog.getArguments();
                                        bundleObs.putInt("posSelected", posDefaultObs);
                                        bundleObs.putString("Comentario", orden.getObservacionGral());
                                        bundleObs.putBoolean("bloq_comentario", bloquear_comentario);
                                        observacionDialog.setArguments(bundleObs);

                                        observacionDialog.show(getActivity().getSupportFragmentManager(), "Observaciones");

                                        DBOrdenLecturas ordenesDB = (DBOrdenLecturas) object;



                                    }
                                }


                                if (accionOK.equals("dispatchTakePictureIntent")) {
                                    DBOrdenLecturas ordenesDB = (DBOrdenLecturas) object;
                                    if (!edit) {
                                        //manda a imprimir verificando si es el último registro asociado por contrato
                                        preparetoprint(ordenesDB);
                                    }


                                    dispatchTakePictureIntent(ordenesDB.getId());
                                }
                            }
                        });

        try {
            builder.create().show();
        } catch (Exception e) {
            System.out.println("No fue posible crear mensaje en pantalla. " + e.getMessage());
        }

    }


    private Location getLastKnownLocation() throws SecurityException {
        mLocationManager = (LocationManager) getActivity().
                getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private void inicializarBluetooth() {
        try {
            if (bluetoothAdapter == null) {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }

            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                FindBluetoothDevice(); // tu método
            } else {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, 1);
            }
        } catch (Exception e) {
            Log.e("BluetoothInit", "Error inicializando Bluetooth", e);
        }
    }

    // this will find a bluetooth printer device
    public void FindBluetoothDevice() {
        mPrefs = getActivity().getSharedPreferences("SYSTEMAPP_PREFS", Context.MODE_PRIVATE);
        String impresora = mPrefs.getString("PREF_PRINTER_NAME", "");

        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (bluetoothAdapter == null) {
                Log.d("Fragment_form_lectura", "No bluetooth adapter available");
                Toast.makeText(getContext(), "Este dispositivo no tiene Bluetooth", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Verificar permisos Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{
                                    Manifest.permission.BLUETOOTH_CONNECT,
                                    Manifest.permission.BLUETOOTH_SCAN
                            }, 100);
                    return;
                }
            }

            // ✅ CORREGIDO: Si NO está habilitado, pedir activarlo
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, 1);
                return;
            }

            // ✅ Cancelar discovery si está activo
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice pairedDev : pairedDevices) {
                    Log.d("Fragment_form_lectura", "Nombre bluetooth: " + pairedDev.getName()
                            + " MAC: " + pairedDev.getAddress());

                    if (pairedDev.getName().equals(impresora)) {
                        bluetoothDevice = pairedDev;
                        printExist = true;
                        Log.d("Fragment_form_lectura", "✅ Impresora encontrada: " + impresora);
                        break;
                    }
                }

                if (bluetoothDevice == null) {
                    Toast.makeText(getContext(), "Impresora '" + impresora + "' no encontrada. Verifique el emparejamiento.", Toast.LENGTH_LONG).show();
                    printExist = false;
                }
            } else {
                Toast.makeText(getContext(), "No hay dispositivos Bluetooth emparejados", Toast.LENGTH_LONG).show();
                printExist = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            printExist = false;
            Toast.makeText(getContext(), "Error al buscar impresora: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ✅ Mejorado: Manejo robusto de conexión
    void openBT() throws IOException {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                throw new IOException("Permisos de Bluetooth no concedidos");
            }
        }

        try {
            // ✅ Cancelar discovery antes de conectar
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            // ✅ Cerrar socket anterior si existe
            if (bluetoothSocket != null) {
                try {
                    bluetoothSocket.close();
                } catch (IOException e) {
                    Log.w("Fragment_form_lectura", "Error cerrando socket anterior");
                }
            }

            // Standard SerialPortService ID
            UUID uuidSting = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuidSting);
            bluetoothSocket.connect();

            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

            beginListenForData();

            Log.d("Fragment_form_lectura", "✅ Bluetooth conectado exitosamente");

        } catch (IOException e) {
            Log.e("Fragment_form_lectura", "❌ Error en conexión Bluetooth", e);
            printExist = false;

            // ✅ Método alternativo (reflexión) - útil para algunas impresoras
            try {
                Log.d("Fragment_form_lectura", "Intentando método alternativo...");
                Method m = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                bluetoothSocket = (BluetoothSocket) m.invoke(bluetoothDevice, 1);
                bluetoothSocket.connect();

                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();

                beginListenForData();

                Log.d("Fragment_form_lectura", "✅ Conectado con método alternativo");
                printExist = true;
            } catch (Exception e2) {
                Log.e("Fragment_form_lectura", "❌ Falló método alternativo", e2);
                throw new IOException("No se pudo conectar a la impresora: " + e.getMessage());
            }
        }
    }

    void beginListenForData() {
        try {
            // ✅ CRÍTICO: Usar Looper del hilo principal
            final Handler handler = new Handler(Looper.getMainLooper());

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            thread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = inputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetByte = new byte[bytesAvailable];
                                inputStream.read(packetByte);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetByte[i];
                                    if (b == delimiter) {

                                        byte[] encodedByte = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedByte, 0,
                                                encodedByte.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedByte, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                Log.d("Fragment_form_lectura", data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Verificar si ya está conectado
    public boolean checkConnection() {
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            Log.d("Fragment_form_lectura", "✅ Socket ya está conectado");
            return true;
        }
        return false;
    }


    // ✅ Cerrar correctamente
    void closeBT() {
        try {
            stopWorker = true;

            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }

            Log.d("Fragment_form_lectura", "Bluetooth cerrado correctamente");
        } catch (Exception e) {
            Log.e("Fragment_form_lectura", "Error cerrando Bluetooth", e);
        }
    }


// Imprimir


    // ✅ CRÍTICO: Ejecutar impresión en hilo separado
    private void preparetoprint(DBOrdenLecturas orden) {


        // 🔹 Verifica que Bluetooth siga activo
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            inicializarBluetooth();
        }

        if (bluetoothDevice == null) {
            FindBluetoothDevice();

            // Esperar un momento para que termine la búsqueda
            new Handler().postDelayed(() -> {
                if (bluetoothDevice != null) {
                    safePrint(orden);
                } else {
                    Toast.makeText(getContext(), "No se encontró la impresora", Toast.LENGTH_LONG).show();
                }
            }, 500);
        } else {
            safePrint(orden);
        }
    }



    private void executePrint(DBOrdenLecturas orden) {
        new Thread(() -> {
            try {
                // ✅ Asegurarse de que el socket y el outputStream estén listos
                if (!checkConnection() || outputStream == null) {
                    Log.d("Fragment_form_lectura", "Socket o OutputStream nulo o cerrado, reconectando...");
                    try {
                        openBT(); // Esto restablece bluetoothSocket, outputStream e inputStream

                        // Esperar hasta que la conexión esté lista (máx 3 segundos)
                        int attempts = 0;
                        while (!checkConnection() && attempts < 30) {
                            Thread.sleep(100);
                            attempts++;
                        }

                        if (!checkConnection() || outputStream == null) {
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "No se pudo conectar a la impresora", Toast.LENGTH_LONG).show()
                            );
                            return; // Abortamos impresión
                        }
                    } catch (IOException e) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Error al conectar a la impresora: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
                        return;
                    }
                }

                if (!printExist) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error: No hay conexión con la impresora", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                // ✅ Preparar datos de impresión
                String dataprint = prepareDataToPrint(orden);

                // ✅ Imprimir
                synchronized (outputStream) {
                    // Inicializar impresora
                    outputStream.write(new byte[]{0x1B, 0x40}); // ESC @

                    // Logo
                    int img = R.drawable.logoprint;
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), img);

                    if (bitmap != null) {
                        byte[] imagen = printPhoto(bitmap);
                        if (imagen != null && imagen.length > 0) {
                            outputStream.write(imagen);
                        }
                        bitmap.recycle();
                    }

                    // Contenido
                    outputStream.write(ESC_ALIGN_LEFT);
                    outputStream.write(FEED_LINE);
                    outputStream.write(dataprint.getBytes("GBK"));
                    outputStream.write(FEED_LINE);
                    outputStream.write(FEED_LINE);

                    outputStream.flush();
                }

                // ✅ Notificar éxito
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "✅ Impresión completada", Toast.LENGTH_SHORT).show()
                );

                Log.d("Fragment_form_lectura", "✅ Impresión exitosa");

            } catch (IOException e) {
                Log.e("Fragment_form_lectura", "❌ Error al imprimir", e);
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error al imprimir: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );

                closeBT();
                printExist = false;

            } catch (InterruptedException e) {
                Log.e("Fragment_form_lectura", "Thread interrumpido", e);
            }
        }).start();
    }


    // ✅ Nueva función auxiliar: preparar datos para imprimir
    private String prepareDataToPrint(DBOrdenLecturas orden) {
        Utilidades.adminSQLiteOpenHelper = adminSQLiteOpenHelper;
        boolean ultimo = Utilidades.ultimaLecturadeContrato(orden.getId(), orden.getSuscriptor());

        StringBuilder lecturas = new StringBuilder();

        if (ultimo || edit) {
            ordenes = adminSQLiteOpenHelper.getData(
                    DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                    "Tipo_orden = 'RUTAS' AND Suscriptor ='" + orden.getSuscriptor() + "'"
            );

            for (DBOrdenLecturas ordenesDB : (List<DBOrdenLecturas>) ordenes) {
                if (ordenesDB.getId().equals(orden.getId())) {
                    ordenesDB = orden;
                }

                if (ordenesDB.getCausa() != null && ordenesDB.getCausa() != 0) {
                    lecturas.append("CAUSA: ").append(ordenesDB.getDescCausa()).append("\r\n");
                } else if (ordenesDB.getObservacion() != null && ordenesDB.getObservacion() != 0) {
                    lecturas.append("OBSERVACIÓN: ").append(ordenesDB.getDescObservacion()).append("\r\n");
                }

                if (ordenesDB.getLectura_actual() != null && ordenesDB.getLectura_actual() != 0) {
                    lecturas.append("LECTURA: ").append(ordenesDB.getLectura_actual()).append("\r\n");
                }
            }
        }

        String header = "CONSTANCIA DE LECTURA\r\n" +
                "Decreto 1942 / 1991\r\n" +
                "----------------------------\r\n";

        return header + "SUSCRIPTOR: " + orden.getSuscriptor() + "\r\n" +
                "FECHA: " + orden.getFinilec() + "\r\n" +
                "NOMBRE: " + orden.getNombre() + "\r\n" +
                "RUTA:" + orden.getRuta() + "\r\n" +
                "CONS: " + orden.getConsecutivoRuta() + "\r\n" +
                "DIRECCIÓN: " + orden.getDireccion() + "\r\n" +
                "MEDIDOR:" + orden.getRef_Medidor() + "\r\n" +
                lecturas.toString() +
                "----------------------------\r\n\r\n\r\n";
    }

    public byte[] printPhoto(Bitmap imagen) {
        try {
            if (imagen == null) {
                Log.e("printPhoto", "❌ La imagen es nula");
                return null;
            }

            Log.d("printPhoto", "📷 Imagen original: " + imagen.getWidth() + "x" + imagen.getHeight());

            // ✅ LÍMITES para formato más rectangular (menos cuadrado)
            int MAX_WIDTH = 384;   // Ancho para 58mm (cambia a 576 para 80mm)
            int MAX_HEIGHT = 200;  // ⬅️ REDUCIDO para hacerlo más rectangular (antes era 255)

            Bitmap processedBitmap = imagen;

            // Redimensionar manteniendo proporción
            if (imagen.getWidth() > MAX_WIDTH || imagen.getHeight() > MAX_HEIGHT) {
                float widthRatio = (float) MAX_WIDTH / imagen.getWidth();
                float heightRatio = (float) MAX_HEIGHT / imagen.getHeight();
                float ratio = Math.min(widthRatio, heightRatio);

                int newWidth = (int) (imagen.getWidth() * ratio);
                int newHeight = (int) (imagen.getHeight() * ratio);

                // Ajustar ancho para que sea múltiplo de 8
                newWidth = (newWidth / 8) * 8;
                if (newWidth < 8) newWidth = 8;

                processedBitmap = Bitmap.createScaledBitmap(imagen, newWidth, newHeight, true);
                Log.d("printPhoto", "✅ Redimensionada a " + newWidth + "x" + newHeight);
            }

            // ✅ Convertir a blanco y negro CON INVERSIÓN DE COLORES
            Bitmap bwBitmap = convertToBlackAndWhiteInverted(processedBitmap);

            Log.d("printPhoto", "✅ Procesando: " + bwBitmap.getWidth() + "x" + bwBitmap.getHeight());

            byte[] command = com.example.systemapp.data.Utils.decodeBitmap(bwBitmap);

            // Limpiar memoria
            if (processedBitmap != imagen) {
                processedBitmap.recycle();
            }
            if (bwBitmap != processedBitmap) {
                bwBitmap.recycle();
            }

            if (command == null) {
                Log.e("printPhoto", "❌ decodeBitmap retornó null");
                return null;
            }

            Log.d("printPhoto", "✅ Comando generado: " + command.length + " bytes");
            return command;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("printPhoto", "❌ Error: " + e.getMessage());
            return null;
        }
    }

    // ✅ NUEVA FUNCIÓN: Convierte a B/N con colores INVERTIDOS
    private Bitmap convertToBlackAndWhiteInverted(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap bwBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);

                // Extraer componentes RGB
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;

                // Calcular luminancia (brillo)
                int luminance = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                // ✅ INVERTIR: si era claro (>127) → negro, si era oscuro → blanco
                int invertedLuminance = 255 - luminance;

                // ✅ AUMENTAR CONTRASTE para mejor definición
                if (invertedLuminance > 127) {
                    invertedLuminance = 255; // Blanco puro
                } else {
                    invertedLuminance = 0;   // Negro puro
                }

                // Crear pixel invertido
                int newPixel = (0xFF << 24) | (invertedLuminance << 16) | (invertedLuminance << 8) | invertedLuminance;
                bwBitmap.setPixel(x, y, newPixel);
            }
        }

        return bwBitmap;
    }

    private void sendEscPosOverBluetooth(final String theBtMacAddress, final byte[] datatoprint) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        }, 1001);
                return;
            }
        }

        new Thread(() -> {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Log.e("ESC_POS", "Bluetooth no disponible o no está habilitado");
                return;
            }

            BluetoothDevice targetDevice = null;
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                Log.d("BLUETOOTH", "Device: " + device.getName() + " - " + device.getAddress());
                if (device.getAddress().equals(theBtMacAddress)) {
                    Log.d("BLUETOOTH", "✅ ¡Dispositivo encontrado! " + device.getName());
                    targetDevice = device;
                    break;
                }
            }

            if (targetDevice == null) {
                Log.e("ESC_POS", "❌ Dispositivo con MAC " + theBtMacAddress + " no está emparejado");
                return;
            }

            BluetoothSocket socket = null;
            OutputStream outputStream = null;

            try {
                // UUID estándar para SPP (Serial Port Profile)
                UUID sppUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                // Cancelar búsqueda antes de conectar
                bluetoothAdapter.cancelDiscovery();

                socket = targetDevice.createRfcommSocketToServiceRecord(sppUUID);
                socket.connect();

                Log.d("ESC_POS", "✅ Conectado a la impresora");

                outputStream = socket.getOutputStream();

                // Inicializar impresora
                outputStream.write(new byte[]{0x1B, 0x40}); // ESC @

                // Enviar los datos
                outputStream.write(datatoprint);

                // Agregar saltos para asegurar impresión
                outputStream.write("\n\n\n".getBytes());

                outputStream.flush();

                // Esperar a que termine de imprimir
                Thread.sleep(1000);

                Log.d("ESC_POS", "✅ Impresión completada");
            } catch (IOException | InterruptedException e) {
                Log.e("ESC_POS", "❌ Error al imprimir", e);
            } finally {
                try {
                    if (outputStream != null) outputStream.close();
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    Log.e("ESC_POS", "⚠️ Error al cerrar recursos", e);
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                // ✅ Todos los permisos fueron concedidos: vuelve a ejecutar la impresión o retoma el flujo
                Log.d("PERMISOS", "Permisos de Bluetooth concedidos");
                // Llama otra vez a tu método aquí si quieres imprimir de nuevo
                // sendEscPosOverBluetooth(macAddress, dataToPrint);
            } else {
                Log.e("PERMISOS", "❌ Permisos de Bluetooth denegados");
                Toast.makeText(getContext(), "Permisos de Bluetooth requeridos para imprimir", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void sendDataFromMotivosNoLectura(Object object, int posSelected) {
        DBListas elementosListasDB = (DBListas) object;

        //asegurar que el comentario esté desbloqueado, se bloqueará de ser necesario en el
        //callback después de elegir la opción 3 de causa 16 de nueva lógica
        bloquear_comentario = false;

        textVmotivov.setText(elementosListasDB.getCodigo() + " - " + elementosListasDB.getDescripcion());
        textVmotivov.setVisibility(View.VISIBLE);
        //txt_lectura_obsval.setVisibility(View.VISIBLE);
        btnLecturaMotivoClose.show();

        //siempre que se selecciona una causa se va a pedir tomar dos fotos
        cantidadFotos = 1;

        //Mensajes para indicar a qué elementos se deben tomar las fotos
        mensajesFotos = new ArrayList<>();
        mensajesFotos.add("Tomar foto claro del motivo");
        //mensajesFotos.add("Tomar foto del Predio");
        //se deshabilita el input y con el, la opción de registrar el dato de lectura
        editTextLectura.setEnabled(false);

        //guardar la última posición elegida
        posDefaultCausa = posSelected;

        orden.setDescCausa(elementosListasDB.getCodigo() + "-" +
                elementosListasDB.getDescripcion());
        //Log.d("LecturasFragment", orden.getDescCausa());

        orden.setCausa(Integer.parseInt(elementosListasDB.getCodigo()));

       /*    if (elementosListasDB.getCodigo().equals(99)){
            ObservacionDialog observacionDialog = ObservacionDialog.getInstance(Fragment_form_lectura.this);

            Bundle bundle = observacionDialog.getArguments();
            bundle.putString("idCausal", "4");
            bundle.putBoolean("bloq_comentario", bloquear_comentario);
            observacionDialog.setArguments(bundle);
            observacionDialog.show(getActivity().getSupportFragmentManager(), "Observdialog");
            orden.setCausa(Integer.parseInt(elementosListasDB.getDescripcion()));

        }else {
               orden.setCausa(Integer.parseInt(elementosListasDB.getCodigo()));
        }*/


        if (orden.getCausa()!=null)
            if (orden.getCausa()==11 || orden.getCausa()==1 ) {

                orden.setCausa(Integer.parseInt(elementosListasDB.getCodigo()));

                CustomDialog customDialog = CustomDialog.getInstance(Fragment_form_lectura.this);
                List<String> list = new ArrayList<>();
                Bundle bundle;

                list.add("Selecciona una opción...");
                list.add("1. Medidor con fuga");
                list.add("2. Daño en registo o talco");
                list.add("3. No registra al realizar prueba");
                list.add("4. Daño Fisico");


                //OP_EL =

                bundle = customDialog.getArguments();
                bundle.putStringArrayList("list", (ArrayList) list);
                customDialog.setArguments(bundle);

                customDialog.show(getActivity().getSupportFragmentManager(), "example dialog");

            }


    }


    //Obtiene el resultado de la elección de opción desde CustomDialog
    @Override
    public void sendDataFromCustomDialog(Object object, int posSelected, String comentario) {
        System.out.println("Aquí recibe "+comentario);

        CustomDialog customDialog = CustomDialog.getInstance(Fragment_form_lectura.this);
        Bundle bundle = customDialog.getArguments();
        List<String> list = new ArrayList<>();
        boolean setcomentario = true;

        if (orden.getCausa()!=null)
            if (orden.getCausa()==11 || orden.getCausa()==1){
                if (comentario.equals("4. Daño Fisico")) {
                    list = new ArrayList<>();
                    list.add("Selecciona opción...");
                    if (orden.getNservic().equals("ACUEDUCTO")){
                        list.add("Sin cabezote");
                        list.add("Con cristal roto");
                        list.add("Con lama");
                    }else{
                        list.add("Con cristal roto");
                        list.add("Quemado");
                        list.add("Con cristal perforado");
                    }

                    list.add("Con dígitos trocados");

                    bundle.putStringArrayList("list", (ArrayList) list);
                    customDialog.setArguments(bundle);

                    customDialog.show(getActivity().getSupportFragmentManager(), "example dialog");

                    if (!comentario.equals("")&&setcomentario)
                    orden.setObservacionGral((orden.getObservacionGral()!=null?orden.getObservacionGral():"")+"-"+comentario);

                    return;
                }
            }


        if (orden.getCritica() == "53-LECTURAS_IGUALES") {

            if (!comentario.equals("")&&setcomentario)
                orden.setObservacionGral((orden.getObservacionGral()!=null?orden.getObservacionGral():"")+"-"+comentario);

        }else {

            String co = (orden.getObservacionGral()!=null?orden.getObservacionGral():"");
            if (!comentario.equals("")&&setcomentario)
                orden.setObservacionGral(((co.length()>0)?co.substring(0, co.lastIndexOf("-")):co)+"-"+comentario);
        }




        if (orden.getCritica() == "53-LECTURAS_IGUALES") {

            if (!edit) {
                //manda a imprimir verificando si es el último registro asociado por contrato
                preparetoprint(orden);
            }
            dispatchTakePictureIntent(orden.getId());

        }

    }

    @Override
    public void sendDataFromObservacionDialog(Object object, int posSelected, String comentario) {

        if (object!=null) {
            DBListas elementosListasDB = (DBListas) object;

            //agregar comentario al objeto orden ¿en qué atributo?
            orden.setObservacionGral(comentario);

            //guardar la última posición elegida
            posDefaultObs = posSelected;

            orden.setDescObservacion(elementosListasDB.getDescripcion() + "-" +
                    elementosListasDB.getCodigo());
            Log.d("LecturasFragment", orden.getDescObservacion());

                   orden.setObservacion(Integer.parseInt(elementosListasDB.getCodigo()));

            }
        else{
            //agregar comentario al objeto orden ¿en qué atributo?
            orden.setObservacionGral(comentario);
        }

        if (orden.getCritica().equals("53-LECTURAS_IGUALES") ) {

            if (!edit) {
                //manda a imprimir verificando si es el último registro asociado por contrato
                preparetoprint(orden);
            }
            dispatchTakePictureIntent(orden.getId());

        }
    }


    public boolean validateObserv30(){

        //actualizar la variable lista allRutasGobal, para obtener la lista de ordenes completas
        if (VariablesSesion.allRutasGobal==null){
            VariablesSesion.setAllRutasGobal(adminSQLiteOpenHelper.getData(
                    DBdefinicionOrdenes.LECTURAS.TABLE_NAME,
                    "Tipo_orden = 'RUTAS'"
            ));
            Log.d("LecturaFragment", "actualiza desde bd ");
        }
        List<DBOrdenLecturas> totalord = RAsignadasFragment.
                getAllOrdeneswithContrato(VariablesSesion.allRutasGobal,
                        orden.getSuscriptor(), orden.getId());
        for (DBOrdenLecturas ordeobs: totalord) {
            try {
                if (ordeobs.getObservacion()==30){
                    return true;
                }
            }catch(NullPointerException e){
                Log.d("LecturaFragment", "null obs "+ordeobs.getObservacion());
                continue;
            }
        }

        return false;

    }

    private void checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        }, 1001);
            }
        }
    }

    private void setupSwipeGesture(View rootView) {
        ScrollView scrollView = rootView.findViewById(R.id.scrollContainer);
        if (scrollView == null) return;

        final View contentView = binding.getRoot();
        final View overlay = binding.overlayView;

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY;
            private float lastX;
            private boolean isHorizontalScroll = false;
            private int screenWidth;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event == null || isProcessing) return false;

                // Obtener ancho de pantalla
                if (screenWidth == 0) {
                    screenWidth = getResources().getDisplayMetrics().widthPixels;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        lastX = startX;
                        initialX = startX;
                        isHorizontalScroll = false;
                        isSwiping = false;
                        return false; // Permitir que otros eventos se procesen

                    case MotionEvent.ACTION_MOVE:
                        float currentX = event.getRawX();
                        float currentY = event.getRawY();
                        float diffX = currentX - startX;
                        float diffY = Math.abs(currentY - startY);

                        // Determinar si es un desplazamiento horizontal
                        if (!isHorizontalScroll && Math.abs(diffX) > 30 && Math.abs(diffX) > diffY * 1.5f) {
                            isHorizontalScroll = true;
                            isSwiping = true;
                            v.getParent().requestDisallowInterceptTouchEvent(true);

                            // Actualizar contenido de las vistas de preview
                            updatePreviewContent();
                        }

                        if (isHorizontalScroll && isSwiping) {
                            // Aplicar límites de desplazamiento
                            float maxTranslation = screenWidth * 0.4f; // Máximo 40% del ancho
                            float translation = diffX * SWIPE_SENSITIVITY;

                            // Limitar el desplazamiento
                            if (translation > maxTranslation) {
                                translation = maxTranslation;
                            } else if (translation < -maxTranslation) {
                                translation = -maxTranslation;
                            }

                            // Aplicar translación al contenido
                            contentView.setTranslationX(translation);
                            currentTranslationX = translation;

                            // ✨ EFECTO CAROUSEL: Mover vistas de preview
                            if (translation > 0) {
                                // Deslizando a la derecha -> mostrar preview ANTERIOR (izquierda)
                                if (posicion > 0 && prevPreviewCard != null) {
                                    prevPreviewCard.setVisibility(View.VISIBLE);
                                    prevPreviewCard.setAlpha(Math.min(translation / (screenWidth * 0.3f), 1f));
                                    prevPreviewCard.setTranslationX(-screenWidth + translation);
                                }
                                // Ocultar preview siguiente
                                if (nextPreviewCard != null) {
                                    nextPreviewCard.setVisibility(View.GONE);
                                }
                            } else if (translation < 0) {
                                // Deslizando a la izquierda -> mostrar preview SIGUIENTE (derecha)
                                if (posicion < allRutas.size() - 1 && nextPreviewCard != null) {
                                    nextPreviewCard.setVisibility(View.VISIBLE);
                                    nextPreviewCard.setAlpha(Math.min(Math.abs(translation) / (screenWidth * 0.3f), 1f));
                                    nextPreviewCard.setTranslationX(screenWidth + translation);
                                }
                                // Ocultar preview anterior
                                if (prevPreviewCard != null) {
                                    prevPreviewCard.setVisibility(View.GONE);
                                }
                            }

                            // Actualizar overlay con efecto de oscurecimiento
                            if (overlay != null) {
                                float progress = Math.abs(translation) / maxTranslation;
                                overlay.setAlpha(progress * 0.3f); // Máximo 30% de oscuridad
                                overlay.setVisibility(View.VISIBLE);
                            }

                            lastX = currentX;
                            return true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isHorizontalScroll && isSwiping) {
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            handleSwipeRelease(screenWidth);
                            isHorizontalScroll = false;
                            isSwiping = false;
                            return true;
                        }
                        break;
                }

                return false;
            }
        });
    }

    /**
     * Configura las vistas de preview para el efecto carousel
     * Crea indicadores visuales que muestran el siguiente/anterior registro mientras deslizas
     */
    private void setupCarouselPreview(View rootView) {
        // Obtener el FrameLayout raíz para agregar las vistas de preview
        ViewGroup rootContainer = (ViewGroup) rootView;
        Context context = getContext();
        if (context == null) return;

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cardWidth = (int) (screenWidth * 0.85f); // 85% del ancho de pantalla
        int cardHeight = (int) (screenWidth * 0.4f); // Altura proporcional

        // Crear vista de preview para el SIGUIENTE registro (derecha)
        nextPreviewCard = createPreviewCard(context, cardWidth, cardHeight);
        nextPreviewText = nextPreviewCard.findViewById(android.R.id.text1);
        nextPreviewCard.setTranslationX(screenWidth); // Posicionar fuera de pantalla (derecha)
        rootContainer.addView(nextPreviewCard);

        // Crear vista de preview para el registro ANTERIOR (izquierda)
        prevPreviewCard = createPreviewCard(context, cardWidth, cardHeight);
        prevPreviewText = prevPreviewCard.findViewById(android.R.id.text1);
        prevPreviewCard.setTranslationX(-screenWidth); // Posicionar fuera de pantalla (izquierda)
        rootContainer.addView(prevPreviewCard);

        // Inicialmente ocultas
        nextPreviewCard.setVisibility(View.GONE);
        prevPreviewCard.setVisibility(View.GONE);
    }

    /**
     * Crea una tarjeta de preview para el carousel
     */
    private View createPreviewCard(Context context, int width, int height) {
        // Crear un CardView programáticamente
        com.google.android.material.card.MaterialCardView card =
            new com.google.android.material.card.MaterialCardView(context);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        params.gravity = android.view.Gravity.CENTER;
        card.setLayoutParams(params);
        card.setCardElevation(12f);
        card.setRadius(24f);
        card.setCardBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));

        // Crear TextView para mostrar información resumida
        TextView textView = new TextView(context);
        textView.setId(android.R.id.text1);
        textView.setTextSize(18);
        textView.setTextColor(android.graphics.Color.parseColor("#333333"));
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setPadding(32, 32, 32, 32);

        card.addView(textView);
        return card;
    }

    /**
     * Actualiza el contenido de las vistas de preview con información del siguiente/anterior registro
     */
    private void updatePreviewContent() {
        if (allRutas == null || allRutas.size() == 0) return;

        // Actualizar preview del SIGUIENTE
        if (posicion < allRutas.size() - 1 && nextPreviewText != null) {
            DBOrdenLecturas nextOrden = (DBOrdenLecturas) allRutas.get(posicion + 1);
            String nextInfo = String.format(
                "#%s\n%s %s\n📍 %s",
                nextOrden.getConsecutivoRuta(),
                nextOrden.getNombre(),
                nextOrden.getApell(),
                nextOrden.getDireccion()
            );
            nextPreviewText.setText(nextInfo);
        }

        // Actualizar preview del ANTERIOR
        if (posicion > 0 && prevPreviewText != null) {
            DBOrdenLecturas prevOrden = (DBOrdenLecturas) allRutas.get(posicion - 1);
            String prevInfo = String.format(
                "#%s\n%s %s\n📍 %s",
                prevOrden.getConsecutivoRuta(),
                prevOrden.getNombre(),
                prevOrden.getApell(),
                prevOrden.getDireccion()
            );
            prevPreviewText.setText(prevInfo);
        }
    }

    /**
     * Maneja el evento cuando el usuario suelta el dedo después de deslizar
     */
    private void handleSwipeRelease(int screenWidth) {
        float threshold = screenWidth * SWIPE_SNAP_THRESHOLD;
        boolean shouldNavigate = Math.abs(currentTranslationX) > threshold;

        if (shouldNavigate) {
            // El usuario deslizó lo suficiente, navegar
            if (currentTranslationX > 0) {
                // Deslizar a la derecha -> ir a anterior
                animateSwipeAndNavigate(true);
            } else {
                // Deslizar a la izquierda -> ir a siguiente
                animateSwipeAndNavigate(false);
            }
        } else {
            // No superó el threshold, volver a la posición original
            animateSwipeCancel();
        }
    }

    /**
     * Anima el swipe completo y navega a la orden anterior/siguiente
     */
    private void animateSwipeAndNavigate(boolean isRight) {
        View contentView = binding.getRoot();
        View overlay = binding.overlayView;

        // Determinar si puede navegar
        boolean canNavigate = isRight ?
            (allRutas.size() > 0 && posicion > 0) :
            (allRutas.size() > 0 && posicion < (allRutas.size() - 1));

        if (!canNavigate) {
            // No puede navegar, cancelar animación
            animateSwipeCancel();
            Toast.makeText(getContext(),
                isRight ? "📄 Primer registro" : "📄 Último registro",
                Toast.LENGTH_SHORT).show();
            return;
        }

        // Haptic feedback
        performHapticFeedback();

        // Animar hacia fuera de la pantalla
        float targetX = isRight ? getResources().getDisplayMetrics().widthPixels :
                                  -getResources().getDisplayMetrics().widthPixels;

        contentView.animate()
            .translationX(targetX)
            .setDuration(200)
            .setInterpolator(new android.view.animation.AccelerateInterpolator())
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    // Navegar
                    if (isRight) {
                        btnBack.performClick();
                    } else {
                        btnNext.performClick();
                    }

                    // Restablecer posición desde el lado opuesto con animación
                    contentView.setTranslationX(-targetX * 0.3f); // Empezar desde el 30% del lado opuesto
                    contentView.animate()
                        .translationX(0)
                        .setDuration(300)
                        .setInterpolator(new OvershootInterpolator(0.8f))
                        .start();

                    // Desvanecer overlay
                    if (overlay != null) {
                        overlay.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    overlay.setVisibility(View.GONE);
                                }
                            })
                            .start();
                    }

                    // Ocultar vistas de preview del carousel
                    hidePreviewCards();

                    currentTranslationX = 0;
                }
            })
            .start();

        // Animar overlay
        if (overlay != null) {
            overlay.animate()
                .alpha(0.5f)
                .setDuration(200)
                .start();
        }

        // Animar vista de preview hacia fuera
        animatePreviewOut(isRight);
    }

    /**
     * Cancela el swipe y vuelve a la posición original con animación de rebote
     */
    private void animateSwipeCancel() {
        View contentView = binding.getRoot();
        View overlay = binding.overlayView;

        // Animar de vuelta a posición original con overshoot
        contentView.animate()
            .translationX(0)
            .setDuration(300)
            .setInterpolator(new OvershootInterpolator(1.2f))
            .start();

        // Desvanecer overlay
        if (overlay != null) {
            overlay.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        overlay.setVisibility(View.GONE);
                    }
                })
                .start();
        }

        // Ocultar vistas de preview con animación
        hidePreviewCards();

        currentTranslationX = 0;
    }

    /**
     * Oculta las tarjetas de preview con animación de fade out
     */
    private void hidePreviewCards() {
        if (nextPreviewCard != null && nextPreviewCard.getVisibility() == View.VISIBLE) {
            nextPreviewCard.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        nextPreviewCard.setVisibility(View.GONE);
                    }
                })
                .start();
        }

        if (prevPreviewCard != null && prevPreviewCard.getVisibility() == View.VISIBLE) {
            prevPreviewCard.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        prevPreviewCard.setVisibility(View.GONE);
                    }
                })
                .start();
        }
    }

    /**
     * Anima la vista de preview hacia fuera de la pantalla
     */
    private void animatePreviewOut(boolean isRight) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        if (isRight && prevPreviewCard != null && prevPreviewCard.getVisibility() == View.VISIBLE) {
            prevPreviewCard.animate()
                .translationX(-screenWidth * 1.2f)
                .alpha(0f)
                .setDuration(200)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        prevPreviewCard.setVisibility(View.GONE);
                        prevPreviewCard.setTranslationX(-screenWidth);
                    }
                })
                .start();
        } else if (!isRight && nextPreviewCard != null && nextPreviewCard.getVisibility() == View.VISIBLE) {
            nextPreviewCard.animate()
                .translationX(screenWidth * 1.2f)
                .alpha(0f)
                .setDuration(200)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        nextPreviewCard.setVisibility(View.GONE);
                        nextPreviewCard.setTranslationX(screenWidth);
                    }
                })
                .start();
        }
    }

    /**
     * Aplica fade in al contenido después de una animación
     */
    private void fadeInContent() {
        View root = binding.getRoot();
        root.setAlpha(0.7f);
        root.animate().alpha(1f).setDuration(200).start();
    }

    /**
     * Anima una sacudida de la vista (cuando no puede avanzar/retroceder)
     */
    private void shakeView(View view) {
        android.animation.ObjectAnimator animator = android.animation.ObjectAnimator.ofFloat(
            view, "translationX", 0f, 25f, -25f, 15f, -15f, 0f
        );
        animator.setDuration(400);
        animator.start();
    }

    /**
     * Proporciona feedback háptico al usuario
     */
    private void performHapticFeedback() {
        View root = binding.getRoot();
        if (root != null) {
            root.performHapticFeedback(
                android.view.HapticFeedbackConstants.VIRTUAL_KEY,
                android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // 🔹 Reiniciar fragmento
        reIniFragment();

        // 🔹 Verificar conexión Bluetooth en un hilo separado
        new Thread(() -> {
            try {
                // Si el socket es nulo o no está conectado
                if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
                    Log.d(TAG, "Socket nulo o cerrado, intentando reconectar Bluetooth...");
                    boolean connected = openBTWithRetry(3000); // 3 segundos máximo

                    if (!connected) {
                        Log.e(TAG, "No se pudo conectar a la impresora");
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "No se pudo conectar a la impresora", Toast.LENGTH_LONG).show()
                        );
                        return;
                    }
                }

                // 🔹 Conexión lista, habilitar outputStream
                if (outputStream != null) {
                    Log.d(TAG, "✅ Bluetooth conectado exitosamente, socket listo");
                } else {
                    Log.e(TAG, "❌ OutputStream nulo, no se puede imprimir");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error al reconectar Bluetooth", e);
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error en conexión Bluetooth", Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    // 🔹 Método para reconectar Bluetooth con retry
    private boolean openBTWithRetry(int timeoutMillis) {
        int attempts = 0;
        int maxAttempts = timeoutMillis / 100; // cada intento 100ms
        while (attempts < maxAttempts) {
            try {
                if (checkConnection()) return true; // ya conectado
                openBT(); // intenta abrir conexión
                if (checkConnection()) return true;
            } catch (IOException e) {
                Log.e(TAG, "Intento " + attempts + " fallido", e);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            attempts++;
        }
        return false;
    }

    // 🔹 Método seguro para imprimir
    private void safePrint(DBOrdenLecturas orden) {
        new Thread(() -> {
            try {
                // 🔹 Verificar si hay conexión
                if (bluetoothSocket == null || !bluetoothSocket.isConnected() || outputStream == null) {
                    Log.d("Fragment_form_lectura", "Socket o OutputStream nulo o cerrado, reconectando...");
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Reconectando impresora...", Toast.LENGTH_SHORT).show()
                    );

                    // Intentar reconectar
                    FindBluetoothDevice();

                    // Esperar hasta 3 segundos a que se establezca la conexión
                    int attempts = 0;
                    while ((bluetoothSocket == null || !bluetoothSocket.isConnected() || outputStream == null) && attempts < 30) {
                        Thread.sleep(100);
                        attempts++;
                    }

                    if (bluetoothSocket == null || !bluetoothSocket.isConnected() || outputStream == null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "No se pudo conectar a la impresora", Toast.LENGTH_LONG).show()
                        );
                        return;
                    }
                }

                // 🔹 Preparar datos de impresión
                String dataprint = prepareDataToPrint(orden);

                // 🔹 Imprimir de manera segura
                synchronized (outputStream) {
                    // Inicializar impresora
                    outputStream.write(new byte[]{0x1B, 0x40}); // ESC @

                    // Logo
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logoprint);
                    if (bitmap != null) {
                        byte[] imagen = printPhoto(bitmap); // tu función para convertir Bitmap a bytes
                        if (imagen != null && imagen.length > 0) {
                            outputStream.write(imagen);
                        }
                        bitmap.recycle();
                    }

                    // Contenido
                    outputStream.write(ESC_ALIGN_LEFT);
                    outputStream.write(FEED_LINE);
                    outputStream.write(dataprint.getBytes("GBK"));
                    outputStream.write(FEED_LINE);
                    outputStream.write(FEED_LINE);
                    outputStream.flush();
                }

                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "✅ Impresión completada", Toast.LENGTH_SHORT).show()
                );

                Log.d("Fragment_form_lectura", "✅ Impresión exitosa");

            } catch (IOException e) {
                Log.e("Fragment_form_lectura", "Error al imprimir", e);
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error al imprimir: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
                printExist = false;
                try { closeBT(); } catch (Exception ex) { ex.printStackTrace(); }

            } catch (InterruptedException e) {
                Log.e("Fragment_form_lectura", "Thread interrumpido", e);
            }
        }).start();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
            outputStream = null;
            inputStream = null;
            stopWorker = true;
        } catch (Exception e) {
            Log.e("Bluetooth", "Error cerrando conexión", e);
        }
        binding = null;
    }
}