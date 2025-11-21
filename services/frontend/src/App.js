import React, { useState, useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Box } from '@mui/material';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import Products from './pages/Products';
import Cart from './pages/Cart';
import Orders from './pages/Orders';
import ApiDocumentation from './pages/ApiDocumentation';

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');
    if (token && userData) {
      setUser(JSON.parse(userData));
    }
  }, []);

  const handleLogin = (userData, token) => {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Navbar user={user} onLogout={handleLogout} />
      <Box component="main" sx={{ flexGrow: 1, mt: 8 }}>
        <Routes>
          <Route path="/login" element={
            user ? <Navigate to="/products" /> : <Login onLogin={handleLogin} />
          } />
          <Route path="/register" element={
            user ? <Navigate to="/products" /> : <Register onLogin={handleLogin} />
          } />
          <Route path="/products" element={<Products user={user} />} />
          <Route path="/cart" element={
            user ? <Cart user={user} /> : <Navigate to="/login" />
          } />
          <Route path="/orders" element={
            user ? <Orders user={user} /> : <Navigate to="/login" />
          } />
          <Route path="/api-docs" element={<ApiDocumentation />} />
          <Route path="/" element={<Navigate to="/products" />} />
        </Routes>
      </Box>
    </Box>
  );
}

export default App;
