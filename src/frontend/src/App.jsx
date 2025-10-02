import { Routes, Route } from 'react-router-dom';
import Home from './pages/home/home';
import Auth from './pages/auth/auth';

function App() {
  return (
    <Routes>
        <Route path='/' element={<Auth />} />
        <Route path='/home' element={<Home />} />
    </Routes>
  );
}

export default App;
