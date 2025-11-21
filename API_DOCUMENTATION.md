# Documentación de API - SystemApp

## Información General

### Base URL
```
https://manteliviano.com/AquaProgrammerData/api/
```

### Protocolo
- HTTPS (TLS 1.2+)

### Formato de Datos
- **Request**: JSON (application/json)
- **Response**: JSON (application/json)

### Autenticación
- **Tipo**: Bearer Token (JWT)
- **Header**: `Authorization: Bearer {api_token}`

### Códigos de Estado HTTP

| Código | Significado | Descripción |
|--------|-------------|-------------|
| 200 | OK | Petición exitosa |
| 201 | Created | Recurso creado exitosamente |
| 400 | Bad Request | Datos inválidos o mal formados |
| 401 | Unauthorized | No autenticado o token inválido |
| 403 | Forbidden | No autorizado para este recurso |
| 404 | Not Found | Recurso no encontrado |
| 422 | Unprocessable Entity | Validación fallida |
| 500 | Internal Server Error | Error del servidor |
| 503 | Service Unavailable | Servicio no disponible |

---

## Tabla de Contenidos

1. [Autenticación](#autenticación)
2. [Endpoints de Órdenes](#endpoints-de-órdenes)
3. [Endpoints de Catálogos](#endpoints-de-catálogos)
4. [Endpoints de Lecturas](#endpoints-de-lecturas)
5. [Modelos de Datos](#modelos-de-datos)
6. [Códigos de Error](#códigos-de-error)
7. [Ejemplos de Uso](#ejemplos-de-uso)

---

## Autenticación

### POST /loginMovil1

Autentica un usuario y obtiene un token de acceso.

#### Request

**Headers:**
```http
Content-Type: application/json
Accept: application/json
```

**Body:**
```json
{
  "usuario": "string",
  "password": "string"
}
```

**Parámetros:**

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| usuario | string | Sí | Nombre de usuario |
| password | string | Sí | Contraseña del usuario |

#### Response

**Success (200 OK):**
```json
[
  {
    "id": "123",
    "usuario": "usuario1",
    "nombre": "Juan",
    "tipodeusuario": "TECNICO",
    "email": "juan@example.com",
    "empresa": "Acueducto XYZ",
    "remenber_token": null,
    "estado": "ACTIVO",
    "created_at": "2024-01-01 00:00:00",
    "api_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
  }
]
```

**Campos de Respuesta:**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | string | ID único del usuario |
| usuario | string | Nombre de usuario |
| nombre | string | Nombre completo |
| tipodeusuario | string | Tipo: ADMIN, TECNICO, SUPERVISOR |
| email | string | Correo electrónico |
| empresa | string | Empresa asociada |
| remenber_token | string/null | Token de recordar sesión |
| estado | string | Estado: ACTIVO, INACTIVO |
| created_at | string | Fecha de creación (ISO 8601) |
| api_token | string | Token JWT para autenticación |

**Error (401 Unauthorized):**
```json
{
  "error": "Credenciales inválidas",
  "code": "AUTH_FAILED"
}
```

**Error (422 Unprocessable Entity):**
```json
{
  "error": "Datos de validación fallidos",
  "errors": {
    "usuario": ["El campo usuario es requerido"],
    "password": ["El campo password es requerido"]
  }
}
```

#### Ejemplo cURL

```bash
curl -X POST \
  https://manteliviano.com/AquaProgrammerData/api/loginMovil1 \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "usuario": "usuario1",
    "password": "mipassword123"
  }'
```

#### Ejemplo Android (Retrofit)

```java
LoginEnvio credentials = new LoginEnvio("usuario1", "mipassword123");

Call<List<LoginRespuesta>> call = systemappAPI.login(credentials);

call.enqueue(new Callback<List<LoginRespuesta>>() {
    @Override
    public void onResponse(Call<List<LoginRespuesta>> call,
                          Response<List<LoginRespuesta>> response) {
        if (response.isSuccessful()) {
            LoginRespuesta user = response.body().get(0);
            String token = user.getApiToken();
            // Guardar token para futuras peticiones
        }
    }

    @Override
    public void onFailure(Call<List<LoginRespuesta>> call, Throwable t) {
        // Manejar error
    }
});
```

---

## Endpoints de Órdenes

### POST /medidoresout

Obtiene las órdenes de lectura asignadas al usuario autenticado.

#### Request

**Headers:**
```http
Authorization: Bearer {api_token}
Content-Type: application/json
Accept: application/json
```

**Body:** (Vacío)

#### Response

**Success (200 OK):**
```json
[
  {
    "id": "ORD001",
    "Ciclo": "202401",
    "Periodo": "ENERO 2024",
    "Año": "2024",
    "Ref_Medidor": "MED12345",
    "Suscriptor": "SUB001",
    "Nombre": "María",
    "Apell": "García",
    "Direccion": "Calle 10 #20-30",
    "Ruta": "R001",
    "consecutivoRuta": "001",
    "id_Ruta": "1",
    "Usuario": "usuario1",
    "Estado": "ACTIVO",
    "LA": "1500",
    "Promedio": 25,
    "Tope": "9999"
  },
  {
    "id": "ORD002",
    "Ciclo": "202401",
    "Periodo": "ENERO 2024",
    "Año": "2024",
    "Ref_Medidor": "MED12346",
    "Suscriptor": "SUB002",
    "Nombre": "Pedro",
    "Apell": "Martínez",
    "Direccion": "Carrera 5 #15-20",
    "Ruta": "R001",
    "consecutivoRuta": "002",
    "id_Ruta": "1",
    "Usuario": "usuario1",
    "Estado": "ACTIVO",
    "LA": "2340",
    "Promedio": 30,
    "Tope": "9999"
  }
]
```

**Campos de Respuesta:**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | string | ID único de la orden |
| Ciclo | string | Ciclo de facturación (YYYYMM) |
| Periodo | string | Periodo descriptivo |
| Año | string | Año de la lectura |
| Ref_Medidor | string | Referencia del medidor |
| Suscriptor | string | Código de suscriptor |
| Nombre | string | Nombre del cliente |
| Apell | string | Apellido del cliente |
| Direccion | string | Dirección del medidor |
| Ruta | string | Código de ruta |
| consecutivoRuta | string | Orden dentro de la ruta |
| id_Ruta | string | ID de la ruta |
| Usuario | string | Usuario asignado |
| Estado | string | Estado: ACTIVO, SUSPENDIDO |
| LA | string | Lectura anterior |
| Promedio | integer | Consumo promedio mensual |
| Tope | string | Valor máximo del medidor |

**Error (401 Unauthorized):**
```json
{
  "error": "Token inválido o expirado",
  "code": "TOKEN_INVALID"
}
```

**Error (404 Not Found):**
```json
{
  "error": "No hay órdenes asignadas",
  "code": "NO_ORDERS"
}
```

#### Ejemplo cURL

```bash
curl -X POST \
  https://manteliviano.com/AquaProgrammerData/api/medidoresout \
  -H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJh...' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json'
```

#### Ejemplo Android (Retrofit)

```java
// Configurar interceptor con token
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(new AuthInterceptor(apiToken))
    .build();

Retrofit retrofit = new Retrofit.Builder()
    .baseUrl(SystemAppAPI.BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build();

SystemAppAPI api = retrofit.create(SystemAppAPI.class);

Call<List<DBOrdenLecturas>> call = api.cargue();

call.enqueue(new Callback<List<DBOrdenLecturas>>() {
    @Override
    public void onResponse(Call<List<DBOrdenLecturas>> call,
                          Response<List<DBOrdenLecturas>> response) {
        if (response.isSuccessful()) {
            List<DBOrdenLecturas> ordenes = response.body();
            // Guardar en base de datos local
            for (DBOrdenLecturas orden : ordenes) {
                db.insertOrden(orden, false);
            }
        }
    }

    @Override
    public void onFailure(Call<List<DBOrdenLecturas>> call, Throwable t) {
        // Manejar error
    }
});
```

---

## Endpoints de Catálogos

### POST /marcas

Obtiene los catálogos (listas) de causas, observaciones y otros valores predefinidos.

#### Request

**Headers:**
```http
Authorization: Bearer {api_token}
Content-Type: application/json
Accept: application/json
```

**Body:** (Vacío)

#### Response

**Success (200 OK):**
```json
[
  {
    "marca_id": "CAUSAS",
    "codigo": "01",
    "descripcion": "Predio cerrado"
  },
  {
    "marca_id": "CAUSAS",
    "codigo": "02",
    "descripcion": "Medidor tapado"
  },
  {
    "marca_id": "CAUSAS",
    "codigo": "03",
    "descripcion": "Medidor roto"
  },
  {
    "marca_id": "CAUSAS",
    "codigo": "04",
    "descripcion": "No hay medidor"
  },
  {
    "marca_id": "OBSERVACIONES",
    "codigo": "01",
    "descripcion": "Fuga visible"
  },
  {
    "marca_id": "OBSERVACIONES",
    "codigo": "02",
    "descripcion": "Medidor en mal estado"
  },
  {
    "marca_id": "OBSERVACIONES",
    "codigo": "03",
    "descripcion": "Conexión irregular"
  }
]
```

**Campos de Respuesta:**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| marca_id | string | Grupo/categoría del catálogo |
| codigo | string | Código único dentro del grupo |
| descripcion | string | Descripción del elemento |

**Grupos (marca_id):**

- **CAUSAS**: Motivos de no lectura
- **OBSERVACIONES**: Observaciones predefinidas
- **ESTADOS_MEDIDOR**: Estados del medidor
- **TIPOS_CONEXION**: Tipos de conexión

**Error (401 Unauthorized):**
```json
{
  "error": "Token inválido o expirado",
  "code": "TOKEN_INVALID"
}
```

#### Ejemplo cURL

```bash
curl -X POST \
  https://manteliviano.com/AquaProgrammerData/api/marcas \
  -H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJh...' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json'
```

#### Ejemplo Android (Retrofit)

```java
Call<List<DBListas>> call = api.listas();

call.enqueue(new Callback<List<DBListas>>() {
    @Override
    public void onResponse(Call<List<DBListas>> call,
                          Response<List<DBListas>> response) {
        if (response.isSuccessful()) {
            List<DBListas> listas = response.body();
            // Guardar en base de datos local
            for (DBListas lista : listas) {
                db.insertElementoLista(lista);
            }
        }
    }

    @Override
    public void onFailure(Call<List<DBListas>> call, Throwable t) {
        // Manejar error
    }
});
```

---

## Endpoints de Lecturas

### POST /medidores

Envía las lecturas capturadas al servidor.

#### Request

**Headers:**
```http
Authorization: Bearer {api_token}
Content-Type: application/json
Accept: application/json
```

**Body:**
```json
{
  "lecturas": [
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
      "Usuario": "usuario1",
      "LA": "1500",
      "Lectura_actual": 1535,
      "Consumo": 35,
      "Promedio": 25,
      "Critica": "",
      "Estado_lectura": "FINALIZADA",
      "finilec": "2024-01-15 09:30:00",
      "ffinlec": "2024-01-15 09:35:00",
      "Causa": null,
      "DescCausa": null,
      "Observacion": null,
      "DescObservacion": null,
      "ObservacionGral": "Todo normal",
      "latitud": "4.624335",
      "longitud": "-74.063644",
      "ruta_foto": "/storage/emulated/0/SystemApp/ORD001_20240115093500.jpg"
    },
    {
      "id": "ORD002",
      "Ciclo": "202401",
      "Periodo": "ENERO 2024",
      "Ref_Medidor": "MED12346",
      "Suscriptor": "SUB002",
      "Nombre": "Pedro",
      "Apell": "Martínez",
      "Direccion": "Carrera 5 #15-20",
      "Ruta": "R001",
      "consecutivoRuta": "002",
      "Usuario": "usuario1",
      "LA": "2340",
      "Lectura_actual": null,
      "Consumo": null,
      "Promedio": 30,
      "Critica": null,
      "Estado_lectura": "NO_LEIDO",
      "finilec": "2024-01-15 09:40:00",
      "ffinlec": "2024-01-15 09:42:00",
      "Causa": 1,
      "DescCausa": "Predio cerrado",
      "Observacion": null,
      "DescObservacion": null,
      "ObservacionGral": "Casa cerrada, nadie responde",
      "latitud": "4.625123",
      "longitud": "-74.064221",
      "ruta_foto": "/storage/emulated/0/SystemApp/ORD002_20240115094200.jpg"
    }
  ]
}
```

**Parámetros del Array lecturas:**

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| id | string | Sí | ID de la orden |
| Lectura_actual | integer | Condicional | Lectura capturada (requerida si no hay causa) |
| Consumo | integer | No | Consumo calculado |
| Critica | string | No | Código de validación crítica |
| Estado_lectura | string | Sí | FINALIZADA, NO_LEIDO, EN_PROCESO |
| finilec | string | Sí | Timestamp inicio lectura (ISO 8601) |
| ffinlec | string | Sí | Timestamp fin lectura (ISO 8601) |
| Causa | integer | Condicional | Código de causa (requerido si no hay lectura) |
| DescCausa | string | No | Descripción de la causa |
| Observacion | integer | No | Código de observación |
| DescObservacion | string | No | Descripción de observación |
| ObservacionGral | string | No | Observación libre |
| latitud | string | Recomendado | Latitud GPS (decimal) |
| longitud | string | Recomendado | Longitud GPS (decimal) |
| ruta_foto | string | No | Ruta local de la foto (se sube separadamente) |

**Validaciones:**

- Si `Lectura_actual` es nula, `Causa` debe estar presente
- Si `Lectura_actual` está presente, `Consumo` debe calcularse
- `finilec` debe ser menor o igual a `ffinlec`
- Coordenadas GPS deben estar en formato decimal válido

#### Response

**Success (200 OK):**
```json
{
  "success": true,
  "message": "Lecturas procesadas exitosamente",
  "data": {
    "total_recibidas": 2,
    "procesadas": 2,
    "errores": 0,
    "detalles": [
      {
        "id": "ORD001",
        "status": "OK",
        "message": "Lectura procesada"
      },
      {
        "id": "ORD002",
        "status": "OK",
        "message": "Causa de no lectura registrada"
      }
    ]
  }
}
```

**Success con Errores Parciales (200 OK):**
```json
{
  "success": true,
  "message": "Lecturas procesadas con algunos errores",
  "data": {
    "total_recibidas": 2,
    "procesadas": 1,
    "errores": 1,
    "detalles": [
      {
        "id": "ORD001",
        "status": "OK",
        "message": "Lectura procesada"
      },
      {
        "id": "ORD002",
        "status": "ERROR",
        "message": "Lectura duplicada",
        "error_code": "DUPLICATE_READING"
      }
    ]
  }
}
```

**Error (400 Bad Request):**
```json
{
  "error": "Datos inválidos",
  "code": "VALIDATION_ERROR",
  "details": {
    "lecturas": ["El campo lecturas es requerido y debe ser un array"]
  }
}
```

**Error (422 Unprocessable Entity):**
```json
{
  "error": "Validación fallida",
  "code": "VALIDATION_FAILED",
  "details": {
    "lecturas.0.id": ["El campo id es requerido"],
    "lecturas.0.Lectura_actual": ["Debe proporcionar lectura o causa"],
    "lecturas.1.finilec": ["Fecha inválida"]
  }
}
```

#### Ejemplo cURL

```bash
curl -X POST \
  https://manteliviano.com/AquaProgrammerData/api/medidores \
  -H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJh...' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "lecturas": [
      {
        "id": "ORD001",
        "Lectura_actual": 1535,
        "Consumo": 35,
        "Estado_lectura": "FINALIZADA",
        "finilec": "2024-01-15 09:30:00",
        "ffinlec": "2024-01-15 09:35:00",
        "latitud": "4.624335",
        "longitud": "-74.063644"
      }
    ]
  }'
```

#### Ejemplo Android (Retrofit)

```java
// Obtener lecturas pendientes de envío
List<DBOrdenLecturas> lecturasEnviar = db.getData(
    "lecturas",
    "Uploadlec IS NULL OR Uploadlec = ''"
);

// Crear objeto de envío
DBOrdenLecturasEnviar envio = new DBOrdenLecturasEnviar();
envio.setLecturas(lecturasEnviar);

// Enviar al servidor
Call<Object> call = api.enviarordenes(envio);

call.enqueue(new Callback<Object>() {
    @Override
    public void onResponse(Call<Object> call, Response<Object> response) {
        if (response.isSuccessful()) {
            // Marcar como enviadas
            for (DBOrdenLecturas lectura : lecturasEnviar) {
                lectura.setUploadlec("SI");
                db.insertOrden(lectura, true);
            }

            Toast.makeText(context,
                lecturasEnviar.size() + " lecturas enviadas",
                Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(Call<Object> call, Throwable t) {
        Toast.makeText(context,
            "Error al enviar: " + t.getMessage(),
            Toast.LENGTH_SHORT).show();
    }
});
```

---

## Modelos de Datos

### LoginEnvio

Modelo para envío de credenciales de login.

```java
public class LoginEnvio {
    private String usuario;
    private String password;

    public LoginEnvio(String usuario, String password) {
        this.usuario = usuario;
        this.password = password;
    }

    // Getters y setters...
}
```

**Representación JSON:**
```json
{
  "usuario": "string",
  "password": "string"
}
```

---

### LoginRespuesta

Modelo para respuesta de autenticación.

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
    private String api_token;

    // Constructor, getters y setters...
}
```

**Representación JSON:**
```json
{
  "id": "string",
  "usuario": "string",
  "nombre": "string",
  "tipodeusuario": "string",
  "email": "string",
  "empresa": "string",
  "remenber_token": "string|null",
  "estado": "string",
  "created_at": "string (ISO 8601)",
  "api_token": "string (JWT)"
}
```

---

### DBOrdenLecturas

Modelo completo de orden de lectura.

```java
public class DBOrdenLecturas {
    // Identificación
    public String id;
    public String Ciclo;
    public String Periodo;
    public String Año;

    // Medidor y Cliente
    public String Ref_Medidor;
    public String Suscriptor;
    public String Nombre;
    public String Apell;
    public String Direccion;

    // Ruta
    public String Ruta;
    public String consecutivoRuta;
    public String id_Ruta;
    public String Usuario;

    // Lectura
    public String LA;
    public Integer Promedio;
    public Integer Lectura_actual;
    public Integer Consumo;
    public String Critica;

    // Estado
    public String Estado;
    public String Estado_lectura;
    public String Uploadlec;
    public String finilec;
    public String ffinlec;

    // Causas y Observaciones
    public Integer Causa;
    public String DescCausa;
    public Integer Observacion;
    public String DescObservacion;
    public String ObservacionGral;

    // Geolocalización
    public String latitud;
    public String longitud;

    // Fotografía
    public String ruta_foto;

    // Otros
    public String Tope;
    public String Categoria_orden;
    public String Tipo_orden;
    public String cservic;
    public String nservic;
    public String ctipcon;
    public String ntipcon;

    // Constructor, getters y setters...
}
```

---

### DBListas

Modelo para elementos de catálogos.

```java
public class DBListas {
    private String marca_id;
    private String codigo;
    private String descripcion;

    // Constructor, getters y setters...
}
```

**Representación JSON:**
```json
{
  "marca_id": "string",
  "codigo": "string",
  "descripcion": "string"
}
```

---

### DBOrdenLecturasEnviar

Modelo contenedor para envío de múltiples lecturas.

```java
public class DBOrdenLecturasEnviar {
    private List<DBOrdenLecturas> lecturas;

    public DBOrdenLecturasEnviar() {
        this.lecturas = new ArrayList<>();
    }

    // Getters y setters...
}
```

**Representación JSON:**
```json
{
  "lecturas": [
    { /* DBOrdenLecturas */ },
    { /* DBOrdenLecturas */ },
    ...
  ]
}
```

---

## Códigos de Error

### Códigos de Autenticación

| Código | HTTP | Descripción | Solución |
|--------|------|-------------|----------|
| AUTH_FAILED | 401 | Credenciales inválidas | Verificar usuario/contraseña |
| TOKEN_INVALID | 401 | Token inválido o expirado | Volver a autenticarse |
| TOKEN_EXPIRED | 401 | Token expirado | Renovar token |
| USER_INACTIVE | 403 | Usuario inactivo | Contactar administrador |
| USER_SUSPENDED | 403 | Usuario suspendido | Contactar administrador |

### Códigos de Validación

| Código | HTTP | Descripción | Solución |
|--------|------|-------------|----------|
| VALIDATION_ERROR | 400 | Error de validación general | Revisar datos enviados |
| VALIDATION_FAILED | 422 | Validación específica fallida | Revisar campo indicado |
| MISSING_FIELD | 422 | Campo requerido faltante | Agregar campo requerido |
| INVALID_FORMAT | 422 | Formato de dato inválido | Corregir formato |
| DUPLICATE_READING | 422 | Lectura duplicada | Verificar si ya fue enviada |

### Códigos de Recursos

| Código | HTTP | Descripción | Solución |
|--------|------|-------------|----------|
| NO_ORDERS | 404 | Sin órdenes asignadas | Normal si no hay asignaciones |
| ORDER_NOT_FOUND | 404 | Orden no encontrada | Verificar ID de orden |
| RESOURCE_NOT_FOUND | 404 | Recurso no existe | Verificar URL/endpoint |

### Códigos de Servidor

| Código | HTTP | Descripción | Solución |
|--------|------|-------------|----------|
| SERVER_ERROR | 500 | Error interno del servidor | Reintentar, contactar soporte |
| DATABASE_ERROR | 500 | Error de base de datos | Contactar soporte |
| SERVICE_UNAVAILABLE | 503 | Servicio no disponible | Esperar, reintentar |

---

## Ejemplos de Uso

### Flujo Completo: Login y Descarga de Órdenes

```java
// 1. Login
LoginEnvio credentials = new LoginEnvio("usuario1", "password123");
Call<List<LoginRespuesta>> loginCall = api.login(credentials);

loginCall.enqueue(new Callback<List<LoginRespuesta>>() {
    @Override
    public void onResponse(Call<List<LoginRespuesta>> call,
                          Response<List<LoginRespuesta>> response) {
        if (response.isSuccessful() && response.body().size() > 0) {
            String apiToken = response.body().get(0).getApiToken();

            // 2. Configurar cliente con token
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(apiToken))
                .build();

            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SystemAppAPI.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

            SystemAppAPI authenticatedApi = retrofit.create(SystemAppAPI.class);

            // 3. Descargar órdenes
            Call<List<DBOrdenLecturas>> ordenesCall = authenticatedApi.cargue();
            ordenesCall.enqueue(new Callback<List<DBOrdenLecturas>>() {
                @Override
                public void onResponse(Call<List<DBOrdenLecturas>> call,
                                      Response<List<DBOrdenLecturas>> response) {
                    if (response.isSuccessful()) {
                        List<DBOrdenLecturas> ordenes = response.body();
                        // Guardar en BD local
                        saveToDatabase(ordenes);
                    }
                }

                @Override
                public void onFailure(Call<List<DBOrdenLecturas>> call, Throwable t) {
                    handleError(t);
                }
            });
        }
    }

    @Override
    public void onFailure(Call<List<LoginRespuesta>> call, Throwable t) {
        handleError(t);
    }
});
```

### Manejo de Errores Robusto

```java
private void handleApiError(Response<?> response) {
    switch (response.code()) {
        case 400:
            showError("Datos inválidos. Verifique la información.");
            break;

        case 401:
            showError("Sesión expirada. Vuelva a iniciar sesión.");
            logout();
            break;

        case 403:
            showError("No tiene permisos para esta acción.");
            break;

        case 404:
            showError("Recurso no encontrado.");
            break;

        case 422:
            // Parsear errores de validación
            try {
                ErrorResponse error = parseError(response);
                showValidationErrors(error);
            } catch (Exception e) {
                showError("Error de validación.");
            }
            break;

        case 500:
            showError("Error del servidor. Intente más tarde.");
            break;

        case 503:
            showError("Servicio no disponible. Intente más tarde.");
            break;

        default:
            showError("Error desconocido: " + response.code());
    }
}
```

---

## Rate Limiting

### Límites de Peticiones

| Endpoint | Límite | Ventana de Tiempo |
|----------|--------|-------------------|
| /loginMovil1 | 5 peticiones | Por minuto |
| /medidoresout | 10 peticiones | Por minuto |
| /marcas | 10 peticiones | Por minuto |
| /medidores | 5 peticiones | Por minuto |

### Headers de Rate Limit

```http
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 7
X-RateLimit-Reset: 1642435200
```

### Respuesta cuando se excede el límite

**HTTP 429 Too Many Requests:**
```json
{
  "error": "Límite de peticiones excedido",
  "code": "RATE_LIMIT_EXCEEDED",
  "retry_after": 60
}
```

---

## Versionado de API

### Versión Actual
**v1** (sin prefijo en la URL)

### Compatibilidad
- Breaking changes se notificarán con 30 días de anticipación
- Versiones antiguas se mantendrán por 6 meses después de deprecación

---

## Seguridad

### Mejores Prácticas

1. **Almacenar Tokens de Forma Segura**
   - Usar SharedPreferences en modo privado
   - No loguear tokens en consola
   - No compartir tokens entre apps

2. **Validar Certificados SSL**
   - No deshabilitar validación SSL
   - Implementar certificate pinning (recomendado)

3. **Timeout de Sesión**
   - Los tokens expiran después de 24 horas
   - Renovar token antes de expiración

4. **Proteger Datos Sensibles**
   - No almacenar contraseñas
   - Cifrar base de datos local
   - Limpiar datos al cerrar sesión

---

## Changelog de API

### v1.0 (2024-01-01)
- Versión inicial
- Endpoints de autenticación, órdenes, catálogos y lecturas

---

**Versión del Documento**: 1.0
**Última Actualización**: 2024
**Contacto**: soporte@empresa.com
