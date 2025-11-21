import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');

// Test configuration
export const options = {
  stages: [
    { duration: '30s', target: 10 },  // Ramp up to 10 users
    { duration: '1m', target: 50 },   // Ramp up to 50 users
    { duration: '2m', target: 50 },   // Stay at 50 users
    { duration: '1m', target: 100 },  // Ramp up to 100 users
    { duration: '2m', target: 100 },  // Stay at 100 users
    { duration: '30s', target: 0 },   // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
    http_req_failed: ['rate<0.05'],   // Error rate should be less than 5%
    errors: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Test data
const testUsers = [];
let productIds = [];
let authToken = '';

export function setup() {
  console.log('Setting up test data...');

  // Register a few test users and get product IDs
  const setupData = {
    users: [],
    products: []
  };

  // Register test users
  for (let i = 1; i <= 5; i++) {
    const timestamp = Date.now();
    const userData = {
      email: `loadtest${i}_${timestamp}@example.com`,
      password: 'Test123!',
      firstName: `LoadTest${i}`,
      lastName: 'User'
    };

    const registerRes = http.post(
      `${BASE_URL}/api/users/register`,
      JSON.stringify(userData),
      {
        headers: { 'Content-Type': 'application/json' },
      }
    );

    if (registerRes.status === 201 || registerRes.status === 200) {
      setupData.users.push(userData);
      console.log(`Registered user: ${userData.email}`);
    }
  }

  // Get products
  const productsRes = http.get(`${BASE_URL}/api/products`);
  if (productsRes.status === 200) {
    try {
      const products = JSON.parse(productsRes.body);
      if (Array.isArray(products) && products.length > 0) {
        setupData.products = products.slice(0, 10).map(p => p.id);
        console.log(`Found ${setupData.products.length} products`);
      }
    } catch (e) {
      console.log('Could not parse products:', e);
    }
  }

  return setupData;
}

export default function(data) {
  // Randomly select a user
  const userData = data.users[Math.floor(Math.random() * data.users.length)];

  if (!userData) {
    console.log('No users available for testing');
    return;
  }

  // Scenario 1: User Login (30% of traffic)
  if (Math.random() < 0.3) {
    testLogin(userData);
  }
  // Scenario 2: Browse Products (40% of traffic)
  else if (Math.random() < 0.7) {
    testBrowseProducts(data);
  }
  // Scenario 3: Complete Purchase Flow (30% of traffic)
  else {
    testCompletePurchaseFlow(userData, data);
  }

  sleep(1);
}

function testLogin(userData) {
  const loginRes = http.post(
    `${BASE_URL}/api/users/login`,
    JSON.stringify({
      email: userData.email,
      password: userData.password
    }),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Login' },
    }
  );

  const success = check(loginRes, {
    'login status is 200': (r) => r.status === 200,
    'login response has token': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.token !== undefined;
      } catch (e) {
        return false;
      }
    },
  });

  errorRate.add(!success);
}

function testBrowseProducts(data) {
  // Get all products
  const productsRes = http.get(`${BASE_URL}/api/products`, {
    tags: { name: 'GetProducts' },
  });

  const success = check(productsRes, {
    'get products status is 200': (r) => r.status === 200,
  });

  errorRate.add(!success);

  // If we have products, get details of a random one
  if (success && data.products.length > 0) {
    const productId = data.products[Math.floor(Math.random() * data.products.length)];

    const productRes = http.get(`${BASE_URL}/api/products/${productId}`, {
      tags: { name: 'GetProductDetails' },
    });

    const detailSuccess = check(productRes, {
      'get product details status is 200': (r) => r.status === 200,
    });

    errorRate.add(!detailSuccess);
  }
}

function testCompletePurchaseFlow(userData, data) {
  // Step 1: Login
  const loginRes = http.post(
    `${BASE_URL}/api/users/login`,
    JSON.stringify({
      email: userData.email,
      password: userData.password
    }),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'PurchaseFlow-Login' },
    }
  );

  if (loginRes.status !== 200) {
    errorRate.add(true);
    return;
  }

  let token;
  try {
    const loginBody = JSON.parse(loginRes.body);
    token = loginBody.token;
  } catch (e) {
    errorRate.add(true);
    return;
  }

  const authHeaders = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };

  // Step 2: Add items to cart
  if (data.products.length > 0) {
    const numItems = Math.floor(Math.random() * 3) + 1; // 1-3 items

    for (let i = 0; i < numItems; i++) {
      const productId = data.products[Math.floor(Math.random() * data.products.length)];
      const quantity = Math.floor(Math.random() * 3) + 1; // 1-3 quantity

      const addToCartRes = http.post(
        `${BASE_URL}/api/cart/items`,
        JSON.stringify({
          productId: productId,
          quantity: quantity
        }),
        {
          headers: authHeaders,
          tags: { name: 'PurchaseFlow-AddToCart' },
        }
      );

      check(addToCartRes, {
        'add to cart status is 200': (r) => r.status === 200 || r.status === 201,
      });

      sleep(0.5);
    }

    // Step 3: View cart
    const cartRes = http.get(`${BASE_URL}/api/cart`, {
      headers: authHeaders,
      tags: { name: 'PurchaseFlow-ViewCart' },
    });

    const cartSuccess = check(cartRes, {
      'view cart status is 200': (r) => r.status === 200,
    });

    if (!cartSuccess) {
      errorRate.add(true);
      return;
    }

    // Step 4: Create order
    const orderRes = http.post(
      `${BASE_URL}/api/orders`,
      JSON.stringify({}),
      {
        headers: authHeaders,
        tags: { name: 'PurchaseFlow-CreateOrder' },
      }
    );

    const orderSuccess = check(orderRes, {
      'create order status is 201 or 200': (r) => r.status === 201 || r.status === 200,
    });

    errorRate.add(!orderSuccess);

    if (orderSuccess) {
      console.log('Order created successfully');
    }
  }
}

export function teardown(data) {
  console.log('Load test completed!');
  console.log(`Tested with ${data.users.length} users and ${data.products.length} products`);
}
