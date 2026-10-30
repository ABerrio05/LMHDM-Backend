# Calidad de arquitectura: SOLID y 12 factores

Este documento describe el estado real de NEXO al cierre de la iteración. No presenta como terminados los elementos que todavía requieren una plataforma de producción.

## Principios SOLID

| Principio | Evidencia en NEXO | Justificación |
| --- | --- | --- |
| Responsabilidad única | Los controladores HTTP, servicios de aplicación, puertos, adaptadores de persistencia y filtros de seguridad están separados. Por ejemplo, `TaskManagementService` gestiona reglas de tareas y `RequestLoggingFilter` únicamente registra metadatos de solicitudes. | Cambiar una regla de tareas no obliga a cambiar JPA, JWT ni HTTP. |
| Abierto/cerrado | Los servicios trabajan contra puertos como `TaskRepositoryPort`, `UserRepositoryPort`, `AuditLogRepositoryPort` y `TokenProviderPort`. | Se puede incorporar una implementación alternativa de persistencia, auditoría o token sin alterar las reglas de negocio. |
| Sustitución de Liskov | Los adaptadores de persistencia implementan los contratos de sus puertos y las pruebas unitarias usan implementaciones de prueba que respetan los mismos contratos. | El comportamiento del servicio se mantiene al sustituir PostgreSQL/JPA por un doble de prueba. |
| Segregación de interfaces | Cada puerto expresa una necesidad puntual: usuarios, tareas, auditoría, hash de contraseña, token o calendario. | Ningún adaptador debe implementar operaciones que no utiliza. |
| Inversión de dependencias | La capa de aplicación depende de puertos; Spring, JPA, BCrypt, JWT y la API externa pertenecen a infraestructura. | La lógica de negocio no queda acoplada a un framework o proveedor específico. |

## Principios de aplicaciones de 12 factores

| Factor | Estado | Evidencia en NEXO |
| --- | --- | --- |
| I. Código base | Aplicado | Frontend y backend están versionados en Git y se orquestan desde `docker-compose.yml`. |
| II. Dependencias | Aplicado | Maven declara dependencias del backend en `pom.xml`; npm declara las del frontend en `package.json`. |
| III. Configuración | Aplicado | `application.yml` toma `DB_HOST`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS` y otros valores desde entorno. `.env.example` evita publicar secretos. |
| IV. Servicios de respaldo | Aplicado | PostgreSQL y la API pública de festivos se consumen como servicios configurables sin modificar las reglas de negocio. |
| V. Construir, liberar, ejecutar | Parcial | Dockerfiles multi-etapa construyen imágenes, Compose ejecuta servicios y GitHub Actions valida calidad. Falta automatizar una liberación productiva. |
| VI. Procesos | Aplicado | Spring Boot y Nginx se ejecutan sin estado; PostgreSQL conserva los datos en un volumen. JWT evita almacenar sesiones en memoria del servidor. |
| VII. Enlace de puertos | Aplicado | API en 8080 y cliente web en 4200; los puertos se declaran en Compose. |
| VIII. Concurrencia | Parcial | Los servicios pueden escalarse como contenedores, aunque la demostración local opera con una instancia por servicio. |
| IX. Desechabilidad | Aplicado | Healthchecks, `restart: unless-stopped` y Docker permiten reiniciar servicios sin perder datos del volumen de PostgreSQL. |
| X. Paridad desarrollo/producción | Parcial | Docker homogeneiza Java, Node, Nginx y PostgreSQL; faltaría definir infraestructura administrada de producción. |
| XI. Logs | Aplicado | `RequestLoggingFilter` produce logs operativos seguros por consola. La auditoría persistida guarda usuario, ruta, método, estado, duración, fecha y `requestId` en `auditoria_log`, sin guardar contraseñas, JWT ni cuerpos de petición. Cada usuario consulta únicamente su actividad mediante `GET /api/audit-logs`. |
| XII. Procesos de administración | Aplicado | Flyway ejecuta migraciones versionadas; Swagger documenta la API y k6 permite repetir las mediciones de rendimiento. |

## Límites y siguientes acciones

- Los registros de auditoría son trazabilidad de acciones de usuario, no reemplazan el monitoreo centralizado de errores o métricas de infraestructura.
- No se deben registrar tokens, contraseñas, cuerpos de solicitud ni parámetros con información sensible.
- Las características marcadas como parciales requieren validación en una plataforma con recursos y despliegue productivo reales.

