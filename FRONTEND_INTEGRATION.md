# Contrato de integración para Angular

## Base URL

- Mismo computador: `http://localhost:8080/api`
- Computadores distintos en la misma red: `http://192.168.1.4:8080/api` mientras el backend se ejecute en ese equipo.

El frontend debe conservar su origen `http://localhost:4200`. En cada petición protegida envía:

```http
Authorization: Bearer <accessToken>
```

El token llega en `accessToken` desde registro e inicio de sesión.

## Autenticación

| Método y ruta | Cuerpo |
|---|---|
| `POST /auth/register` | `{ "name", "email", "password" }` |
| `POST /auth/login` | `{ "email", "password" }` |

Ambas respuestas incluyen `{ userId, name, email, accessToken }`.

## Tareas

- `POST /tasks`: `{ title, description, dueDate, priority, spaceId? }`
- `GET /tasks`
- `GET /tasks/{taskId}`
- `PUT /tasks/{taskId}`: `{ title, description, dueDate, priority }`
- `PATCH /tasks/{taskId}/status`: `{ status }`
- `DELETE /tasks/{taskId}`

Valores válidos: `priority`: `ALTA`, `MEDIA`, `BAJA`; `status`: `PENDIENTE`, `EN_PROGRESO`, `COMPLETADA`.

## Recordatorios

- `POST /tasks/{taskId}/reminders`: `{ scheduledAt, message }`
- `GET /tasks/{taskId}/reminders`
- `PUT /reminders/{reminderId}`: `{ scheduledAt, message }`
- `DELETE /reminders/{reminderId}`

## Espacios colaborativos

- `POST /spaces`: `{ name, description }`
- `GET /spaces`
- `GET /spaces/{spaceId}`
- `POST /spaces/{spaceId}/members`: `{ email, role }`
- `GET /spaces/{spaceId}/members`
- `GET /spaces/{spaceId}/tasks`

Para crear una tarea compartida, usa `POST /tasks` e incluye el `spaceId`. Solo el administrador del espacio puede invitar usuarios. El usuario invitado debe estar registrado primero. Roles válidos: `ADMINISTRADOR`, `MODERADOR`, `MIEMBRO`, `INVITADO`.

## Trabajo solicitado al frontend

1. Configurar `apiUrl` según si ejecuta Angular en su equipo o en el mismo equipo del backend.
2. Guardar el JWT tras login/register e instalar un interceptor que añada el encabezado `Authorization`.
3. Crear vistas de registro/login, listado y formulario de tareas, recordatorios y espacios colaborativos.
4. Mostrar respuestas de error del backend: `{ "message": "..." }`.
5. Para la demo, preparar dos cuentas: una crea el espacio y otra acepta/consulta las tareas compartidas.
