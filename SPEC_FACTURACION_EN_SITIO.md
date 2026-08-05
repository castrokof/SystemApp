# Especificación — Facturación en Sitio (App Lecturistas)

> Flujo que debe implementarse en la app de lecturistas (`SystemApp_nueva`). Debe funcionar 100% offline (sin conexión en el momento de la visita), sincronizando cuando haya señal.

## 1. Flujo paso a paso

### Interruptor general: "Facturación en sitio" (configuración)

- Debe existir un toggle en Configuración: **"Facturación en sitio"** (activado/desactivado).
- **Desactivado** (comportamiento actual, sin cambios): la app solo imprime la **constancia de lectura** (lo que ya hace hoy), sin ningún cálculo de factura.
- **Activado**: se habilita todo el flujo descrito abajo (cálculo, resumen, impresión de factura real).
- Esto permite activar la función gradualmente por dispositivo/lecturista sin afectar a los que aún no la usan.

### Diseño de la factura impresa

- El diseño/formato de la factura impresa en campo debe **replicar el mismo diseño que ya usa el backend** en `resources/views/pdf/factura.blade.php` (generado con DomPDF) — mismos campos, mismo desglose, mismo orden — para que el papel entregado en campo sea consistente con el PDF que vería el cliente desde el panel web.
- **Se necesitan dos plantillas de impresión, no una sola**: una para impresoras térmicas de **58mm** y otra para **80mm** — hay lecturistas con impresoras más económicas (58mm) y otros con 80mm, y el contenido debe adaptarse a cada ancho (más compacto en 58mm). Definir con Claude Code si esto se selecciona por configuración del dispositivo/lecturista, o se detecta automáticamente si la librería de impresión lo permite.
- Referencia visual compartida por el usuario: una factura de servicios públicos (formato ancho, no térmico) que sirve como **modelo de estructura de contenido**, no de layout físico exacto. Elementos clave de esa estructura a replicar, en la medida de lo que quepa en cada ancho térmico:
  1. **Encabezado**: nombre de la empresa/acueducto, número de cuenta/suscriptor, número de factura, mes de servicio.
  2. **Datos del cliente**: nombre, dirección.
  3. **Datos de consumo**: período de servicio (desde/hasta), tipo de lectura, fecha de generación.
  4. **Lecturas**: lectura anterior, lectura actual, consumo del período (m³), promedio.
  5. **Datos técnicos breves**: estrato, clase de servicio (residencial/comercial/etc.), ruta.
  6. **Desglose de valor**: cargo fijo + consumo (con su tarifa unitaria) + subsidio/contribución aplicado, por servicio (acueducto/alcantarillado, y aseo si aplica).
  7. **Total del período, saldo anterior (si aplica), total a pagar.**
  8. **Fechas clave**: pago oportuno / fecha de vencimiento / fecha de posible suspensión.
  9. **Código de barras o QR** con el número de cuenta/factura, para facilitar el pago (si la impresora térmica lo soporta).
- Elementos de la referencia que probablemente **no apliquen o se simplifiquen** por limitación de espacio térmico (especialmente en 58mm): gráfico de historial de consumo en barras, tabla extensa de indicadores de calidad, textos informativos largos — usar solo si el espacio y la librería de impresión lo permiten fácilmente, priorizar siempre los datos financieros y de lectura sobre elementos decorativos/informativos.
- Confirmar con Claude Code cuántos de estos campos caben en cada ancho (58mm vs 80mm) sin saturar el recibo.

### Corrección de mensaje incorrecto: aviso falso de "impresora no conectada"

- **No es un problema funcional** — la impresión siempre funciona correctamente, sin fallos reales de conexión.
- El único problema es un **mensaje (Snackbar) que aparece incorrectamente** diciendo "impresora no conectada", aun cuando sí está conectada y la impresión se completa bien.
- Se necesita simplemente **eliminar o corregir ese aviso** para que no aparezca cuando la impresora sí funciona correctamente — es un ajuste de UI/lógica de validación del mensaje, no un diagnóstico de conectividad Bluetooth.

1. **Captura de lectura**: el lecturista ingresa/escanea la lectura actual del medidor.
2. **Clasificación automática del consumo**: la app calcula el consumo (`lectura_actual - lectura_anterior`) y lo clasifica localmente contra el promedio histórico del cliente (mismo criterio que el backend: normal si está dentro de ±50% del promedio — replicar la lógica de `Cliente::lecturaEsNormal()` documentada en `CONTEXTO_BACKEND.md`, sección 2.1, para que el resultado offline coincida con lo que el servidor recalculará después).
3. **Confirmación del lecturista**: si la lectura es correcta, toma una **foto del medidor** (evidencia, igual que el flujo actual de revisiones).
4. **Bifurcación según clasificación:**

   ### Caso A — Consumo NORMAL, o ALTO/BAJO confirmado correcto por el lecturista
   - Si el consumo es normal, **o** es alto/bajo pero el lecturista confirma que la lectura fue tomada correctamente (no es error de digitación) → **sí se puede facturar en sitio**.
   - La app pregunta: **"¿Desea imprimir la factura ahora?"**
   - Si el lecturista confirma:
     - La app **calcula localmente** el valor de la factura (cargo fijo + tarifa por rangos + subsidio/sobretasa de estrato — replicar la lógica de `FacturacionService::calcular()` documentada en `CONTEXTO_BACKEND.md` sección 4.1, usando la tarifa vigente descargada previamente al dispositivo).
     - Muestra un **resumen en pantalla** al lecturista: lectura anterior/actual, consumo m³, desglose de valor, total a pagar — para que verifique antes de imprimir.
     - Si confirma el resumen: **imprime el recibo/factura** vía impresora térmica Bluetooth (ESC/POS, 58mm/80mm).
     - La factura se genera en estado **PENDIENTE de pago** — no se marca pagada en este flujo (el cliente paga después, vía Wompi u otro medio en el panel web o en oficina).
   - Si el lecturista decide NO imprimir en el momento: la lectura queda guardada localmente para sincronizar después, sin generar factura en sitio (se factura por el flujo normal del backend, como hoy).

   ### Caso B — Consumo NEGATIVO (siempre) — solo desde la web
   - **Nunca se factura en sitio, sin excepción**, sin importar si el lecturista confirma la lectura como correcta.
   - La lectura queda registrada localmente como pendiente de revisión (equivalente a como el backend crea una `OrdenRevision` para consumos fuera de rango — ver `CONTEXTO_BACKEND.md` sección 4.2).
   - Al sincronizar, esta lectura debe quedar disponible para revisión/facturación manual desde el panel web (flujo ya existente: `FacturacionEspecialController`).

   ### Corrección de lectura ya ejecutada
   - El lecturista debe poder **entrar a un registro ya ejecutado** (lectura ya tomada) y **corregir el valor de la lectura** si detecta un error de digitación.
   - Al corregir, la app debe **recalcular automáticamente** la clasificación (normal/alto/bajo/negativo) y, si ya se había facturado/impreso en sitio con el valor erróneo, se debe manejar como corrección: no simplemente sobrescribir silenciosamente una factura ya impresa y entregada al cliente — definir con Claude Code si esto genera una factura de ajuste o requiere anular/reemplazar la anterior (dejar explícito en el plan antes de implementar).

## 2. Requisito crítico: operación 100% offline

- Todo el cálculo (clasificación normal/anómalo + cálculo del valor de la factura) debe poder hacerse **sin conexión a internet**, usando datos de tarifas/clientes ya descargados y cacheados localmente en el dispositivo.
- Esto implica que la app necesita **descargar y cachear previamente** (en la última sincronización con señal):
  - Tarifa vigente completa (cargo fijo + rangos por servicio + estrato) — equivalente a `TarifaPeriodo`/`TarifaCargoFijo`/`TarifaRango` del backend.
  - Datos de estrato de cada cliente de la ruta asignada (para aplicar subsidio/sobretasa correctamente).
  - Promedio histórico de consumo de cada cliente de la ruta (para la clasificación normal/anómalo).
- La factura generada en sitio (offline) debe guardarse localmente con un identificador temporal y sincronizarse al servidor cuando haya señal — el servidor debe ser la fuente de verdad final (recalcular/validar al sincronizar, por si la tarifa cacheada quedó desactualizada).

## 3. Puntos abiertos para decidir con Claude Code antes de implementar

- **Consecutivo offline (definido):** se recomienda asignar **rangos/bloques de numeración exclusivos por lecturista/dispositivo** antes de salir a campo (en la mañana, con señal en oficina) — mismo principio que un talonario de papel o la numeración autorizada DIAN. El servidor controla qué rangos ya se asignaron para no duplicar bloques. Alternativa más simple: número provisional por dispositivo+timestamp, con el número oficial asignándose recién al sincronizar (el cliente recibe un papel "provisional" en ese caso). Definir con Claude Code cuál de las dos se implementa.
- ¿Qué pasa si, al sincronizar, el backend recalcula el valor y da un resultado **distinto** al impreso en campo (por ejemplo, la tarifa cacheada estaba desactualizada)? Definir si prevalece el valor impreso (ya entregado al cliente) o si se genera una nota de ajuste.
- ¿La impresora Bluetooth ya está integrada en la app, o es parte de este desarrollo?
- **Corrección de lectura tras facturar:** si ya se imprimió/entregó una factura y luego se corrige la lectura, ¿se anula y reemplaza, o se genera una factura de ajuste adicional? (ver sección 1, "Corrección de lectura ya ejecutada")

## 5. Configuración parametrizable desde el panel web (Laravel)

Estas configuraciones deben vivir en el backend (probablemente en `Empresa`, ya que es donde vive el resto de configuración por acueducto) y la app debe descargarlas/cachearlas en cada sincronización, para que el comportamiento en campo se ajuste sin necesidad de actualizar la app.

### 5.1 Parametrizar qué clasificaciones se facturan/imprimen automáticamente en sitio

- Necesito, desde el panel web, poder configurar **por clasificación** (NORMAL, ALTO, BAJO, CAUSADO/NEGATIVO) si se permite facturar e imprimir en sitio o no.
- Ejemplo de uso: activar impresión en sitio para NORMAL y ALTO/BAJO confirmados, pero mantener CAUSADO/NEGATIVO siempre restringido a la web (aunque esto último ya es una regla fija de negocio — la parametrización es más relevante para decidir si, por ejemplo, en un momento dado se quiere restringir también los ALTO/BAJO a solo web, sin tener que cambiar código).
- La app debe consultar esta configuración al sincronizar y aplicarla localmente para decidir si ofrece el botón de impresión o no, según la clasificación de cada lectura.

### 5.2 Tarifa de aseo

- Algunos acueductos también facturan el servicio de **aseo** (recolección de basuras), además de acueducto/alcantarillado.
- Hay que agregar en el panel web la configuración de tarifa de aseo (probablemente como un servicio adicional, similar en estructura a `TarifaCargoFijo`/`TarifaRango` pero para `servicio = ASEO`, o como un cargo fijo simple si aseo no maneja rangos por consumo — definir con Claude Code).
- La app debe descargar/cachear esta tarifa igual que hace con acueducto/alcantarillado, e incluirla en el cálculo local de la factura cuando el cliente tenga aseo activo.

## 6. Flujo de resolución posterior de lecturas anómalas (post-ruta)

- Cuando el lecturista **termina la ruta**, las lecturas CAUSADO/NEGATIVO (y cualquier ALTO/BAJO no facturado en sitio) quedan pendientes de revisión en el backend, como ya está definido.
- Desde el panel web, alguien revisa esa lectura pendiente y la **resuelve** de una de estas formas (usando el flujo ya existente de `FacturacionEspecialController`):
  - La recalcula usando el promedio histórico del cliente (en vez de la lectura anómala tal cual), y genera la factura con ese valor ajustado, o
  - La aprueba/factura tal cual, con la lectura original.
- En cualquiera de los dos casos, el resultado es una **factura ya calculada y lista** en el servidor, marcada como "lista para imprimir en campo".
- **La sincronización de la app debe descargar automáticamente estas facturas resueltas** — sin que el lecturista tenga que buscarlas o pedirlas manualmente. Al sincronizar (como ya hace hoy con el resto de datos: rutas, clientes, tarifas), la app trae también estas facturas ya resueltas.
- Una vez descargadas, quedan **listas para imprimir directamente desde el dispositivo** en la próxima visita a ese cliente — sin necesidad de recalcular nada en el dispositivo, porque ya vienen calculadas desde el servidor.
- Esto cierra el ciclo completo: lectura anómala en campo → queda pendiente → se resuelve/factura desde la web (recalculada o aprobada) → se sincroniza automáticamente al dispositivo → se imprime y entrega en la siguiente visita.

## 7. Referencia técnica

Toda la lógica de cálculo de tarifas, clasificación normal/anómalo, y el flujo de revisión para casos anómalos ya está documentada en detalle en `CONTEXTO_BACKEND.md` (secciones 2.4, 4.1, 4.2) — replicar esa lógica localmente, no reinventarla, para que el cálculo offline coincida con el que hace el servidor.
