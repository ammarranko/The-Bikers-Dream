import './App.css';
import Auth from './pages/auth/Auth.jsx';
import Home from './pages/home/Home.jsx';
import History from './pages/history/History.jsx';
import ProtectedRoute from './ProtectedRoute.jsx';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

function App() {
  return (
    <Router>
      <Routes>
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
          path="/history"
          element={
            <ProtectedRoute>
              <History />
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
