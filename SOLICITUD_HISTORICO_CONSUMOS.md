# Solicitud — histórico de consumos (últimos 6 meses) en `POST /api/medidoresout`

**Estado 2026-07-29: YA IMPLEMENTADO EN BACKEND Y CONFIRMADO** (ver doc de rutas API actualizada
del mismo día, sección "`HistoricoConsumos` en `/api/medidoresout`") — el contrato real que
desplegó el backend coincide exactamente con lo pedido en este documento (mismo nombre de campo,
mismo shape `{periodo, consumo_m3}`, mismo orden ascendente, mismo comportamiento `null`/array
corto). El lado Android ya estaba implementado esperando este campo — `DBOrdenLecturas.HistoricoConsumos`,
`data/print/HistoricoConsumoChartRenderer.java` (dibuja el gráfico de barras real como bitmap, no
una versión de texto) y la impresión ya conectada en `Fragment_form_lectura.imprimirFacturaEnPapel()`
(solo 80mm) — no hizo falta ningún cambio de código al llegar la confirmación del backend. Este
documento queda como referencia histórica del pedido original.

## Para qué

La app de lecturistas (`SystemApp_nueva`) va a imprimir un gráfico de barras con el historial de
consumo de los últimos 6 meses en la factura generada en sitio, replicando la estructura visual
de una factura de servicios públicos de referencia. Ese gráfico **solo se imprime en impresoras
de 80mm** (72mm de área imprimible real) — en 58mm no hay espacio y se omite, sin que eso bloquee
nada.

Hoy `POST /api/medidoresout` ya manda `Promedio` (un único entero, el promedio ya calculado), pero
no el detalle mes a mes. Backend ya tiene la lógica para sacar ese detalle
(`ClienteHistoricoConsumo::promedioYDetalle($clienteId, 6)`, con fallback a `Ordenesmtl`/`ordenescu`
si viene vacío) — solo falta exponerlo en la respuesta de este endpoint, igual que se hizo antes
con `EstratoId`/`ServiciosCliente`/`TieneAseo`/`SaldoAnterior`/`NumeroFactura`.

## Qué agregar

Un campo nuevo `HistoricoConsumos` en cada objeto que ya devuelve `POST /api/medidoresout` (mismo
array de filas de `ordenescu`, mismo nivel que `EstratoId`/`SaldoAnterior`):

```json
{
  "id": "12345",
  "...": "...campos ya existentes sin cambios...",
  "Promedio": 18,
  "HistoricoConsumos": [
    { "periodo": "202602", "consumo_m3": 14 },
    { "periodo": "202603", "consumo_m3": 16 },
    { "periodo": "202604", "consumo_m3": 15 },
    { "periodo": "202605", "consumo_m3": 18 },
    { "periodo": "202606", "consumo_m3": 17 },
    { "periodo": "202607", "consumo_m3": 17 }
  ]
}
```

- Orden cronológico ascendente (más viejo primero, período actual al final) — así se dibuja de
  izquierda a derecha igual que en el recibo de referencia.
- `periodo` en formato `YYYYMM`, igual convención que el resto del contrato (`Periodo` de la
  orden, `periodo` de `tarifaVigente`).
- `consumo_m3` entero, igual que `Promedio`.
- Si hay menos de 6 meses de historial (cliente nuevo), array más corto está bien — la app dibuja
  las barras que reciba, no asume exactamente 6.
- Array vacío o campo ausente (`null`) si no hay ningún historial — la app simplemente omite el
  gráfico en ese caso (fail-safe, mismo criterio que ya se usa con `EstratoId` faltante).

## Nota

No hace falta endpoint nuevo ni tocar `tarifaVigente` — este dato es por cliente/lectura, así que
va en `medidoresout` (se descarga junto con la ruta del día), no en la tarifa (que es genérica por
estrato/período).
