import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const orderDuration = new Trend('order_duration');

export const options = {
  stages: [
    { duration: '1m', target: 10 },    // Warm up
    { duration: '2m', target: 30 },    // Normal load
    { duration: '1m', target: 50 },    // Peak load
    { duration: '1m', target: 30 },    // Scale down
    { duration: '30s', target: 0 },    // Cool down
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'], // 95% of requests should be below 1s
    errors: ['rate<0.15'],              // Error rate should be below 15%
    order_duration: ['p(95)<2000'],     // 95% of order flows should complete in 2s
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  let token;
  let userId;
  const email = `fullflow-${Date.now()}-${__VU}-${__ITER}@example.com`;

  // Group 1: User Registration & Authentication
  group('Authentication Flow', function () {
    const registerPayload = JSON.stringify({
      email: email,
      password: 'password123',
      firstName: 'E2E',
      lastName: 'Test'
    });

    const registerRes = http.post(`${BASE_URL}/api/users/register`, registerPayload, {
      headers: { 'Content-Type': 'application/json' },
    });

    check(registerRes, {
      'user registered': (r) => r.status === 200,
    }) || errorRate.add(1);

    if (registerRes.status === 200) {
      token = registerRes.json('token');
      userId = registerRes.json('userId');
    } else {
      console.error(`Registration failed: ${registerRes.status}`);
      return;
    }
  });

  sleep(0.5);

  // Group 2: Product Browsing
  group('Product Browsing', function () {
    const productsRes = http.get(`${BASE_URL}/api/products`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    check(productsRes, {
      'products loaded': (r) => r.status === 200,
      'has products': (r) => r.json('content') && r.json('content').length > 0,
    }) || errorRate.add(1);

    if (productsRes.status !== 200) {
      console.error(`Products fetch failed: ${productsRes.status}`);
      return;
    }

    // Browse product details
    const products = productsRes.json('content');
    if (products && products.length > 0) {
      const product = products[Math.floor(Math.random() * products.length)];

      const productDetailRes = http.get(`${BASE_URL}/api/products/${product.id}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      check(productDetailRes, {
        'product detail loaded': (r) => r.status === 200,
      }) || errorRate.add(1);
    }
  });

  sleep(1);

  // Group 3: Shopping Cart Operations
  group('Shopping Cart', function () {
    const productsRes = http.get(`${BASE_URL}/api/products`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (productsRes.status === 200) {
      const products = productsRes.json('content');

      if (products && products.length > 0) {
        // Add 2-3 random products to cart
        const numProducts = Math.floor(Math.random() * 2) + 2;

        for (let i = 0; i < Math.min(numProducts, products.length); i++) {
          const product = products[Math.floor(Math.random() * products.length)];

          const addToCartPayload = JSON.stringify({
            productId: product.id,
            productName: product.name,
            price: product.price,
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

          sleep(0.3);
        }

        // View cart
        const cartRes = http.get(`${BASE_URL}/api/cart`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });

        check(cartRes, {
          'cart retrieved': (r) => r.status === 200,
          'cart has items': (r) => r.json('items') && r.json('items').length > 0,
        }) || errorRate.add(1);
      }
    }
  });

  sleep(1);

  // Group 4: Order Placement (Critical Path)
  group('Order Placement', function () {
    const orderStart = new Date().getTime();

    const orderRes = http.post(`${BASE_URL}/api/orders`, null, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    const orderEnd = new Date().getTime();
    orderDuration.add(orderEnd - orderStart);

    check(orderRes, {
      'order created': (r) => r.status === 200 || r.status === 201,
      'order has id': (r) => r.json('id') !== undefined,
      'order has status': (r) => r.json('status') !== undefined,
    }) || errorRate.add(1);

    if (orderRes.status === 200 || orderRes.status === 201) {
      const orderId = orderRes.json('id');

      sleep(0.5);

      // Verify order details
      const orderDetailRes = http.get(`${BASE_URL}/api/orders/${orderId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      check(orderDetailRes, {
        'order details retrieved': (r) => r.status === 200,
        'order items present': (r) => r.json('items') && r.json('items').length > 0,
      }) || errorRate.add(1);
    }
  });

  sleep(1);

  // Group 5: Order History
  group('Order History', function () {
    const ordersRes = http.get(`${BASE_URL}/api/orders`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    check(ordersRes, {
      'order history retrieved': (r) => r.status === 200,
      'has orders': (r) => Array.isArray(r.json()) && r.json().length > 0,
    }) || errorRate.add(1);
  });

  sleep(2);
}

export function handleSummary(data) {
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    'summary.json': JSON.stringify(data),
  };
}

function textSummary(data, options) {
  return JSON.stringify(data, null, 2);
}
