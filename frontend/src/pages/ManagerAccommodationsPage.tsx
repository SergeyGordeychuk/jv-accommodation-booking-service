import React, { useEffect, useRef, useState } from 'react';
import { Building, ImagePlus, Loader2, Plus, RefreshCw, Trash2, Upload, X } from 'lucide-react';
import { accommodationsApi } from '../api/accommodations';
import type { Accommodation, AccommodationRequest, AccommodationType } from '../types';

export const ManagerAccommodationsPage: React.FC = () => {
  const [accommodations, setAccommodations] = useState<Accommodation[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Form modal state
  const [isFormOpen, setIsFormOpen] = useState<boolean>(false);
  const [formData, setFormData] = useState<AccommodationRequest>({
    type: 'APARTMENT',
    location: '',
    size: '',
    amenities: '',
    dailyRate: 100,
    availability: 1,
  });
  const [selectedImageFile, setSelectedImageFile] = useState<File | null>(null);
  const [imagePreviewUrl, setImagePreviewUrl] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState<boolean>(false);

  // Inline table upload state
  const [uploadingId, setUploadingId] = useState<number | null>(null);
  const fileInputRefs = useRef<{ [key: number]: HTMLInputElement | null }>({});

  const fetchAccommodations = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await accommodationsApi.getAll();
      setAccommodations(data);
    } catch {
      setError('Не вдалося завантажити список житла.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAccommodations();
  }, []);

  const handleImageFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        alert('Максимальний розмір фото: 5 МБ');
        return;
      }
      setSelectedImageFile(file);
      setImagePreviewUrl(URL.createObjectURL(file));
    }
  };

  const handleClearSelectedImage = () => {
    setSelectedImageFile(null);
    if (imagePreviewUrl) {
      URL.revokeObjectURL(imagePreviewUrl);
      setImagePreviewUrl(null);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Ви впевнені, що хочете видалити цей обʼєкт?')) return;
    try {
      await accommodationsApi.delete(id);
      setAccommodations((prev) => prev.filter((a) => a.id !== id));
    } catch {
      alert('Помилка при видаленні обʼєкта');
    }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      let created = await accommodationsApi.create({
        ...formData,
        dailyRate: Number(formData.dailyRate),
        availability: Number(formData.availability),
      });

      if (selectedImageFile) {
        try {
          created = await accommodationsApi.uploadImage(created.id, selectedImageFile);
        } catch (imgErr) {
          console.error('Image upload failed', imgErr);
          alert('Обʼєкт створено, але не вдалося завантажити фото.');
        }
      }

      setAccommodations((prev) => [...prev, created]);
      setIsFormOpen(false);
      handleClearSelectedImage();
      setFormData({
        type: 'APARTMENT',
        location: '',
        size: '',
        amenities: '',
        dailyRate: 100,
        availability: 1,
      });
    } catch (err: any) {
      alert(err.response?.data?.message || 'Помилка при створенні житла');
    } finally {
      setSubmitting(false);
    }
  };

  const handleRowImageUpload = async (id: number, file: File) => {
    if (file.size > 5 * 1024 * 1024) {
      alert('Розмір файлу не повинен перевищувати 5 МБ');
      return;
    }
    setUploadingId(id);
    try {
      const updated = await accommodationsApi.uploadImage(id, file);
      setAccommodations((prev) => prev.map((item) => (item.id === id ? updated : item)));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Помилка при завантаженні фото');
    } finally {
      setUploadingId(null);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-slate-900 flex items-center gap-2">
            <Building className="w-7 h-7 text-indigo-600" />
            <span>Управління житлом (Панель менеджера)</span>
          </h1>
          <p className="text-sm text-slate-500 mt-1">Додавання фото, оновлення інвентарю та видалення об'єктів</p>
        </div>

        <button
          onClick={() => setIsFormOpen(!isFormOpen)}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold px-4 py-2.5 rounded-xl shadow-md transition w-fit cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>{isFormOpen ? 'Закрити форму' : 'Додати житло'}</span>
        </button>
      </div>

      {/* Add Accommodation Form */}
      {isFormOpen && (
        <div className="bg-white p-6 sm:p-8 rounded-3xl border border-indigo-100 shadow-md space-y-5 animate-in fade-in duration-200">
          <h2 className="text-lg font-bold text-slate-900">Новий об'єкт житла</h2>
          <form onSubmit={handleCreate} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Тип житла</label>
              <select
                value={formData.type}
                onChange={(e) => setFormData({ ...formData, type: e.target.value as AccommodationType })}
                className="w-full px-3.5 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl"
              >
                <option value="APARTMENT">APARTMENT (Апартаменти)</option>
                <option value="HOUSE">HOUSE (Будинок)</option>
                <option value="CONDO">CONDO (Кондо)</option>
                <option value="VACATION_HOME">VACATION_HOME (Вілла)</option>
              </select>
            </div>

            <div className="sm:col-span-2">
              <label className="block text-xs font-semibold text-slate-700 mb-1">Локація / Адреса</label>
              <input
                type="text"
                required
                placeholder="вул. Хрещатик 1, Київ"
                value={formData.location}
                onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                className="w-full px-3.5 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Розмір / Опис кімнат</label>
              <input
                type="text"
                required
                placeholder="2 кімнати, 65 кв.м"
                value={formData.size}
                onChange={(e) => setFormData({ ...formData, size: e.target.value })}
                className="w-full px-3.5 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Ціна за добу ($)</label>
              <input
                type="number"
                required
                step="0.01"
                min="0.01"
                value={formData.dailyRate}
                onChange={(e) => setFormData({ ...formData, dailyRate: Number(e.target.value) })}
                className="w-full px-3.5 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Кількість доступних місць</label>
              <input
                type="number"
                required
                min="0"
                value={formData.availability}
                onChange={(e) => setFormData({ ...formData, availability: Number(e.target.value) })}
                className="w-full px-3.5 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl"
              />
            </div>

            <div className="sm:col-span-3">
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Зручності (Wi-Fi, кондиціонер, парковка...)
              </label>
              <textarea
                rows={2}
                placeholder="Швидкісний Wi-Fi, панорамні вікна, паркінг, кавоварка"
                value={formData.amenities || ''}
                onChange={(e) => setFormData({ ...formData, amenities: e.target.value })}
                className="w-full px-3.5 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl"
              />
            </div>

            {/* Photo upload field */}
            <div className="sm:col-span-3 border-t border-slate-100 pt-3">
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                Фото житла (JPEG, PNG, WEBP, до 5 МБ)
              </label>
              <div className="flex items-center gap-4">
                <label className="flex items-center gap-2 px-4 py-2.5 bg-slate-50 hover:bg-slate-100 border border-slate-300 rounded-xl text-xs font-semibold text-slate-700 cursor-pointer transition">
                  <ImagePlus className="w-4 h-4 text-indigo-600" />
                  <span>{selectedImageFile ? 'Змінити вибране фото' : 'Вибрати файл з компʼютера'}</span>
                  <input
                    type="file"
                    accept="image/jpeg,image/png,image/webp,image/gif"
                    onChange={handleImageFileChange}
                    className="hidden"
                  />
                </label>

                {imagePreviewUrl && (
                  <div className="relative inline-block">
                    <img
                      src={imagePreviewUrl}
                      alt="Preview"
                      className="w-14 h-14 object-cover rounded-xl border border-slate-200 shadow-sm"
                    />
                    <button
                      type="button"
                      onClick={handleClearSelectedImage}
                      className="absolute -top-1.5 -right-1.5 bg-rose-500 text-white rounded-full p-0.5 hover:bg-rose-600 transition shadow"
                      title="Видалити"
                    >
                      <X className="w-3.5 h-3.5" />
                    </button>
                  </div>
                )}
                {selectedImageFile && (
                  <span className="text-xs text-slate-500 truncate max-w-xs">{selectedImageFile.name}</span>
                )}
              </div>
            </div>

            <div className="sm:col-span-3 flex justify-end gap-3 pt-2">
              <button
                type="button"
                onClick={() => {
                  setIsFormOpen(false);
                  handleClearSelectedImage();
                }}
                className="px-4 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-100 rounded-xl cursor-pointer"
              >
                Скасувати
              </button>
              <button
                type="submit"
                disabled={submitting}
                className="px-6 py-2 text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-xl shadow cursor-pointer disabled:opacity-50 flex items-center gap-2"
              >
                {submitting && <Loader2 className="w-4 h-4 animate-spin" />}
                <span>{submitting ? 'Збереження...' : 'Зберегти обʼєкт'}</span>
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Accommodations Table */}
      <div className="bg-white rounded-3xl border border-slate-200/80 shadow-sm overflow-hidden">
        <div className="p-4 sm:p-6 border-b border-slate-100 flex items-center justify-between">
          <h2 className="text-base font-bold text-slate-900">Список об'єктів ({accommodations.length})</h2>
          <button
            onClick={fetchAccommodations}
            className="p-2 text-slate-400 hover:text-indigo-600 rounded-lg hover:bg-slate-50 transition cursor-pointer"
            title="Оновити"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>

        {loading ? (
          <div className="p-12 text-center text-slate-400">Завантаження...</div>
        ) : error ? (
          <div className="p-6 text-center text-rose-600 text-sm">{error}</div>
        ) : accommodations.length === 0 ? (
          <div className="p-12 text-center text-slate-400">Ще немає доданого житла. Створіть перший об'єкт!</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-600">
              <thead className="bg-slate-50 text-xs text-slate-500 uppercase tracking-wider">
                <tr>
                  <th className="py-3.5 px-6">ID</th>
                  <th className="py-3.5 px-6">Фото</th>
                  <th className="py-3.5 px-6">Тип</th>
                  <th className="py-3.5 px-6">Локація</th>
                  <th className="py-3.5 px-6">Розмір</th>
                  <th className="py-3.5 px-6">Ціна / добу</th>
                  <th className="py-3.5 px-6">Доступно</th>
                  <th className="py-3.5 px-6 text-right">Дії</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {accommodations.map((a) => (
                  <tr key={a.id} className="hover:bg-slate-50/80 transition">
                    <td className="py-4 px-6 font-semibold text-slate-900">#{a.id}</td>
                    <td className="py-4 px-6">
                      <div className="flex items-center gap-2">
                        {a.imageUrl ? (
                          <img
                            src={a.imageUrl}
                            alt={a.location}
                            className="w-11 h-11 object-cover rounded-xl border border-slate-200 shadow-xs"
                          />
                        ) : (
                          <div className="w-11 h-11 bg-slate-100 rounded-xl border border-dashed border-slate-300 flex items-center justify-center text-slate-400">
                            <ImagePlus className="w-5 h-5" />
                          </div>
                        )}
                        <input
                          type="file"
                          accept="image/jpeg,image/png,image/webp,image/gif"
                          ref={(el) => {
                            fileInputRefs.current[a.id] = el;
                          }}
                          onChange={(e) => {
                            const file = e.target.files?.[0];
                            if (file) handleRowImageUpload(a.id, file);
                          }}
                          className="hidden"
                        />
                        <button
                          type="button"
                          disabled={uploadingId === a.id}
                          onClick={() => fileInputRefs.current[a.id]?.click()}
                          className="p-1.5 text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition cursor-pointer"
                          title="Завантажити / змінити фото"
                        >
                          {uploadingId === a.id ? (
                            <Loader2 className="w-4 h-4 animate-spin text-indigo-600" />
                          ) : (
                            <Upload className="w-4 h-4" />
                          )}
                        </button>
                      </div>
                    </td>
                    <td className="py-4 px-6">
                      <span className="text-xs font-semibold bg-indigo-50 text-indigo-700 px-2.5 py-1 rounded-md">
                        {a.type}
                      </span>
                    </td>
                    <td className="py-4 px-6 font-medium text-slate-900 max-w-xs truncate">{a.location}</td>
                    <td className="py-4 px-6">{a.size}</td>
                    <td className="py-4 px-6 font-bold text-slate-900">${a.dailyRate}</td>
                    <td className="py-4 px-6">
                      <span
                        className={`inline-flex px-2 py-0.5 text-xs font-semibold rounded-full ${
                          a.availability > 0 ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'
                        }`}
                      >
                        {a.availability}
                      </span>
                    </td>
                    <td className="py-4 px-6 text-right space-x-2">
                      <button
                        onClick={() => handleDelete(a.id)}
                        className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition cursor-pointer"
                        title="Видалити"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
