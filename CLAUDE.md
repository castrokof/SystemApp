# CLAUDE.md

Este archivo da contexto a Claude Code (claude.ai/code) para trabajar con el código de este repositorio.

## Descripción general del proyecto

SystemApp es una app Android nativa (Java) usada por técnicos de campo de empresas de acueducto en Colombia para capturar lecturas de medidores de agua offline, sincronizarlas con un backend Laravel remoto e imprimir recibos vía impresoras POS Bluetooth. No es una app single-tenant: el mismo código se compila en un APK distinto y con marca propia por cada empresa cliente, usando product flavors de Gradle (ver más abajo).

## Compilación y ejecución

Proyecto estándar de Gradle/Android Studio (Gradle 8.5, sin Kotlin, sin Room/RxJava/Coroutines — Java 8 plano, callbacks, estilo AsyncTask).

```bash
./gradlew assembleDebug                 # compila todos los flavors, debug
./gradlew assembleAcuacerDebug          # compila un solo flavor (ver lista de flavors abajo)
./gradlew installAcuacerDebug           # compila e instala un flavor en un dispositivo/emulador conectado
./gradlew assembleRelease                # build de release (minify está DESACTIVADO en todos los flavors)
./gradlew test                           # tests unitarios JVM (app/src/test) — actualmente solo stubs de plantilla
./gradlew connectedAndroidTest           # tests instrumentados (app/src/androidTest) — actualmente solo stubs de plantilla
```

En la práctica no existe una suite de tests real todavía (`ExampleUnitTest`/`ExampleInstrumentedTest` son plantillas sin modificar) — no asumas que hay cobertura de tests para ninguna funcionalidad.

## Product flavors — este es el concepto central de la arquitectura

`app/build.gradle` define una única dimensión de flavor `"cliente"` con un flavor por cada empresa cliente: `acuasur`, `acuacer`, `altosmangos`, `lasirena`, `asovoragine`, `demo`. Cada flavor define, vía `buildConfigField`:

- `IP_DEF` — host del backend (todos apuntan a `manteliviano.com` excepto `asovoragine` → `asovoragine.aquaprogrammer.com` y `demo` → `demo.aquaprogrammer.com`)
- `BASE_API` — prefijo de ruta de la API (cada cliente tiene su propio segmento de ruta sobre el host compartido, ej. `/acuasur/api/`, `/appupdate/api/` para acuacer, `/AquaProgrammerData/api/` para altosmangos)
- `INVERT_LOGO` — flag cosmético para la impresión de recibos
- `resValue "string" "app_name"` — el nombre visible/marca de la app

`Constants.BASE_URL` (`app/src/main/java/com/example/systemapp/data/Constants.java`) arma la URL base final de la API en tiempo de ejecución como `"https://" + BuildConfig.IP_DEF + BuildConfig.BASE_API`, y `SystemAppAPI.BASE_URL` simplemente apunta a `Constants.BASE_URL`. **Para cambiar contra qué backend habla un build, hay que editar los valores de `buildConfigField` del flavor en `app/build.gradle`, no una constante fija en el código.**

Cada flavor también tiene su propio source set bajo `app/src/<flavor>/res/drawable/` (`logo.png`, `logoprint.png`, `logoprint_color.png` para acuasur) que provee el logo de esa empresa, usado en pantalla y en los recibos impresos. Al agregar un cliente nuevo, se agrega un bloque de flavor nuevo en `app/build.gradle` más sus assets `app/src/<flavor>/res/drawable/logo*.png` — no se debe bifurcar el código.

También existe un flavor `demo` con sus propios assets de logo, usado para pruebas/demostraciones independiente del backend real de cualquier cliente.

## Contrato con el backend

La app habla con un backend Laravel (ver `CONTEXTO_BACKEND.md` — **nota**: ese documento fue generado contra un repositorio de backend *distinto*, no este repo de Android, pero documenta el contrato real de API/BD que esta app consume y es la fuente autoritativa sobre el comportamiento del backend, sus particularidades y trampas referenciadas abajo).

- La autenticación es un **token custom vía bearer**, no OAuth/Sanctum/JWT: `POST loginMovil1` con `usuario`/`password` devuelve un array que contiene un único objeto de usuario con `api_token`; ese token se guarda vía `SessionPrefs` y se adjunta como `Authorization: Bearer <token>` mediante `AuthInterceptor` (`app/src/main/java/com/example/systemapp/AuthInterceptor.java`) en todas las llamadas Retrofit posteriores.
- `SystemAppAPI.java` (interfaz Retrofit) expone 4 endpoints: `loginMovil1` (login), `medidoresout` (`cargue()` — descarga órdenes de lectura asignadas), `marcas` (`listas()` — descarga catálogos/listas de valores), `medidores` (`enviarordenes()` — sube las lecturas capturadas).
- Algunas respuestas del backend de "sin resultados" llegan como **HTTP 200 con un body `{"error": "..."}`** en vez de un status distinto de 2xx — no confiar solo en `response.isSuccessful()` al interpretar resultados de sincronización.
- La tabla `listas` que respalda los catálogos (causas, observaciones) usa una clave compuesta `(marca_id, codigo)` donde `marca_id` en realidad es un id de *grupo/categoría*, no un id de marca — no dejarse confundir por el nombre.

## Persistencia local

`SQLiteOpenHelper` plano (`AdminSQLiteOpenHelper.java`, nombre de BD `SystemApp`, versión en `DBdefinicionOrdenes.DATABASE_VERSION`) — sin Room. Dos tablas:

- `lecturas` — una fila por orden de lectura, cubre todo el ciclo de vida de la orden: descargada (asignada), capturada localmente (`Lectura_actual`, `Consumo`, GPS, ruta de foto, causa/observación), y estado de subida (`Uploadlec`). `Categoria_orden` distingue entre `ASIGNADAS` / `REASIGNADAS` / `PROCESADAS`.
- `listas` — tabla plana de catálogos con clave `(marca_id, codigo)`.

**`onUpgrade` ya NO es destructivo (corregido)**: originalmente hacía `DROP TABLE` + `onCreate()` incondicional en cada incremento de versión de BD, borrando cualquier lectura local no sincronizada. Se corrigió para que no toque los datos existentes — ahora es un método vacío, listo para que cada futuro incremento de `DATABASE_VERSION` agregue ahí su propia migración incremental (`ALTER TABLE`) sin destruir lo que ya hay. **Importante**: si en el futuro se necesita cambiar el esquema de `lecturas`/`listas`, hay que escribir esa migración explícitamente ahí (ver comentario en el código) — subir `DATABASE_VERSION` sin agregar la migración correspondiente simplemente no aplicará el cambio de esquema (las columnas nuevas no existirán), en vez de borrar datos como antes.

**Cuándo se dispara, en la práctica**: nadie llama a `onUpgrade` directamente — lo invoca automáticamente el framework `SQLiteOpenHelper` la primera vez que el código llama a `getWritableDatabase()`/`getReadableDatabase()` (dentro de los métodos CRUD de `AdminSQLiteOpenHelper`, usados desde `fragment_ordenes`, `RAsignadasFragment`, `fragment_ejecutadas`, `fragment_sync`, `Fragment_form_lectura`, `fragment_borrar_datos` y los diálogos de causas/observaciones) tras abrir el `.db` del dispositivo y ver que `DATABASE_VERSION` es mayor que la versión guardada en el archivo. Esta app **no tiene ningún mecanismo de auto-actualización** (no hay chequeo de versión contra servidor ni descarga de APK en el código) — la distribución es manual: se genera un APK por flavor (`assemble<Flavor>Release`) y el técnico lo instala encima de la versión existente. Como el `applicationId` no cambia y no se desinstala primero, Android trata esa instalación como upgrade in-place y conserva el directorio de datos (incluida la BD física). `DATABASE_VERSION` es un número totalmente independiente de `versionCode`/`versionName` en `build.gradle`, así que subirlo no es visible a simple vista en el changelog de una release.

Distinto de esto es el botón **"Borrar Ruta"** (`fragment_borrar_datos.java`) — una acción deliberada del usuario que sí borra toda la BD (`deleteDatabase(...)`), pero con guarda: antes de permitirlo cuenta las lecturas con `Categoria_orden = 'RELECTURA'` (la única categoría que el código realmente asigna a una lectura capturada, en `Fragment_form_lectura.java`) y `Uploadlec` vacío/nulo; si hay alguna pendiente de envío, bloquea el borrado con un diálogo de advertencia. No confundir ambos mecanismos: uno es automático y (antes de esta corrección) destructivo sin avisar; el otro es manual y protegido.

La sesión/token/preferencias viven en `SessionPrefs` (`SharedPreferences`, archivo de prefs `SYSTEMAPP_PREFS`) — es la única fuente de verdad para el estado de login, el token de API y el emparejamiento de impresora (`PREF_PRINTER_NAME`/`PREF_PRINTER_ADDRESS`). Existe un scaffold de plantilla de Google sin usar (`LoginRepository`/`LoginDataSource`/`Result`) bajo `data/` — el flujo real de login en `LoginActivity` habla directamente con `SystemAppAPI` y escribe en `SessionPrefs`, sin pasar por ese scaffold.

## Estructura de UI

Prácticamente single-Activity: `MainActivity` aloja un `DrawerLayout` + un grafo de Navigation Component (`res/navigation/mobile_navigation.xml`) para `nav_ordenes` / `nav_ejecutadas` / `nav_borrarruta` / `nav_sync` / `nav_config`, pero `fragment_ordenes` (órdenes pendientes/reasignadas/ejecutadas) y `Fragment_form_lectura` (el formulario de captura de lectura) son los dos fragments con lógica de negocio real, y se intercambian tanto vía Navigation como vía `FragmentTransaction`s manuales (`MainActivity.changeFragment`). `LoginActivity` es una activity separada, sin Navigation Component, que se lanza antes de `MainActivity` cuando `SessionPrefs.isLoggedIn()` es falso.

El manejo del botón back está centralizado y no es el comportamiento por defecto, en `MainActivity.onBackPressed()`: trata como casos especiales el drawer, `fragment_ordenes` (delega a `handleOnBackPress()`) y `fragment_sync` (se traga el back por completo), y luego redirige los destinos "de nivel superior" de vuelta a `nav_ordenes` en lugar de salir de la app. Si se toca la navegación, hay que revisar este método — ha tenido varios commits de fix recientes (ver git log) y es fácil de romper de nuevo.

## Reglas de validación de lectura

Definidas como códigos string en `Constants.java` y aplicadas en `Fragment_form_lectura`:

| Código | Significado |
|---|---|
| `LA=LANT` | la lectura actual es igual a la anterior |
| `LA<LANT` | la lectura actual es menor a la anterior |
| `CA>165CP` | el consumo es > 165% del promedio histórico |
| `CA<35CP` | el consumo es < 35% del promedio histórico |
| `CA<50CP` | el consumo es < 50% del promedio histórico |

Estas son reglas de negocio que reflejan la propia lógica de anomalías de consumo del backend (ver `Cliente::lecturaEsNormal()` en `CONTEXTO_BACKEND.md`, que usa ±50%) — si los umbrales del backend cambian, estas constantes del lado del cliente deben sincronizarse manualmente; no hay una fuente de verdad compartida.

## Impresión

Dos jars de SDK incluidos en `app/libs/` (`SDKLib.jar`, `posprinterconnectandsendsdk.jar`) manejan las impresoras de recibos Bluetooth ESC/POS. `PrinterCommands.java` arma el recibo (con el logo según el flavor vía `INVERT_LOGO`/`logoprint*.png`), `PrinterUtils.java` maneja la conversión de bitmap a comandos de impresora y el flujo de conexión/búsqueda/envío Bluetooth. El emparejamiento de la impresora (nombre + dirección MAC) se guarda por dispositivo en `SessionPrefs`. La lógica de impresión automática fue reestructurada recientemente para reutilizar la misma ruta de búsqueda de canal que la impresión de prueba manual (`testPrintWithChannelSearch`) — ver commit `c6a27de`.

## Documentación en este repo

- `README.md`, `TECHNICAL_DOCUMENTATION.md`, `USER_GUIDE.md` — describen la app Android en sí; útiles como contexto de funcionalidades y flujos, pero fueron escritos temprano y se desactualizaron respecto al código en algunos puntos (ej. el árbol de archivos del README y los snippets de `DBOrdenLecturas`/`SessionPrefs`/`Constants` no coinciden exactamente con el código actual — mejor leer los archivos fuente reales que confiar en estos documentos al pie de la letra).
- `CONTEXTO_BACKEND.md` — documentación generada por ingeniería inversa del backend Laravel (API/BD) con el que habla esta app, incluyendo bugs/particularidades conocidas del backend (ej. la columna con el typo `remenber_token`, la inconsistencia de doble autenticación en `/api/cliente`, que `Macromedidor.lectura_actual` es un string y no un entero). Tratarlo como la mejor referencia disponible sobre el comportamiento del backend, pero documenta el código de otro repositorio, no el de este.

## Backend consumido

Resumen del contrato real expuesto por el backend Laravel (fuente: `CONTEXTO_BACKEND.md`, generado leyendo el código fuente y migraciones reales del backend — no este repo). Guardado aquí como referencia permanente para no tener que releer ese documento completo en cada sesión.

### Autenticación

Token custom (no Sanctum/Passport/JWT). `POST /loginMovil1` (ruta pública, sin middleware, fuera del prefijo `/api`) recibe `usuario`/`password` y, si las credenciales son válidas y `estado=='activo'`, responde **200 con un array que contiene un único objeto** (no un objeto plano):

```json
[{
  "id": 1, "usuario": "jperez", "nombre": "Juan Pérez", "tipodeusuario": "...",
  "email": "...", "empresa": "...", "remenber_token": null, "estado": "activo",
  "api_token": "TOKEN_PLANO_60_CHARS", "created_at": "...", "updated_at": "..."
}]
```

Nótese `remenber_token` (typo literal en la clave JSON del backend, valor siempre `null`). El resto de endpoints van bajo `auth.api.token` (`Authorization: Bearer <token>`; 401 sin token o token inválido/usuario inactivo).

### Endpoints reales usados por esta app (`routes/api.php`)

**Esta app es solo de lecturas de medidor** (lecturista). Los otros endpoints que expone el mismo backend (`ordenesMacro`, `macromedidoresMovil` para macromedidores, y `ordenesRevision`/`revisionesMovilV2` para revisiones de campo) pertenecen a **otra app distinta** — no forman parte del alcance de este repo y no deben tratarse como funcionalidad pendiente de agregar aquí. Se listan solo como referencia por si algún día hay que diferenciar tráfico contra el mismo backend.

| Método | Ruta | Consume | Devuelve |
|---|---|---|---|
| POST | `/loginMovil1` | `usuario`, `password` | Array con 1 objeto de usuario (ver arriba) |
| POST | `/api/medidoresout` | — (usa `$request->user()`) | `200`: array de filas crudas de `ordenescu` (Estado=2, del usuario autenticado) **o** `200 {"error":"Sin medidores asignados"}` |
| POST | `/api/medidores` | `id`, `tipo`, `campoFoto`, `critica`, `causal`, `texcausa`, `observ`, `texobser`, `lectact`, `latitud`, `longitud`, `ffinlec` (`d/m/Y H:i:s`), `suscriptor?`, `consumo`, `observg` — sin validación de backend | Inserta/actualiza `orden_ejecutada` + marca `ordenescu` como `EJECUTADO`. Éxito: `200 {"success":true,"message":"Lectura cargada en servidor"}` |
| POST | `/api/marcas` | — | Si el usuario tiene órdenes `Estado=2` pendientes: `200` array de `Marcas::all()`. Si no: `403 {"error":"No puede sincronizar listas"}` |
| POST | `/api/medidorejecutado` | `year`, `month` | Marca `sync=2` en `ordenescu` (Estado=4): `200 {"mensaje":"Lecturas sincronizadas correctamente","datos":[...]}` **o** `200 {"error":"sin medidores ejecutados"}` |

`SystemAppAPI.java` en este repo implementa 4 de estos 5 (`loginMovil1`, `medidoresout`, `marcas`, `medidores`) — `medidorejecutado` (sync de estado) existe en el backend pero no está invocado desde el código Android actual.

#### Endpoints de OTRA app (no tocar/implementar aquí salvo que se indique explícitamente)

`GET /api/ordenesMacro`, `POST /api/macromedidoresMovil`, `GET /api/ordenesRevision`, `POST /api/revisionesMovilV2`, `GET /api/listasParametros`, `GET /api/cliente`, `POST /api/cliente` — son de la app de macromedidores/revisiones de campo, un producto distinto que comparte el mismo backend Laravel. Quedan documentados en `CONTEXTO_BACKEND.md` por completitud del contrato del backend, pero no son responsabilidad de este repositorio.

### Formato `toApiArray()` relevante para esta app

Esta app no consume ningún `toApiArray()` (esos formatos —`Cliente`, `Macromedidor`, `OrdenRevision`, `ListaParametro`— son para la otra app de macromedidores/revisiones). Los endpoints que sí usa esta app (`medidoresout`, `marcas`, `medidores`) devuelven filas crudas de las tablas `ordenescu`/`Marcas`, no una vista `toApiArray()` curada.

### Advertencias clave al integrar contra este backend

- **Respuestas "sin resultados" devuelven HTTP 200 con `{"error": "..."}` en el body**, no un status code de error (confirmado en `medidorall`/`medidoresout` y `medidorejecutadosync`). Hay que inspeccionar el contenido de la respuesta, no solo el status code — esto ya afecta el parsing de `/api/medidoresout` y `/api/marcas` que sí usa esta app.
- **El login devuelve un array con un único objeto**, no un objeto plano — el código actual (`LoginActivity.attemptLogin()`) ya lo maneja como `List<LoginRespuesta>`, pero cualquier cambio a ese flujo debe preservar esto.
- **`/api/cliente` tiene doble validación de token** — inconsistencia real de diseño en el backend: la ruta está protegida por `auth.api.token` (bearer token), pero además `Api\ClienteApiController` compara un segundo parámetro `api_token` (query string en GET, campo de form en POST) **en texto plano** contra la columna que en teoría guarda el hash. Si esta app llega a consumir este endpoint, hay que probar empíricamente contra el backend real qué valor exacto exige ese segundo parámetro — no asumir que basta con el bearer token.
- Sin rate limiting (`throttle`) en ninguna ruta de la API — no depender de que el backend limite reintentos.
- No existe login para "clientes finales" (el suscriptor); todo el sistema de token es para empleados de campo (lecturistas, revisores) — coincide con el modelo de uso de esta app.
