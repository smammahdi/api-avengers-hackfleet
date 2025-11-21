import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '30s', target: 30 },   // Ramp up to 30 users
    { duration: '1m', target: 60 },    // Stay at 60 users for 1 minute
    { duration: '30s', target: 0 },    // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<400'], // 95% of requests should be below 400ms
    errors: ['rate<0.1'],              // Error rate should be below 10%
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const email = `loadtest-${Date.now()}-${__VU}-${__ITER}@example.com`;

  // Test 1: Register a new user
  const registerPayload = JSON.stringify({
    email: email,
    password: 'password123',
    firstName: 'Load',
    lastName: 'Test'
  });

  const registerRes = http.post(`${BASE_URL}/api/users/register`, registerPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(registerRes, {
    'registration status 200': (r) => r.status === 200,
    'token received on registration': (r) => r.json('token') !== undefined,
    'user id received': (r) => r.json('userId') !== undefined,
  }) || errorRate.add(1);

  sleep(1);

  // Test 2: Login with the registered user
  const loginPayload = JSON.stringify({
    email: email,
    password: 'password123'
  });

  const loginRes = http.post(`${BASE_URL}/api/users/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(loginRes, {
    'login status 200': (r) => r.status === 200,
    'token received on login': (r) => r.json('token') !== undefined,
  }) || errorRate.add(1);

  if (loginRes.status !== 200) {
    console.error(`Login failed: ${loginRes.status}`);
    return;
  }

  const token = loginRes.json('token');

  sleep(0.5);

  // Test 3: Get user profile
  const profileRes = http.get(`${BASE_URL}/api/users/profile`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  check(profileRes, {
    'profile status 200': (r) => r.status === 200,
    'profile has email': (r) => r.json('email') === email,
    'profile has first name': (r) => r.json('firstName') !== undefined,
  }) || errorRate.add(1);

  sleep(1);

  // Test 4: Test invalid login
  const invalidLoginPayload = JSON.stringify({
    email: email,
    password: 'wrongpassword'
  });

  const invalidLoginRes = http.post(`${BASE_URL}/api/users/login`, invalidLoginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(invalidLoginRes, {
    'invalid login rejected': (r) => r.status === 401 || r.status === 400,
  }) || errorRate.add(1);

  sleep(1);
}
