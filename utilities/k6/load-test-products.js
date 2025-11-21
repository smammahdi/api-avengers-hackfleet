import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '30s', target: 50 },   // Ramp up to 50 users
    { duration: '2m', target: 100 },   // Stay at 100 users for 2 minutes
    { duration: '30s', target: 0 },    // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<300'], // 95% of requests should be below 300ms
    errors: ['rate<0.05'],             // Error rate should be below 5%
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  // Test 1: Get all products
  const allProductsRes = http.get(`${BASE_URL}/api/products`);

  check(allProductsRes, {
    'get all products status 200': (r) => r.status === 200,
    'products returned': (r) => r.json('content') && r.json('content').length > 0,
  }) || errorRate.add(1);

  sleep(0.5);

  // Test 2: Search products
  const searchTerms = ['Laptop', 'Phone', 'Headphone', 'Watch', 'Camera'];
  const randomTerm = searchTerms[Math.floor(Math.random() * searchTerms.length)];

  const searchRes = http.get(`${BASE_URL}/api/products/search?query=${randomTerm}`);

  check(searchRes, {
    'search status 200': (r) => r.status === 200,
  }) || errorRate.add(1);

  sleep(0.5);

  // Test 3: Get products by category
  const categories = ['Electronics', 'Clothing', 'Books', 'Home', 'Sports'];
  const randomCategory = categories[Math.floor(Math.random() * categories.length)];

  const categoryRes = http.get(`${BASE_URL}/api/products/category/${randomCategory}`);

  check(categoryRes, {
    'category search status 200': (r) => r.status === 200,
  }) || errorRate.add(1);

  sleep(0.5);

  // Test 4: Get specific product details
  if (allProductsRes.status === 200) {
    const products = allProductsRes.json('content');
    if (products && products.length > 0) {
      const randomProduct = products[Math.floor(Math.random() * products.length)];

      const productDetailRes = http.get(`${BASE_URL}/api/products/${randomProduct.id}`);

      check(productDetailRes, {
        'product detail status 200': (r) => r.status === 200,
        'product has id': (r) => r.json('id') !== undefined,
        'product has name': (r) => r.json('name') !== undefined,
        'product has price': (r) => r.json('price') !== undefined,
      }) || errorRate.add(1);
    }
  }

  sleep(1);
}
