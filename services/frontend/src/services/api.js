import axios from 'axios';

// Base API URL - goes through API Gateway
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

// Create axios instance with default config
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ==================== AUTH SERVICE APIs ====================

export const authAPI = {
  register: (userData) => api.post('/auth-service/api/auth/register', userData),
  login: (credentials) => api.post('/auth-service/api/auth/login', credentials),
  logout: () => api.post('/auth-service/api/auth/logout'),
  getCurrentUser: () => api.get('/auth-service/api/auth/me'),
};

// ==================== CAMPAIGN SERVICE APIs ====================

export const campaignAPI = {
  // Public endpoints
  getAllCampaigns: (page = 0, size = 10) =>
    api.get(`/campaign-service/api/campaigns?page=${page}&size=${size}`),
  getCampaignById: (id) => api.get(`/campaign-service/api/campaigns/${id}`),
  getActiveCampaigns: () => api.get('/campaign-service/api/campaigns/active'),

  // Admin endpoints
  createCampaign: (campaignData) => api.post('/campaign-service/api/campaigns', campaignData),
  updateCampaign: (id, campaignData) => api.put(`/campaign-service/api/campaigns/${id}`, campaignData),
  deleteCampaign: (id) => api.delete(`/campaign-service/api/campaigns/${id}`),

  // Campaign progress
  getCampaignProgress: (id) => api.get(`/campaign-service/api/campaigns/${id}/progress`),
};

// ==================== DONATION SERVICE APIs ====================

export const donationAPI = {
  // Create donation (guest or authenticated)
  createDonation: (donationData) => api.post('/donation-service/api/donations', donationData),

  // Get donations
  getDonationById: (id) => api.get(`/donation-service/api/donations/${id}`),
  getUserDonations: (userId) => api.get(`/donation-service/api/donations/user/${userId}`),
  getDonationsByEmail: (email) => api.get(`/donation-service/api/donations/email/${email}`),
  getCampaignDonations: (campaignId) => api.get(`/donation-service/api/donations/campaign/${campaignId}`),

  // Get all donations (admin)
  getAllDonations: (page = 0, size = 10, status) => {
    let url = `/donation-service/api/donations?page=${page}&size=${size}`;
    if (status) url += `&status=${status}`;
    return api.get(url);
  },
};

// ==================== PAYMENT SERVICE APIs ====================

export const paymentAPI = {
  processPayment: (paymentData) => api.post('/payment-service/api/payments', paymentData),
  getPaymentById: (paymentId) => api.get(`/payment-service/api/payments/${paymentId}`),
  getPaymentByDonation: (donationId) => api.get(`/payment-service/api/payments/donation/${donationId}`),
};

// ==================== BANKING SERVICE APIs ====================

export const bankingAPI = {
  getAccountBalance: (userId) => api.get(`/banking-service/api/banking/accounts/${userId}/balance`),
  addFunds: (userId, amount) => api.post(`/banking-service/api/banking/accounts/${userId}/add-funds`, { amount }),
  getAccountTransactions: (userId, page = 0, size = 10) =>
    api.get(`/banking-service/api/banking/accounts/${userId}/transactions?page=${page}&size=${size}`),
  createAccount: (accountData) => api.post('/banking-service/api/banking/accounts', accountData),
};

// ==================== ANALYTICS SERVICE APIs ====================

export const analyticsAPI = {
  getCampaignStats: (campaignId) => api.get(`/analytics-service/api/analytics/campaigns/${campaignId}`),
  getPlatformStats: () => api.get('/analytics-service/api/analytics/platform/stats'),
  getUserStats: (userId) => api.get(`/analytics-service/api/analytics/users/${userId}`),
  getTopCampaigns: (limit = 5) => api.get(`/analytics-service/api/analytics/campaigns/top?limit=${limit}`),
  getRecentDonations: (limit = 10) => api.get(`/analytics-service/api/analytics/donations/recent?limit=${limit}`),
};

// ==================== NOTIFICATION SERVICE (Internal - not exposed via API Gateway) ====================
// Notifications are sent via RabbitMQ events, but we can simulate checking notification status

// ==================== CART/PRODUCT/ORDER APIs (Placeholder for future e-commerce features) ====================
// These are placeholder APIs for legacy components that are not yet implemented

export const cartAPI = {
  get: () => api.get('/cart-service/api/cart'),
  addItem: (itemData) => api.post('/cart-service/api/cart/items', itemData),
  removeItem: (productId) => api.delete(`/cart-service/api/cart/items/${productId}`),
};

export const productAPI = {
  getAll: () => api.get('/product-service/api/products'),
  search: (query) => api.get(`/product-service/api/products/search?q=${query}`),
};

export const orderAPI = {
  create: () => api.post('/order-service/api/orders'),
};

// ==================== HELPER FUNCTIONS ====================

export const setAuthToken = (token) => {
  if (token) {
    localStorage.setItem('token', token);
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    localStorage.removeItem('token');
    delete api.defaults.headers.common['Authorization'];
  }
};

export const getAuthToken = () => localStorage.getItem('token');

export const setUser = (user) => localStorage.setItem('user', JSON.stringify(user));

export const getUser = () => {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
};

export const clearAuth = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  delete api.defaults.headers.common['Authorization'];
};

export default api;
