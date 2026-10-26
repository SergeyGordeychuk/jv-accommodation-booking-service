import React from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AuthProvider } from './context/AuthContext';
import { AccommodationDetailsPage } from './pages/AccommodationDetailsPage';
import { AccommodationsPage } from './pages/AccommodationsPage';
import { LoginPage } from './pages/LoginPage';
import { ManagerAccommodationsPage } from './pages/ManagerAccommodationsPage';
import { ProfilePage } from './pages/ProfilePage';
import { RegisterPage } from './pages/RegisterPage';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
          <Navbar />
          <main className="flex-1">
            <Routes>
              <Route path="/" element={<AccommodationsPage />} />
              <Route path="/accommodations/:id" element={<AccommodationDetailsPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route
                path="/profile"
                element={
                  <ProtectedRoute>
                    <ProfilePage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/accommodations"
                element={
                  <ProtectedRoute requireManager>
                    <ManagerAccommodationsPage />
                  </ProtectedRoute>
                }
              />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>
          <footer className="border-t border-slate-200 bg-white py-6 text-center text-xs text-slate-500">
            Accommodation Booking Service &copy; {new Date().getFullYear()}
          </footer>
        </div>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;
