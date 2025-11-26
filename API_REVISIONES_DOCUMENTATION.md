# 📚 API DE REVISIONES - DOCUMENTACIÓN COMPLETA

## 🌐 URL BASE
```
https://[tu-acueducto].com/api
```

---

## 🔐 AUTENTICACIÓN

Todos los endpoints (excepto `/login` y `/ping`) requieren autenticación mediante token:

```
Authorization: Bearer {api_token}
```

---

## 📋 ENDPOINTS

### 1. **PING** - Verificar conexión

```http
GET /ping
```

**Response 200:**
```json
{
  "status": "ok",
  "message": "API funcionando correctamente",
  "version": "1.0.0",
  "timestamp": "2025-11-25 10:30:45"
}
```

---

### 2. **LOGIN** - Iniciar sesión

```http
POST /login
```

**Request Body:**
```json
{
  "usuario": "tecnico01",
  "clave": "password123"
}
```

**Response 200 - Técnico de Lecturas:**
```json
{
  "success": true,
  "data": {
    "id": "1",
    "usuario": "tecnico01",
    "nombre": "Juan Pérez",
    "tipodeusuario": "TECNICO",
    "email": "juan@acueducto.com",
    "empresa": "Acueducto Municipal",
    "estado": "activo",
    "api_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
    "firma_url": "https://[servidor]/storage/firmas/tecnico01_firma.png"
  }
}
```

**Response 200 - Técnico de Revisiones:**
```json
{
  "success": true,
  "data": {
    "id": "2",
    "usuario": "revisor01",
    "nombre": "María González",
    "tipodeusuario": "REVISOR",
    "email": "maria@acueducto.com",
    "empresa": "Acueducto Municipal",
    "estado": "activo",
    "api_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
    "firma_url": "https://[servidor]/storage/firmas/revisor01_firma.png"
  }
}
```

**Response 401 - Credenciales inválidas:**
```json
{
  "success": false,
  "message": "Credenciales inválidas"
}
```

---

### 3. **DESCARGAR ÓRDENES DE REVISIÓN** - Sincronizar

```http
GET /revisiones/ordenes
```

**Headers:**
```
Authorization: Bearer {api_token}
```

**Query Parameters:**
- `usuario_id` (opcional): Filtrar por usuario
- `periodo` (opcional): Filtrar por periodo
- `estado` (opcional): PENDIENTE, EN_EJECUCION, EJECUTADA

**Response 200:**
```json
{
  "success": true,
  "data": [
    {
      "id": "REV-2025-001",
      "ciclo": "2025-11",
      "periodo": "Noviembre 2025",
      "suscriptor": "10023456",
      "ref_medidor": "M-789456",
      "direccion": "Calle 50 #23-45",
      "nombre": "Carlos",
      "apell": "Rodríguez",
      "promedio": 25,
      "LA": "1245",
      "usuario": "revisor01",
      "estado": "PENDIENTE",
      "tipo_desviacion": "ALTO",
      "ruta": "RUTA-05",
      "consecutivo_ruta": "15",
      "observacion_inicial": "Consumo superior a 80 m³ en los últimos 3 meses"
    },
    {
      "id": "REV-2025-002",
      "ciclo": "2025-11",
      "periodo": "Noviembre 2025",
      "suscriptor": "10023457",
      "ref_medidor": "M-789457",
      "direccion": "Carrera 30 #15-20",
      "nombre": "Ana",
      "apell": "Martínez",
      "promedio": 18,
      "LA": "856",
      "usuario": "revisor01",
      "estado": "PENDIENTE",
      "tipo_desviacion": "BAJO",
      "ruta": "RUTA-05",
      "consecutivo_ruta": "16",
      "observacion_inicial": "Consumo menor a 5 m³ en los últimos 2 meses"
    }
  ],
  "total": 2
}
```

---

### 4. **ENVIAR REVISIÓN COMPLETADA**

```http
POST /revisiones/enviar
```

**Headers:**
```
Authorization: Bearer {api_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "id": "REV-2025-001",
  "suscriptor": "10023456",
  "usuario": "revisor01",

  "lectura_actual": 1320,
  "consumo": 75,

  "nombre_residente": "Carlos Rodríguez Gómez",

  "estado_acometida": "BUENA",
  "estado_sellos": "INTACTOS",
  "que_surte": "Vivienda unifamiliar",

  "censo_poblacional_familiar": 1,
  "censo_poblacional_personas": 4,
  "censo_poblacional_adultos": 2,
  "censo_poblacional_ninos": 2,

  "censo_hidraulico": [
    {
      "elemento": "SANITARIO",
      "cantidad": 2,
      "estado": "BUENO",
      "foto": "base64_encoded_image_here..."
    },
    {
      "elemento": "LAVAMANOS",
      "cantidad": 3,
      "estado": "BUENO",
      "foto": null
    },
    {
      "elemento": "DUCHA",
      "cantidad": 1,
      "estado": "MALO",
      "foto": "base64_encoded_image_here..."
    }
  ],

  "codigo_causa": 15,
  "desc_causa": "Fuga interna en sanitario",
  "observacion_causa": "Se detectó filtración constante en el tanque del sanitario principal",

  "observacion_general": "Se recomienda reparación inmediata del sanitario. El cliente se compromete a realizar la reparación.",

  "fecha_inicio": "2025-11-25 08:30:00",
  "fecha_cierre": "2025-11-25 09:15:00",
  "latitud": "4.6097102",
  "longitud": "-74.0817500",

  "pdf_base64": "JVBERi0xLjQKJeLjz9MKMyAwIG9iago8PC9UeXBlIC9QYWdlCi9QYXJlbnQgMSAwIFIK...",

  "fotos_adicionales": [
    {
      "tab_numero": 1,
      "descripcion": "Medidor exterior",
      "foto_base64": "iVBORw0KGgoAAAANSUhEUgAA..."
    },
    {
      "tab_numero": 3,
      "descripcion": "Acometida",
      "foto_base64": "iVBORw0KGgoAAAANSUhEUgAA..."
    }
  ]
}
```

**Response 200:**
```json
{
  "success": true,
  "message": "Revisión recibida correctamente",
  "data": {
    "id": "REV-2025-001",
    "estado": "PROCESADA",
    "pdf_url": "https://[servidor]/storage/revisiones/REV-2025-001.pdf",
    "fecha_recepcion": "2025-11-25 09:20:15"
  }
}
```

**Response 422 - Validación:**
```json
{
  "success": false,
  "message": "Errores de validación",
  "errors": {
    "lectura_actual": ["El campo lectura actual es requerido"],
    "nombre_residente": ["El campo nombre residente es requerido"]
  }
}
```

---

### 5. **OBTENER CAUSAS DE DESVIACIÓN**

```http
GET /revisiones/causas
```

**Headers:**
```
Authorization: Bearer {api_token}
```

**Query Parameters:**
- `tipo` (opcional): ALTO, BAJO

**Response 200:**
```json
{
  "success": true,
  "data": [
    {
      "codigo": 1,
      "tipo": "ALTO",
      "descripcion": "Fuga interna en sanitario"
    },
    {
      "codigo": 2,
      "tipo": "ALTO",
      "descripcion": "Fuga en tubería principal"
    },
    {
      "codigo": 3,
      "tipo": "ALTO",
      "descripcion": "Llave de jardín abierta"
    },
    {
      "codigo": 4,
      "tipo": "ALTO",
      "descripcion": "Filtración en ducha"
    },
    {
      "codigo": 5,
      "tipo": "ALTO",
      "descripcion": "Aumento de habitantes"
    },
    {
      "codigo": 6,
      "tipo": "ALTO",
      "descripcion": "Uso comercial no declarado"
    },
    {
      "codigo": 10,
      "tipo": "BAJO",
      "descripcion": "Vivienda desocupada"
    },
    {
      "codigo": 11,
      "tipo": "BAJO",
      "descripcion": "Predio temporal"
    },
    {
      "codigo": 12,
      "tipo": "BAJO",
      "descripcion": "Disminución de habitantes"
    },
    {
      "codigo": 13,
      "tipo": "BAJO",
      "descripcion": "Medidor averiado"
    }
  ]
}
```

---

### 6. **ACTUALIZAR REVISIÓN** (Reapertura)

```http
PUT /revisiones/{id}
```

**Headers:**
```
Authorization: Bearer {api_token}
Content-Type: application/json
```

**Request Body:** (Mismo formato que enviar revisión)

**Response 200:**
```json
{
  "success": true,
  "message": "Revisión actualizada correctamente",
  "data": {
    "id": "REV-2025-001",
    "cantidad_modificaciones": 1,
    "modificaciones_restantes": 2
  }
}
```

**Response 403 - Límite alcanzado:**
```json
{
  "success": false,
  "message": "Esta revisión ha alcanzado el límite de 3 modificaciones"
}
```

---

### 7. **DESCARGAR ÓRDENES DE LECTURA** (Módulo actual - NO TOCAR)

```http
GET /lecturas/ordenes
```

**Response:** (Mantener formato actual)

---

### 8. **ENVIAR LECTURA COMPLETADA** (Módulo actual - NO TOCAR)

```http
POST /lecturas/enviar
```

**Response:** (Mantener formato actual)

---

## 🔒 CÓDIGOS DE RESPUESTA

| Código | Significado |
|--------|-------------|
| 200    | OK - Solicitud exitosa |
| 201    | Created - Recurso creado |
| 400    | Bad Request - Error en la solicitud |
| 401    | Unauthorized - Token inválido o expirado |
| 403    | Forbidden - Sin permisos |
| 404    | Not Found - Recurso no encontrado |
| 422    | Unprocessable Entity - Errores de validación |
| 500    | Internal Server Error - Error del servidor |

---

## 📝 NOTAS IMPORTANTES

1. **Tipos de Usuario:**
   - `TECNICO`: Acceso al módulo de LECTURAS
   - `REVISOR`: Acceso al módulo de REVISIONES
   - `ADMIN`: Acceso a ambos módulos (futuro)

2. **Firma del Técnico:**
   - Se configura en el panel web del acueducto
   - Se descarga una sola vez al hacer login
   - Se guarda localmente en la app

3. **PDFs:**
   - Se envían en Base64
   - Máximo 10MB por archivo
   - Formato: PDF/A para archivo legal

4. **Fotos:**
   - Se envían en Base64
   - Máximo 5MB por foto
   - Formato recomendado: JPEG con calidad 80%

5. **Reaberturas:**
   - Máximo 3 modificaciones por revisión
   - Se incrementa el contador con cada actualización
   - Después de 3 modificaciones, la revisión es de solo lectura

---

## 🛠️ EJEMPLO DE USO

### Flujo completo en la app:

```
1. Configurar URL del servidor
   → Guardar: https://acueducto-abc.com/api

2. Login
   POST /login
   → Recibir token y tipo de usuario

3. Sincronizar órdenes (si es REVISOR)
   GET /revisiones/ordenes
   → Descargar órdenes pendientes

4. Trabajar offline
   → Capturar datos, fotos, firmas

5. Enviar revisiones completadas
   POST /revisiones/enviar
   → Marcar como enviadas

6. Reabrir si es necesario (máximo 3 veces)
   PUT /revisiones/{id}
```

---

Esta API está diseñada para ser **multi-acueducto** mediante la configuración dinámica de URL en la app.
