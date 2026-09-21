# NEXO Backend

API REST para NEXO, construida con Spring Boot, PostgreSQL y arquitectura hexagonal.

## Requisitos

- JDK 21
- Maven 3.9 o superior
- Docker Desktop (para PostgreSQL local)

## Puesta en marcha local

1. Copie `.env.example` a `.env` y reemplace `JWT_SECRET`.
2. Inicie PostgreSQL: `docker compose --env-file .env up -d`.
3. Exporte las variables de `.env` en su terminal.
4. Ejecute: `mvn spring-boot:run`.

Flyway aplicará automáticamente la migración inicial al arrancar. Las credenciales no deben subirse a Git.

## Endpoints iniciales

- `GET /api/health`: verifica que la API está disponible.
- `POST /api/auth/register`: registra un usuario y devuelve un token JWT.
- `POST /api/auth/login`: autentica un usuario y devuelve un token JWT.
- `POST /api/tasks`, `GET /api/tasks`, `GET /api/tasks/{id}`, `PUT /api/tasks/{id}`, `PATCH /api/tasks/{id}/status` y `DELETE /api/tasks/{id}`: CRUD protegido de tareas.
- `POST` y `GET /api/tasks/{taskId}/reminders`, `DELETE /api/reminders/{id}`: recordatorios asociados a una tarea propia.

## Arquitectura

- `domain`: reglas y entidades de negocio, sin Spring ni JPA.
- `application`: casos de uso y puertos de entrada/salida.
- `infrastructure`: controladores HTTP, persistencia JPA y configuración.

## Flujo Git

Se trabaja solo sobre `Desarrollo`. La promoción es `Desarrollo` → `Pre-produccion` → `main` mediante Pull Request.
