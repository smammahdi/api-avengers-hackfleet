import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
import { authAPI, setAuthToken, clearAuth, getUser, setUser } from '../services/api';
import { useNotification } from './NotificationContext';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUserState] = useState(null);
  const [loading, setLoading] = useState(true);
  const { showSuccess, showError } = useNotification();

  useEffect(() => {
    // Check if user is already logged in
    const storedUser = getUser();
    const token = localStorage.getItem('token');

    if (token && storedUser) {
      setAuthToken(token);
      setUserState(storedUser);
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (email, password) => {
    try {
      const response = await authAPI.login({ email, password });
      const { token, user: userData } = response.data;

      setAuthToken(token);
      setUser(userData);
      setUserState(userData);

      showSuccess(`Welcome back, ${userData.fullName || userData.username}!`);
      return { success: true, user: userData };
    } catch (error) {
      const message = error.response?.data?.message || 'Login failed. Please check your credentials.';
      showError(message);
      return { success: false, error: message };
    }
  }, [showSuccess, showError]);

  const register = useCallback(async (userData) => {
    try {
      const response = await authAPI.register(userData);
      const { token, user: newUser } = response.data;

      setAuthToken(token);
      setUser(newUser);
      setUserState(newUser);

      showSuccess(`Welcome to CareForAll, ${newUser.fullName || newUser.username}!`);
      return { success: true, user: newUser };
    } catch (error) {
      const message = error.response?.data?.message || 'Registration failed. Please try again.';
      showError(message);
      return { success: false, error: message };
    }
  }, [showSuccess, showError]);

  const logout = useCallback(() => {
    clearAuth();
    setUserState(null);
    showInfo('You have been logged out.');
  }, [showInfo]);

  const isAuthenticated = !!user;

  const isAdmin = user?.role === 'ADMIN' || user?.role === 'admin';

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        isAuthenticated,
        isAdmin,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
