import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Paper,
  Chip,
  CircularProgress,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Table,
  TableBody,
  TableCell,
  TableRow,
  Grid
} from '@mui/material';
import { ExpandMore } from '@mui/icons-material';
import { orderAPI } from '../services/api';

function Orders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    try {
      const response = await orderAPI.getAll();
      setOrders(response.data);
    } catch (error) {
      console.error('Error loading orders:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    const colors = {
      PENDING: 'warning',
      CONFIRMED: 'info',
      PAYMENT_PROCESSING: 'info',
      COMPLETED: 'success',
      PAYMENT_FAILED: 'error',
      CANCELLED: 'default'
    };
    return colors[status] || 'default';
  };

  if (loading) {
    return (
      <Container sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
        <CircularProgress />
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom fontWeight="bold">
        Order History
      </Typography>

      {orders.length === 0 ? (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="h6" color="text.secondary">
            No orders yet
          </Typography>
        </Paper>
      ) : (
        orders.map((order) => (
          <Accordion key={order.id} sx={{ mb: 2 }}>
            <AccordionSummary expandIcon={<ExpandMore />}>
              <Grid container spacing={2} alignItems="center">
                <Grid item xs={12} sm={3}>
                  <Typography variant="subtitle2" color="text.secondary">
                    Order #{order.id}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={3}>
                  <Chip
                    label={order.status}
                    color={getStatusColor(order.status)}
                    size="small"
                  />
                </Grid>
                <Grid item xs={12} sm={3}>
                  <Typography variant="body2">
                    ${order.totalAmount.toFixed(2)}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={3}>
                  <Typography variant="caption" color="text.secondary">
                    {new Date(order.createdAt).toLocaleDateString()}
                  </Typography>
                </Grid>
              </Grid>
            </AccordionSummary>
            <AccordionDetails>
              <Table size="small">
                <TableBody>
                  {order.items.map((item, index) => (
                    <TableRow key={index}>
                      <TableCell>{item.productName}</TableCell>
                      <TableCell align="right">Qty: {item.quantity}</TableCell>
                      <TableCell align="right">${item.price.toFixed(2)}</TableCell>
                      <TableCell align="right">
                        <strong>${item.subtotal.toFixed(2)}</strong>
                      </TableCell>
                    </TableRow>
                  ))}
                  <TableRow>
                    <TableCell colSpan={3} align="right">
                      <strong>Total:</strong>
                    </TableCell>
                    <TableCell align="right">
                      <strong>${order.totalAmount.toFixed(2)}</strong>
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </AccordionDetails>
          </Accordion>
        ))
      )}
    </Container>
  );
}

export default Orders;
