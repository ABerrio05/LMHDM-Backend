import http from 'k6/http';
import { check, sleep } from 'k6';

// BASE_URL es el origen del backend, sin el sufijo /api.
const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  scenarios: {
    controlled_stress: {
      executor: 'ramping-vus',
      stages: [
        { duration: '20s', target: 10 },
        { duration: '40s', target: 30 },
        { duration: '40s', target: 50 },
        { duration: '20s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    'http_req_duration{operation:list_tasks}': ['p(95)<1500'],
  },
};

export function setup() {
  const suffix = `${Date.now()}-${Math.floor(Math.random() * 100000)}`;
  const registration = http.post(`${baseUrl}/api/auth/register`, JSON.stringify({
    name: 'Usuario de estrés',
    email: `k6.stress.${suffix}@example.test`,
    password: 'NexoStress123',
  }), { headers: { 'Content-Type': 'application/json' } });
  check(registration, { 'registro para estrés creado': (response) => response.status === 201 });
  return { token: registration.json('accessToken') };
}

export default function (data) {
  const response = http.get(`${baseUrl}/api/tasks`, {
    headers: { Authorization: `Bearer ${data.token}` },
    tags: { operation: 'list_tasks' },
  });
  check(response, { 'API responde durante el estrés': (result) => result.status === 200 });
  sleep(0.5);
}
