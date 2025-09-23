import React, { useState, useEffect } from 'react';
import { 
  Paper, Typography, Box, Stack, Card, CardContent, 
  List, ListItem, ListItemText, Chip, Button 
} from '@mui/material';
import { Link } from 'react-router-dom';
import { Warning, CheckCircle, Inventory, Today } from '@mui/icons-material';
import api from '../config/api';
import { useNotification } from '../contexts/NotificationContext';
import LoadingSpinner from '../components/LoadingSpinner';

interface Item {
  id: number;
  name: string;
  category?: string;
  location?: string;
  quantity?: number;
  expiryDate?: string;
  isExpiring?: boolean;
  isExpired?: boolean;
}

interface DashboardStats {
  totalItems: number;
  expiringItems: number;
  expiredItems: number;
}

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats>({ 
    totalItems: 0, 
    expiringItems: 0, 
    expiredItems: 0 
  });
  const [expiringItems, setExpiringItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  
  const { showError } = useNotification();

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      // Fetch all items for stats
      const itemsResponse = await api.get('/items');
      const allItems: Item[] = itemsResponse.data;
      
      // Fetch expiring items
      const expiringResponse = await api.get('/items/expiring?days=7');
      const expiring: Item[] = expiringResponse.data;
      
      // Calculate stats
      const expired = allItems.filter(item => item.isExpired);
      const expiringSoon = allItems.filter(item => item.isExpiring && !item.isExpired);
      
      setStats({
        totalItems: allItems.length,
        expiringItems: expiringSoon.length,
        expiredItems: expired.length,
      });
      
      setExpiringItems(expiring.slice(0, 5)); // Show top 5 expiring items
    } catch (error) {
      console.error('Failed to fetch dashboard data:', error);
      showError('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'No date';
    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  const getStatusChip = (item: Item) => {
    if (item.isExpired) {
      return <Chip label="Expired" color="error" size="small" />;
    }
    if (item.isExpiring) {
      return <Chip label="Expiring Soon" color="warning" size="small" />;
    }
    return <Chip label="Good" color="success" size="small" />;
  };

  if (loading) {
    return <LoadingSpinner message="Loading dashboard..." />;
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        HomeVault Dashboard
      </Typography>
      
      {/* Stats Cards */}
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={3} mb={4}>
        <Card sx={{ flex: 1 }}>
          <CardContent>
            <Box display="flex" alignItems="center" gap={2}>
              <Inventory color="primary" />
              <Box>
                <Typography variant="h4" component="div">
                  {stats.totalItems}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Total Items
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>

        <Card sx={{ flex: 1 }}>
          <CardContent>
            <Box display="flex" alignItems="center" gap={2}>
              <Warning color="warning" />
              <Box>
                <Typography variant="h4" component="div">
                  {stats.expiringItems}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Expiring Soon
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>

        <Card sx={{ flex: 1 }}>
          <CardContent>
            <Box display="flex" alignItems="center" gap={2}>
              <Today color="error" />
              <Box>
                <Typography variant="h4" component="div">
                  {stats.expiredItems}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Expired Items
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Stack>
      
      <Stack spacing={3}>
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={3}>
          {/* Expiring Items */}
          <Paper sx={{ p: 3, flex: 1 }}>
            <Typography variant="h6" gutterBottom>
              Items Expiring Soon
            </Typography>
            {expiringItems.length > 0 ? (
              <>
                <List dense>
                  {expiringItems.map((item) => (
                    <ListItem key={item.id} sx={{ px: 0 }}>
                      <ListItemText
                        primary={item.name}
                        secondary={`Expires: ${formatDate(item.expiryDate)} • ${item.location || 'No location'}`}
                      />
                      {getStatusChip(item)}
                    </ListItem>
                  ))}
                </List>
                <Link to="/items" style={{ textDecoration: 'none' }}>
                  <Button variant="outlined" size="small" sx={{ mt: 2 }}>
                    View All Items
                  </Button>
                </Link>
              </>
            ) : (
              <Box textAlign="center" py={4}>
                <CheckCircle color="success" sx={{ fontSize: 48, mb: 2 }} />
                <Typography variant="body1" color="text.secondary">
                  No items expiring soon
                </Typography>
              </Box>
            )}
          </Paper>
          
          <Paper sx={{ p: 3, textAlign: 'center', flex: 1 }}>
            <Typography variant="h6" gutterBottom>
              Inventory Items
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Manage your home inventory
            </Typography>
            <Link to="/items" style={{ textDecoration: 'none' }}>
              <Button variant="contained" sx={{ mt: 2 }}>
                View Items →
              </Button>
            </Link>
          </Paper>
        </Stack>
        
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={3}>
          <Paper sx={{ p: 3, textAlign: 'center', flex: 1 }}>
            <Typography variant="h6" gutterBottom>
              Documents
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Store important documents
            </Typography>
            <Button variant="outlined" sx={{ mt: 2 }} disabled>
              Coming Soon
            </Button>
          </Paper>
          
          <Paper sx={{ p: 3, textAlign: 'center', flex: 1 }}>
            <Typography variant="h6" gutterBottom>
              Family Sharing
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Share with household members
            </Typography>
            <Button variant="outlined" sx={{ mt: 2 }} disabled>
              Coming Soon
            </Button>
          </Paper>
        </Stack>
      </Stack>
    </Box>
  );
};

export default Dashboard;