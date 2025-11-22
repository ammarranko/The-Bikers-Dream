import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App.jsx';
import reportWebVitals from './reportWebVitals';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'leaflet/dist/leaflet.css';
import axios from 'axios';

// Setup global axios interceptor for token expiration
// This must be done before the app renders to catch initial data loading errors
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    // Check for 401 Unauthorized or 403 Forbidden response
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      console.log('Session expired or unauthorized, logging out...');
      
      // Clear all auth data
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('user_email');
      localStorage.removeItem('user_full_name');
      localStorage.removeItem('user_role');
      localStorage.removeItem('username');
      localStorage.removeItem('actual_user_role');
      localStorage.removeItem('tier');
      localStorage.removeItem('flexMoney');
      
      delete axios.defaults.headers.common['Authorization'];
      
      // Redirect to login if not already there
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login?expired=1';
      }
    }
    return Promise.reject(error);
  }
);

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
    <App />
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
