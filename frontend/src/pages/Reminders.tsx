import React, { useEffect, useState } from 'react';
import {
  Box, Typography, Paper, Stack, Button, TextField, Dialog,
  DialogTitle, DialogContent, DialogActions, List, ListItem,
  ListItemText, ListItemSecondaryAction, IconButton, Chip,
  CircularProgress, Alert, Divider, MenuItem, Select, InputLabel, FormControl
} from '@mui/material';
import { Add, CheckCircle, Delete } from '@mui/icons-material';
import api from '../config/api';

interface Household { id: number; name: string; }
interface Reminder {
  id: number; title: string; description: string;
  dueDate: string; status: 'PENDING' | 'DONE';
  householdId: number | null; householdName: string | null;
  createdByName: string;
}

const Reminders: React.FC = () => {
  const [reminders, setReminders] = useState<Reminder[]>([]);
  const [households, setHouseholds] = useState<Household[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // New reminder form
  const [open, setOpen]             = useState(false);
  const [title, setTitle]           = useState('');
  const [description, setDesc]      = useState('');
  const [dueDate, setDueDate]       = useState('');
  const [householdId, setHhId]      = useState<number | ''>('');

  const load = () => {
    Promise.all([
      api.get('/reminders'),
      api.get('/households'),
    ]).then(([rRes, hRes]) => {
      setReminders(rRes.data);
      setHouseholds(hRes.data);
    }).catch(() => setError('Could not load reminders.'))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleCreate = async () => {
    try {
      await api.post('/reminders', {
        title, description,
        dueDate:     dueDate || null,
        householdId: householdId || null,
      });
      setOpen(false); setTitle(''); setDesc(''); setDueDate(''); setHhId('');
      load();
    } catch { setError('Failed to create reminder.'); }
  };

  const handleDone = async (id: number) => {
    try { await api.patch(`/reminders/${id}/done`); load(); }
    catch { setError('Failed to mark reminder as done.'); }
  };

  const handleDelete = async (id: number) => {
    try { await api.delete(`/reminders/${id}`); load(); }
    catch { setError('Failed to delete reminder.'); }
  };

  const pending = reminders.filter(r => r.status === 'PENDING');
  const done    = reminders.filter(r => r.status === 'DONE');

  const isOverdue = (dueDate: string) =>
    dueDate && new Date(dueDate) < new Date(new Date().toDateString());

  if (loading) return <Box display="flex" justifyContent="center" mt={6}><CircularProgress /></Box>;

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight={700}>🔔 Family Reminders</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => setOpen(true)}>
          Add Reminder
        </Button>
      </Stack>

      {error && <Alert severity="error" onClose={() => setError('')} sx={{ mb: 2 }}>{error}</Alert>}

      {/* Pending */}
      <Typography variant="h6" gutterBottom>Pending ({pending.length})</Typography>
      {pending.length === 0
        ? <Paper sx={{ p: 3, mb: 3, textAlign: 'center' }}>
            <Typography color="text.secondary">All caught up! No pending reminders ✅</Typography>
          </Paper>
        : <Paper sx={{ mb: 3 }}>
            <List disablePadding>
              {pending.map((r, i) => (
                <React.Fragment key={r.id}>
                  {i > 0 && <Divider />}
                  <ListItem>
                    <ListItemText
                      primary={
                        <Stack direction="row" spacing={1} alignItems="center">
                          <span>{r.title}</span>
                          {r.dueDate && isOverdue(r.dueDate) &&
                            <Chip label="Overdue" color="error" size="small" />}
                          {r.householdName &&
                            <Chip label={r.householdName} size="small" variant="outlined" />}
                        </Stack>
                      }
                      secondary={[
                        r.description,
                        r.dueDate ? `Due: ${r.dueDate}` : null,
                        `Added by ${r.createdByName}`,
                      ].filter(Boolean).join(' · ')}
                    />
                    <ListItemSecondaryAction>
                      <IconButton color="success" onClick={() => handleDone(r.id)} title="Mark done">
                        <CheckCircle />
                      </IconButton>
                      <IconButton color="error" onClick={() => handleDelete(r.id)} title="Delete">
                        <Delete />
                      </IconButton>
                    </ListItemSecondaryAction>
                  </ListItem>
                </React.Fragment>
              ))}
            </List>
          </Paper>
      }

      {/* Done */}
      {done.length > 0 && (
        <>
          <Typography variant="h6" gutterBottom color="text.secondary">
            Completed ({done.length})
          </Typography>
          <Paper>
            <List disablePadding>
              {done.map((r, i) => (
                <React.Fragment key={r.id}>
                  {i > 0 && <Divider />}
                  <ListItem sx={{ opacity: 0.6 }}>
                    <ListItemText
                      primary={<s>{r.title}</s>}
                      secondary={`Done · ${r.createdByName}`}
                    />
                    <ListItemSecondaryAction>
                      <IconButton color="error" onClick={() => handleDelete(r.id)}>
                        <Delete />
                      </IconButton>
                    </ListItemSecondaryAction>
                  </ListItem>
                </React.Fragment>
              ))}
            </List>
          </Paper>
        </>
      )}

      {/* Create dialog */}
      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>New Reminder</DialogTitle>
        <DialogContent>
          <TextField autoFocus fullWidth label="Title" value={title}
            onChange={e => setTitle(e.target.value)} margin="normal" required />
          <TextField fullWidth label="Description (optional)" value={description}
            onChange={e => setDesc(e.target.value)} margin="normal" multiline rows={2} />
          <TextField fullWidth label="Due Date (optional)" type="date"
            value={dueDate} onChange={e => setDueDate(e.target.value)}
            margin="normal" InputLabelProps={{ shrink: true }} />
          <FormControl fullWidth margin="normal">
            <InputLabel>Household (optional)</InputLabel>
            <Select value={householdId} label="Household (optional)"
              onChange={e => setHhId(e.target.value as number | '')}>
              <MenuItem value=""><em>Personal reminder</em></MenuItem>
              {households.map(h => <MenuItem key={h.id} value={h.id}>{h.name}</MenuItem>)}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreate} disabled={!title.trim()}>Add</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Reminders;
