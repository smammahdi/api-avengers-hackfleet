import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  IconButton,
  Badge,
  Box,
  Tooltip
} from '@mui/material';
import {
  ShoppingCart,
  Person,
  Logout,
  Description,
  Inventory
} from '@mui/icons-material';

function Navbar({ user, onLogout }) {
  const navigate = useNavigate();
  const location = useLocation();
  // eslint-disable-next-line no-unused-vars
  const [cartCount, setCartCount] = React.useState(0);

  const handleNavigation = (path) => {
    navigate(path);
  };

  const isActive = (path) => location.pathname === path;

  return (
    <AppBar position="fixed">
      <Toolbar>
        <Inventory sx={{ mr: 2 }} />
        <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
          E-Commerce Platform
        </Typography>

        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button
            color="inherit"
            onClick={() => handleNavigation('/products')}
            sx={{
              fontWeight: isActive('/products') ? 'bold' : 'normal',
              borderBottom: isActive('/products') ? '2px solid white' : 'none'
            }}
          >
            Products
          </Button>

          {user && (
            <>
              <Tooltip title="Shopping Cart">
                <IconButton
                  color="inherit"
                  onClick={() => handleNavigation('/cart')}
                >
                  <Badge badgeContent={cartCount} color="secondary">
                    <ShoppingCart />
                  </Badge>
                </IconButton>
              </Tooltip>

              <Button
                color="inherit"
                onClick={() => handleNavigation('/orders')}
                sx={{
                  fontWeight: isActive('/orders') ? 'bold' : 'normal',
                  borderBottom: isActive('/orders') ? '2px solid white' : 'none'
                }}
              >
                Orders
              </Button>
            </>
          )}

          <Tooltip title="API Documentation">
            <Button
              color="inherit"
              startIcon={<Description />}
              onClick={() => handleNavigation('/api-docs')}
              sx={{
                fontWeight: isActive('/api-docs') ? 'bold' : 'normal',
                borderBottom: isActive('/api-docs') ? '2px solid white' : 'none',
                ml: 1
              }}
            >
              API Docs
            </Button>
          </Tooltip>

          {user ? (
            <>
              <Tooltip title={user.email}>
                <IconButton color="inherit" size="small" sx={{ ml: 1 }}>
                  <Person />
                </IconButton>
              </Tooltip>
              <Tooltip title="Logout">
                <IconButton color="inherit" onClick={onLogout}>
                  <Logout />
                </IconButton>
              </Tooltip>
            </>
          ) : (
            <>
              <Button color="inherit" onClick={() => handleNavigation('/login')}>
                Login
              </Button>
              <Button
                variant="outlined"
                color="inherit"
                onClick={() => handleNavigation('/register')}
                sx={{ ml: 1 }}
              >
                Register
              </Button>
            </>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
}

export default Navbar;
