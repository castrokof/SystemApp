# 📘 DOCUMENTACIÓN API LARAVEL - MÓDULO REVISIONES

## 🎯 **DESCRIPCIÓN GENERAL**

API REST para gestionar el sistema de revisiones por desviaciones de consumo.
Compatible con el módulo Android desarrollado.

**Base URL:** `http://tu-dominio.com/api`
**Autenticación:** Bearer Token (JWT)

---

## 🔐 **AUTENTICACIÓN**

### 1. Login
**Endpoint:** `POST /login`

**Request Body:**
```json
{
  "usuario": "revisor01",
  "password": "password123"
}
```

**Response (Success - 200):**
```json
{
  "success": true,
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
    "usuario": "revisor01",
    "tipo_usuario": "REVISOR",
    "nombre_completo": "Juan Pérez",
    "acueducto": "Acueducto Central",
    "firma_url": "https://dominio.com/storage/firmas/revisor01.png"
  },
  "message": "Login exitoso"
}
```

**Response (Error - 401):**
```json
{
  "success": false,
  "message": "Credenciales incorrectas"
}
```

---

## 👤 **USUARIOS**

### 2. Obtener Firma del Técnico
**Endpoint:** `GET /usuarios/{usuario}/firma`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (Success - 200):**
```json
{
  "success": true,
  "firma_base64": "iVBORw0KGgoAAAANSUhEUgAA...",
  "formato": "PNG",
  "fecha_registro": "2025-11-25 10:30:00"
}
```

**Response (No encontrada - 404):**
```json
{
  "success": false,
  "message": "Firma no registrada para este usuario"
}
```

**Notas:**
- La firma se devuelve codificada en Base64
- Formato PNG recomendado
- La app guarda en cache local

---

## 📋 **REVISIONES**

### 3. Descargar Órdenes de Revisión
**Endpoint:** `GET /revisiones/ordenes`

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
```
?estado=PENDIENTE  (opcional: PENDIENTE, EJECUTADA, PROCESADA)
?fecha_desde=2025-11-01  (opcional)
?fecha_hasta=2025-11-30  (opcional)
```

**Response (Success - 200):**
```json
{
  "success": true,
  "data": [
    {
      "id": "REV-2025-001",
      "ciclo": "202511",
      "categoria_orden": "DESVIACION",
      "tipo_orden": "REVISION",
      "periodo": "2025-11",
      "suscriptor": "SUS001",
      "medidor": "MED12345",
      "direccion": "Calle 123 #45-67",
      "nombre": "María García",
      "apellido": "Rodríguez",
      "consumo_promedio_6_meses": 15,
      "lectura_anterior": "1250",
      "tipo_desviacion": "ALTO",
      "ruta": "RUTA-01",
      "consecutivo_ruta": "001",
      "observacion_inicial": "Consumo elevado detectado"
    },
    {
      "id": "REV-2025-002",
      "ciclo": "202511",
      ...
    }
  ],
  "total": 25,
  "message": "Órdenes descargadas exitosamente"
}
```

---

### 4. Enviar Revisión Completada
**Endpoint:** `POST /revisiones/enviar`

**Headers:**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body (Completo):**
```json
{
  "id": "REV-2025-001",
  "medidor": "MED12345",

  // Tab 1: Lectura
  "lectura_actual": 1350,
  "consumo": 100,

  // Tab 2: Residente
  "nombre_residente": "María García Rodríguez",
  "firma_cliente_base64": "iVBORw0KGgoAAAANSUhEUgAA...",

  // Tab 3: Acometida
  "estado_acometida": "BUENO",
  "estado_sellos": "BUENO",
  "que_surte": "Red pública",

  // Tab 4: Censos
  "censo_poblacional_familiar": 2,
  "censo_poblacional_personas": 5,
  "censo_poblacional_adultos": 3,
  "censo_poblacional_ninos": 2,
  "censo_hidraulico": [
    {
      "elemento": "SANITARIO",
      "cantidad": 2,
      "estado": "BUENO",
      "foto_base64": "iVBORw0KGgoAAAANSUhEUgAA..."
    },
    {
      "elemento": "LAVAMANOS",
      "cantidad": 2,
      "estado": "BUENO"
    },
    {
      "elemento": "DUCHA",
      "cantidad": 1,
      "estado": "MALO",
      "foto_base64": "iVBORw0KGgoAAAANSUhEUgAA..."
    }
  ],

  // Tab 5: Clasificación
  "codigo_causa": 1,
  "desc_causa": "Fuga interna en sanitario",
  "observacion_causa": "Sanitario presenta fuga en flotador",

  // Tab 6: Observación General
  "observacion_general": "Se recomienda reparación urgente del sanitario",

  // Fechas y ubicación
  "fecha_inicio": "2025-11-27 08:30:00",
  "fecha_cierre": "2025-11-27 09:45:00",
  "latitud": "6.244203",
  "longitud": "-75.581212",

  // PDF completo
  "pdf_base64": "JVBERi0xLjQKJeLjz9MKMy..."
}
```

**Response (Success - 200):**
```json
{
  "success": true,
  "data": {
    "id": "REV-2025-001",
    "estado": "PROCESADA",
    "fecha_procesamiento": "2025-11-27 10:00:00",
    "pdf_url": "https://dominio.com/storage/revisiones/REV-2025-001.pdf"
  },
  "message": "Revisión procesada exitosamente"
}
```

**Response (Error - 422):**
```json
{
  "success": false,
  "errors": {
    "lectura_actual": ["El campo lectura actual es obligatorio"],
    "nombre_residente": ["El campo nombre residente es obligatorio"]
  },
  "message": "Datos de validación incorrectos"
}
```

---

## 🗂️ **CATÁLOGOS**

### 5. Obtener Causas de Desviación
**Endpoint:** `GET /revisiones/causas`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (Success - 200):**
```json
{
  "success": true,
  "data": [
    {
      "codigo": 1,
      "descripcion": "Fuga interna en sanitario",
      "tipo_desviacion": "ALTO",
      "prioridad": "ALTA"
    },
    {
      "codigo": 2,
      "descripcion": "Fuga externa en acometida",
      "tipo_desviacion": "ALTO",
      "prioridad": "ALTA"
    },
    {
      "codigo": 3,
      "descripcion": "Aumento de habitantes",
      "tipo_desviacion": "ALTO",
      "prioridad": "MEDIA"
    },
    {
      "codigo": 10,
      "descripcion": "Predio desocupado",
      "tipo_desviacion": "BAJO",
      "prioridad": "BAJA"
    }
  ]
}
```

---

## 📊 **REPORTES Y ESTADÍSTICAS**

### 6. Obtener Estadísticas del Técnico
**Endpoint:** `GET /revisiones/estadisticas`

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
```
?fecha_desde=2025-11-01
?fecha_hasta=2025-11-30
```

**Response (Success - 200):**
```json
{
  "success": true,
  "data": {
    "pendientes": 15,
    "ejecutadas": 25,
    "procesadas": 20,
    "total": 60,
    "porcentaje_completado": 75.0,
    "promedio_tiempo_minutos": 45,
    "causas_mas_frecuentes": [
      {
        "causa": "Fuga interna en sanitario",
        "cantidad": 12
      },
      {
        "causa": "Aumento de habitantes",
        "cantidad": 8
      }
    ]
  }
}
```

---

## 🗃️ **ESTRUCTURA DE BASE DE DATOS (SUGERIDA)**

### Tabla: `revisiones`
```sql
CREATE TABLE revisiones (
    id VARCHAR(50) PRIMARY KEY,
    ciclo VARCHAR(10),
    categoria_orden VARCHAR(50),
    tipo_orden VARCHAR(50),
    periodo VARCHAR(10),
    suscriptor VARCHAR(50),
    medidor VARCHAR(50),
    direccion TEXT,
    nombre VARCHAR(200),
    apellido VARCHAR(200),
    consumo_promedio_6_meses INT,
    lectura_anterior VARCHAR(20),
    tipo_desviacion ENUM('ALTO', 'BAJO'),
    ruta VARCHAR(50),
    consecutivo_ruta VARCHAR(10),
    observacion_inicial TEXT,

    -- Datos de ejecución
    tecnico_id INT,
    lectura_actual INT,
    consumo INT,
    nombre_residente VARCHAR(200),
    firma_cliente_path VARCHAR(255),

    estado_acometida VARCHAR(50),
    estado_sellos VARCHAR(50),
    que_surte VARCHAR(100),

    censo_poblacional_familiar INT,
    censo_poblacional_personas INT,
    censo_poblacional_adultos INT,
    censo_poblacional_ninos INT,

    codigo_causa INT,
    desc_causa VARCHAR(200),
    observacion_causa TEXT,
    observacion_general TEXT,

    fecha_inicio DATETIME,
    fecha_cierre DATETIME,
    latitud DECIMAL(10, 6),
    longitud DECIMAL(10, 6),

    pdf_path VARCHAR(255),
    estado ENUM('PENDIENTE', 'EJECUTADA', 'PROCESADA') DEFAULT 'PENDIENTE',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tecnico_id) REFERENCES users(id),
    INDEX idx_medidor (medidor),
    INDEX idx_estado (estado),
    INDEX idx_fecha_cierre (fecha_cierre)
);
```

### Tabla: `censo_hidraulico`
```sql
CREATE TABLE censo_hidraulico (
    id INT AUTO_INCREMENT PRIMARY KEY,
    revision_id VARCHAR(50),
    elemento ENUM('SANITARIO', 'LAVAMANOS', 'DUCHA', 'LAVADERO', 'LLAVE_COCINA', 'TANQUE', 'PISCINA', 'OTRO'),
    cantidad INT,
    estado ENUM('BUENO', 'MALO'),
    foto_path VARCHAR(255),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (revision_id) REFERENCES revisiones(id) ON DELETE CASCADE
);
```

### Tabla: `causas_desviacion`
```sql
CREATE TABLE causas_desviacion (
    codigo INT PRIMARY KEY,
    descripcion VARCHAR(200),
    tipo_desviacion ENUM('ALTO', 'BAJO'),
    prioridad ENUM('ALTA', 'MEDIA', 'BAJA'),
    activo BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Tabla: `users`
```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) UNIQUE,
    password VARCHAR(255),
    nombre_completo VARCHAR(200),
    tipo_usuario ENUM('ADMIN', 'REVISOR', 'TECNICO'),
    acueducto VARCHAR(100),
    firma_path VARCHAR(255),
    activo BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

## 🔑 **CÓDIGOS DE ESTADO HTTP**

- `200 OK` - Solicitud exitosa
- `201 Created` - Recurso creado exitosamente
- `400 Bad Request` - Solicitud inválida
- `401 Unauthorized` - No autenticado
- `403 Forbidden` - No autorizado
- `404 Not Found` - Recurso no encontrado
- `422 Unprocessable Entity` - Error de validación
- `500 Internal Server Error` - Error del servidor

---

## 📝 **NOTAS IMPORTANTES**

### Archivos Base64
- **Firmas:** PNG, tamaño máximo 200KB
- **Fotos:** JPG, tamaño máximo 500KB cada una
- **PDF:** Tamaño máximo 5MB

### Ubicación GPS
- Formato: Decimal (6 decimales)
- Ejemplo: `latitud: 6.244203`, `longitud: -75.581212`
- Se puede generar URL de Google Maps: `https://maps.google.com/?q=6.244203,-75.581212`

### Sincronización
- La app intenta descargar órdenes periódicamente
- Las revisiones se envían cuando hay conexión
- El estado PROCESADA indica que el servidor recibió y procesó la revisión

### Seguridad
- Todos los endpoints (excepto login) requieren autenticación
- El token debe incluirse en el header: `Authorization: Bearer {token}`
- Los tokens deben tener expiración (recomendado: 24 horas)

---

## 🧪 **EJEMPLOS DE IMPLEMENTACIÓN (LARAVEL)**

### Controller: RevisionController.php

```php
<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Revision;
use App\Models\CensoHidraulico;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\Validator;

class RevisionController extends Controller
{
    /**
     * Descargar órdenes de revisión
     */
    public function getOrdenes(Request $request)
    {
        $tecnicoId = auth()->user()->id;

        $ordenes = Revision::where('tecnico_id', $tecnicoId)
            ->where('estado', 'PENDIENTE')
            ->orderBy('consecutivo_ruta')
            ->get();

        return response()->json([
            'success' => true,
            'data' => $ordenes,
            'total' => $ordenes->count(),
            'message' => 'Órdenes descargadas exitosamente'
        ]);
    }

    /**
     * Recibir revisión completada
     */
    public function enviarRevision(Request $request)
    {
        // Validar datos
        $validator = Validator::make($request->all(), [
            'id' => 'required|exists:revisiones,id',
            'lectura_actual' => 'required|integer',
            'nombre_residente' => 'required|string|max:200',
            'latitud' => 'nullable|numeric',
            'longitud' => 'nullable|numeric',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'errors' => $validator->errors(),
                'message' => 'Datos de validación incorrectos'
            ], 422);
        }

        // Buscar revisión
        $revision = Revision::find($request->id);

        // Actualizar datos
        $revision->lectura_actual = $request->lectura_actual;
        $revision->consumo = $request->consumo;
        $revision->nombre_residente = $request->nombre_residente;

        // Guardar firma del cliente
        if ($request->has('firma_cliente_base64')) {
            $firmaPath = $this->guardarBase64Imagen(
                $request->firma_cliente_base64,
                'firmas/clientes',
                $request->id . '_cliente.png'
            );
            $revision->firma_cliente_path = $firmaPath;
        }

        // Guardar datos de acometida
        $revision->estado_acometida = $request->estado_acometida;
        $revision->estado_sellos = $request->estado_sellos;
        $revision->que_surte = $request->que_surte;

        // Censos poblacionales
        $revision->censo_poblacional_familiar = $request->censo_poblacional_familiar;
        $revision->censo_poblacional_personas = $request->censo_poblacional_personas;
        $revision->censo_poblacional_adultos = $request->censo_poblacional_adultos;
        $revision->censo_poblacional_ninos = $request->censo_poblacional_ninos;

        // Censo hidráulico
        if ($request->has('censo_hidraulico')) {
            foreach ($request->censo_hidraulico as $censo) {
                $censoModel = new CensoHidraulico();
                $censoModel->revision_id = $request->id;
                $censoModel->elemento = $censo['elemento'];
                $censoModel->cantidad = $censo['cantidad'];
                $censoModel->estado = $censo['estado'];

                // Guardar foto si existe
                if (isset($censo['foto_base64'])) {
                    $fotoPath = $this->guardarBase64Imagen(
                        $censo['foto_base64'],
                        'fotos/censos',
                        $request->id . '_' . $censo['elemento'] . '.jpg'
                    );
                    $censoModel->foto_path = $fotoPath;
                }

                $censoModel->save();
            }
        }

        // Clasificación
        $revision->codigo_causa = $request->codigo_causa;
        $revision->desc_causa = $request->desc_causa;
        $revision->observacion_causa = $request->observacion_causa;

        // Observación general
        $revision->observacion_general = $request->observacion_general;

        // Fechas y ubicación
        $revision->fecha_inicio = $request->fecha_inicio;
        $revision->fecha_cierre = $request->fecha_cierre;
        $revision->latitud = $request->latitud;
        $revision->longitud = $request->longitud;

        // Guardar PDF
        if ($request->has('pdf_base64')) {
            $pdfPath = $this->guardarBase64PDF(
                $request->pdf_base64,
                'pdfs/revisiones',
                $request->id . '.pdf'
            );
            $revision->pdf_path = $pdfPath;
        }

        // Marcar como procesada
        $revision->estado = 'PROCESADA';
        $revision->save();

        return response()->json([
            'success' => true,
            'data' => [
                'id' => $revision->id,
                'estado' => $revision->estado,
                'fecha_procesamiento' => now(),
                'pdf_url' => Storage::url($revision->pdf_path)
            ],
            'message' => 'Revisión procesada exitosamente'
        ]);
    }

    /**
     * Guardar imagen desde Base64
     */
    private function guardarBase64Imagen($base64, $carpeta, $nombreArchivo)
    {
        $imageData = base64_decode($base64);
        $path = $carpeta . '/' . $nombreArchivo;
        Storage::disk('public')->put($path, $imageData);
        return $path;
    }

    /**
     * Guardar PDF desde Base64
     */
    private function guardarBase64PDF($base64, $carpeta, $nombreArchivo)
    {
        $pdfData = base64_decode($base64);
        $path = $carpeta . '/' . $nombreArchivo;
        Storage::disk('public')->put($path, $pdfData);
        return $path;
    }
}
```

### Routes: api.php

```php
<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\RevisionController;
use App\Http\Controllers\UserController;

// Autenticación
Route::post('/login', [AuthController::class, 'login']);

// Rutas protegidas
Route::middleware('auth:sanctum')->group(function () {

    // Usuarios
    Route::get('/usuarios/{usuario}/firma', [UserController::class, 'getFirma']);

    // Revisiones
    Route::get('/revisiones/ordenes', [RevisionController::class, 'getOrdenes']);
    Route::post('/revisiones/enviar', [RevisionController::class, 'enviarRevision']);
    Route::get('/revisiones/causas', [RevisionController::class, 'getCausas']);
    Route::get('/revisiones/estadisticas', [RevisionController::class, 'getEstadisticas']);
});
```

---

## ✅ **CHECKLIST DE IMPLEMENTACIÓN**

- [ ] Instalar Laravel 8+
- [ ] Configurar base de datos MySQL
- [ ] Crear migraciones para tablas
- [ ] Implementar autenticación con Sanctum/JWT
- [ ] Crear modelos (Revision, CensoHidraulico, Causa, User)
- [ ] Implementar RevisionController
- [ ] Implementar UserController
- [ ] Configurar rutas en api.php
- [ ] Configurar storage para archivos
- [ ] Implementar middleware de autenticación
- [ ] Crear seeders para datos de prueba
- [ ] Probar endpoints con Postman/Insomnia
- [ ] Configurar CORS para permitir requests desde Android
- [ ] Optimizar consultas con índices
- [ ] Implementar logs de auditoría
- [ ] Configurar backups automáticos

---

**Fecha de documentación:** 27/11/2025
**Versión Android compatible:** 1.0
**Estado:** ✅ Listo para implementación
