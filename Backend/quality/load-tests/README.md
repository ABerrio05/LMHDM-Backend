# Pruebas de carga y estrés

Los scripts usan k6 contra una instancia local de NEXO. Cada ejecución crea un usuario y una tarea únicos de prueba; no deben ejecutarse contra ambientes compartidos o productivos.

## Carga normal

Ejecuta 10 usuarios virtuales durante 30 segundos y mide `GET /api/tasks` autenticado.

```powershell
k6 run quality/load-tests/load-tasks.js
```

El criterio es menos de 1 % de errores HTTP y p95 inferior a 1 segundo.

## Estrés controlado

Aumenta progresivamente hasta 50 usuarios virtuales.

```powershell
k6 run quality/load-tests/stress-tasks.js
```

El criterio es menos de 5 % de errores HTTP y p95 inferior a 1,5 segundos.

## Sin instalar k6

Con NEXO ejecutándose mediante Docker Compose, k6 debe unirse a la red
`nexo_default`. En algunos equipos Docker no resuelve el alias del servicio desde
un contenedor externo; por ello se obtiene primero la IP actual del backend:

```powershell
$backendIp = (docker inspect -f "{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}" nexo-backend-1).Trim()
docker run --rm --network nexo_default -v "${PWD}:/scripts" grafana/k6 run -e BASE_URL="http://${backendIp}:8080" /scripts/load-tasks.js
docker run --rm --network nexo_default -v "${PWD}:/scripts" grafana/k6 run -e BASE_URL="http://${backendIp}:8080" /scripts/stress-tasks.js
```

Guarda la salida final de cada prueba; debe incluir `http_req_failed` y `http_req_duration`.

