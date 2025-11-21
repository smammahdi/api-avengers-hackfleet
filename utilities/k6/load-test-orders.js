import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '30s', target: 20 },  // Ramp up to 20 users
    { duration: '1m', target: 50 },   // Stay at 50 users for 1 minute
    { duration: '30s', target: 0 },   // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
    errors: ['rate<0.1'],              // Error rate should be below 10%
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  // Step 1: Register a new user (using unique email)
  const email = `loadtest-${Date.now()}-${__VU}-${__ITER}@example.com`;

  const registerPayload = JSON.stringify({
    email: email,
    password: 'password123',
    firstName: 'Load',
    lastName: 'Test'
  });

  let registerRes = http.post(`${BASE_URL}/api/users/register`, registerPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(registerRes, {
    'registration successful': (r) => r.status === 200,
  }) || errorRate.add(1);

  if (registerRes.status !== 200) {
    console.error(`Registration failed: ${registerRes.status}`);
    return;
  }

  const token = registerRes.json('token');

  // Step 2: Get products
  const productsRes = http.get(`${BASE_URL}/api/products`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  check(productsRes, {
    'products loaded': (r) => r.status === 200,
  }) || errorRate.add(1);

  if (productsRes.status !== 200) {
    console.error(`Products fetch failed: ${productsRes.status}`);
    return;
  }

  const products = productsRes.json('content');
  if (!products || products.length === 0) {
    console.error('No products available');
    return;
  }

  // Step 3: Add random products to cart
  const randomProduct = products[Math.floor(Math.random() * products.length)];

  const addToCartPayload = JSON.stringify({
    productId: randomProduct.id,
    productName: randomProduct.name,
    price: randomProduct.price,
    quantity: Math.floor(Math.random() * 3) + 1
  });

  const addToCartRes = http.post(`${BASE_URL}/api/cart/add`, addToCartPayload, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });

  check(addToCartRes, {
    'product added to cart': (r) => r.status === 200,
  }) || errorRate.add(1);

  // Step 4: View cart
  const cartRes = http.get(`${BASE_URL}/api/cart`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  check(cartRes, {
    'cart retrieved': (r) => r.status === 200,
  }) || errorRate.add(1);

  // Step 5: Place order
  const orderRes = http.post(`${BASE_URL}/api/orders`, null, {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });

  check(orderRes, {
    'order placed': (r) => r.status === 200 || r.status === 201,
    'order has status': (r) => r.json('status') !== undefined
  }) || errorRate.add(1);

  // Step 6: View order history
  const ordersRes = http.get(`${BASE_URL}/api/orders`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  check(ordersRes, {
    'order history retrieved': (r) => r.status === 200,
  }) || errorRate.add(1);

  sleep(1);
}
