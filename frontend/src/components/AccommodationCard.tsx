import React from 'react';
import { Link } from 'react-router-dom';
import { BedDouble, CheckCircle2, MapPin, Sparkles } from 'lucide-react';
import type { Accommodation } from '../types';

interface AccommodationCardProps {
  accommodation: Accommodation;
}

const TYPE_IMAGES: Record<string, string> = {
  HOUSE: 'https://images.unsplash.com/photo-1580587771525-78b9dba3b914?auto=format&fit=crop&w=800&q=80',
  APARTMENT: 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=800&q=80',
  CONDO: 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=800&q=80',
  VACATION_HOME: 'https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?auto=format&fit=crop&w=800&q=80',
};

const TYPE_LABELS: Record<string, string> = {
  HOUSE: 'Будинок',
  APARTMENT: 'Апартаменти',
  CONDO: 'Кондомініум',
  VACATION_HOME: 'Вілла / Відпочинок',
};

export const AccommodationCard: React.FC<AccommodationCardProps> = ({ accommodation }) => {
  const imageUrl = accommodation.imageUrl || TYPE_IMAGES[accommodation.type] || TYPE_IMAGES.APARTMENT;
  const label = TYPE_LABELS[accommodation.type] || accommodation.type;

  return (
    <div className="bg-white rounded-2xl overflow-hidden border border-slate-200/80 shadow-sm hover:shadow-md transition-all duration-300 flex flex-col group">
      {/* Image Container */}
      <div className="relative aspect-[16/10] overflow-hidden bg-slate-100">
        <img
          src={imageUrl}
          alt={accommodation.location}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          loading="lazy"
        />
        <div className="absolute top-3 left-3 bg-white/95 backdrop-blur-sm px-2.5 py-1 rounded-full text-xs font-semibold text-slate-800 shadow-sm">
          {label}
        </div>
        <div className="absolute top-3 right-3 bg-slate-900/80 backdrop-blur-sm text-white px-2.5 py-1 rounded-full text-xs font-medium">
          {accommodation.availability > 0 ? (
            <span className="flex items-center gap-1 text-emerald-300">
              <CheckCircle2 className="w-3 h-3" />
              <span>Доступно ({accommodation.availability})</span>
            </span>
          ) : (
            <span className="text-rose-300">Немає місць</span>
          )}
        </div>
      </div>

      {/* Content */}
      <div className="p-5 flex-1 flex flex-col justify-between">
        <div>
          <div className="flex items-start gap-1.5 text-slate-600 text-sm mb-2">
            <MapPin className="w-4 h-4 text-indigo-500 shrink-0 mt-0.5" />
            <span className="font-medium text-slate-900 line-clamp-1">{accommodation.location}</span>
          </div>

          <div className="flex items-center gap-1.5 text-slate-500 text-xs mb-3">
            <BedDouble className="w-3.5 h-3.5 text-slate-400" />
            <span>{accommodation.size}</span>
          </div>

          {accommodation.amenities && (
            <div className="flex items-center gap-1.5 text-xs text-slate-500 bg-slate-50 p-2 rounded-lg mb-4 line-clamp-1">
              <Sparkles className="w-3.5 h-3.5 text-amber-500 shrink-0" />
              <span>{accommodation.amenities}</span>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="pt-3 border-t border-slate-100 flex items-center justify-between mt-auto">
          <div>
            <span className="text-xl font-bold text-slate-900">${accommodation.dailyRate}</span>
            <span className="text-xs text-slate-500"> / ніч</span>
          </div>

          <Link
            to={`/accommodations/${accommodation.id}`}
            className="text-xs font-semibold text-indigo-600 bg-indigo-50 hover:bg-indigo-600 hover:text-white px-3.5 py-2 rounded-lg transition"
          >
            Деталі
          </Link>
        </div>
      </div>
    </div>
  );
};
