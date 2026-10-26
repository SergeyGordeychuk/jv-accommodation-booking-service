import React, { useEffect, useState } from 'react';
import { Calendar, Check, CreditCard, Mail, Shield, User as UserIcon, XCircle } from 'lucide-react';
import { bookingsApi } from '../api/bookings';
import { paymentsApi } from '../api/payments';
import { usersApi } from '../api/users';
import { useAuth } from '../context/AuthContext';
import type { Booking } from '../types';

export const ProfilePage: React.FC = () => {
  const { user, refreshProfile } = useAuth();

  const [firstName, setFirstName] = useState<string>(user?.firstName || '');
  const [lastName, setLastName] = useState<string>(user?.lastName || '');
  const [loading, setLoading] = useState<boolean>(false);
  const [success, setSuccess] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Bookings list state
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [bookingsLoading, setBookingsLoading] = useState<boolean>(true);
  const [actionMessage, setActionMessage] = useState<string | null>(null);

  const fetchBookings = async () => {
    try {
      setBookingsLoading(true);
      const data = await bookingsApi.getMy();
      setBookings(data);
    } catch (e: unknown) {
      console.warn('Failed to load bookings', e);
    } finally {
      setBookingsLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      await usersApi.updateProfile({ firstName, lastName });
      await refreshProfile();
      setSuccess(true);
    } catch {
      setError('Не вдалося оновити профіль. Спробуйте пізніше.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelBooking = async (bookingId: number) => {
    if (!window.confirm('Ви впевнені, що хочете скасувати це бронювання?')) return;
    try {
      await bookingsApi.cancel(bookingId);
      setActionMessage('Бронювання успішно скасовано.');
      await fetchBookings();
    } catch (err: unknown) {
      const responseData = (err as { response?: { data?: { message?: string } } })?.response?.data;
      setActionMessage(responseData?.message || 'Не вдалося скасувати бронювання.');
    }
  };

  const handlePayBooking = async (bookingId: number) => {
    try {
      const payment = await paymentsApi.createPaymentSession({ bookingId });
      if (payment.sessionUrl) {
        window.open(payment.sessionUrl, '_blank', 'noopener,noreferrer');
      }
    } catch (err: unknown) {
      const responseData = (err as { response?: { data?: { message?: string } } })?.response?.data;
      setActionMessage(responseData?.message || 'Не вдалося розпочати оплату.');
    }
  };

  const getStatusBadge = (status: Booking['status']) => {
    switch (status) {
      case 'CONFIRMED':
        return <span className="bg-emerald-100 text-emerald-800 text-[10px] font-bold px-2 py-0.5 rounded-full">CONFIRMED</span>;
      case 'PENDING':
        return <span className="bg-amber-100 text-amber-800 text-[10px] font-bold px-2 py-0.5 rounded-full">PENDING</span>;
      case 'CANCELED':
        return <span className="bg-slate-100 text-slate-700 text-[10px] font-bold px-2 py-0.5 rounded-full">CANCELED</span>;
      case 'EXPIRED':
        return <span className="bg-rose-100 text-rose-800 text-[10px] font-bold px-2 py-0.5 rounded-full">EXPIRED</span>;
      default:
        return null;
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* User Info card */}
      <div className="bg-white p-6 sm:p-8 rounded-3xl border border-slate-200/80 shadow-sm space-y-8">
        <div className="flex items-center gap-4 border-b border-slate-100 pb-6">
          <div className="w-16 h-16 rounded-2xl bg-indigo-100 text-indigo-600 flex items-center justify-center font-bold text-2xl">
            {user?.firstName?.[0] || 'U'}
          </div>
          <div>
            <h1 className="text-2xl font-bold text-slate-900">{user?.firstName} {user?.lastName}</h1>
            <div className="flex items-center gap-2 mt-1">
              <span className="text-xs text-slate-500">{user?.email}</span>
              <span className={`inline-block text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded-full ${
                user?.role === 'MANAGER' ? 'bg-amber-100 text-amber-800' : 'bg-slate-100 text-slate-700'
              }`}>
                {user?.role}
              </span>
            </div>
          </div>
        </div>

        {success && (
          <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs p-3.5 rounded-xl flex items-center gap-2">
            <Check className="w-4 h-4 text-emerald-600 shrink-0" />
            <span>Профіль успішно оновлено!</span>
          </div>
        )}

        {error && (
          <div className="bg-rose-50 border border-rose-200 text-rose-700 text-xs p-3.5 rounded-xl">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <h2 className="text-base font-bold text-slate-900">Особисті дані</h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">Ім'я</label>
              <div className="relative">
                <UserIcon className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  required
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:bg-white"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">Прізвище</label>
              <div className="relative">
                <UserIcon className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  required
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:bg-white"
                />
              </div>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">Email (не змінюється)</label>
            <div className="relative opacity-70">
              <Mail className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="email"
                disabled
                value={user?.email || ''}
                className="w-full pl-10 pr-4 py-2.5 text-sm bg-slate-100 border border-slate-200 rounded-xl cursor-not-allowed"
              />
            </div>
          </div>

          <div className="pt-2">
            <button
              type="submit"
              disabled={loading}
              className="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold rounded-xl shadow transition disabled:opacity-50 text-sm"
            >
              {loading ? 'Збереження...' : 'Зберегти зміни'}
            </button>
          </div>
        </form>

        <div className="bg-slate-50 p-5 rounded-2xl border border-slate-100 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Shield className="w-5 h-5 text-indigo-500" />
            <div>
              <div className="text-xs font-semibold text-slate-800">Рівень доступу</div>
              <div className="text-xs text-slate-500">
                {user?.role === 'MANAGER'
                  ? 'Ви маєте права Менеджера на додавання та редагування житла'
                  : 'Стандартний акаунт Клієнта для перегляду та бронювання'}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Bookings Section */}
      <div className="bg-white p-6 sm:p-8 rounded-3xl border border-slate-200/80 shadow-sm space-y-6">
        <div className="flex items-center justify-between border-b border-slate-100 pb-4">
          <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
            <Calendar className="w-5 h-5 text-indigo-600" />
            <span>Мої бронювання</span>
          </h2>
          <span className="text-xs font-medium text-slate-500">
            Всього: {bookings.length}
          </span>
        </div>

        {actionMessage && (
          <div className="p-3 bg-indigo-50 border border-indigo-200 text-indigo-800 text-xs rounded-xl flex items-center justify-between">
            <span>{actionMessage}</span>
            <button onClick={() => setActionMessage(null)} className="text-slate-400 hover:text-slate-600">
              <XCircle className="w-4 h-4" />
            </button>
          </div>
        )}

        {bookingsLoading ? (
          <div className="py-8 text-center text-xs text-slate-400">Завантаження бронювань...</div>
        ) : bookings.length === 0 ? (
          <div className="py-8 text-center text-sm text-slate-500">
            У вас ще немає бронювань. Перейдіть до каталогу для вибору житла.
          </div>
        ) : (
          <div className="divide-y divide-slate-100">
            {bookings.map((b) => (
              <div key={b.id} className="py-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-bold text-slate-900 text-sm">Бронювання #{b.id}</span>
                    {getStatusBadge(b.status)}
                  </div>
                  <div className="text-xs text-slate-500 flex items-center gap-2">
                    <span>Житло #{b.accommodationId}</span>
                    <span>•</span>
                    <span>{b.checkInDate} — {b.checkOutDate}</span>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  {b.status === 'PENDING' && (
                    <>
                      <button
                        onClick={() => handlePayBooking(b.id)}
                        className="px-3.5 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-semibold flex items-center gap-1.5 shadow transition"
                      >
                        <CreditCard className="w-3.5 h-3.5" />
                        <span>Оплатити</span>
                      </button>
                      <button
                        onClick={() => handleCancelBooking(b.id)}
                        className="px-3.5 py-1.5 bg-rose-50 hover:bg-rose-100 text-rose-700 rounded-lg text-xs font-semibold transition"
                      >
                        Скасувати
                      </button>
                    </>
                  )}
                  {b.status === 'CONFIRMED' && (
                    <button
                      onClick={() => handleCancelBooking(b.id)}
                      className="px-3 py-1.5 border border-slate-200 text-slate-600 hover:bg-slate-50 rounded-lg text-xs font-medium transition"
                    >
                      Скасувати
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

