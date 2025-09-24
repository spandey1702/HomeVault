import React, { useState, useEffect } from 'react';
import {
  Typography, Box, Button, Paper, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, Stack, Chip, IconButton
} from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import api from '../config/api';

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
  const [open, setOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<Item | null>(null);
  const [formData, setFormData] = useState<Item>({
    name: '',
    description: '',
    category: '',
    location: '',
    quantity: 1,
  });

  useEffect(() => {
    fetchItems();
  }, []);

  const fetchItems = async () => {
    try {
      const response = await api.get('/items');
      setItems(response.data);
    } catch (error) {
      console.error('Failed to fetch items:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async () => {
    try {
      if (editingItem) {
        await api.put(`/items/${editingItem.id}`, formData);
      } else {
        await api.post('/items', formData);
      }
      fetchItems();
      handleClose();
    } catch (error) {
      console.error('Failed to save item:', error);
    }
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this item?')) {
      try {
        await api.delete(`/items/${id}`);
        fetchItems();
      } catch (error) {
        console.error('Failed to delete item:', error);
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
  };

  const handleEdit = (item: Item) => {
    setEditingItem(item);
    setFormData(item);
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

  if (loading) {
    return (
      <Box>
        <Typography variant="h4" gutterBottom>
          Inventory Items
        </Typography>
        <Typography>Loading...</Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">
          Inventory Items ({items.length})
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setOpen(true)}
        >
          Add Item
        </Button>
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
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {items.map((item) => (
                <TableRow key={item.id}>
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
                  <TableCell>{getStatusChip(item)}</TableCell>
                  <TableCell align="right">
                    <IconButton size="small" onClick={() => handleEdit(item)}>
                      <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton size="small" onClick={() => handleDelete(item.id!)}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
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
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              fullWidth
              required
            />
            <TextField
              label="Description"
              value={formData.description || ''}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              fullWidth
              multiline
              rows={2}
            />
            <Stack direction="row" spacing={2}>
              <TextField
                label="Category"
                value={formData.category || ''}
                onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                fullWidth
              />
              <TextField
                label="Location"
                value={formData.location || ''}
                onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                fullWidth
              />
            </Stack>
            <Stack direction="row" spacing={2}>
              <TextField
                label="Quantity"
                type="number"
                value={formData.quantity || 1}
                onChange={(e) => setFormData({ ...formData, quantity: parseInt(e.target.value) })}
                fullWidth
              />
              <TextField
                label="Price"
                type="number"
                value={formData.price || ''}
                onChange={(e) => setFormData({ ...formData, price: parseFloat(e.target.value) })}
                fullWidth
              />
            </Stack>
            <Stack direction="row" spacing={2}>
              <TextField
                label="Brand"
                value={formData.brand || ''}
                onChange={(e) => setFormData({ ...formData, brand: e.target.value })}
                fullWidth
              />
              <TextField
                label="Model"
                value={formData.model || ''}
                onChange={(e) => setFormData({ ...formData, model: e.target.value })}
                fullWidth
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
              />
              <TextField
                label="Expiry Date"
                type="date"
                value={formData.expiryDate || ''}
                onChange={(e) => setFormData({ ...formData, expiryDate: e.target.value })}
                fullWidth
                InputLabelProps={{ shrink: true }}
              />
            </Stack>
            <TextField
              label="Notes"
              value={formData.notes || ''}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              fullWidth
              multiline
              rows={2}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Cancel</Button>
          <Button onClick={handleSubmit} variant="contained">
            {editingItem ? 'Update' : 'Add'} Item
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Items;