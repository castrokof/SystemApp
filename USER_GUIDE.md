# Guía de Usuario - SystemApp

## Introducción

Bienvenido a **SystemApp**, la aplicación móvil diseñada para facilitar la captura de lecturas de medidores de agua en campo. Esta guía le ayudará a utilizar todas las funcionalidades de la aplicación de manera efectiva.

## Contenido

1. [Primeros Pasos](#primeros-pasos)
2. [Inicio de Sesión](#inicio-de-sesión)
3. [Pantalla Principal](#pantalla-principal)
4. [Sincronización de Datos](#sincronización-de-datos)
5. [Gestión de Órdenes](#gestión-de-órdenes)
6. [Captura de Lecturas](#captura-de-lecturas)
7. [Envío de Lecturas](#envío-de-lecturas)
8. [Impresión de Recibos](#impresión-de-recibos)
9. [Borrar Datos de Ruta](#borrar-datos-de-ruta)
10. [Configuración](#configuración)
11. [Preguntas Frecuentes](#preguntas-frecuentes)
12. [Solución de Problemas](#solución-de-problemas)

---

## Primeros Pasos

### Requisitos del Dispositivo

Antes de utilizar SystemApp, asegúrese de que su dispositivo cumpla con los siguientes requisitos:

- **Sistema Operativo**: Android 8.1 (Oreo) o superior
- **Espacio Disponible**: Mínimo 100 MB
- **Permisos Necesarios**:
  - Ubicación (GPS)
  - Cámara
  - Almacenamiento
  - Bluetooth (para impresión)
  - Internet

### Instalación

1. Descargue el archivo APK de SystemApp
2. Habilite la instalación de aplicaciones de fuentes desconocidas en su dispositivo
3. Toque el archivo APK descargado
4. Siga las instrucciones de instalación
5. Conceda todos los permisos solicitados

### Primera Vez

Al abrir la aplicación por primera vez:

1. Se le solicitarán permisos de ubicación, cámara y almacenamiento
2. Toque "Permitir" para cada permiso
3. Será redirigido a la pantalla de inicio de sesión

---

## Inicio de Sesión

### Acceder a la Aplicación

1. **Abrir la aplicación** - Toque el ícono de SystemApp en su dispositivo

2. **Ingresar credenciales**:
   - **Usuario**: Ingrese su nombre de usuario asignado
   - **Contraseña**: Ingrese su contraseña

3. **Recordar sesión** (opcional):
   - Marque la casilla "Recordar sesión" si desea mantener la sesión activa
   - No recomendado en dispositivos compartidos

4. **Iniciar sesión**:
   - Toque el botón "INICIAR SESIÓN"
   - Espere mientras se validan sus credenciales

### Mensajes de Error Comunes

- **"Error de red"**: Verifique su conexión a internet
- **"Usuario o contraseña incorrectos"**: Revise sus credenciales
- **"Error del servidor"**: Intente nuevamente más tarde o contacte a soporte

### Cerrar Sesión

Para cerrar su sesión:

1. Toque el ícono de menú (≡) en la esquina superior izquierda
2. Toque el ícono de configuración (⚙) en la barra superior
3. Confirme que desea cerrar sesión

---

## Pantalla Principal

Después de iniciar sesión, verá la pantalla principal con las siguientes opciones en el menú lateral:

### Menú de Navegación

```
┌─────────────────────────┐
│  ☰  SystemApp           │
├─────────────────────────┤
│  👤 [Nombre de Usuario] │
├─────────────────────────┤
│  📋 Órdenes             │
│  ✓  Ejecutadas          │
│  🔄 Sincronizar         │
│  🗑️ Borrar Ruta         │
│  ⚙️ Configuración       │
└─────────────────────────┘
```

**Opciones del Menú**:

- **Órdenes**: Ver y gestionar órdenes de lectura
- **Ejecutadas**: Ver historial de lecturas realizadas
- **Sincronizar**: Descargar y enviar datos
- **Borrar Ruta**: Eliminar datos de rutas completadas
- **Configuración**: Ajustes de la aplicación

---

## Sincronización de Datos

La sincronización es el proceso de descargar órdenes de lectura del servidor y enviar las lecturas capturadas.

### Descargar Órdenes

**Paso 1: Acceder a Sincronización**
1. Abra el menú lateral (☰)
2. Toque "Sincronizar"

**Paso 2: Verificar Conexión**
- Asegúrese de tener conexión a internet activa
- Preferiblemente use WiFi para evitar consumo de datos móviles

**Paso 3: Descargar**
1. Toque el botón "DESCARGAR ÓRDENES"
2. Espere mientras se descargan los datos
3. Verá un mensaje indicando la cantidad de órdenes descargadas

**Ejemplo de Mensaje**:
```
✓ Descarga exitosa
  - 150 órdenes descargadas
  - 25 catálogos actualizados
```

### ¿Qué se Descarga?

Durante la sincronización se descargan:

- **Órdenes de lectura**: Asignadas a su usuario
- **Catálogos**: Causas de no lectura, observaciones, etc.
- **Información de rutas**: Detalles de las rutas asignadas

### Frecuencia de Sincronización

Se recomienda sincronizar:

- **Al inicio del día**: Antes de salir a campo
- **Durante el día**: Si recibe nuevas asignaciones
- **Al finalizar el día**: Para enviar las lecturas capturadas

---

## Gestión de Órdenes

### Ver Órdenes de Lectura

El menú lateral le permite acceder a dos vistas diferentes:

**Órdenes Pendientes**
1. Abra el menú lateral
2. Toque "Órdenes"
3. Verá el listado de órdenes pendientes de lectura

**Órdenes Ejecutadas**
1. Abra el menú lateral
2. Toque "Ejecutadas"
3. Verá el listado de lecturas ya capturadas (pendientes de envío)

### Información de Cada Orden

Cada tarjeta de orden muestra:

```
┌────────────────────────────────────┐
│ #001 - Ruta: R001                  │
│ Medidor: MED12345                  │
│ Suscriptor: SUB001                 │
│ Nombre: Juan Pérez                 │
│ Dirección: Calle 10 #20-30         │
│ Lectura Anterior: 1500 m³          │
│ Promedio: 25 m³                    │
└────────────────────────────────────┘
```

### Filtrar Órdenes

Las órdenes se organizan automáticamente por:

- Ruta
- Consecutivo de ruta (orden de visita)
- Estado

### Seleccionar una Orden

Para capturar una lectura:

1. Busque la orden en la lista
2. Toque sobre la tarjeta de la orden
3. Será redirigido al formulario de lectura

---

## Captura de Lecturas

### Formulario de Lectura

Al seleccionar una orden, verá el formulario de captura con los siguientes campos:

#### 1. Información del Suscriptor

**Solo Lectura** (no editable):
- Nombre completo
- Dirección
- Número de suscriptor
- Referencia del medidor
- Lectura anterior
- Promedio de consumo

#### 2. Captura de Lectura Actual

**Paso 1: Ingresar Lectura**
1. Localice el campo "Lectura Actual"
2. Ingrese el valor mostrado en el medidor
3. El consumo se calculará automáticamente

**Ejemplo**:
```
Lectura Anterior:    1500 m³
Lectura Actual:      [1535]  ← Ingrese aquí
Consumo:             35 m³   ← Calculado automático
```

**Paso 2: Validaciones Automáticas**

El sistema validará la lectura:

- ✓ **Lectura válida**: Fondo verde, puede continuar
- ⚠️ **Lectura crítica**: Fondo amarillo, requiere confirmación
- ✗ **Lectura inválida**: Fondo rojo, debe corregir

**Tipos de Validaciones**:

| Código | Descripción | Acción |
|--------|-------------|--------|
| LA=LANT | Lectura igual a anterior | Verificar medidor |
| LA<LANT | Lectura menor a anterior | Posible error o medidor cambiado |
| CA>165CP | Consumo muy alto (>165% promedio) | Verificar fuga o error |
| CA<35CP | Consumo muy bajo (<35% promedio) | Verificar medidor |
| CA<50CP | Consumo bajo (<50% promedio) | Puede ser normal |

#### 3. Captura de Fotografía

**Paso 1: Tomar Foto**
1. Toque el botón "TOMAR FOTO" o ícono de cámara
2. Se abrirá la cámara del dispositivo
3. Enfoque el medidor
4. Asegúrese de que la lectura sea visible
5. Tome la fotografía
6. Si está conforme, acepte; si no, repita

**Consejos para Buenas Fotos**:
- Buena iluminación
- Medidor completo en el encuadre
- Números de lectura legibles
- Sin reflejos ni sombras excesivas

**Paso 2: Vista Previa**
- Después de tomar la foto, verá una miniatura
- Toque la miniatura para ver en tamaño completo
- Si no está conforme, toque "Retomar Foto"

#### 4. Causas de No Lectura

Si **NO puede tomar la lectura** del medidor:

**Paso 1: Seleccionar Causa**
1. Toque el campo "Causa de No Lectura"
2. Seleccione el motivo de la lista:
   - Predio cerrado
   - Medidor tapado
   - Medidor roto
   - No hay medidor
   - Zona peligrosa
   - Otros

**Paso 2: Agregar Descripción** (opcional)
- Si seleccionó "Otros", describa el motivo
- Sea específico y claro

#### 5. Observaciones

**Paso 1: Seleccionar Observación**

Si hay algo que reportar:

1. Toque "Observación"
2. Seleccione de la lista:
   - Fuga visible
   - Medidor en mal estado
   - Conexión irregular
   - Otros

**Paso 2: Observación General**

Para comentarios adicionales:

1. Toque "Observación General"
2. Escriba libremente
3. Máximo 500 caracteres

**Ejemplos**:
```
"Se observa humedad en la base del medidor"
"Cliente solicita revisión de factura"
"Medidor de difícil acceso por maleza"
```

#### 6. Geolocalización (GPS)

**Automático**:
- Al abrir el formulario, se captura la ubicación automáticamente
- Asegúrese de tener GPS activado
- Icono de ubicación (📍) indica que se capturó correctamente

**Si no se captura**:
1. Verifique que GPS esté activado
2. Espere unos segundos
3. Toque "Actualizar Ubicación"

#### 7. Guardar Lectura

**Paso 1: Revisar Datos**
- Verifique que todos los campos estén correctos
- Asegúrese de que la foto sea clara (si la tomó)

**Paso 2: Guardar**
1. Toque el botón "GUARDAR" o ícono de disco (💾)
2. Espere el mensaje de confirmación
3. La orden se moverá a "Ejecutadas"

**Mensajes**:
```
✓ Lectura guardada exitosamente
  - Suscriptor: SUB001
  - Lectura: 1535 m³
  - Consumo: 35 m³
```

### Navegar entre Órdenes

**Botones de Navegación**:

- **← Anterior**: Ir a la orden anterior en la ruta
- **Siguiente →**: Ir a la siguiente orden en la ruta
- **Lista**: Volver al listado de órdenes

**Atajos**:
- Deslice hacia la izquierda para ir a siguiente
- Deslice hacia la derecha para ir a anterior

### Editar Lectura Capturada

Si necesita corregir una lectura ya guardada:

1. Vaya al menú lateral y toque "Ejecutadas"
2. Busque la orden
3. Toque sobre ella
4. Realice los cambios necesarios
5. Toque "GUARDAR"

**Nota**: Solo puede editar lecturas que no han sido enviadas al servidor.

---

## Envío de Lecturas

Una vez que haya capturado lecturas en campo, debe enviarlas al servidor.

### Cuándo Enviar

Se recomienda enviar:

- **Al finalizar una ruta completa**
- **Al final del día**
- **Cuando tenga conexión WiFi disponible**

### Proceso de Envío

**Paso 1: Verificar Lecturas Pendientes**
1. Abra el menú lateral y toque "Ejecutadas"
2. Revise la cantidad de lecturas pendientes de envío

**Paso 2: Acceder a Sincronización**
1. Abra el menú lateral
2. Toque "Sincronizar"

**Paso 3: Enviar**
1. Verifique que tenga conexión a internet
2. Toque el botón "ENVIAR LECTURAS"
3. Espere mientras se suben los datos
4. Verá un mensaje de confirmación

**Ejemplo de Mensaje**:
```
✓ Envío exitoso
  - 45 lecturas enviadas
  - 12 fotografías subidas
  - 0 errores
```

### ¿Qué se Envía?

Durante el envío se suben:

- Lecturas capturadas
- Fotografías tomadas
- Coordenadas GPS
- Causas y observaciones
- Timestamp de captura

### Trabajo Offline

**Ventajas**:
- Puede trabajar sin internet en campo
- Los datos se almacenan localmente
- Sincroniza cuando tenga conexión

**Límites**:
- No puede descargar nuevas órdenes sin internet
- Las fotografías ocupan espacio en el dispositivo
- Debe enviar regularmente para liberar espacio

---

## Impresión de Recibos

SystemApp permite imprimir recibos de lectura usando impresoras Bluetooth portátiles.

### Configurar Impresora

**Primera Vez**:

1. **Emparejar Impresora**:
   - Vaya a Configuración del dispositivo Android
   - Active Bluetooth
   - Busque dispositivos disponibles
   - Seleccione su impresora (ej: "POS-5802")
   - Empareje (PIN usualmente: 0000 o 1234)

2. **En SystemApp**:
   - Vaya a Configuración
   - Toque "Impresora"
   - Seleccione la impresora emparejada

### Imprimir Recibo

**Opción 1: Desde Formulario de Lectura**

Después de guardar una lectura:

1. Toque el botón "IMPRIMIR" (🖨)
2. Espere mientras se conecta a la impresora
3. El recibo se imprimirá automáticamente

**Opción 2: Desde Lista de Ejecutadas**

1. Abra el menú lateral y toque "Ejecutadas"
2. Toque y mantenga presionada una orden
3. Seleccione "Imprimir" del menú contextual

### Contenido del Recibo

El recibo impreso incluye:

```
========================================
     COMPAÑÍA DE ACUEDUCTO
========================================
Fecha: 15/01/2024  Hora: 14:30

Suscriptor: SUB001
Nombre: Juan Pérez
Dirección: Calle 10 #20-30

Medidor: MED12345
Ruta: R001 - Consecutivo: 001

----------------------------------------
Lectura Anterior:    1500 m³
Lectura Actual:      1535 m³
Consumo:               35 m³
----------------------------------------

Técnico: usuario123
Firma: _______________

========================================
   Conserve este recibo
========================================
```

### Solución de Problemas de Impresión

| Problema | Solución |
|----------|----------|
| No encuentra impresora | Verifique que esté emparejada y encendida |
| Error de conexión | Desempareje y vuelva a emparejar |
| Impresión incompleta | Verifique papel y batería de impresora |
| Caracteres extraños | Verifique configuración de codificación |

---

## Borrar Datos de Ruta

Esta opción permite eliminar datos de rutas ya completadas y enviadas.

### ⚠️ Advertencia

**PRECAUCIÓN**: Esta acción eliminará permanentemente:
- Órdenes descargadas
- Lecturas capturadas
- Fotografías locales
- Historial de la ruta

**Solo borre datos si**:
- Ya envió todas las lecturas al servidor
- Confirmó que fueron recibidas correctamente
- Necesita liberar espacio en el dispositivo

### Proceso de Borrado

**Paso 1: Acceder**
1. Abra el menú lateral
2. Toque "Borrar Ruta"

**Paso 2: Verificar**
1. Revise la información mostrada:
   - Órdenes totales
   - Lecturas enviadas
   - Lecturas pendientes

**Paso 3: Confirmar**
1. Si tiene lecturas pendientes, aparecerá una advertencia
2. Decida si desea continuar
3. Toque "CONFIRMAR BORRADO"

**Paso 4: Resultado**
```
✓ Datos eliminados
  - 150 órdenes borradas
  - Base de datos limpiada
  - Espacio liberado: 45 MB
```

### Alternativa: Borrado Selectivo

Para borrar solo ciertas rutas:

1. Vaya a Configuración
2. Toque "Gestión de Datos"
3. Seleccione rutas específicas
4. Toque "Borrar Seleccionadas"

---

## Configuración

### Acceder a Configuración

1. Abra el menú lateral
2. Toque "Configuración" o el ícono ⚙️

### Opciones Disponibles

#### 1. Perfil de Usuario

- **Nombre**: Nombre completo del usuario
- **Usuario**: Nombre de usuario para login
- **Email**: Correo electrónico
- **Empresa**: Compañía de acueducto

**No editables** - Estos datos se obtienen del servidor

#### 2. Configuración de Conexión

- **URL del Servidor**: Dirección del servidor API
- **Timeout**: Tiempo de espera para peticiones (segundos)

**Solo editable por administradores**

#### 3. Configuración de GPS

- **Precisión GPS**: Alta / Media / Baja
- **Timeout GPS**: Tiempo de espera para ubicación
- **GPS Obligatorio**: Si debe capturar GPS en cada lectura

#### 4. Configuración de Cámara

- **Calidad de Foto**: Alta / Media / Baja
- **Comprimir Fotos**: Sí / No
- **Foto Obligatoria**: Si debe tomar foto en cada lectura

#### 5. Configuración de Impresora

- **Impresora**: Seleccionar impresora emparejada
- **Imprimir Automáticamente**: Imprimir después de cada lectura
- **Copias**: Número de copias del recibo

#### 6. Configuración de Sincronización

- **Sincronización Automática**: Cada X horas
- **Solo WiFi**: Sincronizar solo con WiFi
- **Avisar antes de Sincronizar**: Pedir confirmación

#### 7. Acerca de

- **Versión de la App**: Número de versión instalada
- **Última Actualización**: Fecha de última actualización
- **Soporte**: Información de contacto

---

## Preguntas Frecuentes

### Generales

**P: ¿Puedo usar la app sin internet?**
R: Sí, puede capturar lecturas offline. Necesita internet solo para descargar órdenes y enviar lecturas.

**P: ¿Cuántas lecturas puedo almacenar localmente?**
R: Depende del espacio disponible en su dispositivo. Generalmente puede almacenar varios miles de lecturas.

**P: ¿Qué pasa si se cierra la app mientras capturo una lectura?**
R: Los datos se guardan automáticamente. Puede continuar donde quedó.

### Lecturas

**P: ¿Puedo editar una lectura después de enviarla?**
R: No, una vez enviada al servidor no puede editarse desde la app. Debe contactar a su supervisor.

**P: ¿Qué hago si el medidor está roto?**
R: Seleccione "Medidor roto" en Causa de No Lectura y tome una fotografía del medidor.

**P: ¿Es obligatorio tomar foto?**
R: Depende de la configuración de su empresa. Generalmente es recomendado pero no obligatorio.

### Sincronización

**P: ¿Con qué frecuencia debo sincronizar?**
R: Se recomienda al inicio y al final del día, y cuando complete una ruta importante.

**P: ¿Qué pasa si falla el envío de lecturas?**
R: Las lecturas permanecen en su dispositivo y puede reintentar más tarde.

**P: ¿Consume muchos datos móviles?**
R: Depende de las fotos. Sin fotos, muy poco. Con fotos, se recomienda usar WiFi.

### Técnicas

**P: ¿Cómo libero espacio si la app está lenta?**
R: Use "Borrar Ruta" después de confirmar que envió todas las lecturas.

**P: ¿Qué permisos necesita la app?**
R: Ubicación (GPS), Cámara, Almacenamiento, Bluetooth e Internet.

**P: ¿Funciona en tablets?**
R: Sí, compatible con tablets Android 8.1 o superior.

---

## Solución de Problemas

### La app no inicia

**Síntomas**: La app se cierra al abrirla

**Soluciones**:
1. Reinicie el dispositivo
2. Limpie caché de la app (Configuración → Apps → SystemApp → Almacenamiento → Limpiar caché)
3. Reinstale la app
4. Verifique que tenga Android 8.1 o superior

### No puedo iniciar sesión

**Síntomas**: Error al intentar login

**Soluciones**:
1. Verifique conexión a internet
2. Revise usuario y contraseña (distingue mayúsculas)
3. Verifique que su cuenta esté activa (contacte a supervisor)
4. Intente más tarde si hay error de servidor

### GPS no funciona

**Síntomas**: No captura ubicación

**Soluciones**:
1. Verifique que GPS esté activado (Configuración del dispositivo)
2. Salga a un lugar abierto (mejor señal)
3. Conceda permisos de ubicación a la app
4. Configure precisión GPS en Alta
5. Reinicie el dispositivo

### Cámara no funciona

**Síntomas**: Error al tomar foto

**Soluciones**:
1. Conceda permisos de cámara a la app
2. Verifique que la cámara del dispositivo funcione
3. Limpie caché de la app
4. Reinicie el dispositivo

### No sincroniza datos

**Síntomas**: Error al descargar u enviar

**Soluciones**:
1. Verifique conexión a internet estable
2. Intente con WiFi en lugar de datos móviles
3. Verifique que su sesión esté activa (cierre y vuelva a iniciar sesión)
4. Espere unos minutos y reintente
5. Contacte a soporte si persiste

### Impresora no conecta

**Síntomas**: Error al imprimir

**Soluciones**:
1. Verifique que la impresora esté encendida
2. Verifique que esté emparejada correctamente
3. Desempareje y vuelva a emparejar
4. Verifique batería de la impresora
5. Reinicie el Bluetooth del dispositivo

### App lenta o se traba

**Síntomas**: Funciona muy lento

**Soluciones**:
1. Cierre otras aplicaciones abiertas
2. Limpie caché de la app
3. Borre rutas antiguas ya enviadas
4. Reinicie el dispositivo
5. Verifique espacio disponible (mínimo 100 MB)

### Lecturas no se guardan

**Síntomas**: Pierde los datos ingresados

**Soluciones**:
1. Verifique espacio disponible en el dispositivo
2. Conceda permisos de almacenamiento
3. Limpie caché de la app
4. Contacte a soporte técnico

---

## Consejos y Mejores Prácticas

### Antes de Salir a Campo

✓ Descargue todas las órdenes asignadas
✓ Verifique batería del dispositivo (>50%)
✓ Lleve cargador portátil
✓ Configure impresora Bluetooth
✓ Verifique espacio de almacenamiento

### Durante el Trabajo de Campo

✓ Capture GPS en cada lectura
✓ Tome fotos claras de los medidores
✓ Sea específico en observaciones
✓ Siga el orden de la ruta
✓ Guarde frecuentemente

### Al Finalizar el Día

✓ Envíe todas las lecturas capturadas
✓ Verifique que se enviaron correctamente
✓ Imprima reporte de actividades
✓ Cargue el dispositivo para el día siguiente

### Seguridad

✓ No comparta su contraseña
✓ Cierre sesión en dispositivos compartidos
✓ Haga backup de fotos importantes
✓ Reporte pérdida del dispositivo inmediatamente

---

## Contacto y Soporte

### Soporte Técnico

**Horario**: Lunes a Viernes, 8:00 AM - 6:00 PM

**Canales de Contacto**:
- **Email**: soporte@empresa.com
- **Teléfono**: +57 (XXX) XXX-XXXX
- **WhatsApp**: +57 XXX XXX XXXX

### Reportar Problemas

Al reportar un problema, incluya:

1. Versión de la app (ver en Configuración)
2. Modelo del dispositivo
3. Versión de Android
4. Descripción detallada del problema
5. Pasos para reproducir el error
6. Capturas de pantalla (si es posible)

### Actualizaciones

Las actualizaciones de la app se notifican por:

- Notificación en el dispositivo
- Email a usuarios
- Mensaje al iniciar sesión

**Instalar Actualización**:
1. Descargue el nuevo APK
2. Instale sobre la versión existente
3. Sus datos se conservarán

---

## Glosario de Términos

| Término | Definición |
|---------|------------|
| **Suscriptor** | Código único que identifica al cliente |
| **Medidor** | Dispositivo que mide el consumo de agua |
| **Lectura Anterior (LA)** | Última lectura registrada del medidor |
| **Lectura Actual** | Lectura que se captura en la visita actual |
| **Consumo** | Diferencia entre lectura actual y anterior |
| **Promedio** | Consumo promedio mensual del suscriptor |
| **Ruta** | Conjunto de medidores agrupados geográficamente |
| **Consecutivo** | Orden de visita dentro de una ruta |
| **Crítica** | Validación que indica posible anomalía en lectura |
| **Causa** | Motivo por el cual no se pudo tomar lectura |
| **Observación** | Nota o comentario sobre el medidor o lectura |
| **Sincronización** | Proceso de descargar y enviar datos con servidor |
| **Offline** | Sin conexión a internet |
| **API Token** | Código de autenticación para usar la app |

---

## Apéndice: Atajos de Teclado

Si usa dispositivo con teclado físico:

| Atajo | Acción |
|-------|--------|
| **Ctrl + S** | Guardar lectura |
| **Ctrl + P** | Imprimir |
| **Ctrl + N** | Siguiente orden |
| **Ctrl + B** | Anterior orden |
| **Ctrl + L** | Ir a lista de órdenes |
| **Esc** | Cancelar/Volver |

---

**Versión de la Guía**: 1.0
**Última Actualización**: 2024
**Compatible con SystemApp**: v1.0+

---

*Esta guía está sujeta a cambios. Consulte la versión más reciente en la documentación oficial.*
