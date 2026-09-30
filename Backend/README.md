# NEXO Backend

API REST para NEXO, construida con Spring Boot, PostgreSQL y arquitectura hexagonal.

## Requisitos

- JDK 21
- Maven 3.9 o superior
- PostgreSQL 14 o superior (local o mediante Docker)

## Puesta en marcha local

1. Copie `.env.example` a `.env` y reemplace `JWT_SECRET`.
2. Inicie PostgreSQL local o, si Docker está disponible: `docker compose --env-file .env up -d`.
3. Ejecute: `mvn spring-boot:run`.

Spring Boot carga automáticamente las variables locales de `.env`; no hace falta exportarlas manualmente en la terminal. Flyway aplicará automáticamente la migración inicial al arrancar. Las credenciales no deben subirse a Git.

La API permite solicitudes CORS desde `http://localhost:4200` por defecto. Para otro origen local, cambie `CORS_ALLOWED_ORIGINS` en su archivo `.env`.

## Endpoints iniciales

- `GET /api/health`: verifica que la API está disponible.
- `POST /api/auth/register`: registra un usuario y devuelve un token JWT.
- `POST /api/auth/login`: autentica un usuario y devuelve un token JWT.
- `POST /api/tasks`, `GET /api/tasks`, `GET /api/tasks/{id}`, `PUT /api/tasks/{id}`, `PATCH /api/tasks/{id}/status` y `DELETE /api/tasks/{id}`: CRUD protegido de tareas.
- `POST` y `GET /api/tasks/{taskId}/reminders`, `PUT` y `DELETE /api/reminders/{id}`: CRUD de recordatorios asociados a una tarea propia.
- `POST` y `GET /api/spaces`, `GET /api/spaces/{id}`, `POST` y `GET /api/spaces/{id}/members`, `GET /api/spaces/{id}/tasks`: espacios colaborativos, invitaciones por correo y tareas compartidas.

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
