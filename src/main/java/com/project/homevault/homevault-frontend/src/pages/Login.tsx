import React, { useState } from 'react';
import { Box, Paper, TextField, Button, Typography } from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import api from '../config/api';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await api.post('/auth/login', { email, password });
      localStorage.setItem('auth_token', response.data.token);
      navigate('/dashboard');
    } catch (error) {
      console.error('Login failed:', error);
    }
  };

  return (
    <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
      <Paper sx={{ p: 4, maxWidth: 400, width: '100%' }}>
        <Typography variant="h5" align="center" gutterBottom>
          Login to HomeVault
        </Typography>
        
        <form onSubmit={handleSubmit}>
          <TextField
            fullWidth
            label="Email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            margin="normal"
            required
          />
          
          <TextField
            fullWidth
            label="Password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            margin="normal"
            required
          />
          
          <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 3, mb: 2 }}
          >
            Login
          </Button>
        </form>
        
        <Box textAlign="center">
          <Link to="/register" style={{ textDecoration: 'none' }}>
            <Typography variant="body2" color="primary">
              Don't have an account? Register here
            </Typography>
          </Link>
        </Box>
      </Paper>
    </Box>
  );
};

export default Login;