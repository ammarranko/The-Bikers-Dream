import './App.css';
import Auth from './pages/auth/Auth.jsx';
import Home from './pages/home/Home.jsx';
import Billing from './pages/billing/Billing.jsx';
import ProtectedRoute from './ProtectedRoute.jsx';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Pricing from './pages/pricing/Pricing.jsx';


function App() {
  return (
    <Router>
      <Routes>
      <Route path="/pricing" element={<Pricing />} />
        <Route path="/login" element={<Auth />} />
        <Route path="/auth" element={<Auth />} />
        <Route
          path="/home"
          element={
            // Wrap Home component in ProtectedRoute to enforce authentication, so only authenticated users can access it
            <ProtectedRoute>
              <Home />
            </ProtectedRoute>
          }
        />
        <Route
          path="/billing"
          element={
            <ProtectedRoute>
              <Billing />
            </ProtectedRoute>
          }
        />

        {/* // Redirect any unknown routes to login */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
