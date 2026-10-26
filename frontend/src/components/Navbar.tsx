import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Building2, Home, LogIn, LogOut, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, isManager, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="sticky top-0 z-50 bg-white/90 backdrop-blur-md border-b border-slate-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand */}
        <Link to="/" className="flex items-center gap-2 text-indigo-600 font-bold text-xl hover:text-indigo-700 transition">
          <Building2 className="w-6 h-6" />
          <span>StayBooking</span>
        </Link>

        {/* Navigation */}
        <nav className="flex items-center gap-6">
          <Link
            to="/"
            className="flex items-center gap-1.5 text-sm font-medium text-slate-600 hover:text-indigo-600 transition"
          >
            <Home className="w-4 h-4" />
            <span>Житло</span>
          </Link>

          {isManager && (
            <Link
              to="/admin/accommodations"
              className="flex items-center gap-1.5 text-sm font-medium text-emerald-600 hover:text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-md transition"
            >
              <ShieldCheck className="w-4 h-4" />
              <span>Панель менеджера</span>
            </Link>
          )}

          {isAuthenticated ? (
            <div className="flex items-center gap-4 pl-4 border-l border-slate-200">
              <Link
                to="/profile"
                className="flex items-center gap-2 text-sm text-slate-700 hover:text-indigo-600 transition"
              >
                <div className="w-8 h-8 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center font-semibold text-xs">
                  {user?.firstName?.[0] || 'U'}
                </div>
                <div className="hidden sm:block text-left">
                  <div className="font-medium text-xs text-slate-900">{user?.firstName} {user?.lastName}</div>
                  <span className={`inline-block text-[10px] font-semibold uppercase tracking-wider px-1.5 py-0.2 rounded ${
                    isManager ? 'bg-amber-100 text-amber-800' : 'bg-slate-100 text-slate-600'
                  }`}>
                    {user?.role}
                  </span>
                </div>
              </Link>

              <button
                onClick={handleLogout}
                className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-md transition"
                title="Вийти"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-3 pl-4 border-l border-slate-200">
              <Link
                to="/login"
                className="flex items-center gap-1 text-sm font-medium text-slate-700 hover:text-indigo-600 px-3 py-1.5 transition"
              >
                <LogIn className="w-4 h-4" />
                <span>Увійти</span>
              </Link>
              <Link
                to="/register"
                className="text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 px-3.5 py-1.5 rounded-lg shadow-sm hover:shadow transition"
              >
                Реєстрація
              </Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
};
