# Contexto del Backend AquaProgrammer — para la app móvil (Android/Flutter)

> Documento generado a partir de lectura directa del código fuente y las migraciones reales de este repositorio (`demo_app/`), el 2026-07-23. Todo lo que aparece aquí fue verificado leyendo archivos concretos (modelo + migración + controlador), no se asumió nada. Donde el código es ambiguo o contradictorio, se dice explícitamente en vez de adivinar. Cuando este backend cambie, este documento puede quedar desactualizado — ante la duda, volver a grepear el código real citado en cada sección.

---

## 1. Resumen del sistema y stack real

AquaProgrammer es el backend de gestión y facturación de una empresa de acueducto/alcantarillado (agua potable). Cubre: gestión de clientes/suscriptores, lecturas de medidores (con app de campo para lecturistas y revisores), cálculo y emisión de facturas con tarifa escalonada y subsidios por estrato, cartera/mora, pagos (manuales y en línea vía Wompi), notificaciones (email + WhatsApp), y balance de macromedidores (pérdidas de agua no contabilizada).

### ⚠️ Versión real de Laravel — MUY IMPORTANTE

- **La aplicación que realmente corre es Laravel `5.8.38`.** Confirmado con `composer show laravel/framework` (versión instalada `v5.8.38`) y con la constante `Illuminate\Foundation\Application::VERSION` en `vendor/laravel/framework/src/Illuminate/Foundation/Application.php:32`, que dice literalmente `'5.8.38'`.
- **`composer.json` y `composer.lock` en la raíz del proyecto dicen `laravel/framework: ^8.0.0`** (composer.lock incluso resuelve a `v8.83.29`). Esto es **residuo de un intento de migración a Laravel 8 que nunca se completó**: el directorio `vendor/` real instalado en el servidor NO tiene los paquetes de Laravel 8 (no existe `vendor/laravel/ui`, `vendor/laravel/helpers`, `vendor/facade/ignition`, todos requeridos por el composer.json actual pero ausentes del `vendor/` real). El código de la aplicación (controladores, rutas, sintaxis) es 100% Laravel 5.8 — usa el kernel HTTP, el sistema de auth, y las convenciones de esa versión.
- **Conclusión práctica para cualquier IA que trabaje sobre este backend:** ignorar por completo lo que dice `composer.json`/`composer.lock` respecto a la versión de Laravel. Verificar siempre contra `vendor/laravel/framework/src/Illuminate/Foundation/Application.php` si hay duda. No asumir características de Laravel 8+ (Sanctum nativo, `Illuminate\Support\Str` con métodos nuevos, etc.) — el comportamiento real es el de 5.8.

### Stack adicional confirmado
- PHP `^7.3|^8.0.2` (composer.json, no verificado contra el intérprete real del servidor).
- Base de datos: MySQL/MariaDB (uso de `ENUM`, sintaxis `ALTER TABLE ... MODIFY` en migraciones crudas).
- Cola: `QUEUE_CONNECTION=database` (confirmado por agente de integraciones), procesada por cron de cPanel cada minuto (`php artisan queue:work --queue=default --once --stop-when-empty --timeout=3000 --tries=1`), ver `app/Console/Kernel.php`.
- PDF: `barryvdh/laravel-dompdf`.
- Excel: `maatwebsite/excel`.
- No hay Sanctum, Passport ni JWT — la API usa un **token custom** (ver sección 3).
- Sistema **multi-tenant de un solo inquilino**: toda la configuración sensible (Wompi, WhatsApp, SMTP, MATIAS) vive en una única fila de la tabla `empresas` (patrón singleton `Empresa::instancia()`, id=1), no en `.env`.

---

## 2. Modelos — campos reales y relaciones

Esta sección resume los modelos más relevantes para una app móvil de campo (lecturistas/revisores) y de facturación/pagos. Los tipos de columna fueron confirmados contra las migraciones citadas. Para los modelos administrativos menos relevantes (menús, permisos, roles del panel web) se da un resumen breve al final.

### 2.1 `Cliente` (`app/Models/Cliente.php`, tabla `clientes`)

Migraciones: `2026_02_27_000001_create_clientes_table.php` + alters `2026_03_04_000006/000008/000009`, `2026_07_01_000001`, `2026_07_16_000001`.

| Campo | Tipo real | Notas |
|---|---|---|
| id | increments (unsigned int) | PK |
| suscriptor | varchar(50) unique | Código suscriptor, coincide con `Entrada.Suscriptor`/`Ordenesmtl.Suscriptor` |
| nuip | varchar(30) nullable | Cédula/documento |
| tipo_documento | varchar(10) nullable | CC, TI, CE, PA… |
| nombre / apellido | varchar(150) nullable | |
| telefono | varchar(30) nullable | |
| email | varchar(150) nullable | |
| acepta_email / acepta_whatsapp | boolean, default true | Opt-in de notificaciones |
| direccion | varchar(255) nullable | |
| serie_medidor | varchar(100) nullable | |
| estrato_id | tinyint unsigned nullable, FK→`estratos.id` (restrict) | **Determina el precio a cobrar** (ver §4) |
| servicios | varchar(10), default `AG-AL` | `AG` acueducto \| `AL` alcantarillado \| `AG-AL` ambos |
| tipo_uso | enum(`RESIDENCIAL`,`COMERCIAL`,`INDUSTRIAL`,`OFICIAL`,`TURISTICO`,`INSTITUCIONAL`), default `RESIDENCIAL` | Ver advertencia importante en §6.1 — **no afecta el cálculo tarifario**, solo es un snapshot informativo |
| tiene_medidor | boolean, default true | Si es false, se factura por promedio, sin lectura de campo (con la excepción de §6.2) |
| sector | varchar(100) nullable | |
| ruta / id_ruta / consecutivo | varchar(100)/int/int nullable | Orden de recorrido del lecturista |
| promedio_consumo | decimal(10,2), default 0 | Se recalcula en cada facturación (últimos 6 meses) |
| estado | enum(`ACTIVO`,`SUSPENDIDO`,`CORTADO`,`INACTIVO`), default `ACTIVO` | |
| fecha_corte | date nullable | |

**Relaciones:** `estrato()` belongsTo `Estrato`; `fotos()` hasMany `ClienteFoto`; `series()` hasMany `ClienteSerie`; `ordenes()` hasMany `Admin\Ordenesmtl` (FK local `suscriptor`); `facturas()` hasMany `Factura`; `otrosCobros()` hasMany `ClienteOtrosCobro`; `historicoConsumos()` hasMany `ClienteHistoricoConsumo`; `macromedidorActual()` hasOne `MacromedidorCliente` (activa = `vigente_hasta IS NULL`); `notificaciones()` hasMany `Notificacion`.

**Métodos clave:** `lecturaEsNormal($consumo, $tolerancia=0.5)` (± 50% del promedio); `telefonoWhatsapp()` (normaliza a E.164 agregando `57` si detecta 10 dígitos); `puedeRecibirEmail()`/`puedeRecibirWhatsapp()`; `upsertDesdeDatos()` (upsert legado usado por la API móvil, auto-puebla desde `Ordenesmtl` si es cliente nuevo); **`toApiArray()` — es literalmente el formato de salida pensado para la app móvil**:
```json
{
  "id": 1, "suscriptor": "...", "nuip": "...", "tipo_documento": "...",
  "nombre": "...", "apellido": "...", "telefono": "...", "direccion": "...",
  "serie_medidor": "...", "foto_medidor": "uploads/...", "foto_predio": "uploads/...",
  "ruta_fotos": "ruta1,ruta2,ruta3"
}
```

### 2.2 `Factura` (`app/Models/Factura.php`, tabla `facturas`)

Migración: `2026_03_04_000008_create_facturas_table.php` + `2026_03_09_000013_add_subsidio_alcantarillado.php`.

Campos principales (decenas de columnas, todas `decimal(14,2)` para valores monetarios salvo donde se indica):
`numero_factura` (varchar30 unique), `suscriptor`, `cliente_id`, `periodo_lectura_id`, `tarifa_periodo_id`, `periodo` (char6 `YYYYMM`), fechas (`fecha_del/hasta/expedicion/vencimiento/corte`), snapshot del predio (`serie_medidor`, `sector`, `estrato_snapshot`, `clase_uso`, `tiene_medidor_snapshot`, `servicios_snapshot`), lectura/promedio (`lectura_anterior/actual`, `consumo_m3`, `prom_m1..m6`, `promedio_consumo_snapshot`), desglose completo de **acueducto** y **alcantarillado** (cargo fijo, básico/complementario/suntuario en m³ y valor, subsidio, total, otros cobros), desglose de **conexión** (financiación de acometida), `saldo_anterior`, `facturas_en_mora`, `total_a_pagar`, y control: `estado` **enum(`PENDIENTE`,`PAGADA`,`VENCIDA`,`ANULADA`)**, `es_automatica` (boolean), `orden_revision_id`, `usuario_id`, `observaciones`.

**Relaciones:** `cliente()`, `periodoLectura()`, `tarifaPeriodo()` (belongsTo); `pagos()` hasMany `Pago`; `notificaciones()` hasMany `Notificacion`.

**Métodos:** `totalPagado()` (suma `pagos.total_pago_realizado`), `saldoPendiente()` = `max(0, total_a_pagar - totalPagado())`, `estaPagada()`, `estaVencida()`, `yaSeNotifico($tipo,$canal)`, `generarNumero($periodo)` estático (correlativo `{periodo}{secuencia5digitos}`).

⚠️ **No se encontró en el código el mecanismo que transiciona automáticamente `PENDIENTE → VENCIDA`** al pasar `fecha_vencimiento`. `CarteraController` calcula antigüedad de mora directamente sobre `fecha_vencimiento`, sin depender de ese cambio de estado, así que funciona igual, pero no se pudo confirmar si existe un job/comando que actualice el campo `estado` formalmente.

### 2.3 `Pago` (`app/Models/Pago.php`, tabla `pagos`)

Campos: `factura_id` (FK restrict), `fecha_pago`, `numero_recibo`, `medio_pago` enum(`EFECTIVO`,`TRANSFERENCIA`,`CONSIGNACION`,`DATAFONO`,`OTRO`), `banco`, `referencia_pasarela`, `estado_pasarela`, 6 columnas de desglose (`pagos_acueducto`, `pagos_alcantarillado`, `pago_otros_cobros_acueducto/alcantarillado`, `pago_conexion_acueducto/alcantarillado`, todas decimal(14,2)), `total_pago_realizado`, `usuario_id`, `observaciones`.

**Hook automático (`booted()` → `static::created`)**: al crear cualquier `Pago` (venga de donde venga: panel manual, Wompi, etc.), recarga la factura y si `saldoPendiente() <= 0` la marca `estado = 'PAGADA'` automáticamente. Esto es clave: **cualquier flujo nuevo que registre pagos (incluida una futura pasarela desde la app móvil) hereda este comportamiento gratis con solo crear el modelo `Pago`.**

### 2.4 Motor de tarifas: `TarifaPeriodo`, `TarifaCargoFijo`, `TarifaRango`, `Estrato`

- **`tarifa_periodos`** (`TarifaPeriodo`): `nombre`, `numero_resolucion`, `vigente_desde`/`vigente_hasta` (date, null=vigente), `activo` (boolean), `observaciones`. Método estático `vigente()` obtiene la tarifa activa actual — **ver bug potencial en §6.7**.
- **`tarifa_cargos_fijos`** (`TarifaCargoFijo`): único por `(tarifa_periodo_id, servicio, estrato_id)`, `cargo_fijo` decimal(14,2). `servicio` enum(`ACUEDUCTO`,`ALCANTARILLADO`).
- **`tarifa_rangos`** (`TarifaRango`): único por `(tarifa_periodo_id, servicio, estrato_id, tipo)`, `tipo` enum(`BASICO`,`COMPLEMENTARIO`,`SUNTUARIO`), `rango_desde`/`rango_hasta` (m³, `rango_hasta` null = ilimitado), `precio_m3` decimal(14,4).
- **`estratos`** (`Estrato`): `numero` (tinyint unique), `nombre`, `codigo`, `porcentaje_subsidio` decimal(5,2) (positivo=subsidio, negativo=sobretasa), `subsidio_fijo_acueducto`/`subsidio_fijo_alcantarillado` decimal(14,2) (0 = usar porcentaje), `consumo_minimo_subsidio` decimal(8,2) (umbral mínimo de consumo para que aplique; **default real en columna = 4**, confirmado en migración `2026_03_14_000014`, aunque el `?? 0` del código de `FacturacionService` sugiere que si el valor viniera `null` se trataría como 0), `activo` (boolean). Helpers `tieneSubsidio()`/`tieneSobretasa()`.

  Datos reales sembrados (`EstratoSubsidioSeeder.php` y migración base): estratos 1-3 con subsidio positivo (70%/40%/15%), estrato 4 neutro (0%), estratos 5-6 y Comercial/Industrial con sobretasa (-50%/-60%/-60%/-60%), Oficial neutro (0%). El seeder de Voragine agregó dos estratos adicionales: **10=Turístico (-60%, igual que Comercial/Industrial)** y **11=Institucional (0%, igual que Oficial)** — ver §6.1.

### 2.5 Otros cobros: `OtrosCobrosCatalogo` / `ClienteOtrosCobro`

- **`otros_cobros_catalogo`**: catálogo maestro (`nombre`, `codigo` unique, `descripcion`, `aplica_acueducto`/`aplica_alcantarillado` (bool), `requiere_diametro` (bool), `permite_cuotas` (bool), `activo`). Sembrado con: Cambio de Medidor, Instalación de Acometida, Reconexión, Conexión Acueducto, Conexión Alcantarillado, Multa/Sanción.
- **`cliente_otros_cobros`**: instancia por cliente (`cliente_id`, `catalogo_id`, `tipo_servicio` enum ACUEDUCTO/ALCANTARILLADO, `concepto`, `diametro` nullable, `monto_total`, `num_cuotas`, `cuota_mensual`, `cuotas_pagadas`, `saldo`, `fecha_inicio`, `estado` enum(`ACTIVO`,`PAGADO`,`ANULADO`)). Métodos `pagarCuota()` (descuenta 1 cuota al generar factura) y `revertirCuota()` (al anular factura).

### 2.6 Macromedidores: `Macromedidor`, `MacroLectura`, `MacromedidorCliente`

- **`macromedidores`**: `codigo_macro` (unique), `ubicacion`, `lectura_anterior` (**integer**), `estado` (string, default PENDIENTE), `lectura_actual` (⚠ **varchar**, no integer — asimetría de tipos confirmada en migración, el modelo no lo castea), `observacion`, GPS, `fecha_lectura`, `sincronizado`, `usuario_id` (FK→`usuario`).
- **`macro_lecturas`** (historial, reemplaza el modelo de lectura única): `macromedidor_id`, `usuario_id`, `lectura_anterior`/`lectura_actual`/`consumo` (integer), GPS, `fecha_lectura` (datetime), `sincronizado`.
- **`macromedidor_clientes`** (asignación histórica cliente↔macro): `macromedidor_id`, `cliente_id`, `vigente_desde`/`vigente_hasta` (date, null=activa). Scope `vigentesEn($fecha)`.
- `Macromedidor::toApiArray()` — formato para la app Android (**`MacroEntity`**, según comentario del código): `lectura_anterior` en la respuesta es en realidad la **última lectura registrada** (o la inicial si no hay ninguna); `estado` se fuerza siempre a `"PENDIENTE"` para que la app siempre lo ofrezca a leer.

### 2.7 Revisiones de campo: `OrdenRevision`, `CensoHidraulico`, `RevisionFoto`

`ordenes_revision`: vinculada a la lectura original (`lectura_id` → `Admin\Ordenesmtl`), copia datos del predio, `estado_orden` (PENDIENTE/EJECUTADO), campos llenados por el revisor en campo (`estado_acometida`, `estado_sellos`, `nombre_atiende`, `tipo_documento`, `documento`, `num_familias`, `num_personas`, `motivo_revision`, `motivo_detalle`, `generalidades`), `firma_cliente` (ruta imagen), `acta_pdf` (ruta PDF), `nueva_lectura` (integer, agregada después), GPS del predio, `fecha_cierre`, `sincronizado`, `usuario_id`. `censo_hidraulico` (puntos hidráulicos: `tipo_punto`, `cantidad`, `estado` BUENO/MALO) y `revision_fotos` son hijas 1-N.

`OrdenRevision::crearDesdeLectura()` deriva automáticamente el motivo (`DESVIACION_BAJA`/`DESVIACION_ALTA`/`OTRO`) comparando consumo vs. promedio (±50%/±200%). `toApiArray()` — formato **`RevisionEntity`** para Android, con `ruta_fotos` como string de rutas separadas por coma (no array JSON).

### 2.8 `Notificacion` (tabla `notificaciones`)

`factura_id` (nullable, FK cascade), `cliente_id` (FK restrict), `canal` (`email`|`whatsapp`), `tipo` (`FACTURA_GENERADA`|`RECORDATORIO_VENCIMIENTO`|`ALERTA_CORTE`|`MANUAL`), `destinatario` (snapshot), `estado` (`PENDIENTE`|`ENVIADO`|`ERROR`|`OMITIDO`), `proveedor_mensaje_id`, `error_mensaje`, `intentos`, `usuario_id`, `enviado_en`. Ver §5.3 para el ciclo de vida.

### 2.9 `Empresa` (tabla `empresas`, singleton `id=1`)

Configuración global de la única empresa (`Empresa::instancia()`). Contiene: datos generales, **credenciales Wompi** (`wompi_public_key`, `wompi_private_key`, `wompi_integrity_key`, `wompi_test_mode`, `wompi_redirect_url`, `wompi_recargo_fijo`, `wompi_recargo_porcentaje`), **MATIAS/DIAN** (`matias_api_token`, `matias_modo_pruebas`, campos de resolución DIAN — ver §5.1, no implementado), **notificaciones** (SMTP por-empresa: `notif_mail_*`; WhatsApp Meta Cloud API: `whatsapp_phone_number_id`, `whatsapp_business_account_id`, `whatsapp_access_token`, `whatsapp_api_version`, plantillas por tipo, `whatsapp_modo_pruebas`; `notif_dias_antes_vencimiento`/`notif_dias_antes_corte`/`notif_canal_automatico_factura`), y **diseño de factura** (colores, flags `factura_mostrar_*`). Método clave: `calcularRecargoWompi($saldoPendiente)` (ver §4.5).

⚠️ Ninguna de estas credenciales está encriptada a nivel de columna (`$casts` no declara ningún `encrypted`), y no viven en `.env` — quedan en texto plano en la tabla `empresas`.

### 2.10 `Usuario` — Seguridad (`app/Models/Seguridad/Usuario.php`, tabla `usuario`)

Este es el **modelo de autenticación real** (empleados/operarios: lecturistas, revisores, administradores). `config/auth.php` apunta el provider `users` a `App\Models\Seguridad\Usuario`, no a `App\User`.

Campos: `usuario` (unique), `nombre`, `tipodeusuario`, `email` (unique), `empresa`, `password`, `api_token` (varchar191, unique, nullable — token hasheado para la API móvil), `remenber_token` (⚠ ver bug abajo), `estado`.

⚠️ **Bug de nombre de columna:** el `$fillable` del modelo declara `remember_token` (ortografía estándar de Laravel), pero la **columna real en base de datos se llama `remenber_token`** (con error tipográfico, confirmado en la migración `2020_02_12_182855_create_usuario_table.php` y en el mutador `setPasswordAttribute()`, que escribe explícitamente en `$this->attributes['remenber_token']`). Esto significa que el campo `remember_token` del fillable no corresponde a ninguna columna física — cualquier asignación masiva a `remember_token` no persiste. Además, `setPasswordAttribute()` sobreescribe `remenber_token` con el **hash de la contraseña** (no con un remember-token real), lo cual parece un error de copy-paste, no un diseño intencional.

**Relación:** `roles1()` belongsToMany `Admin\Rol` vía `usuario_rol`.

### 2.11 `App\User` (`app/Models/User.php`) — VESTIGIAL, no usar

Namespace `App` (no `App\Models`, a pesar de la ubicación del archivo). Scaffolding por defecto de Laravel (`name`, `email`, `password`). **No existe ninguna migración `create_users_table` en el repositorio** — no se puede confirmar si la tabla `users` existe físicamente. No se encontró ninguna referencia a esta clase en el resto del código (`grep` sin resultados). Tratar como código muerto.

### 2.12 Resto de modelos (resumen breve)

| Modelo | Tabla | Rol |
|---|---|---|
| `ClienteFoto` | `cliente_fotos` | Fotos del cliente (`tipo`: documento\|medidor\|predio) |
| `ClienteSerie` | `cliente_series` | Historial de series de medidor por período |
| `ClienteHistoricoConsumo` | `cliente_historico_consumos` | 1 fila por factura; base del promedio de 6 meses |
| `PeriodoLectura` | `periodos_lectura` | Ciclo de vida `PLANIFICADO→ACTIVO→LECTURA_CERRADA→FACTURADO→CERRADO` (⚠ `tarifa_periodo_id` sin FK física, solo relación Eloquent) |
| `ListaParametro` | `listas_parametros` | Catálogos para dropdowns de la app móvil (motivos, tipo de documento, tipo de punto, etc.), expone `toApiArray()` |
| `Exportacion` | `exportaciones` | Jobs de exportación masiva (PDF/Excel), con progreso |
| `Admin\Entrada` | `entrada` | Registro "maestro" de una orden de lectura (⚠ `servicio` en `$fillable` sin columna real, ver §6.8) |
| `Admin\Ordenesmtl` | **`ordenescu`** (⚠ el nombre de la clase NO coincide con el de la tabla) | Orden de lectura de campo/legado. Tiene columnas `sync`/`sync_at` en `$fillable` **sin migración que las respalde** — no se pudo confirmar si existen físicamente (ver §6.8) |
| `Admin\Orden_ejecutada` / `Admin\Bitacora_Orden_ejecutada` | `orden_ejecutada` / `bitacora_orden_ejecutada` | Resultado de una lectura ejecutada en campo; bitácora tiene también un campo fillable (`tabla_origen`) sin columna real |
| `Admin\Marcas` | `marcas` | Catálogo de marcas de medidor (`marca_id` cambiado de integer a varchar por migración posterior) |
| `Admin\Mtl` | `mtl` | ⚠ **Sin migración localizable en el repo** — no se pudo confirmar su esquema |
| `Admin\Menu`, `Admin\Rol`, `Admin\Permiso`, `Admin\MenuRol`, `Admin\PermisoRol` | `menu`, `rol`, `permiso`, `menu_rol`, `permiso_rol` | Sistema de menús/roles/permisos del panel web administrativo — no relevante para la app móvil de campo. `menu_rol` y `permiso_rol` son tablas pivote **sin columna `id` propia**; usarlas vía Eloquent estándar (`::find()`) fallaría, el código real las consulta con `DB::table()`. |

---

## 3. Autenticación y endpoints de la API (para consumo desde Android/Flutter)

### 3.1 Mecanismo de autenticación — token custom (NO Sanctum/Passport/JWT)

- `config/auth.php`: guard `api` con `driver: token`, `hash: true`, `storage_key: api_token`, provider `users` → `App\Models\Seguridad\Usuario`.
- El middleware realmente aplicado en las rutas (`app/Http/Kernel.php`, alias `auth.api.token` → `App\Http\Middleware\AuthenticateWithApiToken`):
  1. Lee `Authorization: Bearer <token>` (`$request->bearerToken()`). Sin token → `401 {"error":"Token no proporcionado"}`.
  2. Hashea con `sha256` y busca `Usuario::where('api_token', $hash)->where('estado','activo')->first()`. Si no existe/inactivo → `401 {"error":"Token inválido o usuario inactivo"}`.
  3. Si es válido: `Auth::setUser($user)` — luego `$request->user()` en los controladores devuelve ese `Usuario`.
- **Login/obtención de token — `POST /loginMovil1`** → `Seguridad\LoginController@loginMovil` (ruta pública, sin middleware, fuera del prefijo `/api`):
  - Body: `usuario`, `password` (sin Form Request, `$request->only(...)` + `Auth::attempt()`).
  - Éxito + `estado=='activo'`: genera `Str::random(60)`, guarda su hash sha256 en `usuario.api_token`, responde **200** con un **array que contiene un único objeto** (¡ojo, no un objeto plano!):
    ```json
    [{
      "id": 1, "usuario": "jperez", "nombre": "Juan Pérez", "tipodeusuario": "...",
      "email": "...", "empresa": "...", "remenber_token": null, "estado": "activo",
      "api_token": "TOKEN_PLANO_60_CHARS", "created_at": "...", "updated_at": "..."
    }]
    ```
    Nótese `remenber_token` (typo literal en la clave JSON, y su valor será siempre `null` por el bug de §2.10).
  - `estado != 'activo'` → `403 {"error":"Usuario no activo"}`. Credenciales inválidas → `401 {"error":"Credenciales incorrectas"}`.
- **No existe login para "clientes finales"** (el suscriptor del servicio de agua). Todo el sistema de token es para **empleados de campo** (lecturistas, revisores). El modelo `Cliente` no tiene autenticación propia.
- **Sin rate limiting** (`throttle`) en ninguna ruta de la API.
- Ningún endpoint usa Form Requests ni API Resources — todo es `$request->input()`/`response()->json([...])` manual (excepto los 3 controladores `Api\*` y `PagoPublicoController::buscar`, que sí validan puntualmente).

⚠️ **Inconsistencia real de seguridad/diseño en `/api/cliente`:** esta ruta está protegida por el middleware `auth.api.token` (exige bearer token válido), pero **además** `Api\ClienteApiController` implementa su propio helper privado `_validarToken()` que compara un parámetro `api_token` (query string en GET, campo de form en POST) **en texto plano** contra la columna `api_token` (que en teoría guarda el hash). Cualquier cliente de esta API debe probar empíricamente contra el backend real qué valor exacto exige ese segundo parámetro — el código, tal como está, sugiere una inconsistencia que documentar y no asumir.

### 3.2 Endpoints de `routes/api.php`

Todos bajo `Route::middleware('auth.api.token')->group(...)` salvo `loginMovil1`:

| Método | Ruta | Controlador@método | Consume | Devuelve |
|---|---|---|---|---|
| POST | `/loginMovil1` | `Seguridad\LoginController@loginMovil` | `usuario`, `password` | Ver arriba (array con 1 objeto) |
| POST | `/api/medidoresout` | `OrdenesmtlasignarController@medidorall` | — (usa `$request->user()`) | `200`: array de filas crudas de `ordenescu` (Estado=2, del usuario autenticado) **o** `200 {"error":"Sin medidores asignados"}` si no hay |
| POST | `/api/medidores` | `OrdenEjecutadaController@medidorejecutado` | `id`, `tipo`, `campoFoto`, `critica`, `causal`, `texcausa`, `observ`, `texobser`, `lectact`, `latitud`, `longitud`, `ffinlec` (`d/m/Y H:i:s`), `suscriptor` (opcional), `consumo`, `observg` — **sin validación** | Inserta/actualiza `orden_ejecutada` + `ordenescu` (marca `EJECUTADO`). Éxito (rama insert): `200 {"success":true,"message":"Lectura cargada en servidor"}` |
| POST | `/api/marcas` | `MarcasController@marcasall` | — | Si el usuario tiene órdenes con `Estado=2` pendientes: `200` array de `Marcas::all()` (todas las columnas). Si no: `403 {"error":"No puede sincronizar listas"}` |
| POST | `/api/medidorejecutado` | `OrdenesmtlasignarController@medidorejecutadosync` | `year`, `month` | Marca `sync=2` en `ordenescu` (Estado=4) y responde `200 {"mensaje":"Lecturas sincronizadas correctamente","datos":[...]}` o `200 {"error":"sin medidores ejecutados"}` |
| GET | `/api/ordenesMacro` | `Api\MacromedidorApiController@ordenesMacro` | — | `200` array de `Macromedidor::toApiArray()` de los macros del usuario (ver §2.6) |
| POST | `/api/macromedidoresMovil` | `Api\MacromedidorApiController@enviarMacro` | `id_orden`, `lectura_actual`, `observacion?`, `gps_latitud?`, `gps_longitud?`, `fotos[]?` | `404` si el macro no pertenece al usuario. Éxito: `{"success":true,"message":"Lectura registrada correctamente","id":N,"consumo":N,"lectura_anterior":N}`. **`consumo` puede ser negativo, no hay validación**; `fecha_lectura` se fija con la hora del **servidor**, no del dispositivo |
| GET | `/api/ordenesRevision` | `Api\RevisionApiController@ordenesRevision` | — | `200` array de `OrdenRevision::toApiArray()` del usuario |
| POST | `/api/revisionesMovilV2` | `Api\RevisionApiController@enviarRevisionV2` | Wizard completo: `id_orden`, `estado_acometida`, `estado_sellos`, `nombre_atiende`, `tipo_documento`, `documento`, `num_familias`, `num_personas`, `motivo_revision`, `motivo_detalle`, `generalidades`, `gps_latitud`, `gps_longitud`, `nueva_lectura?`, `censo_hidraulico_json` (string JSON: array de `{tipo_punto,cantidad,estado}`), `fotos[]?`, `firma_cliente` (archivo), `acta_pdf?` (archivo) | `404` si no pertenece al usuario. Éxito: `{"success":true,"message":"Revision recibida correctamente","id":N}`. Borra y re-crea el censo hidráulico en cada envío (idempotente para esa subtabla) |
| GET | `/api/listasParametros` | `Api\RevisionApiController@listasParametros` | — | `200` array de `ListaParametro::toApiArray()` activos |
| GET | `/api/cliente?api_token=...&suscriptor=...` | `Api\ClienteApiController@consultar` | `suscriptor` (query, requerido) | `200 {"encontrado":true/false,"cliente": <toApiArray()> \| null}`. `422` si falta `suscriptor` |
| POST | `/api/cliente` | `Api\ClienteApiController@guardar` | `suscriptor` (requerido), `nuip`, `tipo_documento`, `nombre`, `apellido`, `telefono`, `direccion`, `serie_medidor`, `periodo?` (`YYYYMM`, default mes actual), `orden_ejecutada_id?`, `foto_medidor?`, `foto_predio?`, `fotos[]?`, `tipos[]?` (`documento`\|`medidor`\|`predio`) | `422` si falta `suscriptor`. Éxito: `{"success":true,"message":"Perfil de cliente guardado","cliente":<toApiArray() fresco>}` |

**Observaciones para quien construya el cliente Flutter/Android:**
- Varias respuestas "sin resultados" devuelven **HTTP 200** con `{"error": "..."}` en el cuerpo (`medidorall`, `medidorejecutadosync`) — hay que inspeccionar el contenido, no solo el status code.
- Los 3 controladores `Api\*ApiController` (Macromedidor, Revision, Cliente-parcialmente) sí tienen métodos `toApiArray()` pensados explícitamente para las entidades Room de la app Android existente (comentarios en código citan `MacroEntity`, `RevisionEntity`, `ListaEntity`) — son el contrato más estable de la API.

### 3.3 `routes/web.php` — lo único relevante fuera del panel admin

Todo lo demás en `web.php` es panel administrativo (HTML, middlewares `auth`/`superadmin`/`superEditor`/`superConsultor`). Lo potencialmente relevante para un futuro flujo de pago desde app móvil:

| Método | Ruta | Controlador@método | Notas |
|---|---|---|---|
| GET | `/pagar` | `PagoPublicoController@index` | Vista HTML — formulario de búsqueda |
| POST | `/pagar/buscar` | `PagoPublicoController@buscar` | Vista HTML — busca factura, arma referencia/firma Wompi |
| GET | `/pagar/resultado` | `PagoPublicoController@resultado` | Vista HTML — Wompi redirige aquí con `?id=` |
| POST | `/webhook/wompi` | `PagoPublicoController@webhook` | JSON — webhook real de Wompi (excluido de CSRF) |

**Hoy no existe ningún endpoint JSON pensado para que un cliente final consulte o pague su factura desde una app.** El flujo actual (`/pagar`) es 100% HTML/Blade + JS de Wompi Checkout. Si la app Flutter necesita permitir pagos, hay que **crear endpoints JSON nuevos** replicando la lógica de `PagoPublicoController::buscar()` / `registrarPagoWompi()` (detallada en §4.5) — no hay nada reusable "tal cual" para consumo API hoy.

---

## 4. Reglas de negocio de tarifas y facturación (paso a paso)

Toda la lógica vive en **`App\Services\FacturacionService::calcular(Cliente $cliente, int $consumoM3, PeriodoLectura $periodo, ?int $lecturaAnterior=null, ?int $lecturaActual=null): array`** (`app/Services/FacturacionService.php`). Es invocada desde 5 puntos distintos: `FacturaController` (preview/store/lote), `FacturacionMasivaController`, `FacturacionEspecialController`, y `PeriodoLecturaController::generarOrdenes()` (para clientes sin medidor).

### 4.1 Flujo completo

1. **Determinar el consumo a facturar:**
   - Si `!$cliente->tiene_medidor`: se ignora `$consumoM3` recibido y se usa `max(1, round($cliente->promedio_consumo))` (mínimo facturable 1 m³).
   - Si tiene medidor, se busca un **override por revisión ejecutada**: si existe una `OrdenRevision` con `estado_orden='EJECUTADO'` y `nueva_lectura` menor a la lectura original, y el consumo resultante (`nueva_lectura - lecturaAnterior`) es ≤ 2× el promedio histórico (o sin límite si no hay promedio), se reemplaza el consumo y la lectura actual por los de la revisión.
2. **Historial de 6 meses**: `ClienteHistoricoConsumo::promedioYDetalle($clienteId, 6)`. Si viene vacío y el cliente tiene medidor, hace *fallback* leyendo directo de `Ordenesmtl` (tabla legado `ordenescu`, `Estado=4`, períodos anteriores, últimos 6 `Cons_Act`).
3. **Cálculo por servicio** (`ACUEDUCTO` y `ALCANTARILLADO` independientes, cada uno activo según si `cliente.servicios` lo incluye):
   - `cargo_fijo = TarifaPeriodo->cargoFijo($servicio, $estratoId)` — monto plano, no depende del consumo.
   - `TarifaPeriodo->calcularConsumo($consumoM3, $servicio, $estratoId)` desglosa el consumo en tramos **BASICO / COMPLEMENTARIO / SUNTUARIO** según los rangos configurados en `tarifa_rangos` para ese servicio+estrato (ordenados por `rango_desde`). **Es tarifa escalonada por bloques real** (no "todo al precio del tramo en que cae"): el algoritmo calcula la intersección entre el consumo total y cada rango acumulado, cobrando cada tramo a su propio `precio_m3`. Ejemplo real sembrado (Asovoragine, junio 2026): BASICO = 1-16 m³, COMPLEMENTARIO = 17-32 m³, SUNTUARIO = 33+ m³ (sin tope), con precio distinto por servicio × clase de uso.
   - `subtotal = cargo_fijo + basico_valor + complementario_valor + suntuario_valor`.
4. **Subsidio/sobretasa por estrato** (aplica **solo sobre `basico_valor`**, no sobre cargo fijo ni sobre complementario/suntuario):
   - Condición: `consumoM3 > estrato.consumo_minimo_subsidio` (si el consumo real es menor o igual al umbral, no se aplica ni descuento ni sobretasa).
   - Prioridad: si `subsidio_fijo_acueducto/alcantarillado != 0`, se usa ese **monto fijo** (positivo=descuento). Si no, se usa `basico_valor * porcentaje_subsidio / 100` (positivo=descuento, negativo=sobretasa, por el signo del porcentaje).
   - El resultado se **resta** del total del servicio (`total -= subsidio`) — con signo negativo esto se convierte en una suma real (sobretasa).
   - El campo de salida para acueducto se llama, por razones históricas, `subsidio_emergencia` (aunque hoy es de uso general).
5. **Otros cobros activos** (`ClienteOtrosCobro`, por servicio): se suma la `cuota_mensual` (no el `monto_total`) de los cobros `ACTIVO` de ese cliente al subtotal del servicio correspondiente.
6. **Saldo anterior**: suma de `saldoPendiente()` de todas las facturas `PENDIENTE`/`VENCIDA` de períodos **anteriores** al que se está generando (más el conteo `facturas_en_mora`).
7. **Total**: `(acueducto.total + otros_acueducto) + (alcantarillado.total + otros_alcantarillado) + saldo_anterior`.
8. Se devuelve un array listo para `Factura::create()`, con `estado='PENDIENTE'`, `es_automatica = cliente->lecturaEsNormal($consumo)` (dentro de ±50% del promedio), y `numero_factura` correlativo.

Al **persistir** (`FacturaController::store`, y equivalentes en los otros flujos): se verifica que no exista ya una factura vigente para ese cliente+período; se llama `ClienteHistoricoConsumo::registrarYActualizarPromedio(...)` (recalcula y persiste `cliente.promedio_consumo`); se descuentan las cuotas de "otros cobros" activos (`->each->pagarCuota()`); se dispara `EnviarNotificacionFacturaJob` si hay canal automático configurado.

### 4.2 Tres flujos de generación de facturas (parcialmente solapados)

- **`FacturacionMasivaController`**: recorre todas las lecturas del período. Si `Critica` de la lectura de campo es `NORMAL-54`/`54-NORMAL`, factura automáticamente (`es_automatica=true`). Cualquier otra crítica **no se factura**: se crea una `OrdenRevision` (`REVISION_FACTURACION`) y queda pendiente de revisión manual.
- **`FacturacionEspecialController`**: trabaja exactamente sobre el complemento (lecturas **excluyendo** `NORMAL-54`/`54-NORMAL`) — pantalla de trabajo manual para que un operador revise/edite y facture selectivamente lo anómalo (`es_automatica=false`).
- **`FacturaController::lote()`/`storeLote()`**: un tercer flujo más granular, clasifica clientes sin factura en buckets (`sin_medidor`, `consumo_cero`, `promedio_medidor`, `causado`, `alto`, `bajo`, `negativo`, `normal`) permitiendo generar facturas para cualquier subconjunto.

⚠️ No hay indicios en el código de cuál de los tres es "el vigente" o si alguno está deprecado — los tres llaman al mismo `FacturacionService::calcular()` y parecen activos simultáneamente.

### 4.3 Cartera / mora (`CarteraController`)

Consolidado de antigüedad ("aging") por cliente: por cada factura `PENDIENTE`/`VENCIDA` con saldo real > $0.01, clasifica `diasVencido` (Carbon, negativo si aún no vence) en buckets: `corriente` (≤0), `b_1_30`, `b_31_60`, `b_61_90`, `b_90_mas`. Es un módulo de **solo consulta/reporte**, no ejecuta ninguna acción de corte de servicio automáticamente.

### 4.4 Balance de macromedidores / pérdidas (`MacromedidorBalanceController::calcularBalance()`)

```
consumoMacro    = SUM(MacroLectura.consumo) donde fecha_lectura ∈ [periodo.fecha_inicio_lectura, periodo.fecha_fin_lectura]
consumoClientes = SUM(ordenescu.Cons_Act) de los clientes vigentemente asignados a ese macro
                  (MacromedidorCliente::vigentesEn(periodo.fecha_fin_lectura)) en ese mismo periodo_lectura_id
perdida         = consumoMacro - consumoClientes
%perdida        = perdida / consumoMacro * 100
```
Esto es el Índice de Agua No Contabilizada (IANC) por macromedidor y consolidado. **Usa el consumo de campo bruto (`ordenescu.Cons_Act`), no el consumo efectivamente facturado** (`facturas.consumo_m3`, que puede diferir por ajustes de revisión/promedios) — no se pudo confirmar si esto es intencional (medir pérdida física real de agua) o una inconsistencia.

La asignación cliente↔macro nunca se borra, se "cierra" seteando `vigente_hasta` y se crea una nueva fila vigente — permite reconstruir el histórico de qué macro surtía a qué cliente en cualquier fecha pasada.

### 4.5 Wompi — recargo, flujo y persistencia

- **Fórmula del recargo** (`Empresa::calcularRecargoWompi($saldoPendiente)`): `round(wompi_recargo_fijo + saldoPendiente * wompi_recargo_porcentaje / 100)`. Ambos configurables por empresa, no hardcodeados. **Lo asume el suscriptor** (comentario explícito en el código): se cobra `saldoPendiente + recargo` en Wompi, pero solo se abona a la factura `min(monto_recibido, saldoPendiente)` — el excedente no reduce el saldo, cubre la comisión de la pasarela.
- **Flujo (`PagoPublicoController`, sin API server-to-server de creación — usa el Widget/Checkout hospedado de Wompi):**
  1. `buscar()`: genera `referencia = "FACTURA-{facturaId}-{timestamp}"`, `amountCents = round(saldo + recargo)`, firma `hash('sha256', referencia.amountCents.'COP'.wompi_integrity_key)` para el checkout.
  2. `resultado()`: Wompi redirige con `?id=`; se consulta `GET {sandbox|production}.wompi.co/v1/transactions/{id}` con `Bearer wompi_private_key`; si `status==='APPROVED'`, llama a `registrarPagoWompi()`.
  3. `webhook()` (`POST /webhook/wompi`, excluida de CSRF): valida `X-Event-Checksum` contra `hash('sha256', json_encode(data).sent_at.wompi_private_key)` con `hash_equals`; si `event==='transaction.updated'` y `status==='APPROVED'`, llama `registrarPagoWompi()`.
  4. `registrarPagoWompi()`: extrae `factura_id` del regex `/^FACTURA-(\d+)-/`; evita duplicados (`Pago::where('referencia_pasarela', $tx['id'])->exists()`); crea `Pago` (`medio_pago='TRANSFERENCIA'`, `banco='Wompi (en línea)'`, `pagos_acueducto = min(amount_in_cents, saldoPendiente)`); el hook del modelo `Pago` marca la factura `PAGADA` si el saldo llega a 0.
- Credenciales/sandbox: por-empresa en tabla `empresas`, no en `.env`. `wompi_test_mode` decide `sandbox.wompi.co` vs `production.wompi.co`.

---

## 5. Integraciones externas

### 5.1 MATIAS API (facturación electrónica DIAN) — ⚠️ **NO IMPLEMENTADA**

Búsqueda exhaustiva (`grep -rli matias|dian|cufe|ubl` en todo `app/`) solo encuentra:
- Migraciones que agregan columnas de configuración a `empresas`: `matias_api_token` (varchar500, comentario "PAT de Matias API"), `matias_modo_pruebas` (boolean), y campos de la resolución DIAN (`resolucion_dian_numero`, vigencias, rangos, consecutivo).
- `Empresa.php`: expone esos campos, **sin ningún método que arme un payload o llame a un endpoint**.
- `EmpresaController.php`: solo valida y guarda esos campos desde el formulario de configuración.
- La vista `configuracion/empresa.blade.php` tiene texto aspiracional ("Se envía como Bearer token en cada solicitud a Matias API") que **no corresponde a código real** — no existe ningún cliente HTTP en el proyecto que use `matias_api_token`.

**Conclusión: no hay ningún servicio, job ni controlador que dispare una llamada a MATIAS al generar factura, facturar masivamente, ni manualmente.** `FacturacionService.php` no tiene ninguna referencia a "matias" ni "dian". Es una funcionalidad preparada solo a nivel de esquema de datos y UI, pendiente de implementación real. **Cualquier IA que trabaje sobre este backend no debe asumir que la facturación electrónica DIAN funciona** — si el negocio la necesita, hay que construirla desde cero.

### 5.2 Wompi — ver §4.5 (implementada y funcional)

### 5.3 Notificaciones (`app/Services/Notificaciones/`)

Arquitectura *Strategy*: `NotificacionService::enviarParaFactura(Factura, string $tipo, array $canales, ?int $usuarioId)` orquesta el envío; crea el registro `Notificacion` en `PENDIENTE` **antes** de intentar enviar, y lo actualiza según resultado. Nunca lanza excepción por falta de datos de contacto — registra `OMITIDO` y sigue con los demás canales; si algún canal falla de verdad, relanza la excepción al final (para que el Job la cuente como intento fallido y reintente).

- **Canal `email`** (`EmailNotificacionChannel`): configura el driver SMTP **dinámicamente en runtime** desde `Empresa` (multi-tenant, no usa `.env`: `notif_mail_host/port/username/password/encryption/from_*`). Envía `App\Mail\FacturaMail` (adjunta el PDF vía DomPDF). Disponible solo si `Empresa::tieneCorreoConfigurado()`.
- **Canal `whatsapp`** (`WhatsappNotificacionChannel`): **Meta Cloud API (WhatsApp Business Platform)**, confirmado en el código. Endpoint `https://graph.facebook.com/{whatsapp_api_version}/{whatsapp_phone_number_id}/messages`, `Authorization: Bearer {whatsapp_access_token}` (vía `GuzzleHttp\Client` directo, no la fachada `Http::`). Envía siempre **"message templates" pre-aprobadas** (no texto libre, por la ventana de 24h de Meta) — mapa tipo→plantilla: `FACTURA_GENERADA`, `RECORDATORIO_VENCIMIENTO`, `ALERTA_CORTE`, `MANUAL`. Si `whatsapp_modo_pruebas=true`, **no llama a Meta**, simula y loguea (`return 'SIMULADO-'.uniqid()`). Disponible solo si `Empresa::tieneWhatsappConfigurado()`.

**Disparadores confirmados (`grep -rn EnviarNotificacionFacturaJob`):**
1. Generación individual de factura (`FacturaController.php:471`) — tipo `FACTURA_GENERADA`, canales según `Empresa.notif_canal_automatico_factura`.
2. Envío manual (`FacturaController::notificar()`, `:584`) — tipo `MANUAL`, canales elegidos por el usuario.
3. Facturación masiva (`FacturaController.php:893`) — mismo tipo `FACTURA_GENERADA`, en bucle.
4. **Cron diario 08:00** — comando `notificaciones:recordatorio-vencimiento` (`EnviarRecordatoriosVencimiento.php`): facturas `PENDIENTE`/`VENCIDA` cuyo `fecha_vencimiento` cae exactamente `notif_dias_antes_vencimiento` días adelante (default 3).
5. **Cron diario 08:15** — comando `notificaciones:alerta-corte` (`EnviarAlertasCorte.php`): usa `Factura::mora()` y `fecha_corte = hoy + notif_dias_antes_corte` (default 2).

Ambos comandos filtran con `Factura::yaSeNotifico($tipo,$canal)` antes de encolar, para no duplicar envíos ya exitosos. El scheduler corre vía cron de cPanel cada minuto (`schedule:run` → despacha `queue:work --once --stop-when-empty`).

**Estados de `Notificacion`:** `PENDIENTE` (creado, no enviado aún) → `ENVIADO` (éxito, guarda `proveedor_mensaje_id`+`enviado_en`) | `ERROR` (excepción, incrementa `intentos`) | `OMITIDO` (cliente sin opt-in/dato de contacto, o canal no configurado en `Empresa`).

---

## 6. Reglas de negocio no obvias, advertencias y deuda técnica

### 6.1 Turístico / Institucional — inconsistencia real confirmada

- La migración `2026_07_16_000001_add_turistico_institucional_to_clientes_tipo_uso.php` **sí amplió** el enum `clientes.tipo_uso` para incluir `TURISTICO` e `INSTITUCIONAL` (comentario: *"La tarifa real del acueducto diferencia 2 clases de uso adicionales... cada una con cargo fijo y precio m³ propios"*).
- **PERO** `ClienteController.php:137` sigue validando `'tipo_uso' => 'nullable|in:RESIDENCIAL,COMERCIAL,INDUSTRIAL,OFICIAL'` — **no permite guardar esos dos valores nuevos a través del formulario/API validada del panel**. Esto es una inconsistencia real y verificable entre el esquema de BD y la capa de validación de la aplicación.
- **El motor de tarifas (`FacturacionService`) no usa `tipo_uso` en absoluto para calcular precio — usa exclusivamente `estrato_id`.** Por eso `TarifasVoragineSeeder.php` no tocó `tipo_uso`: en vez de eso creó **dos estratos nuevos** (`10=Turístico`, `11=Institucional`) con su propio cargo fijo y rangos de precio por m³, y la forma real de "convertir" un cliente en tarifa turística/institucional es **asignarle `estrato_id = 10` o `11`**, no cambiar su `tipo_uso`.
- **Para la app móvil:** `tipo_uso` es un campo puramente descriptivo/snapshot en la factura (y controla mostrar/ocultar en el diseño del PDF vía `Empresa.factura_mostrar_tipo_uso`), no debe usarse como criterio de negocio para tarifas. Lo que determina el precio es siempre `cliente.estrato_id`.

### 6.2 Clientes sin medidor pero con acueducto también deben salir a lectura de campo

Confirmado en `PeriodoLecturaController::generarOrdenes()` (líneas ~142-147) y en el seeder de corrección `CorreccionSinMedidorConAcueductoSeeder.php`: la regla **no es simplemente "sin medidor → facturación automática sin visita"**. La regla real es:
- `tiene_medidor = true` → sale a **orden de lectura de campo** (incluye clientes con acueducto pero sin serie de medidor real registrada, a los que **se deja `tiene_medidor=true` a propósito** para forzar la visita y verificar el predio).
- `tiene_medidor = false` → **solo** aplica para clientes exclusivamente de alcantarillado (`servicios` sin componente de acueducto) — se factura automático por promedio, sin nada que revisar en campo.
- Es decir: **cualquier cliente con componente de acueducto (`servicios IN ('AG','AG-AL')`) debe salir a lectura de campo**, tenga o no medidor instalado, salvo que sea puramente de alcantarillado.

### 6.3 MATIAS no implementada — ver §5.1 (repetido aquí por su relevancia como advertencia)

### 6.4 Bug de nombre de columna `remenber_token` — ver §2.10

### 6.5 Inconsistencia de doble token en `/api/cliente` — ver §3.1

### 6.6 `Macromedidor.lectura_actual` es `varchar`, no `integer`

Confirmado en la migración `2024_01_01_000001_create_macromedidores_table.php`: `lectura_actual` se declaró `string`, mientras que `lectura_anterior` es `integer`. El modelo no lo castea. No se pudo confirmar si es intencional; tratar el valor como string al leerlo de esta tabla legada (la tabla nueva `macro_lecturas` sí usa `integer` para ambos campos).

### 6.7 Posible bug de precedencia SQL en `TarifaPeriodo::vigente()`

```php
self::where('activo', true)->whereNull('vigente_hasta')->orWhere('vigente_hasta', '>=', now()->toDateString())->orderBy('vigente_desde','desc')->first();
```
Sin agrupar el `OR` en un closure, esto compila a `WHERE activo=1 AND vigente_hasta IS NULL OR vigente_hasta >= hoy` (no `activo=1 AND (vigente_hasta IS NULL OR vigente_hasta >= hoy)`), lo que **podría devolver una tarifa con `activo=false`** si su `vigente_hasta` es futura. No confirmado en producción, pero es un riesgo real de lectura del código tal cual está.

### 6.8 Discrepancias entre `$fillable` y columnas físicas reales (sin migración de respaldo)

Confirmadas por el agente de modelos al cruzar cada `$fillable` contra las migraciones:
- `Admin\Ordenesmtl` (tabla `ordenescu`): declara `sync` y `sync_at` en `$fillable`, **sin ninguna migración que las cree**. No se pudo confirmar si existen físicamente en la BD real (podrían haberse agregado manualmente en producción sin migración versionada).
- `Admin\Entrada` (tabla `entrada`): declara `servicio` en `$fillable`, pero esa columna solo se agregó a `ordenescu` (migración `2026_07_16_000002`), no a `entrada`.
- `Admin\Bitacora_Orden_ejecutada`: declara `tabla_origen` en `$fillable` sin columna correspondiente en su migración.
- `Admin\Mtl`: **no se encontró ninguna migración** para la tabla `mtl` en todo el repositorio — esquema no confirmable.

### 6.9 Tres flujos de facturación potencialmente redundantes — ver §4.2

### 6.10 Credenciales sensibles sin encriptar en BD

Wompi (`wompi_private_key`, `wompi_integrity_key`), WhatsApp (`whatsapp_access_token`) y SMTP (`notif_mail_password`) se guardan en texto plano en la tabla `empresas`, sin `$casts` de tipo `encrypted`. Riesgo a tener en cuenta si se expone algún endpoint de administración a través de la app móvil.

### 6.11 Referencia suelta a otro sistema ("Acusys")

`app/Http/Controllers/Admin/EntradaController.php` contiene una llamada `file_get_contents('http://localhost/Acusyscom_Backend/public/api')` — parece vestigio de una migración de datos desde otro sistema/backend anterior. No se investigó en profundidad por estar fuera del alcance de este análisis, pero se deja registrado por si es relevante.

### 6.12 Migración a medias Laravel 5.8 → 8 (repetido de §1 por completitud)

`composer.json`/`composer.lock` declaran Laravel 8, pero el `vendor/` real y el código en ejecución son Laravel 5.8.38. No hay commits ni ramas adicionales en este repo que documenten el estado de esa migración (el historial de git tiene un único commit "Estado inicial"). Cualquier cambio futuro debe seguir asumiendo APIs y comportamiento de Laravel 5.8 hasta que alguien complete y verifique la migración real (actualizar `vendor/`, correr el árbol de tests, revisar breaking changes de routing/auth/validación entre 5.8 y 8).

---

## 7. Archivos clave para profundizar (por si esta IA necesita releer el código fuente)

- Facturación: `app/Services/FacturacionService.php`, `app/Models/TarifaPeriodo.php`, `app/Models/Estrato.php`, `app/Models/Factura.php`.
- Controladores de facturación: `app/Http/Controllers/{FacturaController,FacturacionMasivaController,FacturacionEspecialController,TarifaController,CarteraController,PagoController,PagoPublicoController,MacromedidorBalanceController}.php`.
- API móvil: `routes/api.php`, `app/Http/Controllers/Api/{ClienteApiController,MacromedidorApiController,RevisionApiController}.php`, `app/Http/Middleware/AuthenticateWithApiToken.php`, `app/Http/Controllers/Seguridad/LoginController.php`.
- Integraciones: `app/Models/Empresa.php`, `app/Services/Notificaciones/*.php`, `app/Console/Commands/{EnviarAlertasCorte,EnviarRecordatoriosVencimiento}.php`, `app/Console/Kernel.php`.
- Seeders con datos reales de tarifas/subsidios: `database/seeds/{EstratoSubsidioSeeder,TarifasVoragineSeeder}.php`.
