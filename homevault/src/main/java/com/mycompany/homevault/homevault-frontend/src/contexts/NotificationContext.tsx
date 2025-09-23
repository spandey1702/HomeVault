import React, { createContext, useContext, useState, ReactNode } from 'react';
import { Snackbar, Alert, AlertColor } from '@mui/material';

interface NotificationContextType {
  showNotification: (message: string, severity?: AlertColor) => void;
  showError: (message: string) => void;
  showSuccess: (message: string) => void;
  showWarning: (message: string) => void;
  showInfo: (message: string) => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

interface NotificationProviderProps {
  children: ReactNode;
}

interface Notification {
  message: string;
  severity: AlertColor;
  open: boolean;
}

export const NotificationProvider: React.FC<NotificationProviderProps> = ({ children }) => {
  const [notification, setNotification] = useState<Notification>({
    message: '',
    severity: 'info',
    open: false,
  });

  const showNotification = (message: string, severity: AlertColor = 'info') => {
    setNotification({
      message,
      severity,
      open: true,
    });
  };

  const showError = (message: string) => showNotification(message, 'error');
  const showSuccess = (message: string) => showNotification(message, 'success');
  const showWarning = (message: string) => showNotification(message, 'warning');
  const showInfo = (message: string) => showNotification(message, 'info');

  const handleClose = () => {
    setNotification(prev => ({ ...prev, open: false }));
  };

  const value: NotificationContextType = {
    showNotification,
    showError,
    showSuccess,
    showWarning,
    showInfo,
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
      <Snackbar
        open={notification.open}
        autoHideDuration={6000}
        onClose={handleClose}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert onClose={handleClose} severity={notification.severity} sx={{ width: '100%' }}>
          {notification.message}
        </Alert>
      </Snackbar>
    </NotificationContext.Provider>
  );
};

export const useNotification = () => {
  const context = useContext(NotificationContext);
  if (context === undefined) {
    throw new Error('useNotification must be used within a NotificationProvider');
  }
  return context;
};