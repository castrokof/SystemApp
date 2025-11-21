# SystemApp - Sistema de Lectura de Medidores

**SystemApp** es una aplicación Android nativa diseñada para la gestión y captura de lecturas de medidores de agua/acueducto. La aplicación permite a los técnicos de campo realizar lecturas de manera eficiente, trabajar offline, y sincronizar datos con el servidor central.

## Características Principales

### Gestión de Órdenes de Lectura
- **Órdenes Pendientes**: Visualización y gestión de lecturas por realizar
- **Órdenes Ejecutadas**: Historial de lecturas completadas
- **Órdenes Reasignadas**: Gestión de lecturas reasignadas
- **Navegación por rutas**: Organización de lecturas por rutas de trabajo

### Captura de Datos
- Registro de lectura actual del medidor
- Cálculo automático de consumo
- Validación de lecturas (críticas, promedios)
- Captura de fotografías del medidor
- Geolocalización GPS de cada lectura
- Registro de causas de no lectura
- Observaciones generales y específicas

### Funcionalidades Offline
- Base de datos SQLite local
- Sincronización bidireccional con servidor
- Trabajo sin conexión a internet
- Cola de sincronización de datos

### Impresión y Reportes
- Impresión de recibos vía Bluetooth
- Generación de reportes de lecturas
- Resumen de actividades diarias

### Seguridad
- Autenticación mediante API Token
- Sesiones persistentes
- Validación de usuarios

## Tecnologías Utilizadas

### Plataforma
- **Android SDK**: 33 (compileSdk)
- **Min SDK**: 27 (Android 8.1 Oreo)
- **Target SDK**: 33 (Android 13)
- **Lenguaje**: Java 8

### Frameworks y Librerías
- **Retrofit 2.3.0**: Cliente HTTP para consumo de API REST
- **Gson 2.8.6**: Serialización/deserialización JSON
- **AndroidX Libraries**:
  - AppCompat 1.6.0
  - Material Components 1.7.0
  - Navigation Component 2.5.3
  - Lifecycle & ViewModel 2.5.1
  - RecyclerView 1.2.1
- **SQLite**: Base de datos local
- **Apache Commons IO 2.2**: Operaciones de archivos
- **SDKLib.jar**: SDK para impresoras POS
- **posprinterconnectandsendsdk.jar**: Conectividad con impresoras

### Arquitectura
- **Patrón**: MVVM (Model-View-ViewModel)
- **Navigation Component**: Navegación entre fragments
- **ViewBinding**: Vinculación de vistas
- **Repository Pattern**: Capa de datos
- **SharedPreferences**: Almacenamiento de sesión

## Estructura del Proyecto

```
com.example.systemapp/
│
├── ui/                          # Capa de Presentación
│   ├── login/                   # Módulo de autenticación
│   │   ├── LoginActivity.java
│   │   ├── LoginViewModel.java
│   │   └── LoginFormState.java
│   │
│   ├── home/                    # Pantalla principal
│   ├── sync/                    # Sincronización de datos
│   │   └── fragment_sync.java
│   │
│   ├── data/                    # Formulario de lectura
│   │   └── Fragment_form_lectura.java
│   │
│   ├── config/                  # Configuración
│   │   └── ConfigFragment.java
│   │
│   ├── borrardatos/            # Gestión de datos
│   │   └── fragment_borrar_datos.java
│   │
│   ├── fragment_ordenes.java    # Listado de órdenes
│   ├── fragment_ejecutadas.java # Órdenes ejecutadas
│   ├── RAsignadasFragment.java  # Órdenes asignadas
│   └── REjecutadasFragment.java # Órdenes procesadas
│
├── data/                        # Capa de Datos
│   ├── model/                   # Modelos de datos
│   │   ├── DBOrdenLecturas.java     # Modelo de orden de lectura
│   │   ├── DBListas.java            # Listas de catálogos
│   │   ├── DBdefinicionOrdenes.java # Definición de esquema BD
│   │   ├── LoginRespuesta.java      # Respuesta de login
│   │   ├── LoginEnvio.java          # Request de login
│   │   └── EnviarRespuesta.java     # Respuesta de envío
│   │
│   ├── causas/                  # Gestión de causas
│   │   ├── MotivosNoLectura.java
│   │   ├── CustomDialog.java
│   │   └── ObservacionDialog.java
│   │
│   ├── AdminSQLiteOpenHelper.java   # Helper de SQLite
│   ├── SessionPrefs.java            # Gestión de sesión
│   ├── Constants.java               # Constantes
│   ├── Validador.java              # Validaciones
│   ├── Utils.java                  # Utilidades
│   ├── GuardarFotos.java           # Gestión de imágenes
│   ├── PrinterUtils.java           # Utilidades de impresión
│   ├── PrinterCommands.java        # Comandos de impresora
│   ├── Adaptador.java              # Adaptador de RecyclerView
│   ├── AdaptadorResumen.java       # Adaptador de resumen
│   └── VariablesSesion.java        # Variables globales
│
├── MainActivity.java            # Actividad principal
├── SystemAppAPI.java           # Interface Retrofit
└── AuthInterceptor.java        # Interceptor de autenticación
```

## Requisitos del Sistema

### Dispositivo Android
- Android 8.1 (API 27) o superior
- GPS/Localización habilitado
- Cámara
- Conexión Bluetooth (para impresión)
- Mínimo 100 MB de espacio libre

### Servidor Backend
- API REST en: `https://manteliviano.com/AquaProgrammerData/api/`
- Endpoints requeridos:
  - `POST /loginMovil1` - Autenticación
  - `POST /medidoresout` - Descarga de órdenes
  - `POST /marcas` - Descarga de catálogos
  - `POST /medidores` - Envío de lecturas

## Instalación

### Compilar desde código fuente

1. Clonar el repositorio:
```bash
git clone <repository-url>
cd SystemApp
```

2. Abrir el proyecto en Android Studio

3. Sincronizar Gradle:
```bash
./gradlew build
```

4. Conectar dispositivo Android o iniciar emulador

5. Ejecutar la aplicación:
```bash
./gradlew installDebug
```

### Configuración

1. **API URL**: La URL base está configurada en `SystemAppAPI.java`:
```java
public static final String BASE_URL = "https://manteliviano.com/AquaProgrammerData/api/";
```

2. **Permisos**: La aplicación requiere los siguientes permisos (configurados en `AndroidManifest.xml`):
   - `INTERNET`
   - `ACCESS_NETWORK_STATE`
   - `ACCESS_FINE_LOCATION`
   - `BLUETOOTH`
   - `BLUETOOTH_ADMIN`
   - `BLUETOOTH_CONNECT`
   - `BLUETOOTH_SCAN`
   - `WRITE_EXTERNAL_STORAGE` (hasta API 27)

## Uso Básico

### 1. Iniciar Sesión
1. Abrir la aplicación
2. Ingresar usuario y contraseña
3. Marcar "Recordar sesión" (opcional)
4. Presionar "Iniciar Sesión"

### 2. Sincronizar Datos
1. Ir al menú lateral → "Sincronizar"
2. Presionar "Descargar Órdenes"
3. Esperar descarga de órdenes y catálogos
4. Verificar cantidad de órdenes descargadas

### 3. Realizar Lectura
1. Ir a "Órdenes" → "Pendientes"
2. Seleccionar una orden de la lista
3. Ingresar lectura actual
4. Tomar fotografía del medidor (opcional)
5. Agregar observaciones si es necesario
6. Guardar lectura

### 4. Enviar Lecturas
1. Ir a "Sincronizar"
2. Presionar "Enviar Lecturas"
3. Esperar confirmación de envío
4. Verificar órdenes sincronizadas

## Base de Datos Local

### Tabla: lecturas
Almacena todas las órdenes de lectura y su información completa.

**Campos principales**:
- `id`: Identificador único
- `Ciclo`: Ciclo de facturación
- `Periodo`: Periodo de lectura
- `Ruta`: Código de ruta
- `Suscriptor`: Código de suscriptor
- `Nombre`, `Apell`: Nombre del cliente
- `Direccion`: Dirección del medidor
- `Ref_Medidor`: Referencia del medidor
- `LA`: Lectura anterior
- `Lectura_actual`: Lectura capturada
- `Consumo`: Consumo calculado
- `latitud`, `longitud`: Coordenadas GPS
- `ruta_foto`: Ruta de la fotografía
- `Causa`: Código de causa de no lectura
- `Observacion`: Código de observación

### Tabla: listas
Catálogos de causas, observaciones y otras listas.

**Campos**:
- `marca_id`: Grupo/categoría
- `codigo`: Código del elemento
- `descripcion`: Descripción

## API REST

### Autenticación
**Endpoint**: `POST /loginMovil1`

**Request**:
```json
{
  "usuario": "string",
  "password": "string"
}
```

**Response**:
```json
[
  {
    "id": "string",
    "usuario": "string",
    "nombre": "string",
    "email": "string",
    "api_token": "string",
    "empresa": "string",
    "estado": "string"
  }
]
```

### Descargar Órdenes
**Endpoint**: `POST /medidoresout`

**Headers**: `Authorization: Bearer {api_token}`

**Response**: Array de órdenes de lectura

### Enviar Lecturas
**Endpoint**: `POST /medidores`

**Headers**: `Authorization: Bearer {api_token}`

**Request**: Array de lecturas capturadas

## Validaciones de Lectura

La aplicación implementa las siguientes validaciones:

1. **LA=LANT**: Lectura actual igual a lectura anterior
2. **LA<LANT**: Lectura actual menor a lectura anterior
3. **CA>165CP**: Consumo actual mayor al 165% del consumo promedio
4. **CA<35CP**: Consumo actual menor al 35% del consumo promedio
5. **CA<50CP**: Consumo actual menor al 50% del consumo promedio

## Impresión

La aplicación soporta impresoras POS vía Bluetooth mediante los SDKs:
- `posprinterconnectandsendsdk.jar`
- `SDKLib.jar`

Funcionalidades:
- Búsqueda de impresoras Bluetooth
- Conexión automática
- Impresión de recibos de lectura
- Formato personalizable

## Seguridad

### Autenticación
- Login mediante usuario/contraseña
- Token API (Bearer Token) para todas las peticiones
- Almacenamiento seguro en SharedPreferences

### Datos
- Base de datos local cifrada
- Sincronización mediante HTTPS
- Validación de integridad de datos

## Troubleshooting

### Problemas Comunes

**1. Error de conexión al servidor**
- Verificar conectividad a internet
- Validar URL del servidor en `SystemAppAPI.java`
- Revisar firewall/proxy

**2. GPS no funciona**
- Verificar permisos de ubicación
- Activar GPS en el dispositivo
- Verificar que la app tenga permisos en Configuración

**3. No se sincronizan las lecturas**
- Verificar que haya lecturas pendientes de envío
- Revisar conexión a internet
- Validar token de autenticación

**4. Problemas de impresión**
- Verificar que el Bluetooth esté activado
- Emparejar impresora previamente
- Verificar permisos Bluetooth

## Contribución

Para contribuir al proyecto:

1. Fork del repositorio
2. Crear rama de feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit de cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

## Licencia

[Especificar licencia del proyecto]

## Contacto

Para soporte o consultas:
- **Backend API**: https://manteliviano.com/AquaProgrammerData/api/
- **Empresa**: [Información de contacto]

## Documentación Adicional

- [Documentación Técnica](TECHNICAL_DOCUMENTATION.md)
- [Guía de Usuario](USER_GUIDE.md)
- [Documentación de API](API_DOCUMENTATION.md)

---

**Versión**: 1.0
**Última actualización**: 2024
**Android Version Code**: 1
