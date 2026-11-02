# Backlog de NEXO

Este backlog se revisa durante la planificación de cada sprint de Scrum. Los ítems de Sprint 1 están implementados o en validación; los siguientes quedan priorizados para el comité.

## Incremento actual — funcionalidades implementadas y validadas

| ID | Historia / requerimiento | Criterio de terminado |
|---|---|---|
| RF-04 | Como usuario, quiero crear, consultar, editar y eliminar notas personales. | Implementado: CRUD protegido por JWT, migración y vista de notas. |
| RF-06 | Como usuario, quiero definir prioridad alta, media o baja. | Implementado: selección, edición, indicadores y distribución por prioridad en la interfaz. |
| RF-07 | Como usuario, quiero recurrencias diaria, semanal o personalizada. | Implementado: al completar una tarea recurrente se crea la siguiente ocurrencia respetando intervalo y fecha de finalización. |
| RF-08 | Como usuario, quiero gestionar espacios colaborativos. | Implementado: creación, listado, invitación y consulta de tareas compartidas, con interfaz protegida. |
| RF-10 | Como usuario, quiero recibir avisos push o por correo. | Implementado para correo: adaptador SMTP configurable, tarea programada, registro de entregas y pruebas. |
| RF-11 | Como administrador técnico, quiero bloquear cuentas, recuperar accesos y gestionar roles globales. | Implementado: endpoints restringidos a `ROLE_DEVELOPER`, listado de cuentas, bloqueo, cambio de rol permitido y restablecimiento de contraseña temporal sin exponer la anterior. |
| RF-12 | Como administrador técnico, quiero consultar métricas de uso y fallos de notificaciones. | Implementado: panel técnico con auditoría, métricas de notificaciones pendientes/enviadas/fallidas y consulta de cuentas. |
| RF-13 | Como usuario, quiero consultar fechas no laborables antes de planificar. | Implementado: integración de backend con Nager.Date para festivos de Colombia, caché y vista de planificación. |
| RF-14 | Como usuario, quiero recibir sugerencias para organizar mis tareas. | Implementado: asistente de planificación local y adaptador compatible con proveedores de IA, activable mediante variables de entorno. |
| NF-02 | Como equipo, queremos respuestas de tareas menores a 3 segundos. | Medición documentada con Postman o prueba de carga. |
| NF-04 | Como equipo, queremos soportar usuarios concurrentes. | Prueba de carga básica y resultados documentados. |
| NF-05 | Como usuario nuevo, quiero crear una tarea en máximo tres pasos. | Prueba de usabilidad y ajuste de interfaz. |

## Próximas mejoras de producto

| ID | Historia / requerimiento | Criterio de terminado |
|---|---|---|
| NF-03 | Como equipo, queremos disponibilidad mensual de 98%. | Despliegue, monitor de disponibilidad y reporte. |
| NF-06 | Como equipo, queremos 95% de notificaciones enviadas a tiempo. | Métricas de programación/envío y manejo de reintentos. |

## Trabajo técnico continuo (XP)

- Mantener pruebas unitarias y de integración por cada funcionalidad nueva.
- Ejecutar `mvn test` antes de promover cambios.
- Usar Pull Request en la promoción `Desarrollo` → `Pre-produccion` → `main`.
- Refactorizar sin alterar el contrato de las APIs ya consumidas por Angular.
