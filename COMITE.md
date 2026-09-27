# Guion técnico para el comité — NEXO

## Objetivo de la demostración

Demostrar el flujo completo **Angular → API Spring Boot → PostgreSQL**, la arquitectura hexagonal, el trabajo con Scrum/XP y el control de versiones con las ramas obligatorias.

## Antes de comenzar

- Backend activo: `GET http://localhost:8080/api/health` devuelve `{"status":"UP"}`.
- PostgreSQL activo y base `nexo` disponible.
- En Postman, importar `Backend/postman/NEXO.postman_collection.json` y el entorno local.
- Frontend activo en el puerto 4200 y configurado con la URL de API correcta.
- Si frontend y backend están en computadores distintos, usar la IP LAN del backend, no `localhost`.
- En el computador del backend, permitir el puerto TCP 8080 para redes privadas en el Firewall de Windows.

## Demostración de funcionalidades

### 1. Registro e inicio de sesión seguro (RF-01 y RF-02)

1. Registrar un usuario desde Angular o Postman.
2. Mostrar que el backend devuelve un JWT (`accessToken`).
3. Iniciar sesión con el mismo correo.
4. Explicar que la contraseña se guarda con BCrypt, no en texto plano, y que las rutas privadas requieren `Authorization: Bearer <token>`.

### 2. Gestión de tareas y prioridades (RF-03 y RF-06)

1. Crear una tarea con prioridad `ALTA`.
2. Listarla, editarla y cambiar su estado a `COMPLETADA`.
3. Mostrar la fila correspondiente en PostgreSQL o su respuesta JSON en Postman.

### 3. Recordatorios (RF-05)

1. Crear un recordatorio asociado a la tarea.
2. Listarlo, editarlo y eliminarlo.
3. Explicar que el recordatorio pertenece a una tarea del mismo usuario autenticado.

### 4. Colaboración (RF-08 y RF-09)

1. Registrar una segunda cuenta.
2. Con la primera cuenta, crear un espacio colaborativo.
3. Invitar por correo a la segunda cuenta con rol `MIEMBRO`.
4. Crear una tarea con el `spaceId` del espacio.
5. Con la segunda cuenta, consultar `GET /spaces/{spaceId}/tasks` y mostrar la tarea compartida.

## Arquitectura hexagonal

```text
Angular / Postman
       ↓ HTTP + JSON
Controladores REST (infrastructure/adapters/in/web)
       ↓ puertos de entrada
Servicios de aplicación (application/service)
       ↓ puertos de salida
Adaptadores JPA/JWT (infrastructure/adapters/out)
       ↓
PostgreSQL

domain = modelos y reglas puras; no depende de Spring ni JPA.
```

Muestra las carpetas `domain`, `application` e `infrastructure` para probar la separación.

## Scrum, XP y Git

- Sprint 1: autenticación, tareas, recordatorios y colaboración.
- Sprints 2 y 3: [BACKLOG.md](BACKLOG.md) contiene los requisitos pendientes y su prioridad.
- XP: pruebas unitarias, prueba HTTP de registro, refactorización y CI con GitHub Actions.
- Flujo de ramas obligatorio: `Desarrollo` → `Pre-produccion` → `main` mediante Pull Request.

## Evidencia técnica

- Pruebas backend: `mvn test`.
- Postman: colección versionada en `Backend/postman`.
- Migraciones de base de datos: `Backend/src/main/resources/db/migration`.
- Variables de entorno: `Backend/.env.example`; las credenciales reales están en `.env`, que no se sube a Git.

## Pendiente de interfaz

El backend está listo para las rutas descritas en [FRONTEND_INTEGRATION.md](FRONTEND_INTEGRATION.md). El frontend debe presentar las vistas para autenticación, tareas, recordatorios y espacios colaborativos, y usar un interceptor JWT.
