import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, BedDouble, Calendar, CheckCircle2, DollarSign, ExternalLink, MapPin, ShieldAlert, Sparkles } from 'lucide-react';
import { accommodationsApi } from '../api/accommodations';
import { bookingsApi } from '../api/bookings';
import { paymentsApi } from '../api/payments';
import { useAuth } from '../context/AuthContext';
import type { Accommodation, Booking, Payment } from '../types';

const TYPE_IMAGES: Record<string, string> = {
  HOUSE: 'https://images.unsplash.com/photo-1580587771525-78b9dba3b914?auto=format&fit=crop&w=1200&q=80',
  APARTMENT: 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1200&q=80',
  CONDO: 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=1200&q=80',
  VACATION_HOME: 'https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?auto=format&fit=crop&w=1200&q=80',
};

export const AccommodationDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [accommodation, setAccommodation] = useState<Accommodation | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Booking widget state
  const today = new Date().toISOString().split('T')[0];
  const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];
  const [checkIn, setCheckIn] = useState<string>(today);
  const [checkOut, setCheckOut] = useState<string>(tomorrow);
  const [bookingLoading, setBookingLoading] = useState<boolean>(false);
  const [bookingError, setBookingError] = useState<string | null>(null);
  const [createdBooking, setCreatedBooking] = useState<Booking | null>(null);
  const [paymentSession, setPaymentSession] = useState<Payment | null>(null);

  useEffect(() => {
    if (!id) return;
    const fetchItem = async () => {
      try {
        setLoading(true);
        const data = await accommodationsApi.getById(Number(id));
        setAccommodation(data);
      } catch {
        setError('Житло не знайдено або сталася помилка завантаження.');
      } finally {
        setLoading(false);
      }
    };
    fetchItem();
  }, [id]);

  const calculateNights = () => {
    const start = new Date(checkIn);
    const end = new Date(checkOut);
    const diff = end.getTime() - start.getTime();
    const days = Math.ceil(diff / (1000 * 3600 * 24));
    return days > 0 ? days : 1;
  };

  const nights = calculateNights();
  const totalPrice = accommodation ? nights * Number(accommodation.dailyRate) : 0;

  const handleBook = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (!accommodation) return;

    try {
      setBookingLoading(true);
      setBookingError(null);

      const booking = await bookingsApi.create({
        accommodationId: accommodation.id,
        checkInDate: checkIn,
        checkOutDate: checkOut,
      });
      setCreatedBooking(booking);

      try {
        const payment = await paymentsApi.createPaymentSession({
          bookingId: booking.id,
        });
        setPaymentSession(payment);
      } catch (payErr: unknown) {
        console.warn('Could not auto-generate payment session', payErr);
      }
    } catch (err: unknown) {
      const responseData = (err as { response?: { data?: { message?: string; error?: string } } })?.response?.data;
      const msg = responseData?.message || responseData?.error || 'Помилка при створенні бронювання';
      setBookingError(msg);
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-16 flex items-center justify-center">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  if (error || !accommodation) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-16 text-center">
        <p className="text-rose-600 font-semibold mb-4">{error || 'Не вдалося завантажити'}</p>
        <Link to="/" className="text-indigo-600 hover:underline inline-flex items-center gap-1.5 text-sm font-medium">
          <ArrowLeft className="w-4 h-4" /> Назад до каталогу
        </Link>
      </div>
    );
  }

  const imageUrl = TYPE_IMAGES[accommodation.type] || TYPE_IMAGES.APARTMENT;

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Back button */}
      <Link
        to="/"
        className="inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-slate-900 mb-6 transition"
      >
        <ArrowLeft className="w-4 h-4" /> Назад до всіх варіантів
      </Link>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left 2 Cols: Details & Photos */}
        <div className="lg:col-span-2 space-y-6">
          <div className="rounded-3xl overflow-hidden border border-slate-200/80 aspect-[16/9] shadow-sm">
            <img src={imageUrl} alt={accommodation.location} className="w-full h-full object-cover" />
          </div>

          <div className="bg-white p-6 sm:p-8 rounded-3xl border border-slate-200/80 shadow-sm space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 pb-5">
              <div>
                <span className="text-xs font-bold text-indigo-600 uppercase tracking-wider bg-indigo-50 px-2.5 py-1 rounded-full">
                  {accommodation.type}
                </span>
                <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 mt-2 flex items-center gap-2">
                  <MapPin className="w-6 h-6 text-indigo-500 shrink-0" />
                  <span>{accommodation.location}</span>
                </h1>
              </div>

              <div className="text-right">
                <div className="text-3xl font-black text-slate-900">${accommodation.dailyRate}</div>
                <div className="text-xs text-slate-500">за добу</div>
              </div>
            </div>

            {/* Features */}
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
              <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100">
                <div className="text-xs text-slate-500 mb-1">Розмір та спальні</div>
                <div className="text-sm font-semibold text-slate-800 flex items-center gap-1.5">
                  <BedDouble className="w-4 h-4 text-slate-400" />
                  <span>{accommodation.size}</span>
                </div>
              </div>

              <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100">
                <div className="text-xs text-slate-500 mb-1">Доступність</div>
                <div className="text-sm font-semibold text-emerald-600 flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4" />
                  <span>{accommodation.availability} вільних одиниць</span>
                </div>
              </div>

              <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100 col-span-2 sm:col-span-1">
                <div className="text-xs text-slate-500 mb-1">Ціна за 7 днів</div>
                <div className="text-sm font-semibold text-slate-800 flex items-center gap-1">
                  <DollarSign className="w-4 h-4 text-slate-400" />
                  <span>${Number(accommodation.dailyRate) * 7}</span>
                </div>
              </div>
            </div>

            {/* Amenities */}
            {accommodation.amenities && (
              <div className="pt-2">
                <h3 className="text-base font-bold text-slate-900 mb-3 flex items-center gap-2">
                  <Sparkles className="w-4 h-4 text-amber-500" />
                  <span>Зручності та переваги</span>
                </h3>
                <div className="bg-amber-50/50 border border-amber-200/60 p-4 rounded-2xl text-sm text-slate-700 leading-relaxed">
                  {accommodation.amenities}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Right Col: Booking Widget */}
        <div className="lg:col-span-1">
          <div className="sticky top-24 bg-white p-6 rounded-3xl border border-slate-200/80 shadow-lg space-y-6">
            <h2 className="text-xl font-bold text-slate-900">Бронювання</h2>

            {createdBooking ? (
              <div className="bg-emerald-50 border border-emerald-200 p-5 rounded-2xl text-center space-y-3">
                <CheckCircle2 className="w-10 h-10 text-emerald-600 mx-auto" />
                <h4 className="font-bold text-emerald-900">Бронювання #{createdBooking.id} створено!</h4>
                <p className="text-xs text-emerald-700 leading-relaxed">
                  Дати: <span className="font-semibold">{createdBooking.checkInDate}</span> — <span className="font-semibold">{createdBooking.checkOutDate}</span> ({nights} {nights === 1 ? 'ніч' : 'ночей'}).
                  <br />
                  Статус: <span className="font-bold uppercase tracking-wider">{createdBooking.status}</span>
                </p>

                {paymentSession?.sessionUrl && (
                  <div className="pt-2">
                    <a
                      href={paymentSession.sessionUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="w-full py-3 px-4 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-sm rounded-xl shadow transition flex items-center justify-center gap-2"
                    >
                      <span>Оплатити карткою (${paymentSession.amountToPay})</span>
                      <ExternalLink className="w-4 h-4" />
                    </a>
                    <span className="text-[10px] text-slate-500 block mt-1">Безпечна оплата через Stripe Checkout</span>
                  </div>
                )}

                <div className="pt-2 flex items-center justify-center gap-4">
                  <Link
                    to="/profile"
                    className="text-xs font-semibold text-indigo-600 hover:text-indigo-800 underline"
                  >
                    Перейти до моїх бронювань
                  </Link>
                  <button
                    onClick={() => {
                      setCreatedBooking(null);
                      setPaymentSession(null);
                    }}
                    className="text-xs font-semibold text-slate-500 hover:text-slate-800 underline"
                  >
                    Нове бронювання
                  </button>
                </div>
              </div>
            ) : (
              <div className="space-y-4">
                {bookingError && (
                  <div className="p-3 bg-rose-50 text-rose-700 text-xs rounded-xl flex items-center gap-2 font-medium border border-rose-200">
                    <ShieldAlert className="w-4 h-4 shrink-0 text-rose-600" />
                    <span>{bookingError}</span>
                  </div>
                )}

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1.5 flex items-center gap-1.5">
                    <Calendar className="w-3.5 h-3.5 text-indigo-500" />
                    <span>Дата заїзду</span>
                  </label>
                  <input
                    type="date"
                    min={today}
                    value={checkIn}
                    onChange={(e) => setCheckIn(e.target.value)}
                    className="w-full px-3.5 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:bg-white"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1.5 flex items-center gap-1.5">
                    <Calendar className="w-3.5 h-3.5 text-indigo-500" />
                    <span>Дата виїзду</span>
                  </label>
                  <input
                    type="date"
                    min={checkIn}
                    value={checkOut}
                    onChange={(e) => setCheckOut(e.target.value)}
                    className="w-full px-3.5 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:bg-white"
                  />
                </div>

                {/* Price Breakdown */}
                <div className="bg-slate-50 p-4 rounded-2xl border border-slate-100 space-y-2 text-sm">
                  <div className="flex justify-between text-slate-600">
                    <span>${accommodation.dailyRate} × {nights} {nights === 1 ? 'ніч' : 'ночей'}</span>
                    <span>${totalPrice.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between text-slate-600">
                    <span>Сервісний збір</span>
                    <span className="text-emerald-600 font-medium">Безкоштовно</span>
                  </div>
                  <div className="border-t border-slate-200 pt-2 flex justify-between font-bold text-slate-900 text-base">
                    <span>Разом до сплати:</span>
                    <span className="text-indigo-600">${totalPrice.toFixed(2)}</span>
                  </div>
                </div>

                {accommodation.availability > 0 ? (
                  <button
                    onClick={handleBook}
                    disabled={bookingLoading}
                    className="w-full py-3 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold rounded-xl shadow-md hover:shadow-lg transition flex items-center justify-center gap-2 disabled:opacity-50"
                  >
                    <span>
                      {bookingLoading
                        ? 'Бронювання...'
                        : isAuthenticated
                        ? 'Забронювати зараз'
                        : 'Увійти для бронювання'}
                    </span>
                  </button>
                ) : (
                  <div className="p-3 bg-rose-50 text-rose-700 text-xs rounded-xl flex items-center gap-2 font-medium">
                    <ShieldAlert className="w-4 h-4 shrink-0" />
                    <span>На жаль, на цей об'єкт немає вільних місць.</span>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

