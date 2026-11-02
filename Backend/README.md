# NEXO Backend

API REST para NEXO, construida con Spring Boot, PostgreSQL y arquitectura hexagonal.

## Requisitos

- JDK 21
- Maven 3.9 o superior
- PostgreSQL 14 o superior (local o mediante Docker)

## Puesta en marcha local

1. Copie `.env.example` a `.env` y reemplace `JWT_SECRET`.
2. Para iniciar solo la base de datos localmente, ejecute: `docker compose --env-file .env up -d postgres`.
3. Ejecute: `mvn spring-boot:run`.

## Ejecución con Docker

Este directorio incluye un `Dockerfile` multi-etapa y un `docker-compose.yml` para ejecutar PostgreSQL y el backend sin instalar Maven ni Java en el host:

```powershell
Copy-Item .env.example .env
# Actualiza DB_PASSWORD y JWT_SECRET en .env.
docker compose up --build
```

La API estará disponible en `http://localhost:8080/api/health`. Para levantar también el frontend junto con la base de datos y el backend, usa el `docker-compose.yml` ubicado en la raíz del proyecto NEXO.

Spring Boot carga automáticamente las variables locales de `.env`; no hace falta exportarlas manualmente en la terminal. Flyway aplicará automáticamente la migración inicial al arrancar. Las credenciales no deben subirse a Git.

La API permite solicitudes CORS desde `http://localhost:4200` por defecto. Para otro origen local, cambie `CORS_ALLOWED_ORIGINS` en su archivo `.env`.

## Endpoints iniciales

- `GET /api/health`: verifica que la API está disponible.
- `POST /api/auth/register`: registra un usuario y devuelve un token JWT.
- `POST /api/auth/login`: autentica un usuario y devuelve un token JWT.
- `POST /api/tasks`, `GET /api/tasks`, `GET /api/tasks/{id}`, `PUT /api/tasks/{id}`, `PATCH /api/tasks/{id}/status` y `DELETE /api/tasks/{id}`: CRUD protegido de tareas. Al crear una tarea se puede indicar una recurrencia diaria, semanal o personalizada; al completarla se genera la siguiente ocurrencia dentro del límite configurado.
- `POST` y `GET /api/tasks/{taskId}/reminders`, `PUT` y `DELETE /api/reminders/{id}`: CRUD de recordatorios asociados a una tarea propia.
- `POST` y `GET /api/spaces`, `GET /api/spaces/{id}`, `POST` y `GET /api/spaces/{id}/members`, `GET /api/spaces/{id}/tasks`: espacios colaborativos, invitaciones por correo y tareas compartidas.
- `GET /api/calendar/holidays/colombia`: consulta los próximos festivos de Colombia mediante la API externa Nager.Date. La llamada se hace desde el backend y mantiene una caché temporal para evitar solicitudes repetidas.
- `POST /api/assistant/suggestions`: genera una recomendación con IA usando únicamente el contexto de las tareas pendientes del usuario autenticado.
- `GET /api/developer/audit-logs`: consulta de auditoría exclusiva para el usuario configurado como desarrollador. Los registros no exponen contraseñas, tokens ni cuerpos de formularios.

## Configuración de funciones externas

### Asistente de IA

El asistente está desactivado por defecto para que la aplicación pueda iniciar sin una clave de un proveedor externo. Para habilitarlo, completa estas variables únicamente en `.env`:

```text
AI_ENABLED=true
AI_BASE_URL=https://api.openai.com/v1
AI_API_KEY=tu_clave_del_proveedor
AI_MODEL=gpt-4o-mini
```

El proveedor debe ser compatible con el endpoint `POST /chat/completions`. La clave se usa solo dentro del backend y nunca se entrega al navegador.

### Notificaciones de recordatorios por correo

Los recordatorios se revisan periódicamente y cada entrega queda registrada para impedir correos duplicados. Para enviar correos reales, configura un proveedor SMTP en `.env`:

```text
EMAIL_ENABLED=true
EMAIL_FROM=notificaciones@tu-dominio.com
MAIL_HOST=smtp.tu-proveedor.com
MAIL_PORT=587
MAIL_USERNAME=tu_usuario_smtp
MAIL_PASSWORD=tu_app_password
```

Con `EMAIL_ENABLED=false`, el sistema conserva el comportamiento local y no intenta enviar mensajes. Los datos SMTP y la clave de IA son secretos: no se incluyen en Git ni en el frontend.

### Auditoría para desarrollador

Define `APP_DEVELOPER_EMAIL` con la cuenta que administrará la plataforma. Al registrarse o iniciar sesión con ese correo, el backend asigna el rol `DEVELOPER`; el resto de usuarios conserva el rol `USER` y no puede acceder a los logs. La interfaz correspondiente está en `/developer/logs`.

## Documentación Swagger / OpenAPI

Con el backend iniciado, abre la documentación interactiva en:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger agrupa las rutas por funcionalidad y permite probarlas. Para los grupos protegidos, primero ejecuta **Iniciar sesión**, copia el valor de `accessToken` y pégalo en **Authorize** como token Bearer. La especificación JSON está disponible en `GET /v3/api-docs`.

## Pruebas con Postman

1. Importe `postman/NEXO.postman_collection.json` y `postman/NEXO.local.postman_environment.json` en Postman.
2. Seleccione el entorno **NEXO Local**.
3. Cambie el valor de `email` por uno que no esté registrado aún.
4. Con la API y PostgreSQL levantados, ejecute en orden: **Health**, **Registrar usuario** (o **Iniciar sesión**), **Crear tarea** y el resto de solicitudes.

La colección guarda automáticamente `accessToken`, `taskId` y `reminderId` para las solicitudes siguientes. No suba valores reales de token o contraseñas al repositorio.

Para revisar casos de error, ejecute la carpeta **Errores esperados**: un correo registrado responde `409`, una contraseña incorrecta al iniciar sesión responde `401` y los campos inválidos responden `400` con el detalle del campo que se debe corregir.

## Arquitectura

- `domain`: reglas y entidades de negocio, sin Spring ni JPA.
- `application`: casos de uso y puertos de entrada/salida.
- `infrastructure`: controladores HTTP, persistencia JPA y configuración.

## Flujo Git

Se trabaja solo sobre `Desarrollo`. La promoción es `Desarrollo` → `Pre-produccion` → `main` mediante Pull Request.
