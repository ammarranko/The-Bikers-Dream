import logo from './assets/logo.png';
import './App.css';
import Header from './Header.jsx';
import Auth from './auth/auth.jsx';
import Home from './home/Home.jsx';
import ProtectedRoute from './ProtectedRoute.jsx';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Test from "./test/Test";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Auth />} />
        <Route path="/auth" element={<Auth />} />
        <Route path="/home" element={
          // Wrap Home component in ProtectedRoute to enforce authentication, so only authenticated users can access it
          <ProtectedRoute>
            <Home />
          </ProtectedRoute>
        } />

        <Route path="/Test" element={
          // Wrap Test component in ProtectedRoute to enforce authentication, so only authenticated users can access it
          <ProtectedRoute>
            <Test />
          </ProtectedRoute>
        } />
        // Redirect any unknown routes to login
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
