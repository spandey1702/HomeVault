import React from 'react';
import { Paper, Typography, Box, Stack } from '@mui/material';
import { Link } from 'react-router-dom';

const Dashboard: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        HomeVault Dashboard
      </Typography>
      
      <Stack spacing={3}>
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={3}>
          <Paper sx={{ p: 3, textAlign: 'center', flex: 1 }}>
            <Typography variant="h6" gutterBottom>
              Inventory Items
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Manage your home inventory
            </Typography>
            <Link to="/items" style={{ textDecoration: 'none' }}>
              <Box sx={{ mt: 2, color: 'primary.main', cursor: 'pointer' }}>
                View Items →
              </Box>
            </Link>
          </Paper>
          
          <Paper sx={{ p: 3, textAlign: 'center', flex: 1 }}>
            <Typography variant="h6" gutterBottom>
              Documents
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Store important documents
            </Typography>
            <Box sx={{ mt: 2, color: 'primary.main' }}>
              Coming Soon
            </Box>
          </Paper>
        </Stack>
        
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={3}>
          <Paper sx={{ p: 3, textAlign: 'center', flex: 1 }}>
            <Typography variant="h6" gutterBottom>
              Family Sharing
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Share with household members
            </Typography>
            <Box sx={{ mt: 2, color: 'primary.main' }}>
              Coming Soon
            </Box>
          </Paper>
          
          <Paper sx={{ p: 3, textAlign: 'center', flex: 1 }}>
            <Typography variant="h6" gutterBottom>
              Expiring Items
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Track expiration dates
            </Typography>
            <Box sx={{ mt: 2, color: 'primary.main' }}>
              Coming Soon
            </Box>
          </Paper>
        </Stack>
      </Stack>
    </Box>
  );
};

export default Dashboard;