import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
// A wrapper for <Route> that redirects to the login
// screen if you're not yet authenticated.
// see example in app.js

const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('jwt_token');
  const location = useLocation();

  if (!token) {
    // Not authenticated, redirect to login
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  // Token exists, render children - let actual API calls handle token validation
  return children;
};

export default ProtectedRoute;
