# Backlog de NEXO

Este backlog se revisa durante la planificación de cada sprint de Scrum. Los ítems de Sprint 1 están implementados o en validación; los siguientes quedan priorizados para el comité.

## Sprint 2 — prioridad media

| ID | Historia / requerimiento | Criterio de terminado |
|---|---|---|
| RF-04 | Como usuario, quiero crear, consultar, editar y eliminar notas personales. | CRUD de notas protegido por JWT, migración existente usada y pruebas. |
| RF-06 | Como usuario, quiero definir prioridad alta, media o baja. | Ya disponible en tareas; falta mostrarla completamente en la interfaz. |
| RF-07 | Como usuario, quiero recurrencias diaria, semanal o personalizada. | Endpoints, reglas de dominio y tareas/recordatorios recurrentes. |
| RF-08 | Como usuario, quiero gestionar espacios colaborativos. | Creación, listado, invitación y consulta de tareas compartidas. Base backend disponible; falta interfaz y pruebas end-to-end. |
| RF-10 | Como usuario, quiero recibir avisos push o por correo. | Adaptador de notificación, tarea programada, registro de envíos y prueba. |
| NF-02 | Como equipo, queremos respuestas de tareas menores a 3 segundos. | Medición documentada con Postman o prueba de carga. |
| NF-04 | Como equipo, queremos soportar usuarios concurrentes. | Prueba de carga básica y resultados documentados. |
| NF-05 | Como usuario nuevo, quiero crear una tarea en máximo tres pasos. | Prueba de usabilidad y ajuste de interfaz. |

## Sprint 3 — prioridad baja

| ID | Historia / requerimiento | Criterio de terminado |
|---|---|---|
| RF-11 | Como administrador, quiero bloquear cuentas, recuperar accesos y gestionar roles globales. | Roles administrativos, endpoints protegidos e interfaz. |
| RF-12 | Como administrador, quiero consultar métricas de uso y fallos de notificaciones. | Panel con métricas y registros de error. |
| NF-03 | Como equipo, queremos disponibilidad mensual de 98%. | Despliegue, monitor de disponibilidad y reporte. |
| NF-06 | Como equipo, queremos 95% de notificaciones enviadas a tiempo. | Métricas de programación/envío y manejo de reintentos. |

## Trabajo técnico continuo (XP)

- Mantener pruebas unitarias y de integración por cada funcionalidad nueva.
- Ejecutar `mvn test` antes de promover cambios.
- Usar Pull Request en la promoción `Desarrollo` → `Pre-produccion` → `main`.
- Refactorizar sin alterar el contrato de las APIs ya consumidas por Angular.
