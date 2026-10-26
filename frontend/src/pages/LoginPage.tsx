import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Building2, Lock, LogIn, Mail } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/';

  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await login({ email, password });
      navigate(from, { replace: true });
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Невірний email або пароль';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const fillAdmin = () => {
    setEmail('admin@booking.com');
    setPassword('admin1234');
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4 py-12">
      <div className="max-w-md w-full bg-white p-8 rounded-3xl border border-slate-200/80 shadow-xl space-y-6">
        <div className="text-center space-y-2">
          <div className="inline-flex p-3 bg-indigo-50 text-indigo-600 rounded-2xl mb-1">
            <Building2 className="w-8 h-8" />
          </div>
          <h1 className="text-2xl font-bold text-slate-900">Вхід у кабінет</h1>
          <p className="text-sm text-slate-500">Увійдіть, щоб керувати бронюваннями та житлом</p>
        </div>

        {error && (
          <div className="bg-rose-50 border border-rose-200 text-rose-700 text-xs p-3.5 rounded-xl">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">Email</label>
            <div className="relative">
              <Mail className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@example.com"
                className="w-full pl-10 pr-4 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:bg-white"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">Пароль</label>
            <div className="relative">
              <Lock className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full pl-10 pr-4 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:bg-white"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold rounded-xl shadow-md transition flex items-center justify-center gap-2 disabled:opacity-50"
          >
            {loading ? (
              <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
            ) : (
              <>
                <LogIn className="w-4 h-4" />
                <span>Увійти</span>
              </>
            )}
          </button>
        </form>

        {/* Demo Fast Login */}
        <div className="pt-2 border-t border-slate-100 text-center">
          <p className="text-xs text-slate-400 mb-2">Швидке тестування:</p>
          <button
            type="button"
            onClick={fillAdmin}
            className="text-xs font-semibold text-amber-700 bg-amber-50 hover:bg-amber-100 px-3 py-1.5 rounded-lg border border-amber-200/60 transition"
          >
            Вставити менеджера (admin@booking.com)
          </button>
        </div>

        <div className="text-center text-xs text-slate-500">
          Ще не маєте акаунту?{' '}
          <Link to="/register" className="font-semibold text-indigo-600 hover:underline">
            Зареєструватися
          </Link>
        </div>
      </div>
    </div>
  );
};
