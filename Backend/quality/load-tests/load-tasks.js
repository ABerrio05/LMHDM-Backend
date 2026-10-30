import http from 'k6/http';
import { check, sleep } from 'k6';

// BASE_URL es el origen del backend, sin el sufijo /api.
const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  scenarios: {
    normal_load: {
      executor: 'constant-vus',
      vus: Number(__ENV.VUS || 10),
      duration: __ENV.DURATION || '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{operation:list_tasks}': ['p(95)<1000'],
  },
};

export function setup() {
  const suffix = `${Date.now()}-${Math.floor(Math.random() * 100000)}`;
  const registration = http.post(`${baseUrl}/api/auth/register`, JSON.stringify({
    name: 'Usuario de carga',
    email: `k6.load.${suffix}@example.test`,
    password: 'NexoLoad123',
  }), { headers: { 'Content-Type': 'application/json' } });

  check(registration, { 'registro de usuario de carga creado': (response) => response.status === 201 });
  const token = registration.json('accessToken');
  const headers = { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` };
  const task = http.post(`${baseUrl}/api/tasks`, JSON.stringify({
    title: 'Tarea inicial de carga',
    description: 'Datos controlados para la prueba de rendimiento.',
    priority: 'MEDIA',
  }), { headers });
  check(task, { 'tarea inicial de carga creada': (response) => response.status === 201 });
  return { token };
}

export default function (data) {
  const response = http.get(`${baseUrl}/api/tasks`, {
    headers: { Authorization: `Bearer ${data.token}` },
    tags: { operation: 'list_tasks' },
  });
  check(response, { 'listar tareas responde 200': (result) => result.status === 200 });
  sleep(1);
}
