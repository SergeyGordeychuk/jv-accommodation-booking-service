import React, { useEffect, useState } from 'react';
import { Filter, RefreshCw, Search } from 'lucide-react';
import { accommodationsApi } from '../api/accommodations';
import { AccommodationCard } from '../components/AccommodationCard';
import type { Accommodation, AccommodationType } from '../types';

const TYPES: { label: string; value: AccommodationType | 'ALL' }[] = [
  { label: 'Усі', value: 'ALL' },
  { label: 'Будинки', value: 'HOUSE' },
  { label: 'Апартаменти', value: 'APARTMENT' },
  { label: 'Кондо', value: 'CONDO' },
  { label: 'Вілли', value: 'VACATION_HOME' },
];

export const AccommodationsPage: React.FC = () => {
  const [accommodations, setAccommodations] = useState<Accommodation[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedType, setSelectedType] = useState<AccommodationType | 'ALL'>('ALL');
  const [searchQuery, setSearchQuery] = useState<string>('');

  const fetchAccommodations = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await accommodationsApi.getAll();
      setAccommodations(data);
    } catch {
      setError('Не вдалося завантажити список житла. Перевірте, чи запущений бекенд-сервер на http://localhost:8080.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAccommodations();
  }, []);

  const filteredAccommodations = accommodations.filter((item) => {
    const matchesType = selectedType === 'ALL' || item.type === selectedType;
    const matchesSearch =
      item.location.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.size.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (item.amenities && item.amenities.toLowerCase().includes(searchQuery.toLowerCase()));
    return matchesType && matchesSearch;
  });

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Hero Header */}
      <div className="mb-8 text-center sm:text-left">
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 tracking-tight">
          Знайдіть комфортне житло
        </h1>
        <p className="mt-2 text-slate-600 text-base">
          Обирайте з доступних пропозицій для відпочинку чи відрядження.
        </p>
      </div>

      {/* Filters and Search Bar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm mb-8 flex flex-col md:flex-row gap-4 items-center justify-between">
        {/* Search Input */}
        <div className="relative w-full md:w-96">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Пошук за містом або зручностями..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:bg-white transition"
          />
        </div>

        {/* Type Filter Pills */}
        <div className="flex items-center gap-1.5 overflow-x-auto w-full md:w-auto pb-2 md:pb-0 scrollbar-none">
          <Filter className="w-4 h-4 text-slate-400 hidden sm:block mr-1" />
          {TYPES.map((t) => (
            <button
              key={t.value}
              onClick={() => setSelectedType(t.value)}
              className={`px-3 py-1.5 text-xs font-semibold rounded-lg shrink-0 transition ${
                selectedType === t.value
                  ? 'bg-indigo-600 text-white shadow-sm'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>
      </div>

      {/* Error State */}
      {error && (
        <div className="bg-rose-50 border border-rose-200 text-rose-700 px-5 py-4 rounded-xl mb-8 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <p className="text-sm">{error}</p>
          <button
            onClick={fetchAccommodations}
            className="flex items-center gap-1.5 text-xs font-semibold bg-rose-600 text-white px-3.5 py-2 rounded-lg hover:bg-rose-700 transition w-fit"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>Спробувати знову</span>
          </button>
        </div>
      )}

      {/* Loading Skeleton */}
      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="bg-white rounded-2xl p-4 border border-slate-200 animate-pulse space-y-3">
              <div className="bg-slate-200 aspect-[16/10] rounded-xl"></div>
              <div className="h-4 bg-slate-200 rounded w-3/4"></div>
              <div className="h-3 bg-slate-200 rounded w-1/2"></div>
              <div className="h-4 bg-slate-200 rounded w-1/3 pt-4"></div>
            </div>
          ))}
        </div>
      ) : filteredAccommodations.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredAccommodations.map((acc) => (
            <AccommodationCard key={acc.id} accommodation={acc} />
          ))}
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 text-center py-16 px-4">
          <p className="text-slate-500 text-base font-medium">Житла за вибраними критеріями не знайдено.</p>
          <button
            onClick={() => {
              setSelectedType('ALL');
              setSearchQuery('');
            }}
            className="mt-4 text-xs font-semibold text-indigo-600 hover:underline"
          >
            Скинути фільтри
          </button>
        </div>
      )}
    </div>
  );
};
