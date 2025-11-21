/**
 * API Endpoint Mappings
 * Shows how client-facing API endpoints map to internal microservices
 */

export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export const ENDPOINT_MAPPINGS = [
  {
    category: 'Authentication',
    icon: 'Lock',
    endpoints: [
      {
        clientApi: 'POST /api/auth/register',
        microservice: 'auth-service',
        internalApi: 'POST /register',
        description: 'Register new user account',
        requiresAuth: false
      },
      {
        clientApi: 'POST /api/auth/login',
        microservice: 'auth-service',
        internalApi: 'POST /login',
        description: 'Login and receive JWT token',
        requiresAuth: false
      }
    ]
  },
  {
    category: 'User Management',
    icon: 'Person',
    endpoints: [
      {
        clientApi: 'GET /api/users/me',
        microservice: 'user-service',
        internalApi: 'GET /me',
        description: 'Get current user profile',
        requiresAuth: true
      },
      {
        clientApi: 'PUT /api/users/me',
        microservice: 'user-service',
        internalApi: 'PUT /me',
        description: 'Update user profile',
        requiresAuth: true
      }
    ]
  },
  {
    category: 'Product Catalog',
    icon: 'Inventory',
    endpoints: [
      {
        clientApi: 'GET /api/products',
        microservice: 'product-service',
        internalApi: 'GET /',
        description: 'List all products',
        requiresAuth: false
      },
      {
        clientApi: 'GET /api/products/{id}',
        microservice: 'product-service',
        internalApi: 'GET /{id}',
        description: 'Get product details',
        requiresAuth: false
      },
      {
        clientApi: 'GET /api/products/search',
        microservice: 'product-service',
        internalApi: 'GET /search',
        description: 'Search products by keyword',
        requiresAuth: false
      },
      {
        clientApi: 'GET /api/products/category/{category}',
        microservice: 'product-service',
        internalApi: 'GET /category/{category}',
        description: 'Filter products by category',
        requiresAuth: false
      }
    ]
  },
  {
    category: 'Inventory',
    icon: 'Storage',
    endpoints: [
      {
        clientApi: 'GET /api/inventory/{productId}',
        microservice: 'inventory-service',
        internalApi: 'GET /{productId}',
        description: 'Check stock availability',
        requiresAuth: true
      },
      {
        clientApi: 'POST /api/inventory/reserve',
        microservice: 'inventory-service',
        internalApi: 'POST /reserve',
        description: 'Reserve stock for order',
        requiresAuth: true
      }
    ]
  },
  {
    category: 'Shopping Cart',
    icon: 'ShoppingCart',
    endpoints: [
      {
        clientApi: 'GET /api/cart',
        microservice: 'cart-service',
        internalApi: 'GET /',
        description: 'Get user cart',
        requiresAuth: true
      },
      {
        clientApi: 'POST /api/cart/items',
        microservice: 'cart-service',
        internalApi: 'POST /items',
        description: 'Add item to cart',
        requiresAuth: true
      },
      {
        clientApi: 'DELETE /api/cart/items/{productId}',
        microservice: 'cart-service',
        internalApi: 'DELETE /items/{productId}',
        description: 'Remove item from cart',
        requiresAuth: true
      },
      {
        clientApi: 'DELETE /api/cart',
        microservice: 'cart-service',
        internalApi: 'DELETE /',
        description: 'Clear cart',
        requiresAuth: true
      }
    ]
  },
  {
    category: 'Orders',
    icon: 'Receipt',
    endpoints: [
      {
        clientApi: 'POST /api/orders',
        microservice: 'order-service',
        internalApi: 'POST /',
        description: 'Create new order (Saga pattern)',
        requiresAuth: true
      },
      {
        clientApi: 'GET /api/orders',
        microservice: 'order-service',
        internalApi: 'GET /',
        description: 'Get user orders',
        requiresAuth: true
      },
      {
        clientApi: 'GET /api/orders/{id}',
        microservice: 'order-service',
        internalApi: 'GET /{id}',
        description: 'Get order details',
        requiresAuth: true
      },
      {
        clientApi: 'POST /api/orders/{id}/cancel',
        microservice: 'order-service',
        internalApi: 'POST /{id}/cancel',
        description: 'Cancel order',
        requiresAuth: true
      }
    ]
  },
  {
    category: 'Payments',
    icon: 'Payment',
    endpoints: [
      {
        clientApi: 'POST /api/payments',
        microservice: 'payment-service',
        internalApi: 'POST /',
        description: 'Process payment',
        requiresAuth: true
      }
    ]
  }
];

export const MICROSERVICES = {
  'auth-service': {
    color: '#1976d2',
    description: 'Handles user authentication and JWT token generation'
  },
  'user-service': {
    color: '#2e7d32',
    description: 'Manages user profiles and account information'
  },
  'product-service': {
    color: '#ed6c02',
    description: 'Product catalog and search functionality'
  },
  'inventory-service': {
    color: '#9c27b0',
    description: 'Stock management and reservation'
  },
  'cart-service': {
    color: '#d32f2f',
    description: 'Shopping cart management with Redis caching'
  },
  'order-service': {
    color: '#0288d1',
    description: 'Order orchestration using Saga pattern'
  },
  'payment-service': {
    color: '#c62828',
    description: 'Payment processing and transactions'
  }
};
