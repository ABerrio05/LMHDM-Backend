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

## Funcionalidades disponibles para el comité

- Registro e inicio de sesión seguro con BCrypt y JWT.
- CRUD de tareas con prioridad y estado.
- CRUD de recordatorios vinculados a tareas.
- Espacios colaborativos: crear espacio, invitar usuarios registrados por correo, asignar roles y consultar tareas compartidas.

La colección de demostración está en `Backend/postman/NEXO.postman_collection.json`. Importe también el entorno `NEXO.local.postman_environment.json`.

## Decisiones técnicas y alcance actual

- Se usa **JWT** para autenticar solicitudes REST y **BCrypt** para almacenar contraseñas de forma no reversible.
- Los identificadores de las tablas se manejan como `BIGINT` para que correspondan con `Long` en Java. Flyway conserva las ocho tablas y relaciones requeridas por el modelo de datos.
- Los valores de prioridad, estado y rol se validan como enumeraciones de dominio; no se aceptan valores arbitrarios desde la API.
- La integración remota entre los dos computadores se resuelve temporalmente mediante un túnel HTTPS de ngrok. Las credenciales, JWT y la URL temporal no se versionan.
- En este primer incremento se completaron autenticación, tareas, recordatorios y colaboración. Notas, recurrencias, envío real de notificaciones y panel administrativo permanecen priorizados en [BACKLOG.md](BACKLOG.md) para los siguientes sprints.

## Evidencia de calidad

- `mvn test` ejecuta pruebas unitarias de dominio y servicios, y una prueba HTTP de integración del registro.
- GitHub Actions ejecuta esas pruebas al enviar cambios a `Desarrollo`, `Pre-produccion` o `main`.
- La colección de Postman tiene aserciones automáticas y puede ejecutarse en orden para demostrar el flujo de autenticación, tareas y recordatorios.

## Integración Angular

- Si ambos proyectos se ejecutan en el mismo computador: `http://localhost:8080/api`.
- Si están en equipos ubicados en redes distintas: use una URL HTTPS temporal de ngrok que exponga el backend, seguida de `/api`. Esa URL puede cambiar al reiniciar el túnel, por lo que no se versiona en el repositorio.
- El backend permite solicitudes CORS desde `http://localhost:4200`; se configura con `CORS_ALLOWED_ORIGINS` en `.env`.
- Cuando se use ngrok, el interceptor Angular debe añadir `ngrok-skip-browser-warning: true` a las peticiones, además de `Authorization: Bearer <accessToken>` cuando corresponda.
- Para el repositorio frontend: entre a su carpeta `Frontend`, ejecute `npm install` una sola vez y luego `npm start`. Angular queda disponible por defecto en `http://localhost:4200`.

## Git y Scrum

Se desarrolla únicamente sobre `Desarrollo`. Cuando se valida con pruebas y revisión, se crea Pull Request hacia `Pre-produccion`; tras la validación final se promueve a `main`. El backlog de los próximos sprints está en [BACKLOG.md](BACKLOG.md).
