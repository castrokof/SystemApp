# Documentación Técnica - SystemApp

## Tabla de Contenidos

1. [Arquitectura de la Aplicación](#arquitectura-de-la-aplicación)
2. [Componentes Principales](#componentes-principales)
3. [Base de Datos](#base-de-datos)
4. [Modelos de Datos](#modelos-de-datos)
5. [Capa de Red y API](#capa-de-red-y-api)
6. [Flujo de Datos](#flujo-de-datos)
7. [Gestión de Sesión](#gestión-de-sesión)
8. [Validaciones](#validaciones)
9. [Impresión](#impresión)
10. [Geolocalización](#geolocalización)

---

## Arquitectura de la Aplicación

SystemApp sigue el patrón de arquitectura **MVVM (Model-View-ViewModel)** combinado con el **Repository Pattern** para la gestión de datos.

### Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Activities  │  │  Fragments   │  │  ViewModels  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                     Data Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Repositories │  │   Data       │  │    Models    │  │
│  │              │  │   Sources    │  │              │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┴───────────────────┐
        ▼                                       ▼
┌──────────────────┐                   ┌──────────────────┐
│   Local Database │                   │   Remote API     │
│     (SQLite)     │                   │    (Retrofit)    │
└──────────────────┘                   └──────────────────┘
```

### Capas de la Aplicación

#### 1. Capa de Presentación (UI Layer)
**Ubicación**: `com.example.systemapp.ui.*`

**Componentes**:
- **MainActivity**: Actividad principal con Navigation Drawer
- **Fragments**: Pantallas de la aplicación
- **ViewModels**: Lógica de presentación y estado de UI
- **Adapters**: Adaptadores para RecyclerView

**Responsabilidades**:
- Renderizar la interfaz de usuario
- Capturar eventos del usuario
- Observar cambios en el ViewModel
- Navegación entre pantallas

#### 2. Capa de Datos (Data Layer)
**Ubicación**: `com.example.systemapp.data.*`

**Componentes**:
- **Repositories**: Coordinan fuentes de datos
- **Data Sources**: Local (SQLite) y Remote (API)
- **Models**: Representación de datos
- **Utilities**: Clases helper

**Responsabilidades**:
- Gestión de persistencia local
- Comunicación con API REST
- Transformación de datos
- Lógica de negocio

---

## Componentes Principales

### 1. MainActivity.java

**Ubicación**: `com.example.systemapp.MainActivity`

**Funcionalidad**:
- Activity principal de la aplicación
- Implementa Navigation Drawer
- Gestiona navegación entre fragments
- Verifica sesión activa
- Maneja cierre de sesión

**Métodos Principales**:

```java
// Verificación de sesión al inicio
@Override
protected void onCreate(Bundle savedInstanceState) {
    if (!SessionPrefs.get(this).isLoggedIn()) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
        return;
    }
}

// Manejo del botón Back
@Override
public void onBackPressed() {
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
        drawer.closeDrawer(GravityCompat.START);
        return;
    }
    // Lógica específica por fragment
}

// Cierre de sesión
@Override
public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.action_settings) {
        SessionPrefs.get(this).logOut();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
```

**Configuración de Navegación**:
- `R.id.nav_ordenes` - Órdenes de lectura
- `R.id.nav_ejecutadas` - Órdenes ejecutadas
- `R.id.nav_borrarruta` - Borrar datos de ruta
- `R.id.nav_sync` - Sincronización
- `R.id.nav_config` - Configuración

---

### 2. LoginActivity.java

**Ubicación**: `com.example.systemapp.ui.login.LoginActivity`

**Funcionalidad**:
- Autenticación de usuarios
- Validación de credenciales
- Almacenamiento de sesión
- Redirección a MainActivity

**Flujo de Autenticación**:

```java
private void attemptLogin() {
    // 1. Validar campos
    String usuario = usernameEditText.getText().toString();
    String password = passwordEditText.getText().toString();

    // 2. Verificar conexión
    if (!isOnline()) {
        showLoginError(getString(R.string.error_network));
        return;
    }

    // 3. Llamada a API
    Call<List<LoginRespuesta>> getUsers =
        systemappAPI.login(new LoginEnvio(usuario, password));

    getUsers.enqueue(new Callback<List<LoginRespuesta>>() {
        @Override
        public void onResponse(...) {
            // 4. Guardar sesión
            SessionPrefs.get(LoginActivity.this)
                .saveUserPref(elemento, usuario);

            // 5. Redirigir a MainActivity
            showMainPanelActivity();
        }
    });
}
```

**SharedPreferences Utilizadas**:
- `PREFERENCE_USUARIO`: Usuario actual
- `PREFERENCE_ESTADO_BUTTON_SESION`: Estado de "Recordar sesión"

---

### 3. fragment_ordenes.java

**Ubicación**: `com.example.systemapp.ui.fragment_ordenes`

**Funcionalidad**:
- Muestra listado de órdenes de lectura
- Tabs de navegación (Pendientes/Reasignadas/Procesadas)
- Integración con base de datos local
- Navegación a formulario de lectura

**Estructura de Tabs**:

```
┌──────────────────────────────────────────────────────┐
│ Bottom Navigation                                     │
│ ┌──────────┐  ┌──────────┐  ┌──────────────┐        │
│ │Pendientes│  │Reasignadas│ │Procesadas    │        │
│ └──────────┘  └──────────┘  └──────────────┘        │
└──────────────────────────────────────────────────────┘
```

**Fragments Internos**:
- `RAsignadasFragment`: Órdenes pendientes/asignadas
- `REjecutadasFragment`: Órdenes procesadas

---

### 4. Fragment_form_lectura.java

**Ubicación**: `com.example.systemapp.ui.data.Fragment_form_lectura`

**Funcionalidad**:
- Formulario de captura de lectura
- Validación de lectura
- Captura de fotografía
- Geolocalización
- Guardado en base de datos local

**Campos del Formulario**:
- Lectura actual (numérica)
- Consumo (calculado automáticamente)
- Fotografía del medidor
- Causa de no lectura (si aplica)
- Observación
- Observación general
- Coordenadas GPS (automáticas)

**Validaciones Implementadas**:
```java
// Ejemplo de validación
if (lecturaActual == lecturaAnterior) {
    critica = "LA=LANT";
}
if (lecturaActual < lecturaAnterior) {
    critica = "LA<LANT";
}
int consumo = lecturaActual - lecturaAnterior;
if (consumo > (promedio * 1.65)) {
    critica = "CA>165CP";
}
```

---

### 5. fragment_sync.java

**Ubicación**: `com.example.systemapp.ui.sync.sync.fragment_sync`

**Funcionalidad**:
- Sincronización bidireccional con servidor
- Descarga de órdenes de lectura
- Descarga de catálogos (listas)
- Envío de lecturas capturadas

**Proceso de Sincronización**:

#### Descarga de Órdenes:
```java
Call<List<DBOrdenLecturas>> call = systemappAPI.cargue();
call.enqueue(new Callback<List<DBOrdenLecturas>>() {
    @Override
    public void onResponse(...) {
        // 1. Recibir órdenes del servidor
        List<DBOrdenLecturas> ordenes = response.body();

        // 2. Guardar en base de datos local
        for (DBOrdenLecturas orden : ordenes) {
            adminSQLiteOpenHelper.insertOrden(orden, false);
        }
    }
});
```

#### Envío de Lecturas:
```java
// 1. Obtener lecturas pendientes de envío
List<DBOrdenLecturas> lecturasEnviar =
    adminSQLiteOpenHelper.getData("lecturas",
        "Uploadlec IS NULL OR Uploadlec = ''");

// 2. Preparar objeto de envío
DBOrdenLecturasEnviar envio = new DBOrdenLecturasEnviar();
envio.setLecturas(lecturasEnviar);

// 3. Enviar al servidor
Call<Object> call = systemappAPI.enviarordenes(envio);
```

---

## Base de Datos

### Esquema de Base de Datos

**Nombre**: `SystemApp`
**Versión**: 3
**Motor**: SQLite

### Tabla: lecturas

**Propósito**: Almacenar todas las órdenes de lectura con su información completa

**DDL**:
```sql
CREATE TABLE lecturas (
    -- Identificación
    id VARCHAR(30) NOT NULL PRIMARY KEY,
    Ciclo VARCHAR(100) NOT NULL,
    Periodo VARCHAR(100) NOT NULL,
    Año VARCHAR(10),

    -- Información de la Orden
    Categoria_orden VARCHAR(30),
    Tipo_orden VARCHAR(30),
    Estado VARCHAR(50) NOT NULL,

    -- Información del Medidor
    Ref_Medidor VARCHAR(100) NOT NULL,
    Suscriptor VARCHAR(100) NOT NULL,

    -- Información del Cliente
    Nombre VARCHAR(100) NOT NULL,
    Apell VARCHAR(100) NOT NULL,
    Direccion VARCHAR(100) NOT NULL,

    -- Información de Ruta
    id_Ruta VARCHAR(100) NOT NULL,
    Ruta VARCHAR(100) NOT NULL,
    consecutivoRuta VARCHAR(100) NOT NULL,
    Usuario VARCHAR(100) NOT NULL,

    -- Información de Servicio
    cservic VARCHAR(15),      -- Código: "0040"
    nservic VARCHAR(15),      -- Nombre: "ACUEDUCTO"
    ctipcon VARCHAR(15),      -- Código: "16"
    ntipcon VARCHAR(16),      -- Nombre: "ACUEDUCTO"

    -- Lecturas y Consumo
    LA VARCHAR(100),          -- Lectura Anterior
    Promedio INTEGER,
    Lectura_actual INTEGER,
    Consumo INTEGER,
    Tope VARCHAR(20) NOT NULL,

    -- Estado de Lectura
    Estado_lectura VARCHAR(15),
    Uploadlec VARCHAR(15),
    finilec TEXT,             -- Fecha inicio lectura
    ffinlec TEXT,             -- Fecha fin lectura

    -- Validaciones y Críticas
    Critica VARCHAR(100),

    -- Causas y Observaciones
    Causa INTEGER,
    DescCausa TEXT,
    Observacion INTEGER,
    DescObservacion TEXT,
    ObservacionGral TEXT,

    -- Geolocalización
    latitud TEXT,
    longitud TEXT,

    -- Fotografía
    ruta_foto TEXT
);
```

**Índices Recomendados** (no implementados actualmente):
```sql
CREATE INDEX idx_ruta ON lecturas(Ruta);
CREATE INDEX idx_estado ON lecturas(Estado);
CREATE INDEX idx_uploadlec ON lecturas(Uploadlec);
CREATE INDEX idx_categoria ON lecturas(Categoria_orden);
```

### Tabla: listas

**Propósito**: Almacenar catálogos de causas, observaciones y otras listas

**DDL**:
```sql
CREATE TABLE listas (
    marca_id VARCHAR(100) NOT NULL,
    codigo VARCHAR(30) NOT NULL,
    descripcion VARCHAR(100) NOT NULL,
    PRIMARY KEY (marca_id, codigo)
);
```

**Grupos de Listas** (marca_id):
- Causas de no lectura
- Observaciones
- Otros catálogos

---

## Modelos de Datos

### DBOrdenLecturas.java

**Ubicación**: `com.example.systemapp.data.model.DBOrdenLecturas`

**Propósito**: Representar una orden de lectura completa

**Atributos Principales**:

```java
public class DBOrdenLecturas {
    // Identificación
    public String id;
    public String Ciclo;
    public String Periodo;
    public String Año;

    // Orden
    public String Categoria_orden;    // ASIGNADAS, REASIGNADAS
    public String Tipo_orden;         // RUTAS
    public String Estado;

    // Medidor
    public String Ref_Medidor;
    public String Suscriptor;

    // Cliente
    public String Nombre;
    public String Apell;
    public String Direccion;

    // Ruta
    public String id_Ruta;
    public String Ruta;
    public String consecutivoRuta;
    public String Usuario;

    // Servicio
    public String cservic;            // "0040"
    public String nservic;            // "ACUEDUCTO"
    public String ctipcon;            // "16"
    public String ntipcon;            // "ACUEDUCTO"

    // Lectura
    public String LA;                 // Lectura Anterior
    public Integer Promedio;
    public Integer Lectura_actual;
    public Integer Consumo;
    public String Tope;

    // Estado
    public String Estado_lectura;
    public String Uploadlec;          // Flag de sincronización
    public String finilec;            // Timestamp inicio
    public String ffinlec;            // Timestamp fin

    // Validaciones
    public String Critica;

    // Causas
    public Integer Causa;
    public String DescCausa;
    public Integer Observacion;
    public String DescObservacion;
    public String ObservacionGral;

    // GPS
    public String latitud;
    public String longitud;

    // Foto
    public String ruta_foto;
}
```

**Estados de Orden**:
- `ASIGNADAS`: Orden nueva asignada al usuario
- `REASIGNADAS`: Orden reasignada
- `PROCESADAS`: Orden con lectura capturada

**Estados de Lectura**:
- `null` o `""`: Pendiente
- `FINALIZADA`: Lectura completada
- `EN_PROCESO`: Lectura en progreso

---

### DBListas.java

**Ubicación**: `com.example.systemapp.data.model.DBListas`

**Propósito**: Representar elementos de catálogos

```java
public class DBListas {
    private String marca_id;        // Grupo/Categoría
    private String codigo;          // Código único
    private String descripcion;     // Descripción
}
```

---

### LoginRespuesta.java

**Ubicación**: `com.example.systemapp.data.model.LoginRespuesta`

**Propósito**: Respuesta del endpoint de autenticación

```java
public class LoginRespuesta {
    private String id;
    private String usuario;
    private String nombre;
    private String tipodeusuario;
    private String email;
    private String empresa;
    private String remenber_token;
    private String estado;
    private String created_at;

    @SerializedName("api_token")
    private String api_token;       // Token de autenticación
}
```

---

### DBdefinicionOrdenes.java

**Ubicación**: `com.example.systemapp.data.model.DBdefinicionOrdenes`

**Propósito**: Definir estructura de base de datos

```java
public class DBdefinicionOrdenes {
    public static final String DATABASE_NAME = "SystemApp";
    public static final int DATABASE_VERSION = 3;

    public static class LECTURAS {
        public static final String TABLE_NAME = "lecturas";
        // Definición de nombres de columnas...
    }

    public static class LISTAS {
        public static final String TABLE_NAME = "listas";
        // Definición de nombres de columnas...
    }

    // Sentencias SQL
    public static final String ORDENES_TABLE_CREATE = "...";
    public static final String ORDENES_TABLE_DROP = "...";
    public static final String LISTAS_TABLE_CREATE = "...";
    public static final String LISTAS_TABLE_DROP = "...";
}
```

---

## Capa de Red y API

### SystemAppAPI.java

**Ubicación**: `com.example.systemapp.SystemAppAPI`

**Propósito**: Definir endpoints de la API REST usando Retrofit

```java
public interface SystemAppAPI {

    String BASE_URL = "https://manteliviano.com/AquaProgrammerData/api/";

    // Autenticación
    @POST("loginMovil1")
    Call<List<LoginRespuesta>> login(@Body LoginEnvio loginEnvio);

    // Descarga de órdenes
    @POST("medidoresout")
    Call<List<DBOrdenLecturas>> cargue();

    // Descarga de catálogos
    @POST("marcas")
    Call<List<DBListas>> listas();

    // Envío de lecturas
    @POST("medidores")
    Call<Object> enviarordenes(@Body DBOrdenLecturasEnviar lecturas);
}
```

### AuthInterceptor.java

**Ubicación**: `com.example.systemapp.AuthInterceptor`

**Propósito**: Interceptor para agregar token de autenticación a todas las peticiones

```java
public class AuthInterceptor implements Interceptor {

    private String apiToken;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder requestBuilder = original.newBuilder()
            .header("Authorization", "Bearer " + apiToken)
            .header("Accept", "application/json")
            .method(original.method(), original.body());

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}
```

### Configuración de Retrofit

```java
// En LoginActivity o fragment_sync

// Sin autenticación (para login)
Retrofit systemapp = new Retrofit.Builder()
    .baseUrl(SystemAppAPI.BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build();

// Con autenticación (para otras operaciones)
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(new AuthInterceptor(apiToken))
    .build();

Retrofit systemapp = new Retrofit.Builder()
    .baseUrl(SystemAppAPI.BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build();
```

---

## Flujo de Datos

### 1. Flujo de Autenticación

```
┌──────────────┐
│ LoginActivity│
└───────┬──────┘
        │
        │ 1. Ingresa credenciales
        ▼
┌────────────────────┐
│ attemptLogin()     │
│ - Validar campos   │
│ - Verificar red    │
└────────┬───────────┘
         │
         │ 2. POST /loginMovil1
         ▼
┌────────────────────┐
│ Servidor API       │
│ - Valida usuario   │
│ - Genera token     │
└────────┬───────────┘
         │
         │ 3. LoginRespuesta + api_token
         ▼
┌────────────────────┐
│ SessionPrefs       │
│ - Guarda token     │
│ - Guarda usuario   │
└────────┬───────────┘
         │
         │ 4. Redirige
         ▼
┌────────────────────┐
│ MainActivity       │
└────────────────────┘
```

### 2. Flujo de Sincronización (Descarga)

```
┌──────────────┐
│ fragment_sync│
└───────┬──────┘
        │
        │ 1. Click "Descargar"
        ▼
┌────────────────────┐
│ Verificar conexión │
└────────┬───────────┘
         │
         ├─────────────────────┬─────────────────────┐
         │                     │                     │
         │ 2a. GET órdenes     │ 2b. GET listas     │
         ▼                     ▼                     │
┌──────────────┐      ┌──────────────┐             │
│POST          │      │POST          │             │
│/medidoresout │      │/marcas       │             │
└──────┬───────┘      └──────┬───────┘             │
       │                     │                     │
       │                     │                     │
       ▼                     ▼                     │
┌────────────────────────────────────────┐        │
│ Servidor API                            │        │
│ - Filtra por usuario                    │        │
│ - Retorna datos                         │        │
└────────┬───────────────────────────────┘        │
         │                                         │
         │ 3. Array de datos                      │
         ▼                                         │
┌────────────────────────────────────────┐        │
│ AdminSQLiteOpenHelper                   │        │
│ - Elimina datos antiguos (opcional)     │        │
│ - Inserta nuevos registros              │        │
└────────┬───────────────────────────────┘        │
         │                                         │
         │ 4. Confirmación                        │
         ▼                                         │
┌────────────────────────────────────────┐        │
│ UI - Mostrar resultado                  │        │
│ "X órdenes descargadas"                 │        │
└─────────────────────────────────────────┘       │
```

### 3. Flujo de Captura de Lectura

```
┌──────────────────┐
│ fragment_ordenes │
│ - Lista órdenes  │
└────────┬─────────┘
         │
         │ 1. Click en orden
         ▼
┌──────────────────────┐
│Fragment_form_lectura │
│ - Carga datos orden  │
└────────┬─────────────┘
         │
         │ 2. Usuario captura
         ├──────────────┬──────────────┬──────────────┐
         │              │              │              │
         ▼              ▼              ▼              ▼
┌──────────┐    ┌────────────┐  ┌─────────┐   ┌─────────┐
│ Lectura  │    │ Fotografía │  │   GPS   │   │ Causas/ │
│ actual   │    │            │  │         │   │  Obs.   │
└──────────┘    └────────────┘  └─────────┘   └─────────┘
         │              │              │              │
         └──────────────┴──────────────┴──────────────┘
                        │
                        │ 3. Validar lectura
                        ▼
                ┌───────────────┐
                │ Validador     │
                │ - LA vs LANT  │
                │ - Consumo     │
                │ - Promedio    │
                └───────┬───────┘
                        │
                        │ 4. Calcular consumo
                        ▼
                ┌───────────────┐
                │ Consumo =     │
                │ LA - LANT     │
                └───────┬───────┘
                        │
                        │ 5. Guardar en BD
                        ▼
                ┌────────────────────┐
                │AdminSQLiteOpen     │
                │Helper.insertOrden()│
                │ update = true      │
                └────────┬───────────┘
                         │
                         │ 6. Marca como procesada
                         ▼
                ┌─────────────────────┐
                │ Categoria_orden =   │
                │ "PROCESADAS"        │
                │ Uploadlec = null    │
                └─────────────────────┘
```

### 4. Flujo de Envío de Lecturas

```
┌──────────────┐
│ fragment_sync│
└───────┬──────┘
        │
        │ 1. Click "Enviar"
        ▼
┌────────────────────────────┐
│ Consultar BD local         │
│ WHERE Uploadlec IS NULL    │
└────────┬───────────────────┘
         │
         │ 2. Lecturas pendientes
         ▼
┌────────────────────────────┐
│ Preparar objeto            │
│ DBOrdenLecturasEnviar      │
└────────┬───────────────────┘
         │
         │ 3. POST /medidores
         ▼
┌────────────────────────────┐
│ Servidor API               │
│ - Valida datos             │
│ - Procesa lecturas         │
│ - Retorna confirmación     │
└────────┬───────────────────┘
         │
         │ 4. Respuesta exitosa
         ▼
┌────────────────────────────┐
│ Actualizar BD local        │
│ SET Uploadlec = 'SI'       │
└────────┬───────────────────┘
         │
         │ 5. Notificar usuario
         ▼
┌────────────────────────────┐
│ "X lecturas enviadas"      │
└────────────────────────────┘
```

---

## Gestión de Sesión

### SessionPrefs.java

**Ubicación**: `com.example.systemapp.data.SessionPrefs`

**Propósito**: Gestionar la sesión del usuario usando SharedPreferences

**Métodos Principales**:

```java
public class SessionPrefs {

    private static final String PREF_NAME = "SYSTEMAPP_PREFS";
    private static final String KEY_IS_LOGGED_IN = "PREF_IS_LOGGED_IN";
    private static final String KEY_API_TOKEN = "PREF_API_TOKEN";
    private static final String KEY_USER_NAME = "PREF_USER_NAME";

    // Singleton
    public static SessionPrefs get(Context context) {
        if (instance == null) {
            instance = new SessionPrefs(context);
        }
        return instance;
    }

    // Guardar sesión
    public void saveUserPref(LoginRespuesta user, String username) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_API_TOKEN, user.getApiToken())
            .putString(KEY_USER_NAME, username)
            .apply();
    }

    // Verificar sesión
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Obtener token
    public String getApiToken() {
        return prefs.getString(KEY_API_TOKEN, null);
    }

    // Cerrar sesión
    public void logOut() {
        prefs.edit().clear().apply();
    }
}
```

**Datos Almacenados**:
- `PREF_IS_LOGGED_IN`: Estado de sesión (boolean)
- `PREF_API_TOKEN`: Token de autenticación
- `PREF_USER_NAME`: Nombre de usuario
- `PREF_USER_ID`: ID del usuario
- `PREF_USER_EMAIL`: Email del usuario

---

## Validaciones

### Constants.java

**Ubicación**: `com.example.systemapp.data.Constants`

**Definición de Validaciones**:

```java
public class Constants {

    // Códigos de validación de lectura
    public static final String VALIDACION1 = "LA=LANT";
    public static final String VALIDACION2 = "LA<LANT";
    public static final String VALIDACION3 = "CA>165CP";
    public static final String VALIDACION4 = "CA<35CP";
    public static final String VALIDACION5 = "CA<50CP";

    // Estados
    public static final String RESPONSE_CODE_STATUS_ERROR = "ERROR";
    public static final String RESPONSE_CODE_STATUS_OK = "OK";
}
```

### Implementación de Validaciones

```java
// En Fragment_form_lectura

private String validarLectura(int lecturaActual, int lecturaAnterior,
                              int promedio) {

    String critica = "";

    // Validación 1: Lectura igual a anterior
    if (lecturaActual == lecturaAnterior) {
        critica = Constants.VALIDACION1;  // "LA=LANT"
        return critica;
    }

    // Validación 2: Lectura menor a anterior
    if (lecturaActual < lecturaAnterior) {
        critica = Constants.VALIDACION2;  // "LA<LANT"
        return critica;
    }

    // Calcular consumo
    int consumo = lecturaActual - lecturaAnterior;

    // Validación 3: Consumo > 165% del promedio
    if (consumo > (promedio * 1.65)) {
        critica = Constants.VALIDACION3;  // "CA>165CP"
    }

    // Validación 4: Consumo < 35% del promedio
    else if (consumo < (promedio * 0.35)) {
        critica = Constants.VALIDACION4;  // "CA<35CP"
    }

    // Validación 5: Consumo < 50% del promedio
    else if (consumo < (promedio * 0.50)) {
        critica = Constants.VALIDACION5;  // "CA<50CP"
    }

    return critica;
}
```

### Validador.java

**Ubicación**: `com.example.systemapp.data.Validador`

**Funciones de Validación Adicionales**:
- Validación de formato de datos
- Validación de rangos numéricos
- Validación de campos requeridos

---

## Impresión

### PrinterUtils.java

**Ubicación**: `com.example.systemapp.data.PrinterUtils`

**Funcionalidad**:
- Gestión de conexión Bluetooth
- Búsqueda de impresoras
- Envío de comandos de impresión

### PrinterCommands.java

**Ubicación**: `com.example.systemapp.data.PrinterCommands`

**Funcionalidad**:
- Generación de comandos ESC/POS
- Formato de recibos
- Control de impresora

**Formato de Recibo**:
```
========================================
         COMPAÑÍA DE ACUEDUCTO
========================================
Suscriptor: 123456
Nombre: Juan Pérez
Dirección: Calle 123 #45-67
----------------------------------------
Lectura Anterior:    12345 m³
Lectura Actual:      12380 m³
Consumo:                35 m³
----------------------------------------
Fecha: 2024-01-15 10:30:00
Técnico: usuario123
========================================
```

**Librerías Utilizadas**:
- `posprinterconnectandsendsdk.jar`: SDK de conexión
- `SDKLib.jar`: SDK de comandos
- `commons-io-2.2.jar`: Operaciones de I/O

---

## Geolocalización

### Permisos Requeridos

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

### Captura de Coordenadas

```java
// En Fragment_form_lectura

private void obtenerUbicacion() {
    if (checkLocationPermission()) {
        LocationManager locationManager =
            (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);

        Location location = locationManager
            .getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            double latitud = location.getLatitude();
            double longitud = location.getLongitude();

            // Guardar en objeto orden
            orden.setLatitud(String.valueOf(latitud));
            orden.setLongitud(String.valueOf(longitud));
        }
    }
}
```

### Almacenamiento de Coordenadas

Las coordenadas se almacenan como TEXT en la base de datos:
- Campo `latitud`: Latitud en formato decimal
- Campo `longitud`: Longitud en formato decimal

---

## Gestión de Fotografías

### GuardarFotos.java

**Ubicación**: `com.example.systemapp.data.GuardarFotos`

**Funcionalidad**:
- Captura de fotografías usando la cámara
- Almacenamiento en directorio de la app
- Compresión de imágenes
- Gestión de rutas de archivo

### FileProvider

**Configuración** en `AndroidManifest.xml`:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="com.example.systemapp.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### Flujo de Captura de Foto

```java
// 1. Crear archivo para la foto
File photoFile = createImageFile();

// 2. Obtener URI usando FileProvider
Uri photoURI = FileProvider.getUriForFile(
    context,
    "com.example.systemapp.fileprovider",
    photoFile
);

// 3. Lanzar intent de cámara
Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

// 4. Guardar ruta en BD
orden.setRuta_foto(photoFile.getAbsolutePath());
```

---

## Manejo de Errores

### Errores de Red

```java
@Override
public void onFailure(Call<T> call, Throwable t) {
    if (t instanceof IOException) {
        // Error de conectividad
        showError("Error de conexión. Verifique su internet.");
    } else {
        // Error de conversión u otros
        showError("Error: " + t.getMessage());
    }
}
```

### Errores de API

```java
@Override
public void onResponse(Call<T> call, Response<T> response) {
    if (!response.isSuccessful()) {
        switch (response.code()) {
            case 400:
                showError("Datos inválidos");
                break;
            case 401:
                showError("No autorizado. Inicie sesión nuevamente.");
                // Redirigir a login
                break;
            case 404:
                showError("Recurso no encontrado");
                break;
            case 500:
                showError("Error del servidor");
                break;
            default:
                showError("Error: " + response.message());
        }
    }
}
```

### Errores de Base de Datos

```java
try {
    Long result = adminSQLiteOpenHelper.insertOrden(orden, false);
    if (result == -1) {
        showError("Error al guardar en base de datos");
    }
} catch (Exception e) {
    Log.e(TAG, "Error BD: " + e.getMessage());
    showError("Error al procesar datos");
}
```

---

## Optimizaciones y Mejores Prácticas

### Recomendaciones de Mejora

1. **Implementar WorkManager** para sincronización en segundo plano
2. **Agregar índices** a la base de datos SQLite
3. **Implementar paginación** en listados de órdenes
4. **Usar Coroutines/RxJava** para operaciones asíncronas
5. **Implementar Room** en lugar de SQLite directo
6. **Agregar pruebas unitarias** y de integración
7. **Implementar logging** centralizado (Timber)
8. **Agregar analytics** para monitoreo de uso
9. **Implementar crashlytics** para reporte de errores
10. **Mejorar manejo de imágenes** (Glide/Picasso)

### Seguridad

1. **Cifrar base de datos** local (SQLCipher)
2. **Implementar certificate pinning** para HTTPS
3. **Ofuscar código** con ProGuard/R8
4. **Validar certificados** SSL
5. **No almacenar contraseñas** en SharedPreferences

---

## Apéndices

### A. Estructura de Respuestas API

#### Login Response
```json
[
  {
    "id": "123",
    "usuario": "usuario1",
    "nombre": "Juan Pérez",
    "tipodeusuario": "TECNICO",
    "email": "juan@example.com",
    "empresa": "Acueducto XYZ",
    "remenber_token": null,
    "estado": "ACTIVO",
    "created_at": "2024-01-01 00:00:00",
    "api_token": "eyJ0eXAiOiJKV1QiLCJhbGci..."
  }
]
```

#### Órdenes Response
```json
[
  {
    "id": "ORD001",
    "Ciclo": "202401",
    "Periodo": "ENERO 2024",
    "Ref_Medidor": "MED12345",
    "Suscriptor": "SUB001",
    "Nombre": "María",
    "Apell": "García",
    "Direccion": "Calle 10 #20-30",
    "Ruta": "R001",
    "consecutivoRuta": "001",
    "LA": "1500",
    "Promedio": 25,
    "Usuario": "usuario1",
    "Estado": "ACTIVO",
    "Tope": "9999",
    "Año": "2024",
    "id_Ruta": "1"
  }
]
```

### B. Glosario

- **LA**: Lectura Anterior
- **LANT**: Lectura Anterior (abreviación)
- **CA**: Consumo Actual
- **CP**: Consumo Promedio
- **API Token**: Token de autenticación JWT
- **Suscriptor**: Código único del cliente
- **Ref_Medidor**: Referencia del medidor
- **Ruta**: Código de ruta de lectura

---

**Versión del Documento**: 1.0
**Última Actualización**: 2024
**Mantenedor**: Equipo de Desarrollo SystemApp
