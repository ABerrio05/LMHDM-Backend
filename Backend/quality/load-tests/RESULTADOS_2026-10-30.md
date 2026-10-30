# Resultados de carga y estrés — 30 de octubre de 2026

Entorno: Docker Compose local, PostgreSQL, backend Spring Boot y frontend Nginx. Las pruebas se ejecutaron desde el contenedor oficial `grafana/k6` conectado a la red de Compose. Se midió la operación protegida `GET /api/tasks` después de registrar de forma controlada un usuario único.

## Carga normal

- Escenario: 10 usuarios virtuales constantes durante 30 segundos.
- Comprobaciones: 266 de 266 correctas.
- Errores HTTP: 0,00 %.
- Solicitudes HTTP: 266.
- p95 de `GET /api/tasks`: 587,57 ms.
- Criterio: p95 menor a 1 segundo y menos de 1 % de errores.
- Resultado: **aprobado**.

## Estrés controlado

- Escenario: rampa de 10 a 50 usuarios virtuales durante 120 segundos.
- Comprobaciones: 2.647 de 2.647 correctas.
- Errores HTTP: 0,00 %.
- Solicitudes HTTP: 2.647.
- p95 de `GET /api/tasks`: 2,90 s.
- Criterio: p95 menor a 1,5 segundos y menos de 5 % de errores.
- Resultado: **la disponibilidad aprobó; la meta de latencia no aprobó**.

La API no respondió con errores durante el pico, pero el entorno local mostró degradación de latencia bajo 50 usuarios virtuales. La siguiente mejora debe medir el pool de conexiones y los recursos disponibles en un agente CI o infraestructura con más capacidad, antes de fijar un compromiso de producción.

