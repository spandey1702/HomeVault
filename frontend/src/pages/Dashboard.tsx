import React, { useEffect, useState } from 'react';
import {
  Box, Typography, Paper, Stack, Chip, CircularProgress, Alert,
  List, ListItem, ListItemText, Divider
} from '@mui/material';
import {
  Inventory2Outlined, WarningAmberOutlined, CheckCircleOutline,
  PeopleOutline, NotificationsOutlined
} from '@mui/icons-material';
import { Link } from 'react-router-dom';
import api from '../config/api';

interface DashboardStats {
  totalItems: number;
  expiringItems: number;
  expiredItems: number;
  totalMembers: number;
  pendingReminders: number;
  recentlyExpiring: Array<{ id: number; name: string; expiryDate: string; householdName: string }>;
  upcomingReminders: Array<{ id: number; title: string; dueDate: string; status: string }>;
}

const StatCard: React.FC<{
  label: string; value: number | string; icon: React.ReactNode; color: string;
}> = ({ label, value, icon, color }) => (
  <Paper sx={{ p: 3, flex: 1, minWidth: 160 }}>
    <Stack direction="row" alignItems="center" spacing={1} mb={1}>
      <Box sx={{ color }}>{icon}</Box>
      <Typography variant="body2" color="text.secondary">{label}</Typography>
    </Stack>
    <Typography variant="h4" fontWeight={700}>{value}</Typography>
  </Paper>
);

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/dashboard/stats')
      .then(r => setStats(r.data))
      .catch(() => setError('Could not load dashboard stats.'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Box display="flex" justifyContent="center" mt={6}><CircularProgress /></Box>;
  if (error)   return <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>;
  if (!stats)  return null;

  return (
    <Box>
      <Typography variant="h4" fontWeight={700} gutterBottom>Family Dashboard</Typography>

      {/* ── Stat cards ── */}
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} flexWrap="wrap" mb={4}>
        <StatCard label="Total Items"       value={stats.totalItems}       icon={<Inventory2Outlined />}      color="primary.main" />
        <StatCard label="Expiring (7 days)" value={stats.expiringItems}    icon={<WarningAmberOutlined />}    color="warning.main" />
        <StatCard label="Expired"           value={stats.expiredItems}     icon={<CheckCircleOutline />}      color="error.main" />
        <StatCard label="Members"           value={stats.totalMembers}     icon={<PeopleOutline />}           color="success.main" />
        <StatCard label="Reminders Pending" value={stats.pendingReminders} icon={<NotificationsOutlined />}  color="info.main" />
      </Stack>

      <Stack direction={{ xs: 'column', md: 'row' }} spacing={3}>

        {/* ── Expiring soon ── */}
        <Paper sx={{ p: 3, flex: 1 }}>
          <Stack direction="row" justifyContent="space-between" alignItems="center" mb={1}>
            <Typography variant="h6">⚠️ Expiring Soon</Typography>
            <Link to="/items" style={{ textDecoration: 'none' }}>
              <Typography variant="body2" color="primary">View all →</Typography>
            </Link>
          </Stack>
          {stats.recentlyExpiring.length === 0
            ? <Typography color="text.secondary">No items expiring in the next 7 days 🎉</Typography>
            : <List dense disablePadding>
                {stats.recentlyExpiring.map((item, i) => (
                  <React.Fragment key={item.id}>
                    {i > 0 && <Divider />}
                    <ListItem disableGutters>
                      <ListItemText
                        primary={item.name}
                        secondary={`Expires ${item.expiryDate}${item.householdName ? ` · ${item.householdName}` : ''}`}
                      />
                      <Chip label="Soon" color="warning" size="small" />
                    </ListItem>
                  </React.Fragment>
                ))}
              </List>
          }
        </Paper>

        {/* ── Upcoming reminders ── */}
        <Paper sx={{ p: 3, flex: 1 }}>
          <Stack direction="row" justifyContent="space-between" alignItems="center" mb={1}>
            <Typography variant="h6">🔔 Upcoming Reminders</Typography>
            <Link to="/reminders" style={{ textDecoration: 'none' }}>
              <Typography variant="body2" color="primary">View all →</Typography>
            </Link>
          </Stack>
          {stats.upcomingReminders.length === 0
            ? <Typography color="text.secondary">No pending reminders ✅</Typography>
            : <List dense disablePadding>
                {stats.upcomingReminders.map((r, i) => (
                  <React.Fragment key={r.id}>
                    {i > 0 && <Divider />}
                    <ListItem disableGutters>
                      <ListItemText
                        primary={r.title}
                        secondary={r.dueDate ? `Due ${r.dueDate}` : 'No due date'}
                      />
                      <Chip label={r.status} color="info" size="small" />
                    </ListItem>
                  </React.Fragment>
                ))}
              </List>
          }
        </Paper>

      </Stack>

      {/* ── Quick links ── */}
      <Stack direction="row" spacing={2} mt={4} flexWrap="wrap">
        {[
          { to: '/items',      label: '📦 Manage Inventory' },
          { to: '/household',  label: '👨‍👩‍👧 Family Household' },
          { to: '/reminders',  label: '🔔 Reminders' },
        ].map(link => (
          <Link key={link.to} to={link.to} style={{ textDecoration: 'none' }}>
            <Paper sx={{ px: 3, py: 1.5, cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}>
              <Typography>{link.label}</Typography>
            </Paper>
          </Link>
        ))}
      </Stack>
    </Box>
  );
};

export default Dashboard;
