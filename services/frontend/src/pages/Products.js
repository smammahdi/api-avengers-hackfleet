import React, { useState, useEffect } from 'react';
import {
  Container,
  Grid,
  Card,
  CardContent,
  CardMedia,
  Typography,
  Button,
  TextField,
  Box,
  Chip,
  Alert,
  CircularProgress
} from '@mui/material';
import { AddShoppingCart, Search } from '@mui/icons-material';
import { productAPI, cartAPI } from '../services/api';

function Products({ user }) {
  const [products, setProducts] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      const response = await productAPI.getAll();
      setProducts(response.data);
    } catch (error) {
      console.error('Error loading products:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      loadProducts();
      return;
    }
    setLoading(true);
    try {
      const response = await productAPI.search(searchQuery);
      setProducts(response.data);
    } catch (error) {
      console.error('Error searching products:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = async (product) => {
    if (!user) {
      setMessage({ type: 'warning', text: 'Please login to add items to cart' });
      return;
    }

    try {
      await cartAPI.addItem({
        productId: product.id,
        quantity: 1
      });
      setMessage({ type: 'success', text: `${product.name} added to cart!` });
      setTimeout(() => setMessage(null), 3000);
    } catch (error) {
      setMessage({ type: 'error', text: 'Failed to add item to cart' });
    }
  };

  if (loading) {
    return (
      <Container sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
        <CircularProgress />
      </Container>
    );
  }

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* Header */}
      <Typography variant="h4" gutterBottom fontWeight="bold">
        Products
      </Typography>

      {/* Search */}
      <Box sx={{ mb: 4, display: 'flex', gap: 2 }}>
        <TextField
          fullWidth
          placeholder="Search products..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
        />
        <Button
          variant="contained"
          startIcon={<Search />}
          onClick={handleSearch}
          sx={{ minWidth: '120px' }}
        >
          Search
        </Button>
      </Box>

      {/* Messages */}
      {message && (
        <Alert severity={message.type} sx={{ mb: 2 }} onClose={() => setMessage(null)}>
          {message.text}
        </Alert>
      )}

      {/* Products Grid */}
      <Grid container spacing={3}>
        {products.map((product) => (
          <Grid item xs={12} sm={6} md={4} lg={3} key={product.id}>
            <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
              <CardMedia
                component="div"
                sx={{
                  height: 200,
                  bgcolor: 'grey.200',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}
              >
                <Typography variant="h6" color="text.secondary">
                  {product.name.substring(0, 2).toUpperCase()}
                </Typography>
              </CardMedia>
              <CardContent sx={{ flexGrow: 1 }}>
                <Typography gutterBottom variant="h6" component="div">
                  {product.name}
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                  {product.description}
                </Typography>
                <Chip
                  label={product.category}
                  size="small"
                  color="primary"
                  variant="outlined"
                  sx={{ mb: 1 }}
                />
                <Typography variant="h6" color="primary" sx={{ mt: 1 }}>
                  ${product.price.toFixed(2)}
                </Typography>
              </CardContent>
              <Box sx={{ p: 2, pt: 0 }}>
                <Button
                  fullWidth
                  variant="contained"
                  startIcon={<AddShoppingCart />}
                  onClick={() => handleAddToCart(product)}
                >
                  Add to Cart
                </Button>
              </Box>
            </Card>
          </Grid>
        ))}
      </Grid>

      {products.length === 0 && (
        <Box sx={{ textAlign: 'center', mt: 8 }}>
          <Typography variant="h6" color="text.secondary">
            No products found
          </Typography>
        </Box>
      )}
    </Container>
  );
}

export default Products;
