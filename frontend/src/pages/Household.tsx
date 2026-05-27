import React, { useEffect, useState } from 'react';
import {
  Box, Typography, Paper, Stack, Button, TextField, Dialog,
  DialogTitle, DialogContent, DialogActions, List, ListItem,
  ListItemText, ListItemSecondaryAction, IconButton, Chip,
  CircularProgress, Alert, Divider
} from '@mui/material';
import { PersonRemove, Add, ExitToApp } from '@mui/icons-material';
import api from '../config/api';

interface Member { id: number; name: string; email: string; }
interface Household {
  id: number; name: string; description: string;
  ownerId: number; ownerName: string;
  members: Member[];
  totalItems: number; expiringItems: number; pendingReminders: number;
}

const HouseholdPage: React.FC = () => {
  const [households, setHouseholds] = useState<Household[]>([]);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState('');

  // Create dialog
  const [createOpen, setCreateOpen] = useState(false);
  const [newName, setNewName]       = useState('');
  const [newDesc, setNewDesc]       = useState('');

  // Invite dialog
  const [inviteOpen, setInviteOpen]   = useState(false);
  const [inviteHhId, setInviteHhId]   = useState<number | null>(null);
  const [inviteEmail, setInviteEmail] = useState('');

  const currentUserId = Number(
    JSON.parse(atob((localStorage.getItem('auth_token') || '..').split('.')[1] || 'e30='))?.sub || 0
  );

  const load = () => {
    setLoading(true);
    api.get('/households')
      .then(r => setHouseholds(r.data))
      .catch(() => setError('Could not load households.'))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleCreate = async () => {
    try {
      await api.post('/households', { name: newName, description: newDesc });
      setCreateOpen(false); setNewName(''); setNewDesc('');
      load();
    } catch { setError('Failed to create household.'); }
  };

  const handleInvite = async () => {
    if (!inviteHhId) return;
    try {
      await api.post(`/households/${inviteHhId}/invite`, { email: inviteEmail });
      setInviteOpen(false); setInviteEmail('');
      load();
    } catch { setError('Failed to invite member — check the email address.'); }
  };

  const handleRemove = async (hhId: number, memberId: number) => {
    try {
      await api.delete(`/households/${hhId}/members/${memberId}`);
      load();
    } catch { setError('Failed to remove member.'); }
  };

  const handleLeave = async (hhId: number) => {
    try {
      await api.delete(`/households/${hhId}/leave`);
      load();
    } catch { setError('Failed to leave household.'); }
  };

  if (loading) return <Box display="flex" justifyContent="center" mt={6}><CircularProgress /></Box>;

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight={700}>👨‍👩‍👧 Family Household</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => setCreateOpen(true)}>
          New Household
        </Button>
      </Stack>

      {error && <Alert severity="error" onClose={() => setError('')} sx={{ mb: 2 }}>{error}</Alert>}

      {households.length === 0
        ? <Paper sx={{ p: 4, textAlign: 'center' }}>
            <Typography color="text.secondary">
              You're not in any household yet. Create one and invite your family!
            </Typography>
          </Paper>
        : households.map(hh => (
            <Paper key={hh.id} sx={{ p: 3, mb: 3 }}>
              <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                <Box>
                  <Typography variant="h6">{hh.name}</Typography>
                  {hh.description && <Typography color="text.secondary">{hh.description}</Typography>}
                  <Stack direction="row" spacing={1} mt={1}>
                    <Chip label={`${hh.totalItems} items`}           size="small" />
                    <Chip label={`${hh.expiringItems} expiring`}     size="small" color="warning" />
                    <Chip label={`${hh.pendingReminders} reminders`} size="small" color="info" />
                  </Stack>
                </Box>
                <Stack direction="row" spacing={1}>
                  <Button size="small" variant="outlined" startIcon={<Add />}
                    onClick={() => { setInviteHhId(hh.id); setInviteOpen(true); }}>
                    Invite
                  </Button>
                  <Button size="small" color="error" startIcon={<ExitToApp />}
                    onClick={() => handleLeave(hh.id)}>
                    Leave
                  </Button>
                </Stack>
              </Stack>

              <Divider sx={{ my: 2 }} />
              <Typography variant="subtitle2" gutterBottom>Members</Typography>
              <List dense disablePadding>
                {hh.members.map(m => (
                  <ListItem key={m.id} disableGutters>
                    <ListItemText
                      primary={m.name + (m.id === hh.ownerId ? ' 👑' : '')}
                      secondary={m.email}
                    />
                    {hh.ownerId === currentUserId && m.id !== hh.ownerId && (
                      <ListItemSecondaryAction>
                        <IconButton edge="end" size="small" color="error"
                          onClick={() => handleRemove(hh.id, m.id)}>
                          <PersonRemove fontSize="small" />
                        </IconButton>
                      </ListItemSecondaryAction>
                    )}
                  </ListItem>
                ))}
              </List>
            </Paper>
          ))
      }

      {/* Create household dialog */}
      <Dialog open={createOpen} onClose={() => setCreateOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>Create Household</DialogTitle>
        <DialogContent>
          <TextField autoFocus fullWidth label="Household Name" value={newName}
            onChange={e => setNewName(e.target.value)} margin="normal" required />
          <TextField fullWidth label="Description (optional)" value={newDesc}
            onChange={e => setNewDesc(e.target.value)} margin="normal" multiline rows={2} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreate} disabled={!newName.trim()}>Create</Button>
        </DialogActions>
      </Dialog>

      {/* Invite dialog */}
      <Dialog open={inviteOpen} onClose={() => setInviteOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>Invite Family Member</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" mb={2}>
            They must already have a HomeVault account.
          </Typography>
          <TextField autoFocus fullWidth label="Email Address" type="email"
            value={inviteEmail} onChange={e => setInviteEmail(e.target.value)} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setInviteOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleInvite} disabled={!inviteEmail.trim()}>Invite</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default HouseholdPage;
