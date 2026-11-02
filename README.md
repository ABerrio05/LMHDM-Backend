# NEXO — Backend y guía de integración

NEXO es un gestor colaborativo de tareas y recordatorios. Este repositorio contiene la API REST en Spring Boot; el frontend Angular/PWA vive en el repositorio independiente `LMHDM-Frontend`.

## Ejecución local

1. Dentro de `Backend`, copie `.env.example` como `.env` y complete las credenciales de PostgreSQL y `JWT_SECRET`. El archivo `.env` no se sube a Git.
2. Cree la base `nexo` y el usuario local configurado en `.env`.
3. Con PostgreSQL activo, exporte las variables del archivo `.env` en la terminal y ejecute `mvn spring-boot:run`.
4. Compruebe `GET http://localhost:8080/api/health`.

Flyway crea y versiona el esquema de PostgreSQL. Para pruebas locales sin Docker se puede usar la instalación local de PostgreSQL.

## Arquitectura hexagonal

- `Backend/src/main/java/com/nexo/domain`: modelos y reglas de negocio sin Spring ni JPA.
- `Backend/src/main/java/com/nexo/application`: casos de uso y puertos de entrada/salida.
- `Backend/src/main/java/com/nexo/infrastructure`: controladores REST, adaptadores JPA/PostgreSQL, JWT y configuración.

El flujo es: Angular/Postman → controlador REST → puerto de entrada → servicio de aplicación → puerto de salida → adaptador JPA → PostgreSQL.

## Funcionalidades disponibles

- Registro e inicio de sesión seguro con BCrypt y JWT.
- CRUD de tareas con prioridad y estado.
- CRUD de recordatorios vinculados a tareas, recurrencias y entrega por correo mediante SMTP/Mailpit.
- Notas personales con CRUD protegido por JWT.
- Espacios colaborativos: crear espacio, invitar usuarios registrados por correo, asignar roles y consultar tareas compartidas.
- Planificación con consulta de festivos de Colombia mediante la API externa Nager.Date; la llamada externa ocurre en el backend.
- Auditoría de solicitudes y panel técnico para el usuario desarrollador: consulta de actividad, métricas de notificaciones y administración de cuentas.
- Asistente de planificación: funciona con sugerencias locales y puede conectarse a un proveedor compatible con OpenAI cuando se configuren `AI_ENABLED`, `AI_BASE_URL`, `AI_API_KEY` y `AI_MODEL`.

La colección de demostración está en `Backend/postman/NEXO.postman_collection.json`. Importe también el entorno `NEXO.local.postman_environment.json`.

## Decisiones técnicas y alcance actual

- Se usa **JWT** para autenticar solicitudes REST y **BCrypt** para almacenar contraseñas de forma no reversible.
- Los identificadores de las tablas se manejan como `BIGINT` para que correspondan con `Long` en Java. Flyway conserva las ocho tablas y relaciones requeridas por el modelo de datos.
- Los valores de prioridad, estado y rol se validan como enumeraciones de dominio; no se aceptan valores arbitrarios desde la API.
- La integración remota entre los dos computadores se resuelve temporalmente mediante un túnel HTTPS de ngrok. Las credenciales, JWT y la URL temporal no se versionan.
- La IA externa no se habilita por defecto: se requiere una clave válida de un proveedor compatible. Sin esa clave el asistente conserva sugerencias locales, dejando explícito que no se está usando un modelo remoto.
- Los registros técnicos no incluyen contraseñas, tokens ni cuerpos de formularios. El acceso se limita al rol de desarrollador.

## Evidencia de calidad

- `mvn test` ejecuta pruebas unitarias de dominio y servicios, pruebas de integración HTTP y pruebas de los flujos de recordatorios, auditoría, calendario y recuperación de acceso.
- GitHub Actions ejecuta esas pruebas al enviar cambios a `Desarrollo`, `Pre-produccion` o `main`.
- La colección de Postman tiene aserciones automáticas y puede ejecutarse en orden para demostrar el flujo de autenticación, tareas y recordatorios.

## Servicios de Docker

Desde la raíz del proyecto se puede iniciar el conjunto con `docker compose up --build`.

- Frontend: `http://localhost:4200`.
- API y Swagger: `http://localhost:8080/api/health` y `http://localhost:8080/swagger-ui/index.html`.
- PostgreSQL: puerto local `5433`.
- Mailpit (buzón de pruebas): `http://localhost:8025`.

Las credenciales y variables sensibles se declaran en `.env`; nunca deben incluirse en Git.

## Integración Angular

- Si ambos proyectos se ejecutan en el mismo computador: `http://localhost:8080/api`.
- Si están en equipos ubicados en redes distintas: use una URL HTTPS temporal de ngrok que exponga el backend, seguida de `/api`. Esa URL puede cambiar al reiniciar el túnel, por lo que no se versiona en el repositorio.
- El backend permite solicitudes CORS desde `http://localhost:4200`; se configura con `CORS_ALLOWED_ORIGINS` en `.env`.
- Cuando se use ngrok, el interceptor Angular debe añadir `ngrok-skip-browser-warning: true` a las peticiones, además de `Authorization: Bearer <accessToken>` cuando corresponda.
- Para el repositorio frontend: entre a su carpeta `Frontend`, ejecute `npm install` una sola vez y luego `npm start`. Angular queda disponible por defecto en `http://localhost:4200`.

## Git y Scrum

Se desarrolla únicamente sobre `Desarrollo`. Cuando se valida con pruebas y revisión, se crea Pull Request hacia `Pre-produccion`; tras la validación final se promueve a `main`. El backlog de los próximos sprints está en [BACKLOG.md](BACKLOG.md).
