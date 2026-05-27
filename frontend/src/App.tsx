import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, Link, useNavigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { AppBar, Toolbar, Typography, Container, Button, Stack } from '@mui/material';
import Dashboard  from './pages/Dashboard';
import Login      from './pages/Login';
import Register   from './pages/Register';
import Items      from './pages/Items';
import HouseholdPage from './pages/Household';
import Reminders  from './pages/Reminders';

const theme = createTheme({
  palette: {
    primary:   { main: '#1976d2' },
    secondary: { main: '#dc004e' },
  },
});

const NavBar: React.FC = () => {
  const navigate = useNavigate();
  const isAuth = !!localStorage.getItem('auth_token');

  const logout = () => {
    localStorage.removeItem('auth_token');
    navigate('/login');
  };

  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
          🏠 HomeVault
        </Typography>
        {isAuth && (
          <Stack direction="row" spacing={1}>
            {[
              { to: '/dashboard', label: 'Dashboard' },
              { to: '/items',     label: 'Inventory'  },
              { to: '/household', label: 'Family'     },
              { to: '/reminders', label: 'Reminders'  },
            ].map(link => (
              <Button key={link.to} color="inherit" component={Link} to={link.to}>
                {link.label}
              </Button>
            ))}
            <Button color="inherit" onClick={logout}>Logout</Button>
          </Stack>
        )}
      </Toolbar>
    </AppBar>
  );
};

const PrivateRoute: React.FC<{ element: React.ReactElement }> = ({ element }) => {
  const isAuth = !!localStorage.getItem('auth_token');
  return isAuth ? element : <Navigate to="/login" replace />;
};

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <div className="App">
          <NavBar />
          <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
            <Routes>
              <Route path="/" element={
                localStorage.getItem('auth_token')
                  ? <Navigate to="/dashboard" replace />
                  : <Navigate to="/login" replace />
              } />
              <Route path="/login"     element={<Login />} />
              <Route path="/register"  element={<Register />} />
              <Route path="/dashboard" element={<PrivateRoute element={<Dashboard />} />} />
              <Route path="/items"     element={<PrivateRoute element={<Items />} />} />
              <Route path="/household" element={<PrivateRoute element={<HouseholdPage />} />} />
              <Route path="/reminders" element={<PrivateRoute element={<Reminders />} />} />
            </Routes>
          </Container>
        </div>
      </Router>
    </ThemeProvider>
  );
}

export default App;
