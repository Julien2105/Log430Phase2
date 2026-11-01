// Test de charge k6 via l'API Gateway : 60 % consultations, 30 % commandes, 10 % enregistrements de drones.
// Montée en charge d'une minute puis cinq minutes à débit constant. Cible Phase 2 : débit >= 600 opérations/s.
// Le P95 du délai d'activation (acceptedAt -> activatedAt) se calcule en SQL : voir performance/p95-activation.sql.
//
// Exécution : k6 run performance/load-test.js
// Variables : GATEWAY, KEYCLOAK, CLIENT_ID, CLIENT_SECRET, TARGET_RATE (600 par défaut), SETUP_DRONES (50).
import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const GATEWAY = __ENV.GATEWAY || 'http://localhost:8000';
const KEYCLOAK = __ENV.KEYCLOAK || 'http://localhost:8080';
const CLIENT_ID = __ENV.CLIENT_ID || 'client-a';
const CLIENT_SECRET = __ENV.CLIENT_SECRET || 'client-a-secret';
const TARGET_RATE = parseInt(__ENV.TARGET_RATE || '600');
const SETUP_DRONES = parseInt(__ENV.SETUP_DRONES || '50');

const operations = new Counter('operations');

export const options = {
  scenarios: {
    mixed: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 200,
      maxVUs: 1000,
      stages: [
        { target: TARGET_RATE, duration: '1m' },   // montée en charge progressive
        { target: TARGET_RATE, duration: '5m' },   // débit constant
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<5000'],             // objectif Phase 2 : P95 <= 5 s
  },
};

function headers(token, extra) {
  return Object.assign({ Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }, extra || {});
}

export function setup() {
  const res = http.post(`${KEYCLOAK}/realms/candronex/protocol/openid-connect/token`, {
    grant_type: 'client_credentials', client_id: CLIENT_ID, client_secret: CLIENT_SECRET,
  });
  check(res, { 'jeton Keycloak obtenu': (r) => r.status === 200 });
  const token = res.json('access_token');
  const run = Date.now() % 100000;
  const imsiRun = Math.floor(Date.now() / 1000) % 10000;
  const drones = [];
  for (let i = 0; i < SETUP_DRONES; i++) {
    const droneId = `DRN-LT-${run}-${i}`;
    const imsi = `303${String(imsiRun).padStart(4, '0')}${String(0).padStart(4, '0')}${String(i).padStart(4, '0')}`;
    const r = http.post(`${GATEWAY}/api/v1/drones`, JSON.stringify({ droneId, imsi, label: 'charge' }), { headers: headers(token) });
    if (r.status === 201 || r.status === 200) drones.push(droneId);
  }
  return { token, drones, run, imsiRun };
}

export default function (data) {
  const dice = Math.random();
  const h = headers(data.token);
  let res;
  if (dice < 0.6) {                                   // 60 % consultations
    res = http.get(`${GATEWAY}/api/v1/service-offerings`, { headers: h });
    check(res, { 'consultation 200': (r) => r.status === 200 });
  } else if (dice < 0.9) {                            // 30 % commandes
    const droneId = data.drones[Math.floor(Math.random() * data.drones.length)];
    const offering = Math.random() < 0.5 ? 'C2_URLLC' : 'IMAGERY_EMBB';
    const body = JSON.stringify({ items: [{ action: 'ADD', droneId, serviceOfferingId: offering }] });
    res = http.post(`${GATEWAY}/api/v1/orders`, body, { headers: headers(data.token, { 'Idempotency-Key': `k6-${__VU}-${__ITER}-${data.run}` }) });
    check(res, { 'commande 201': (r) => r.status === 201 });
  } else {                                            // 10 % enregistrements de drones
        const droneId = `DRN-K6-${data.run}-${__VU}-${__ITER}`;
    const imsi = `302${String(data.imsiRun).padStart(4, '0')}${String(__VU % 10000).padStart(4, '0')}${String(__ITER % 10000).padStart(4, '0')}`;
    res = http.post(`${GATEWAY}/api/v1/drones`, JSON.stringify({ droneId, imsi, label: 'k6' }), { headers: h });
    check(res, { 'drone 201/409': (r) => r.status === 201 || r.status === 409 });
  }
  operations.add(1);
}
