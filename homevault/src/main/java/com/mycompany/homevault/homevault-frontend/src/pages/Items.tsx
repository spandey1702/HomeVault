import React, { useState, useEffect } from 'react';
import {
  Typography, Box, Button, Paper, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, Stack, Chip, IconButton,
  Alert, Tooltip
} from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, Refresh as RefreshIcon } from '@mui/icons-material';
import api from '../config/api';
import { useNotification } from '../contexts/NotificationContext';
import LoadingSpinner from '../components/LoadingSpinner';

interface Item {
  id?: number;
  name: string;
  description?: string;
  category?: string;
  location?: string;
  quantity?: number;
  price?: number;
  purchaseDate?: string;
  expiryDate?: string;
  brand?: string;
  model?: string;
  notes?: string;
  isExpiring?: boolean;
  isExpired?: boolean;
}

const Items: React.FC = () => {
  const [items, setItems] = useState<Item[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [open, setOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<Item | null>(null);
  const [formData, setFormData] = useState<Item>({
    name: '',
    description: '',
    category: '',
    location: '',
    quantity: 1,
  });
  const [formErrors, setFormErrors] = useState<{ [key: string]: string }>({});

  const { showError, showSuccess } = useNotification();

  useEffect(() => {
    fetchItems();
  }, []);

  const fetchItems = async () => {
    try {
      setLoading(true);
      const response = await api.get('/items');
      setItems(response.data);
    } catch (error) {
      console.error('Failed to fetch items:', error);
      showError('Failed to load items');
    } finally {
      setLoading(false);
    }
  };

  const validateForm = (): boolean => {
    const errors: { [key: string]: string } = {};

    if (!formData.name?.trim()) {
      errors.name = 'Item name is required';
    }

    if (formData.quantity !== undefined && formData.quantity < 0) {
      errors.quantity = 'Quantity cannot be negative';
    }

    if (formData.price !== undefined && formData.price < 0) {
      errors.price = 'Price cannot be negative';
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async () => {
    if (!validateForm()) {
      return;
    }

    setSubmitting(true);
    try {
      // Clean up the data before sending
      const cleanData = { ...formData };
      if (cleanData.price === 0 || cleanData.price === undefined) {
        delete cleanData.price;
      }
      
      if (editingItem) {
        await api.put(`/items/${editingItem.id}`, cleanData);
        showSuccess('Item updated successfully');
      } else {
        await api.post('/items', cleanData);
        showSuccess('Item added successfully');
      }
      fetchItems();
      handleClose();
    } catch (error: any) {
      console.error('Failed to save item:', error);
      if (error.response?.status === 400) {
        showError(error.response?.data?.error || 'Invalid item data');
      } else {
        showError('Failed to save item');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: number, name: string) => {
    if (window.confirm(`Are you sure you want to delete "${name}"?`)) {
      try {
        await api.delete(`/items/${id}`);
        showSuccess('Item deleted successfully');
        fetchItems();
      } catch (error) {
        console.error('Failed to delete item:', error);
        showError('Failed to delete item');
      }
    }
  };

  const handleClose = () => {
    setOpen(false);
    setEditingItem(null);
    setFormData({
      name: '',
      description: '',
      category: '',
      location: '',
      quantity: 1,
    });
    setFormErrors({});
  };

  const handleEdit = (item: Item) => {
    setEditingItem(item);
    setFormData({ ...item });
    setOpen(true);
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

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  const formatPrice = (price?: number) => {
    if (price === undefined || price === null) return '-';
    return `$${price.toFixed(2)}`;
  };

  if (loading) {
    return <LoadingSpinner message="Loading items..." />;
  }

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">
          Inventory Items ({items.length})
        </Typography>
        <Stack direction="row" spacing={2}>
          <Tooltip title="Refresh">
            <IconButton onClick={fetchItems} disabled={loading}>
              <RefreshIcon />
            </IconButton>
          </Tooltip>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setOpen(true)}
          >
            Add Item
          </Button>
        </Stack>
      </Stack>

      {items.length === 0 ? (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="h6" gutterBottom>
            No items in your inventory
          </Typography>
          <Typography variant="body1" color="text.secondary" mb={2}>
            Start building your home inventory by adding your first item.
          </Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setOpen(true)}>
            Add Your First Item
          </Button>
        </Paper>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Category</TableCell>
                <TableCell>Location</TableCell>
                <TableCell>Quantity</TableCell>
                <TableCell>Price</TableCell>
                <TableCell>Expiry Date</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {items.map((item) => (
                <TableRow key={item.id} hover>
                  <TableCell>
                    <Box>
                      <Typography variant="subtitle2">{item.name}</Typography>
                      {item.brand && (
                        <Typography variant="caption" color="text.secondary">
                          {item.brand} {item.model}
                        </Typography>
                      )}
                    </Box>
                  </TableCell>
                  <TableCell>{item.category || '-'}</TableCell>
                  <TableCell>{item.location || '-'}</TableCell>
                  <TableCell>{item.quantity || 1}</TableCell>
                  <TableCell>{formatPrice(item.price)}</TableCell>
                  <TableCell>{formatDate(item.expiryDate)}</TableCell>
                  <TableCell>{getStatusChip(item)}</TableCell>
                  <TableCell align="right">
                    <Tooltip title="Edit">
                      <IconButton size="small" onClick={() => handleEdit(item)}>
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Delete">
                      <IconButton 
                        size="small" 
                        onClick={() => handleDelete(item.id!, item.name)}
                        color="error"
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
        <DialogTitle>
          {editingItem ? 'Edit Item' : 'Add New Item'}
        </DialogTitle>
        <DialogContent>
          <Stack spacing={2} mt={1}>
            <TextField
              label="Name"
              value={formData.name}
              onChange={(e) => {
                setFormData({ ...formData, name: e.target.value });
                if (formErrors.name) setFormErrors(prev => ({ ...prev, name: '' }));
              }}
              error={!!formErrors.name}
              helperText={formErrors.name}
              fullWidth
              required
              disabled={submitting}
            />
            <TextField
              label="Description"
              value={formData.description || ''}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              fullWidth
              multiline
              rows={2}
              disabled={submitting}
            />
            <Stack direction="row" spacing={2}>
              <TextField
                label="Category"
                value={formData.category || ''}
                onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                fullWidth
                disabled={submitting}
              />
              <TextField
                label="Location"
                value={formData.location || ''}
                onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                fullWidth
                disabled={submitting}
              />
            </Stack>
            <Stack direction="row" spacing={2}>
              <TextField
                label="Quantity"
                type="number"
                value={formData.quantity || 1}
                onChange={(e) => {
                  setFormData({ ...formData, quantity: parseInt(e.target.value) || 1 });
                  if (formErrors.quantity) setFormErrors(prev => ({ ...prev, quantity: '' }));
                }}
                error={!!formErrors.quantity}
                helperText={formErrors.quantity}
                fullWidth
                disabled={submitting}
                inputProps={{ min: 0 }}
              />
              <TextField
                label="Price"
                type="number"
                value={formData.price || ''}
                onChange={(e) => {
                  setFormData({ ...formData, price: parseFloat(e.target.value) || undefined });
                  if (formErrors.price) setFormErrors(prev => ({ ...prev, price: '' }));
                }}
                error={!!formErrors.price}
                helperText={formErrors.price}
                fullWidth
                disabled={submitting}
                inputProps={{ min: 0, step: 0.01 }}
              />
            </Stack>
            <Stack direction="row" spacing={2}>
              <TextField
                label="Brand"
                value={formData.brand || ''}
                onChange={(e) => setFormData({ ...formData, brand: e.target.value })}
                fullWidth
                disabled={submitting}
              />
              <TextField
                label="Model"
                value={formData.model || ''}
                onChange={(e) => setFormData({ ...formData, model: e.target.value })}
                fullWidth
                disabled={submitting}
              />
            </Stack>
            <Stack direction="row" spacing={2}>
              <TextField
                label="Purchase Date"
                type="date"
                value={formData.purchaseDate || ''}
                onChange={(e) => setFormData({ ...formData, purchaseDate: e.target.value })}
                fullWidth
                InputLabelProps={{ shrink: true }}
                disabled={submitting}
              />
              <TextField
                label="Expiry Date"
                type="date"
                value={formData.expiryDate || ''}
                onChange={(e) => setFormData({ ...formData, expiryDate: e.target.value })}
                fullWidth
                InputLabelProps={{ shrink: true }}
                disabled={submitting}
              />
            </Stack>
            <TextField
              label="Notes"
              value={formData.notes || ''}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              fullWidth
              multiline
              rows={2}
              disabled={submitting}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose} disabled={submitting}>
            Cancel
          </Button>
          <Button 
            onClick={handleSubmit} 
            variant="contained"
            disabled={submitting}
          >
            {submitting ? (
              <LoadingSpinner size={20} message="" />
            ) : (
              editingItem ? 'Update' : 'Add'
            )} Item
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Items;