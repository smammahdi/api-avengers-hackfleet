import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Paper,
  Box,
  Button,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Alert,
  CircularProgress,
  Divider
} from '@mui/material';
import { Delete, ShoppingCartCheckout } from '@mui/icons-material';
import { cartAPI, orderAPI } from '../services/api';
import { useNavigate } from 'react-router-dom';

function Cart() {
  const navigate = useNavigate();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);
  const [placingOrder, setPlacingOrder] = useState(false);

  useEffect(() => {
    loadCart();
  }, []);

  const loadCart = async () => {
    try {
      const response = await cartAPI.get();
      setCart(response.data);
    } catch (error) {
      console.error('Error loading cart:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveItem = async (productId) => {
    try {
      await cartAPI.removeItem(productId);
      setMessage({ type: 'success', text: 'Item removed from cart' });
      loadCart();
    } catch (error) {
      setMessage({ type: 'error', text: 'Failed to remove item' });
    }
  };

  const handlePlaceOrder = async () => {
    setPlacingOrder(true);
    try {
      await orderAPI.create();
      setMessage({ type: 'success', text: 'Order placed successfully!' });
      setTimeout(() => {
        navigate('/orders');
      }, 1500);
    } catch (error) {
      setMessage({
        type: 'error',
        text: error.response?.data?.message || 'Failed to place order'
      });
      setPlacingOrder(false);
    }
  };

  if (loading) {
    return (
      <Container sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
        <CircularProgress />
      </Container>
    );
  }

  const isEmpty = !cart || !cart.items || cart.items.length === 0;

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom fontWeight="bold">
        Shopping Cart
      </Typography>

      {message && (
        <Alert severity={message.type} sx={{ mb: 2 }} onClose={() => setMessage(null)}>
          {message.text}
        </Alert>
      )}

      {isEmpty ? (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="h6" color="text.secondary">
            Your cart is empty
          </Typography>
          <Button
            variant="contained"
            onClick={() => navigate('/products')}
            sx={{ mt: 2 }}
          >
            Continue Shopping
          </Button>
        </Paper>
      ) : (
        <>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Product</TableCell>
                  <TableCell align="right">Price</TableCell>
                  <TableCell align="right">Quantity</TableCell>
                  <TableCell align="right">Subtotal</TableCell>
                  <TableCell align="right">Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {cart.items.map((item) => (
                  <TableRow key={item.productId}>
                    <TableCell>{item.productName}</TableCell>
                    <TableCell align="right">${item.price.toFixed(2)}</TableCell>
                    <TableCell align="right">{item.quantity}</TableCell>
                    <TableCell align="right">${item.subtotal.toFixed(2)}</TableCell>
                    <TableCell align="right">
                      <IconButton
                        color="error"
                        onClick={() => handleRemoveItem(item.productId)}
                      >
                        <Delete />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <Paper sx={{ p: 3, mt: 2 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
              <Typography variant="h6">Total:</Typography>
              <Typography variant="h6" color="primary">
                ${cart.totalPrice.toFixed(2)}
              </Typography>
            </Box>
            <Divider sx={{ my: 2 }} />
            <Button
              fullWidth
              variant="contained"
              size="large"
              startIcon={<ShoppingCartCheckout />}
              onClick={handlePlaceOrder}
              disabled={placingOrder}
            >
              {placingOrder ? 'Placing Order...' : 'Place Order'}
            </Button>
          </Paper>
        </>
      )}
    </Container>
  );
}

export default Cart;
