# 🚀 Backend API - SystemApp

Backend Laravel para la aplicación móvil SystemApp (Lecturas y Revisiones de Acueducto)

---

## 📋 Requisitos

- PHP >= 8.1
- Composer
- MySQL >= 8.0 o MariaDB >= 10.3
- Extensiones PHP: PDO, mbstring, OpenSSL, JSON, Tokenizer, XML

---

## ⚙️ Instalación

### 1. Instalar dependencias

```bash
composer install
```

### 2. Configurar variables de entorno

```bash
cp .env.example .env
php artisan key:generate
```

Editar `.env` y configurar la base de datos:

```env
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=systemapp
DB_USERNAME=root
DB_PASSWORD=tu_password
```

### 3. Crear la base de datos

```sql
CREATE DATABASE systemapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. Ejecutar migraciones

```bash
php artisan migrate
```

### 5. Ejecutar seeders (datos de prueba)

```bash
php artisan db:seed
```

Esto creará:
- ✅ 5 usuarios (2 técnicos, 2 revisores, 1 admin)
- ✅ 4 órdenes de revisión de ejemplo
- ✅ 16 causas de desviación predefinidas

### 6. Configurar storage

```bash
php artisan storage:link
```

### 7. Iniciar servidor de desarrollo

```bash
php artisan serve
```

La API estará disponible en: `http://127.0.0.1:8000/api`

---

## 👤 Usuarios de Prueba

### Técnicos de Lecturas:
- **Usuario:** `tecnico01` | **Clave:** `password123`
- **Usuario:** `tecnico02` | **Clave:** `password123`

### Técnicos de Revisiones:
- **Usuario:** `revisor01` | **Clave:** `password123`
- **Usuario:** `revisor02` | **Clave:** `password123`

### Administrador:
- **Usuario:** `admin` | **Clave:** `admin123`

---

## 📡 Endpoints Principales

### Público (sin autenticación):

```http
GET  /api/ping          # Verificar estado de la API
POST /api/login         # Iniciar sesión
```

### Protegido (requiere Bearer Token):

```http
# Revisiones
GET  /api/revisiones/ordenes      # Descargar órdenes
POST /api/revisiones/enviar       # Enviar revisión completada
PUT  /api/revisiones/{id}         # Actualizar revisión
GET  /api/revisiones/causas       # Obtener causas de desviación

# Autenticación
POST /api/logout                  # Cerrar sesión
```

Ver documentación completa en: `API_REVISIONES_DOCUMENTATION.md`

---

## 🧪 Probar la API

### 1. Ping

```bash
curl http://localhost:8000/api/ping
```

### 2. Login

```bash
curl -X POST http://localhost:8000/api/login \
  -H "Content-Type: application/json" \
  -d '{
    "usuario": "revisor01",
    "clave": "password123"
  }'
```

Respuesta:
```json
{
  "success": true,
  "data": {
    "id": "3",
    "usuario": "revisor01",
    "nombre": "Carlos Rodríguez",
    "tipodeusuario": "REVISOR",
    "api_token": "eyJ0eXAiOiJKV1Q..."
  }
}
```

### 3. Obtener órdenes (usar el token recibido)

```bash
curl http://localhost:8000/api/revisiones/ordenes \
  -H "Authorization: Bearer eyJ0eXAiOiJKV1Q..."
```

---

## 📁 Estructura del Proyecto

```
backend_laravel/
├── app/
│   ├── Http/
│   │   ├── Controllers/
│   │   │   ├── AuthController.php
│   │   │   └── RevisionController.php
│   │   └── Middleware/
│   │       └── ApiTokenAuth.php
│   └── Models/
│       ├── User.php
│       ├── Revision.php
│       ├── CensoHidraulico.php
│       ├── FotoRevision.php
│       └── CausaDesviacion.php
├── database/
│   ├── migrations/
│   │   ├── create_users_table.php
│   │   ├── create_revisiones_table.php
│   │   ├── create_censo_hidraulico_table.php
│   │   ├── create_fotos_revision_table.php
│   │   └── create_causas_desviacion_table.php
│   └── seeders/
│       ├── UserSeeder.php
│       ├── RevisionSeeder.php
│       └── DatabaseSeeder.php
├── routes/
│   └── api.php
├── config/
│   └── cors.php
└── bootstrap/
    └── app.php
```

---

## 🔒 Seguridad

1. **Autenticación:** Token Bearer en header `Authorization`
2. **CORS:** Configurado para aceptar todas las peticiones (ajustar en producción)
3. **Passwords:** Hasheados con bcrypt
4. **Validación:** Laravel Request Validation en todos los endpoints

---

## 🚀 Despliegue en Producción

### 1. Optimizar

```bash
php artisan config:cache
php artisan route:cache
php artisan view:cache
composer install --optimize-autoloader --no-dev
```

### 2. Configurar permisos

```bash
chmod -R 775 storage bootstrap/cache
chown -R www-data:www-data storage bootstrap/cache
```

### 3. Configurar CORS

Editar `config/cors.php` y especificar los dominios permitidos:

```php
'allowed_origins' => [
    'https://miapp.com',
    'https://otrodominio.com',
],
```

### 4. HTTPS

Asegurar que todas las peticiones sean por HTTPS.

---

## 📝 Notas Importantes

- **Firmas de Técnicos:** Se deben subir desde el panel web y se guardan en `storage/app/public/firmas/`
- **PDFs de Revisiones:** Se guardan en `storage/app/public/revisiones_pdf/`
- **Fotos:** Se guardan en `storage/app/public/censo_fotos/` y `storage/app/public/fotos_revision/`
- **Límite de Modificaciones:** Cada revisión puede ser modificada máximo 3 veces

---

## 🐛 Troubleshooting

### Error: "Base table or view not found"
```bash
php artisan migrate:fresh --seed
```

### Error: "The stream or file could not be opened"
```bash
chmod -R 775 storage bootstrap/cache
```

### Error: "No application encryption key"
```bash
php artisan key:generate
```

---

## 📚 Documentación Adicional

- Ver `../API_REVISIONES_DOCUMENTATION.md` para documentación completa de la API
- Laravel Docs: https://laravel.com/docs

---

**Desarrollado para SystemApp - Gestión de Acueductos**
