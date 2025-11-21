import React, { useState } from 'react';
import {
  Container,
  Typography,
  Box,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Chip,
  Paper,
  Grid,
  Card,
  CardContent,
  Alert
} from '@mui/material';
import {
  ExpandMore,
  Lock,
  Person,
  Inventory,
  Storage,
  ShoppingCart,
  Receipt,
  Payment,
  ArrowForward,
  Check
} from '@mui/icons-material';
import { ENDPOINT_MAPPINGS, MICROSERVICES, API_BASE_URL } from '../config/endpointMappings';

const iconMap = {
  Lock,
  Person,
  Inventory,
  Storage,
  ShoppingCart,
  Receipt,
  Payment
};

function ApiDocumentation() {
  const [expanded, setExpanded] = useState('panel0');

  const handleChange = (panel) => (event, isExpanded) => {
    setExpanded(isExpanded ? panel : false);
  };

  const getHttpMethodColor = (method) => {
    const colors = {
      GET: 'success',
      POST: 'primary',
      PUT: 'warning',
      DELETE: 'error'
    };
    return colors[method] || 'default';
  };

  const extractMethod = (api) => {
    return api.split(' ')[0];
  };

  const extractPath = (api) => {
    return api.split(' ')[1];
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h3" gutterBottom fontWeight="bold">
          API Documentation
        </Typography>
        <Typography variant="subtitle1" color="text.secondary" gutterBottom>
          Client-facing REST API endpoints and their microservice mappings
        </Typography>
        <Alert severity="info" sx={{ mt: 2 }}>
          <strong>Base URL:</strong> {API_BASE_URL}
        </Alert>
      </Box>

      {/* Microservices Overview */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" gutterBottom fontWeight="bold" sx={{ mb: 2 }}>
          Microservices Architecture
        </Typography>
        <Grid container spacing={2}>
          {Object.entries(MICROSERVICES).map(([service, info]) => (
            <Grid item xs={12} sm={6} md={4} key={service}>
              <Card
                sx={{
                  borderLeft: `4px solid ${info.color}`,
                  height: '100%'
                }}
              >
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    {service}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {info.description}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Box>

      {/* API Endpoints by Category */}
      <Box>
        <Typography variant="h5" gutterBottom fontWeight="bold" sx={{ mb: 2 }}>
          API Endpoints
        </Typography>

        {ENDPOINT_MAPPINGS.map((category, categoryIndex) => {
          const IconComponent = iconMap[category.icon];
          return (
            <Accordion
              key={categoryIndex}
              expanded={expanded === `panel${categoryIndex}`}
              onChange={handleChange(`panel${categoryIndex}`)}
              sx={{ mb: 1 }}
            >
              <AccordionSummary expandIcon={<ExpandMore />}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  {IconComponent && <IconComponent color="primary" />}
                  <Typography variant="h6">{category.category}</Typography>
                  <Chip
                    label={`${category.endpoints.length} endpoints`}
                    size="small"
                    color="primary"
                    variant="outlined"
                  />
                </Box>
              </AccordionSummary>
              <AccordionDetails>
                {category.endpoints.map((endpoint, endpointIndex) => (
                  <Paper
                    key={endpointIndex}
                    elevation={0}
                    sx={{
                      p: 2,
                      mb: 2,
                      border: '1px solid',
                      borderColor: 'divider',
                      '&:last-child': { mb: 0 }
                    }}
                  >
                    {/* Endpoint Description */}
                    <Typography variant="subtitle1" gutterBottom fontWeight="bold">
                      {endpoint.description}
                    </Typography>

                    {/* Client API → Microservice Mapping */}
                    <Box sx={{ my: 2 }}>
                      <Grid container spacing={2} alignItems="center">
                        {/* Client API */}
                        <Grid item xs={12} md={5}>
                          <Box
                            sx={{
                              p: 2,
                              bgcolor: 'primary.light',
                              borderRadius: 1,
                              color: 'white'
                            }}
                          >
                            <Typography variant="caption" sx={{ opacity: 0.9 }}>
                              CLIENT API
                            </Typography>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
                              <Chip
                                label={extractMethod(endpoint.clientApi)}
                                color={getHttpMethodColor(extractMethod(endpoint.clientApi))}
                                size="small"
                              />
                              <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                                {extractPath(endpoint.clientApi)}
                              </Typography>
                            </Box>
                          </Box>
                        </Grid>

                        {/* Arrow */}
                        <Grid item xs={12} md={2} sx={{ textAlign: 'center' }}>
                          <ArrowForward color="action" />
                        </Grid>

                        {/* Microservice API */}
                        <Grid item xs={12} md={5}>
                          <Box
                            sx={{
                              p: 2,
                              bgcolor: MICROSERVICES[endpoint.microservice]?.color || 'grey.300',
                              borderRadius: 1,
                              color: 'white'
                            }}
                          >
                            <Typography variant="caption" sx={{ opacity: 0.9 }}>
                              {endpoint.microservice.toUpperCase()}
                            </Typography>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
                              <Chip
                                label={extractMethod(endpoint.clientApi)}
                                color={getHttpMethodColor(extractMethod(endpoint.clientApi))}
                                size="small"
                              />
                              <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                                {endpoint.internalApi}
                              </Typography>
                            </Box>
                          </Box>
                        </Grid>
                      </Grid>
                    </Box>

                    {/* Authentication Required */}
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      {endpoint.requiresAuth ? (
                        <>
                          <Lock fontSize="small" color="warning" />
                          <Typography variant="caption" color="text.secondary">
                            Requires Authentication (JWT Token)
                          </Typography>
                        </>
                      ) : (
                        <>
                          <Check fontSize="small" color="success" />
                          <Typography variant="caption" color="text.secondary">
                            Public Endpoint
                          </Typography>
                        </>
                      )}
                    </Box>
                  </Paper>
                ))}
              </AccordionDetails>
            </Accordion>
          );
        })}
      </Box>

      {/* How to Use */}
      <Box sx={{ mt: 4 }}>
        <Paper sx={{ p: 3, bgcolor: 'grey.50' }}>
          <Typography variant="h6" gutterBottom>
            How to Use This API
          </Typography>
          <Typography variant="body2" paragraph>
            1. <strong>Register/Login:</strong> Create an account or login to receive a JWT token
          </Typography>
          <Typography variant="body2" paragraph>
            2. <strong>Include Token:</strong> Add the JWT token in the Authorization header for protected endpoints:
            <br />
            <code style={{ backgroundColor: '#e0e0e0', padding: '2px 6px', borderRadius: '4px' }}>
              Authorization: Bearer YOUR_TOKEN
            </code>
          </Typography>
          <Typography variant="body2" paragraph>
            3. <strong>API Gateway:</strong> All requests go through the API Gateway at <code>{API_BASE_URL}</code>
          </Typography>
          <Typography variant="body2">
            4. <strong>Microservices:</strong> The gateway routes requests to the appropriate microservice based on the URL path
          </Typography>
        </Paper>
      </Box>
    </Container>
  );
}

export default ApiDocumentation;
