# Plan de implementación — Facturación en Sitio

> Plan de trabajo para implementar `SPEC_FACTURACION_EN_SITIO.md` en la app de lecturistas (`SystemApp_nueva`). Se construyó junto con el usuario en sesión de planeación (2026-07-23), leyendo el código real del repo y `CONTEXTO_BACKEND.md`. Este documento es la fuente de verdad para retomar el trabajo en sesiones futuras — no reabrir las decisiones ya tomadas aquí sin que el usuario lo pida explícitamente.

## Contexto

`SPEC_FACTURACION_EN_SITIO.md` pide que el lecturista pueda, en el momento de tomar una lectura y sin conexión a internet, calcular el valor de la factura (tarifa + subsidio de estrato), mostrarlo, e imprimirlo en una impresora térmica Bluetooth — quedando la factura en estado PENDIENTE de pago hasta que el cliente pague después. Es una funcionalidad grande: toca la captura de lectura, la impresión (que hoy tiene deuda técnica real), el esquema de BD local, y requiere que el backend Laravel (otro repositorio, no éste) exponga datos que hoy no expone. Este plan la parte en 13 fases verificables por separado.

## Estado de avance

Fases 1-12 implementadas y compilando en los 6 flavors (`acuacer`, `acuasur`, `altosmangos`, `asovoragine`, `demo`, `lasirena`), 2026-07-23. **No probadas en dispositivo real ni contra backend real** — ver "Pendiente de verificar" al final de cada fase en este documento y la sección QA (Fase 13).

- [x] **Fase 1** — Fix del aviso falso "impresora no conectada".
- [x] **Fase 2** — `BluetoothPrinterClient` (conexión Bluetooth extraída, sin duplicación).
- [x] **Fase 3** — Config de ancho de papel 58/80mm.
- [x] **Fase 4** — Toggle "Facturación en sitio" + prefs de clasificaciones permitidas.
- [x] **Fase 5** — Cantidad de fotos configurable por crítica (`marca_id='FOTOS_CRITICA'`).
- [x] **Fase 6** — Esquema SQLite v4 (7 tablas nuevas + 3 columnas en `lecturas`) + sync de tarifas/estratos/rango de facturación.
- [x] **Fase 7** — `FacturacionServiceLocal` (motor de cálculo de tarifa offline).
- [x] **Fase 8** — Flujo de facturación integrado en `Fragment_form_lectura` (bifurcación Normal/Alto/Bajo/Negativo).
- [x] **Fase 9** — `FacturaPrintTemplateBuilder` (plantilla 58/80mm).
- [x] **Fase 10** — Persistencia local de factura + consumo síncrono del rango de numeración.
- [x] **Fase 11** — Corrección de lectura ya facturada (anula + reemplaza).
- [x] **Fase 12** — Sync de descarga de facturas resueltas + subida de facturas locales.
- [ ] **Fase 13** — QA end-to-end contra backend real: **ya no está bloqueada por falta de backend** — confirmado 2026-07-28 vía doc de rutas reales del repo Laravel (`demo_app`) que los 4 endpoints (`tarifaVigente`, `rangoFacturacion`, `facturasResueltas`, `facturas`) están desplegados y el contrato coincide con lo documentado aquí (mismo shape de JSON). Falta todavía la prueba real en dispositivo (checklist sin cambios).

**Ajuste 2026-07-28 — plantilla de impresión más parecida a una factura de servicios de referencia** (`fac_muesta.jpeg`, aportada por el usuario): `FacturaPrintTemplateBuilder.build()` ahora también imprime nombre de empresa (del flavor, `R.string.app_name`), mes de servicio (`orden.getPeriodo()`), estrato y ruta (solo en 80mm), y valor unitario por tramo ($/m³, derivado como `valor/m3` del propio desglose, sin campo nuevo en el contrato).

**Ajuste 2026-07-29 — dos cambios de contrato del backend, ya implementados en Android:**

1. **Fecha de vencimiento resuelta.** El gap del punto anterior ("`tarifaVigente` no expone un plazo de pago") ya no existe: el backend agregó `periodo_lectura` a `POST /api/tarifaVigente` (distinto de `periodo` — `periodo_lectura` es el ciclo de lectura ACTIVO, con `fecha_inicio_lectura`/`fecha_fin_lectura`/`fecha_expedicion`/`fecha_vencimiento`/`fecha_corte`; `periodo` sigue siendo la resolución tarifaria). Implementado: `TarifaVigenteResponse.PeriodoLecturaDTO`, tabla local nueva `periodo_lectura` (se reemplaza entera en cada sync, no acumula histórico — ver `AdminSQLiteOpenHelper.guardarTarifaVigente`), `FacturaCalculada.fechaVencimiento`/`fechaExpedicion` (poblados en `FacturacionServiceLocal.calcular()`), impresos en `FacturaPrintTemplateBuilder` como línea `Vence: dd/MM/yyyy`. Sigue habiendo un gap residual menor: factura resuelta desde la web (`facturasResueltas`) sí trae su propia `fecha_vencimiento` por factura, pero ese flujo de impresión (spec §6, "imprimir directamente en la próxima visita") todavía no está implementado — solo se descarga y queda en `factura_resuelta_servidor` sin UI de impresión.
2. **Numeración de consecutivos rediseñada — decisión 5 (abajo) queda superada, no la reabras sin avisar al usuario primero de este cambio.** El backend ahora pre-asigna `numero_factura` a cada lectura en el momento en que genera el período (`PeriodoLecturaController::generarOrdenes()`), viaja como `NumeroFactura` en cada fila de `/api/medidoresout`, y **debe usarse tal cual** al subir la factura — ya no hay que llamar a `/api/rangoFacturacion` para el caso normal (lectura con orden asociada). Implementado: `DBOrdenLecturas.NumeroFactura` (columna nueva en `lecturas`, mapeo Gson por nombre exacto), `Fragment_form_lectura.generarYImprimirFactura()` usa `ordenGuardada.getNumeroFactura()` si viene no vacío. El mecanismo de rango original (`getYConsumirSiguienteNumeroFactura`/tabla `rango_facturacion`) **no se eliminó** — queda como respaldo automático solo cuando `NumeroFactura` viene `null` (lecturas de períodos generados antes del 2026-07-29, o casos ad-hoc sin orden de lectura), tal como el backend documentó que seguiría funcionando igual.
3. **Gráfico de barras de historial de consumo — implementado en Android y confirmado por el backend, contrato coincide exactamente.** El usuario confirmó que el gráfico de barras (no una versión de texto) es un requisito, no decorativo, solo para 80mm. Implementado: `HistoricoConsumoDTO` (nuevo, `data/model/`), `DBOrdenLecturas.HistoricoConsumos` (campo nuevo, mapeo Gson directo — campo `HistoricoConsumos` en cada fila de `medidoresout`, pedido en `SOLICITUD_HISTORICO_CONSUMOS.md` y **ya desplegado por el backend** el mismo 2026-07-29 con exactamente ese shape), columna local `lecturas.HistoricoConsumosJson` (serializado, mismo patrón que `desglose_json`), y `data/print/HistoricoConsumoChartRenderer.java` — dibuja un bitmap de barras (576px = 72mm @ 203dpi, igual ancho que el logo) vía `Canvas`/`Paint` propios (no reutiliza `Fragment_form_lectura.printPhoto()` a propósito, para no heredar `BuildConfig.INVERT_LOGO`), convertido a comando ESC/POS crudo con el mismo `Utils.decodeBitmap()` que ya usa el logo. Se imprime en `imprimirFacturaEnPapel()` solo si `anchoMM == 80` y `orden.getHistoricoConsumos()` no viene vacío. Simplificación de diseño: el mes actual se dibuja como barra hueca (contorno) en vez de sólida, para diferenciarlo del historial sin depender de escala de grises (el 1-bit de ESC/POS no la soporta).

## Decisiones de negocio ya tomadas (no reabrir)

1. **Alcance de esta ronda de trabajo**: diseñar el contrato de datos/endpoints que el backend Laravel deberá exponer, e implementar todo el lado Android asumiendo ese contrato. El sync de las piezas nuevas no traerá datos reales hasta que el backend se construya (otro repo) — pero el código Android debe quedar listo, con el contrato documentado para copiar a un ticket de backend.
2. **Reconciliación al sincronizar**: si el backend recalcula un valor distinto al ya impreso en campo, **prevalece el valor impreso**. No se genera nota de ajuste automática.
3. **Corrección de lectura tras ya haber facturado/impreso en sitio**: se **anula** la factura original (queda registrada como `ANULADA`, su número no se reutiliza) y se genera una **nueva** factura con el valor corregido, consumiendo el siguiente número disponible del rango local (ver decisión 5).
4. **Config de "cantidad de fotos según la crítica"** (pedido agregado a último momento, no estaba en la spec original): se agrega como un grupo nuevo dentro de la tabla de catálogos `listas` que YA se sincroniza hoy vía `POST /api/marcas` (`marca_id='FOTOS_CRITICA'`, `codigo`=código de la crítica 50/51/52/53, `descripcion`=cantidad de fotos). Cero endpoints nuevos para esto.
5. **Consecutivo de factura — diseño de rangos asignados al sincronizar la ruta** (decisión final del usuario, reemplaza un diseño anterior de "número provisional" que se descartó): un incremental simple funciona en el backend porque hay un solo controlador de secuencia con locks; offline eso no existe — si dos lecturistas están sin señal el mismo día, un esquema de "número provisional + reconciliar al subir" puede generar colisiones o inconsistencias. En su lugar: cuando el lecturista descarga su ruta del día (momento en que sí hay señal), el servidor le asigna de una vez un **rango exclusivo de correlativos** para ese día, en el mismo formato que ya usa el backend real (`Factura::generarNumero()`, `CONTEXTO_BACKEND.md` §2.2): `{periodo}{secuencia de 5 dígitos}` (ej. `20260700123`). El dispositivo consume números de ese rango localmente, en orden, sin volver a tocar el servidor por cada factura — **el número generado en campo ya es el número final**, no hay que reconciliar nada al subir. Ver detalle completo en la sección "Contrato de backend, 1.2" y en la Fase 10.
6. **Selección de ancho de papel térmico (58mm/80mm)**: configuración manual por dispositivo (junto a la config de impresora ya existente). No hay auto-detección — los SDKs vendored (`SDKLib.jar`, `posprinterconnectandsendsdk.jar`) solo envían bytes ESC/POS crudos, sin capacidad de consulta de estado/ancho.

## Hallazgo importante (no es una decisión, es un dato a tener presente)

**`Validador.java` (ya en producción) NO usa el mismo umbral que el backend.** `Validador.validaciones()` (`app/src/main/java/com/example/systemapp/data/Validador.java:29-40`) clasifica alto/bajo con **35%–165%** del promedio. El backend real usa **50%–150%** (`Cliente::lecturaEsNormal()`, `CONTEXTO_BACKEND.md` §2.1). Son distintos y **no se unifican**: `Validador.java` sigue gobernando fotos/confirmaciones/crítica exactamente como hoy (cambiar sus umbrales afectaría a todos los usuarios en producción, fuera de alcance de este trabajo). Para decidir "¿se puede facturar en sitio?" se usa una clasificación **nueva y separada**, con el ±50% real del backend (`FacturacionServiceLocal.esConsumoNormal()`, Fase 7). Las dos clasificaciones conviven con propósitos distintos — no confundirlas al implementar o revisar código.

---

## Contrato de backend (para copiar a un ticket del repo Laravel)

Todo bajo el mismo mecanismo de auth actual (`Authorization: Bearer <api_token>`), mismo estilo de respuesta JSON plano sin envelope (igual que `/api/medidoresout` y `/api/marcas` hoy).

### 1.1 Extender el payload existente de `POST /api/medidoresout` (no es endpoint nuevo)

Añadir estos campos a cada objeto del array que ya devuelve hoy (nombres calcados a los getters/setters de `DBOrdenLecturas` para mapeo Gson directo, sin `@SerializedName`):

```json
{
  "id": "12345", "Ciclo": "...", "Periodo": "202607", "Ref_Medidor": "...",
  "LA": "120", "Promedio": 18,
  "EstratoId": 3,
  "ServiciosCliente": "AG-AL",
  "TieneAseo": false,
  "SaldoAnterior": 15200.00
}
```

- `EstratoId` (int, FK al catálogo de estratos de 1.3) — hoy `AdminSQLiteOpenHelper.insertOrden()` (`app/src/main/java/com/example/systemapp/data/AdminSQLiteOpenHelper.java:146-149`) escribe `cservic="0040"`/`nservic="ACUEDUCTO"` **hardcodeado, ignorando lo que venga del servidor** — se corrige en la Fase 6 para usar el valor real si viene, con ese hardcode solo como fallback.
- `SaldoAnterior` (decimal, default 0) — deuda real del suscriptor (facturas `PENDIENTE`/`VENCIDA` de períodos anteriores, neta de pagos), calculada server-side con una subquery correlacionada contra `facturas`/`pagos` (mismo cálculo que `Factura::saldoPendiente()`/`FacturacionService::calcularSaldoAnterior()`). Se descarga junto con la ruta para que el lecturista pueda informarle la deuda al cliente **en el momento de la visita**, no recién al subir la factura. `FacturacionServiceLocal.calcular()` lo suma a `totalAPagar` y se imprime como línea separada ("Saldo anterior") cuando es > 0. Al subir la factura (contrato 1.5), viaja tal cual en `saldo_anterior` — el backend **no lo vuelve a calcular ni sumar** al persistir (evita duplicarlo, ya viene incluido en `total_a_pagar`).
- `ServiciosCliente` (string `AG`|`AL`|`AG-AL`, igual formato que `Cliente.servicios` en el backend).
- `TieneAseo` (bool).

### 1.2 Nuevo endpoint: `POST /api/rangoFacturacion`

Implementa la decisión 5. Se llama una vez al sincronizar/descargar la ruta del día (mismo momento que `medidoresout`, pero endpoint separado para no romper el contrato de array plano que ya consume la app).

**Request**: `{ "cantidad_ordenes": 214 }` (tamaño de la ruta que se está descargando, para dimensionar el bloque — el backend decide el margen, ej. `cantidad_ordenes * 1.2` redondeado).

**Response 200**:
```json
{
  "periodo": "202607",
  "secuencia_desde": 100,
  "secuencia_hasta": 356,
  "asignado_en": "2026-07-23T07:00:00-05:00"
}
```
- Cada llamada asigna un **bloque nuevo y exclusivo** (el backend reserva ese rango en su secuencia real de `numero_factura`, con el mismo lock que usa hoy `Factura::generarNumero()` — pero reservando N números de una vez en vez de uno). Si el mismo dispositivo sincroniza dos veces el mismo día, es aceptable que reciba un segundo bloque distinto (deja huecos en la numeración, no duplica) — **esto no es facturación electrónica DIAN** (`CONTEXTO_BACKEND.md` confirma que esa integración no existe), así que huecos en la numeración no son un problema legal, solo estético.
- El dispositivo arma `numero_factura` como `periodo + secuencia_actual` con padding a 5 dígitos (ej. periodo `202607` + secuencia `100` → `"20260700100"`).
- Si el rango se agota durante el día (más facturas generadas que el bloque), la app deja de ofrecer facturación en sitio hasta el próximo sync con señal (fail-safe, no reutiliza ni inventa números) — ver Fase 10.

### 1.3 Nuevo endpoint: `POST /api/tarifaVigente`

Request: sin body, solo el token.

Response `200`:
```json
{
  "periodo": { "id": 7, "nombre": "Tarifa Junio 2026", "vigente_desde": "2026-06-01", "vigente_hasta": null },
  "cargos_fijos": [
    { "servicio": "ACUEDUCTO", "estrato_id": 3, "cargo_fijo": 4500.00 },
    { "servicio": "ALCANTARILLADO", "estrato_id": 3, "cargo_fijo": 2200.00 },
    { "servicio": "ASEO", "estrato_id": 3, "cargo_fijo": 6000.00 }
  ],
  "rangos": [
    { "servicio": "ACUEDUCTO", "estrato_id": 3, "tipo": "BASICO", "rango_desde": 1, "rango_hasta": 16, "precio_m3": 950.30 },
    { "servicio": "ACUEDUCTO", "estrato_id": 3, "tipo": "COMPLEMENTARIO", "rango_desde": 17, "rango_hasta": 32, "precio_m3": 1350.10 },
    { "servicio": "ACUEDUCTO", "estrato_id": 3, "tipo": "SUNTUARIO", "rango_desde": 33, "rango_hasta": null, "precio_m3": 1800.00 }
  ],
  "estratos": [
    { "id": 3, "numero": 3, "nombre": "Estrato 3", "porcentaje_subsidio": 15.0,
      "subsidio_fijo_acueducto": 0, "subsidio_fijo_alcantarillado": 0,
      "consumo_minimo_subsidio": 4.0, "activo": true }
  ],
  "config_facturacion_sitio": {
    "habilitar_normal": true, "habilitar_alto": true, "habilitar_bajo": true, "habilitar_negativo": false
  }
}
```
- `servicio` incluye `ASEO`: se trata como cargo fijo simple (sin filas en `rangos`, salvo que algún acueducto sí mida aseo por m³ — el mismo array ya lo soportaría sin cambio de esquema).
- `config_facturacion_sitio` implementa la spec §5.1 (parametrización por clasificación) — vive en `Empresa` en el backend.
- Si no hay fila de `rangos`/`cargos_fijos` para el estrato de un cliente, ese servicio no se factura en sitio (fail-safe, no inventar).

### 1.4 Nuevo endpoint: `POST /api/facturasResueltas`

Implementa spec §6 (descarga automática de facturas resueltas desde `FacturacionEspecialController`). Mismo patrón que `/api/ordenesRevision`.

Response `200`, array de:
```json
{
  "factura_id": 9931, "numero_factura": "20260700123", "lectura_id": "12345", "suscriptor": "SUSC-001",
  "periodo": "202607", "fecha_expedicion": "2026-07-20", "fecha_vencimiento": "2026-08-05",
  "lectura_anterior": 100, "lectura_actual": 120, "consumo_m3": 20, "estrato_snapshot": 3,
  "acueducto": { "cargo_fijo": 4500.00, "basico_m3": 16, "basico_valor": 15204.80, "complementario_m3": 4, "complementario_valor": 5400.40, "suntuario_m3": 0, "suntuario_valor": 0, "subsidio": -1200.50, "total": 23904.70 },
  "alcantarillado": { "...": "mismo desglose" },
  "aseo": { "cargo_fijo": 6000.00, "total": 6000.00 },
  "saldo_anterior": 0, "total_a_pagar": 35704.70, "estado": "PENDIENTE"
}
```
`lectura_id` enlaza contra `lecturas.id` local, para saber a qué orden/cliente corresponde y ofrecer "imprimir directamente" en la próxima visita.

### 1.5 Nuevo endpoint: `POST /api/facturas` (subir factura generada en sitio)

Con el diseño de rangos (decisión 5), `numero_factura` **ya es el número final** al momento de generarse en campo — este endpoint solo persiste, no asigna número.

Request:
```json
{
  "id_local": "a1b2c3d4-...-uuid",
  "numero_factura": "20260700123",
  "lectura_id": "12345", "suscriptor": "SUSC-001", "periodo": "202607",
  "lectura_anterior": 100, "lectura_actual": 120, "consumo_m3": 20,
  "estrato_id_usado": 3, "tarifa_periodo_id_usado": 7,
  "acueducto": { "...": "mismo desglose que 1.3/1.4" },
  "alcantarillado": { "...": "..." },
  "aseo": { "...": "..." },
  "saldo_anterior": 15200.00,
  "total_a_pagar": 35704.70,
  "fecha_impresion": "2026-07-23T14:32:10-05:00",
  "clasificacion": "NORMAL",
  "anula_numero_factura": null
}
```
- `anula_numero_factura` (nullable): si esta factura reemplaza una anulada por corrección de lectura (decisión 3), va el `numero_factura` de la anulada; el backend debe marcarla `ANULADA` al recibir esto.
- `saldo_anterior`: ya descargado con la ruta (`SaldoAnterior` de `medidoresout`, contrato 1.1) e incluido en `total_a_pagar` por la app antes de imprimir. El backend **no lo recalcula ni lo vuelve a sumar** — solo lo persiste, igual que el resto de los totales (decisión 2 extendida: prevalece el valor impreso, incluida la deuda previa).
- El backend **no debe recalcular ni sobrescribir** los totales recibidos (decisión 2: prevalece el valor impreso) — solo persiste y, si quiere, loguea la diferencia contra su propio cálculo para revisión administrativa manual.

Response `200`: `{ "success": true, "factura_id": 9932, "saldo_anterior": 15200.00, "total_a_pagar": 35704.70 }` (`factura_id` = id interno de la fila en el backend, para `factura_id_servidor`; `numero_factura` no cambia, ya vino fijo desde el dispositivo; `saldo_anterior`/`total_a_pagar` se devuelven tal como quedaron guardados, solo de confirmación).

---

## Esquema SQLite nuevo

Todo en `app/src/main/java/com/example/systemapp/data/model/DBdefinicionOrdenes.java`, subiendo `DATABASE_VERSION` de 3 a 4, migración incremental en `AdminSQLiteOpenHelper.onUpgrade()` (ya preparado para esto, solo llenar el `if (oldVersion < 4)`).

**Addendum 2026-07-29** (ver "Ajuste 2026-07-29" arriba) — agregado dentro del mismo `DATABASE_VERSION 4` (todavía no había build en producción con esta versión, no ameritó bump a 5): columna `lecturas.NumeroFactura VARCHAR(20)` (consecutivo pre-asignado) y tabla nueva `periodo_lectura` (id, codigo, nombre, fecha_inicio_lectura, fecha_fin_lectura, fecha_expedicion, fecha_vencimiento, fecha_corte; PK id; se reemplaza entera en cada sync de tarifas, no acumula histórico).

```sql
-- ALTER a lecturas (contrato 1.1)
ALTER TABLE lecturas ADD COLUMN EstratoId INTEGER;
ALTER TABLE lecturas ADD COLUMN ServiciosCliente VARCHAR(10) DEFAULT 'AG';
ALTER TABLE lecturas ADD COLUMN TieneAseo INTEGER DEFAULT 0;

-- tarifa vigente cacheada (contrato 1.3)
CREATE TABLE tarifa_periodo (
  id INTEGER NOT NULL, nombre VARCHAR(150), vigente_desde TEXT, vigente_hasta TEXT,
  PRIMARY KEY(id)
);
CREATE TABLE tarifa_cargo_fijo (
  tarifa_periodo_id INTEGER NOT NULL, servicio VARCHAR(20) NOT NULL, estrato_id INTEGER NOT NULL,
  cargo_fijo REAL NOT NULL,
  PRIMARY KEY(tarifa_periodo_id, servicio, estrato_id)
);
CREATE TABLE tarifa_rango (
  tarifa_periodo_id INTEGER NOT NULL, servicio VARCHAR(20) NOT NULL, estrato_id INTEGER NOT NULL,
  tipo VARCHAR(20) NOT NULL, rango_desde INTEGER NOT NULL, rango_hasta INTEGER, precio_m3 REAL NOT NULL,
  PRIMARY KEY(tarifa_periodo_id, servicio, estrato_id, tipo)
);
CREATE TABLE estrato_cache (
  id INTEGER NOT NULL, numero INTEGER, nombre VARCHAR(50), porcentaje_subsidio REAL,
  subsidio_fijo_acueducto REAL, subsidio_fijo_alcantarillado REAL, consumo_minimo_subsidio REAL, activo INTEGER,
  PRIMARY KEY(id)
);

-- rango de numeración de facturas asignado al sincronizar la ruta (contrato 1.2 / decisión 5)
CREATE TABLE rango_facturacion (
  periodo VARCHAR(10) NOT NULL,
  secuencia_desde INTEGER NOT NULL,
  secuencia_hasta INTEGER NOT NULL,
  siguiente INTEGER NOT NULL,      -- próximo número a consumir; se incrementa de forma síncrona antes de imprimir
  PRIMARY KEY(periodo)
);

-- facturas generadas en sitio, pendientes o ya sincronizadas
CREATE TABLE factura_local (
  id_local VARCHAR(60) NOT NULL,             -- UUID, PK interna del dispositivo (no es el número de factura)
  numero_factura VARCHAR(20) NOT NULL,       -- YA final, formato periodo+5 dígitos, consumido de rango_facturacion
  lectura_id VARCHAR(30) NOT NULL,
  suscriptor VARCHAR(100) NOT NULL,
  periodo VARCHAR(10),
  lectura_anterior INTEGER, lectura_actual INTEGER, consumo_m3 INTEGER,
  estrato_id_usado INTEGER, tarifa_periodo_id_usado INTEGER,
  desglose_json TEXT NOT NULL,               -- JSON serializado del desglose completo (acueducto/alcantarillado/aseo)
  total_a_pagar REAL,
  clasificacion VARCHAR(20),                 -- NORMAL | ALTO | BAJO
  fecha_impresion TEXT,
  estado VARCHAR(20) NOT NULL,               -- PENDIENTE_SYNC | SINCRONIZADA | ANULADA
  anula_a_id_local VARCHAR(60),              -- self-FK: si esta factura reemplaza una anulada
  factura_id_servidor INTEGER,               -- llega al sincronizar (contrato 1.5)
  sincronizado INTEGER DEFAULT 0,
  PRIMARY KEY(id_local)
);

-- facturas ya calculadas/resueltas desde el panel web (contrato 1.4)
CREATE TABLE factura_resuelta_servidor (
  factura_id INTEGER NOT NULL, numero_factura VARCHAR(30), lectura_id VARCHAR(30) NOT NULL, suscriptor VARCHAR(100),
  desglose_json TEXT NOT NULL, total_a_pagar REAL, estado VARCHAR(20),
  impresa INTEGER DEFAULT 0,     -- se marca 1 localmente cuando el lecturista ya la imprimió en campo
  PRIMARY KEY(factura_id)
);
```

Nuevos `case` en `AdminSQLiteOpenHelper.getData()` (switch en líneas 235-302 hoy) para cada tabla nueva, siguiendo exactamente el patrón `case "listas":`. Nuevos métodos `insertElementoTarifaCargoFijo`, `insertElementoTarifaRango`, `insertElementoEstrato`, `insertFacturaLocal(FacturaLocal, boolean update)`, `insertFacturaResuelta`, `getYConsumirSiguienteNumeroFactura(String periodo)` (lee `siguiente`, valida `<= secuencia_hasta`, incrementa y persiste antes de devolver — debe ser síncrono para evitar doble-consumo), siguiendo el patrón de `insertElementoLista`/`insertOrden`.

---

## Fases de implementación

### Fase 1 — Fix del aviso falso "impresora no conectada" ✅ COMPLETADA

Bug: `onResume()` en `Fragment_form_lectura.java` verificaba conectividad usando el campo de instancia `bluetoothSocket` (que la impresión real nunca toca — usa un socket local dentro de `printWithChannelSearch`) y `openBT()` leía la clave de preferencias `PREF_PRINTER_MAC`, que **nunca se escribe en el proyecto** (la clave real es `PREF_PRINTER_ADDRESS`). Por eso el Toast de error salía siempre, sin importar si la impresión (que sí funciona) tuvo éxito.

**Hecho**: se eliminó el bloque de verificación en `onResume()` (era código huérfano desde el commit `c6a27de`, que dejó de usar `bluetoothSocket`/`openBT` para imprimir sin limpiar esta verificación). `onResume()` ahora solo hace `super.onResume(); reIniFragment();`. `openBT()`/`openBTWithRetry()` se dejaron intactos (quedan sin uso, riesgo cero, se pueden limpiar después si se quiere).

**Verificación pendiente en dispositivo real**: abrir el formulario de lectura con impresora ya configurada, hacer una lectura + imprimir constancia, confirmar que el Toast falso ya no aparece y que la impresión sigue funcionando igual que antes.

---

### Fase 2 — Extraer la conexión Bluetooth duplicada a una clase reutilizable

Hoy existen dos copias casi idénticas de la lógica de conexión RFCOMM (búsqueda de canal 1-30, apertura de socket): `printWithChannelSearch` (`Fragment_form_lectura.java:1598-1764`) y `testPrintWithChannelSearch` (`ConfigFragment.java:547-775`). Agregar una tercera ruta (factura) sin refactorizar sería la tercera copia.

**Archivo nuevo**: `app/src/main/java/com/example/systemapp/data/print/BluetoothPrinterClient.java` — extrae solo la conexión (cancelar discovery, resolver `BluetoothDevice`, probar canal guardado, fuerza bruta 1-30, obtener `OutputStream`) como método estático bloqueante `connect(...)`, más `sendInit`/`sendLogo`/`sendText`/`closeQuietly`.

**Modificar** `Fragment_form_lectura.java` y `ConfigFragment.java`: reemplazar sus bloques de conexión por la llamada a la clase nueva, manteniendo Toasts de progreso específicos de cada pantalla sin mover a la clase compartida.

**Nota menor**: `PrinterUtils.decodeBitmap()` está huérfano (el código real usa `com.example.systemapp.data.Utils.decodeBitmap`, una clase paralela) — no replicar en la clase nueva, no hace falta borrarlo en esta fase.

**Verificación**: constancia de lectura se imprime igual que antes; botón "Probar impresión" en Configuración imprime el mismo ticket de siempre, guardando `PREF_PRINTER_CHANNEL` igual. Cero diferencia de comportamiento esperada.

---

### Fase 3 — Configuración de ancho de papel térmico (58mm/80mm)

`SessionPrefs.java`: agregar `PREF_PRINTER_WIDTH_MM` + `getPrefPrinterWidthMM()` (default 58) / `setPrefPrinterWidthMM(int)`, mismo patrón que `setPrefPrinterAddress`.

`ConfigFragment.java` + `fragment_config.xml`: `RadioGroup`/`Switch` 58mm/80mm junto a la config de impresora existente, persistencia inmediata al cambiar.

`Fragment_form_lectura.java`, `printPhoto()`: reemplazar `int MAX_WIDTH = 384;` (línea ~1847, hardcodeado) por lectura de `SessionPrefs` (80mm → 576, según el propio comentario ya presente en esa línea).

**Verificación**: cambiar el switch, confirmar por log que `MAX_WIDTH` cambia a 576/384 según corresponda; probar impresión real en 58mm (80mm si hay impresora disponible).

---

### Fase 4 — Toggle maestro "Facturación en sitio" + prefs de clasificaciones permitidas

`SessionPrefs.java`: `PREF_FACTURACION_SITIO_ENABLED` (default `false`), `PREF_FACTURA_PERMITE_NORMAL`/`_ALTO`/`_BAJO` (default `true` — sobrescritos por sync en Fase 6 con el valor real de `config_facturacion_sitio`). NEGATIVO no es config, es regla fija de negocio (Caso B: nunca se factura en sitio) — hardcodear en el código de decisión de la Fase 8, no en preferencias.

`ConfigFragment.java` + `fragment_config.xml`: `Switch` "Facturación en sitio", persistencia inmediata.

**Verificación**: activar/desactivar y confirmar el valor leído desde `Fragment_form_lectura`. Sin efecto visible en el flujo de lectura todavía (eso es la Fase 8) — esta fase es solo infraestructura.

---

### Fase 5 — Config "cantidad de fotos según la crítica" (reutiliza sync existente)

`Fragment_form_lectura.java`: junto a la lectura de críticas ya existente (`condicion_criticas = "marca_id = 'CRITICA'"`, líneas ~438-442), agregar consulta hermana `marca_id = 'FOTOS_CRITICA'`. En los 4 bloques del `switch(validacion)` donde hoy se hace `cantidadFotos = 1;` (líneas ~526, 545, 566, 584), reemplazar el literal por un helper `getCantidadFotosParaCritica(codigoCritica, fotosCriticaConfig)` que busca por `codigo` (50/51/52/53) y parsea `descripcion` a int, con fallback a `1` si no hay config sembrada (para no romper el comportamiento actual si el backend/admin aún no cargó esos datos).

**Verificación**: sembrar manualmente 4 filas en `listas` (`marca_id='FOTOS_CRITICA'`, códigos 50/51/52/53, descripciones con cantidades distintas), forzar cada validación y confirmar que pide la cantidad configurada; sin esas filas, confirmar fallback a 1 (comportamiento idéntico al actual).

---

### Fase 6 — Sync de tarifas/estratos/rango de facturación + extensión de `medidoresout` (requiere backend nuevo)

`SystemAppAPI.java`: agregar `tarifaVigente()` (POST `tarifaVigente`) y `rangoFacturacion(RangoFacturacionRequest)` (POST `rangoFacturacion`), mismo patrón que `listas()`.

Nuevos modelos DTO Gson planos para las respuestas de 1.2 y 1.3.

`DBOrdenLecturas.java`: agregar `EstratoId`, `ServiciosCliente`, `TieneAseo` + getters/setters.

`AdminSQLiteOpenHelper.insertOrden()`: usar los 3 campos nuevos; **corregir el hardcode de `cservic`/`nservic`** (líneas ~146-149) para usar el valor real del servidor si viene, con el valor actual solo como fallback.

`fragment_sync.java`: nueva card "Descargar tarifas" (pobla `tarifa_cargo_fijo`/`tarifa_rango`/`estrato_cache`) y lógica para pedir `rangoFacturacion` junto con la descarga de ruta (`cardSyncOrdenes`), guardando el resultado en `rango_facturacion` — **importante**: si ya existe un rango vigente para el mismo período con `siguiente <= secuencia_hasta` (aún no agotado), decidir si se reemplaza o se conserva (recomendado: conservar el existente si no está agotado, para no desperdiciar números ya "reservados" mentalmente por el lecturista; solo pedir uno nuevo si se agotó o cambió el período).

**Verificación**: con un mock HTTP que responda el contrato 1.2/1.3, confirmar que las tablas nuevas quedan pobladas correctamente y que `cargue()` no rompe si el backend real aún no manda los campos nuevos (deben quedar null/default sin crashear el parseo Gson).

---

### Fase 7 — Motor de cálculo de tarifa local (100% client-side, testeable sin backend)

**Archivo nuevo**: `app/src/main/java/com/example/systemapp/data/factura/FacturacionServiceLocal.java` — réplica de `FacturacionService::calcular()` (`CONTEXTO_BACKEND.md` §4.1):
- `esConsumoNormal(consumo, promedio)`: ±50%, independiente de `Validador.java` (ver "Hallazgo importante" arriba).
- `calcularServicio(...)`: tramos BASICO/COMPLEMENTARIO/SUNTUARIO por bloques acumulados reales (no "todo al precio del tramo en que cae"); subsidio/sobretasa solo sobre `basico_valor`, condicionado a `consumo > consumo_minimo_subsidio`, prioridad `subsidio_fijo_*` sobre porcentaje.
- `calcular(...)`: orquesta acueducto + alcantarillado + aseo según `ServiciosCliente`/`TieneAseo`, y suma `orden.getSaldoAnterior()` (descargado con la ruta vía contrato 1.1, `SaldoAnterior` en `medidoresout`) al total — **decisión revisada**: ya no queda en 0; el servidor lo calcula al sincronizar y la app lo conoce offline desde ese momento, así que sí se incluye en `FacturaCalculada.totalAPagar` y se imprime como línea separada.

**Verificación**: sembrar en SQLite el ejemplo de `CONTEXTO_BACKEND.md` §2.4 (BASICO 1-16, COMPLEMENTARIO 17-32, SUNTUARIO 33+) y comparar el resultado a mano, o contra `FacturaController::preview()` real si hay acceso de staging al backend.

---

### Fase 8 — Integración del flujo en `Fragment_form_lectura` (bifurcación Caso A/B, diálogos, resumen)

**Punto de inserción exacto**: dentro de `finalizarRegistroLectura()`, inmediatamente después de `sendDataToServer(orden);` (línea ~963) y antes de `position++;` (línea ~965) — el registro ya está persistido con éxito ahí, y todavía no arrancó el avance automático a la siguiente orden.

Nuevo método `ofrecerFacturacionEnSitio(orden)`:
- Si el toggle (Fase 4) está apagado → no hace nada, cero cambio de comportamiento.
- Si `consumo < 0` → nunca ofrece factura (Caso B, regla fija, sin excepción).
- Si no, clasifica con `FacturacionServiceLocal.esConsumoNormal()` (Fase 7) y consulta `PREF_FACTURA_PERMITE_NORMAL`/`_ALTO_BAJO` (Fase 4/6). La doble confirmación de "alto/bajo correcto" ya la garantiza el switch existente de validación (líneas ~524-627) antes de llegar aquí — no se vuelve a pedir.
- Si procede, reutiliza `displayPrompt(...)` (patrón ya existente, líneas ~1226-1307) con una nueva rama de acción `"ofrecerResumenFactura"` → calcula con `FacturacionServiceLocal` → muestra un segundo diálogo con el resumen (lectura, consumo, desglose, total) → botón "Imprimir" dispara Fase 9/10.
- Si el lecturista dice NO en cualquiera de los dos diálogos: la lectura ya quedó guardada igual que hoy, sin generar factura.

No rompe `isProcessing`/`btnSave.setEnabled` (se resetean después del bloque insertado, igual que hoy) ni el gesto de swipe (reutiliza los mismos botones).

**Verificación**: con toggle activo y tarifa sembrada (Fase 6/7 con mock), completar lecturas normal / alto-bajo confirmado / negativa y confirmar que el diálogo aparece solo cuando corresponde, y que cancelar en cualquier paso no interrumpe el avance normal a la siguiente orden.

---

### Fase 9 — Plantilla de impresión de factura (58/80mm)

**Archivo nuevo**: `app/src/main/java/com/example/systemapp/data/print/FacturaPrintTemplateBuilder.java` — replica la estructura de `resources/views/pdf/factura.blade.php` del backend en texto plano ESC/POS, en dos anchos (58mm ≈ 32 columnas, 80mm ≈ 48). Campos siempre presentes: encabezado, datos de lectura, desglose por servicio, total, vencimiento. Campos solo si sobra espacio en 80mm: QR/código de barras (investigar si el SDK vendored lo soporta; si no, omitir y documentar como no soportado), saldo anterior detallado.

`Fragment_form_lectura.java`: nuevo método `imprimirFactura(...)` usando `BluetoothPrinterClient` (Fase 2) + `FacturaPrintTemplateBuilder` (esta fase) + `MAX_WIDTH` dinámico (Fase 3).

**Verificación**: imprimir factura de prueba en impresora real 58mm (y 80mm si hay disponible), confirmar que nada se corta y el total es legible.

---

### Fase 10 — Persistencia local de la factura + consumo del rango de numeración

Al confirmar "Imprimir" en el resumen (Fase 8/9), antes de imprimir:

```java
Integer siguiente = adminSQLiteOpenHelper.getYConsumirSiguienteNumeroFactura(periodoActual);
if (siguiente == null) {
    // rango agotado: mostrar mensaje "sin números disponibles, sincronice para obtener más" y NO imprimir factura
    // (fail-safe — no inventar número, no reutilizar)
    return;
}
String numeroFactura = periodoActual + String.format("%05d", siguiente);
String idLocal = UUID.randomUUID().toString();
FacturaLocal facturaLocal = new FacturaLocal(idLocal, numeroFactura, orden.getId(), orden.getSuscriptor(),
    new Gson().toJson(factura), factura.getTotalAPagar(), clasificacion,
    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), "PENDIENTE_SYNC");
adminSQLiteOpenHelper.insertFacturaLocal(facturaLocal, false);
```

`getYConsumirSiguienteNumeroFactura` debe ser síncrono (leer `siguiente`, validar contra `secuencia_hasta`, incrementar y persistir en la misma operación) para que dos facturas seguidas en el mismo dispositivo nunca consuman el mismo número.

**Verificación**: generar varias facturas seguidas y confirmar por SQL que `numero_factura` es correlativo sin repetirse; agotar el rango a propósito (sembrando `secuencia_hasta` bajo) y confirmar que la app bloquea con el mensaje correcto en vez de duplicar o crashear.

---

### Fase 11 — Corrección de lectura ya facturada (anula + reemplaza)

En el bloque `edit` de `finalizarRegistroLectura()`: si existe una `factura_local` con `estado != 'ANULADA'` para esa `lectura_id`, marcarla `ANULADA` (su número no se reutiliza). Si el toggle sigue activo y la nueva clasificación (recalculada tras la corrección) permite facturar, ofrecer el mismo diálogo de la Fase 8, generando una **nueva** fila en `factura_local` (nuevo `numero_factura` consumido del rango, `anula_a_id_local` apuntando a la anulada). Al subir (Fase 12), usar `anula_numero_factura` del contrato 1.5 para que el backend marque la original `ANULADA` también ahí.

**Verificación**: generar una factura, editar la lectura desde `REjecutadasFragment`, corregir, guardar, confirmar por SQL que la original quedó `ANULADA` y la nueva (si se reimprimió) tiene `anula_a_id_local` correcto y un número de factura distinto y mayor.

---

### Fase 12 — Sync de facturas resueltas (descarga) + subida de facturas locales (requiere backend nuevo)

`SystemAppAPI.java`: `facturasResueltas()` y `subirFactura(FacturaLocalDTO)`.

`fragment_sync.java`: nueva card "Descargar facturas resueltas" (pobla `factura_resuelta_servidor`, mismo patrón que `cardSyncListas`). Nuevo método `sendFacturasLocalesOnetoOne(...)`, calcado de `sendDataToServerOnetoOne` (mismo patrón recursivo secuencial, no batch), filtrando `factura_local` por `estado = 'PENDIENTE_SYNC'`. Al recibir `factura_id` de la respuesta, guardar en `factura_id_servidor` y marcar `sincronizado=1` — **no se sobrescribe `numero_factura` ni `total_a_pagar`** (decisión 2: prevalece lo impreso; con el diseño de rangos ya no hay número que reconciliar).

**Verificación**: con mock del contrato 1.4/1.5, descargar facturas resueltas de prueba y confirmar que llegan a `factura_resuelta_servidor` cruzando por `lectura_id`; generar 2-3 facturas locales, sincronizar, confirmar `sincronizado=1` en todas sin cambios en `numero_factura`/`total_a_pagar`.

---

### Fase 13 — QA end-to-end contra backend real

Checklist sin mocks, una vez el equipo de Laravel implemente el contrato completo:

1. Toggle apagado → cero cambios de comportamiento (regresión completa del flujo actual).
2. Normal, backend permite NORMAL → diálogo, resumen correcto, imprime, sube, `factura_id_servidor` queda poblado.
3. Alto/bajo confirmado, backend permite ALTO/BAJO → igual que 2.
4. Negativo → nunca ofrece factura; llega a revisión en el panel web (`OrdenRevision`/`FacturacionEspecialController`).
5. Backend cambia `config_facturacion_sitio.habilitar_alto=false`, la app resincroniza tarifas → el próximo alto confirmado ya no ofrece imprimir en sitio.
6. Corrección de lectura ya facturada → original `ANULADA` + nueva visible correctamente en el panel web tras sync.
7. Factura resuelta desde la web aparece en la siguiente sync y es imprimible sin recalcular nada en el dispositivo.
8. Rango de numeración: dos dispositivos sincronizando la misma mañana reciben rangos distintos sin colisión; agotar un rango a propósito y confirmar el fail-safe de la Fase 10.
9. Impresión 58mm y 80mm ambas legibles con datos reales de tarifa.

---

## Archivos críticos (referencia rápida)

- `app/src/main/java/com/example/systemapp/ui/data/Fragment_form_lectura.java`
- `app/src/main/java/com/example/systemapp/data/AdminSQLiteOpenHelper.java`
- `app/src/main/java/com/example/systemapp/data/model/DBdefinicionOrdenes.java`
- `app/src/main/java/com/example/systemapp/ui/config/ConfigFragment.java`
- `app/src/main/java/com/example/systemapp/ui/sync/sync/fragment_sync.java`
- `app/src/main/java/com/example/systemapp/SystemAppAPI.java`
- `app/src/main/java/com/example/systemapp/data/SessionPrefs.java`
- `CONTEXTO_BACKEND.md` (lógica de tarifas a replicar, secciones 2.1, 2.4, 4.1, 4.2)
- `SPEC_FACTURACION_EN_SITIO.md` (requisitos funcionales originales)
