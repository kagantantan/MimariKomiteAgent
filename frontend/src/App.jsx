import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import ConsultPage from './pages/ConsultPage';
import DiagramPage from './pages/DiagramPage';
import KnowledgePage from './pages/KnowledgePage';

function App() {
  return (
    <Router>
      <div className="flex h-screen w-full bg-slate-50 font-sans text-slate-900 overflow-hidden">
        <Sidebar />
        <Routes>
          <Route path="/" element={<Navigate to="/consult" replace />} />
          <Route path="/consult" element={<ConsultPage />} />
          <Route path="/diagram" element={<DiagramPage />} />
          <Route path="/knowledge" element={<KnowledgePage />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
