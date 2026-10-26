import { apiClient } from './client';
import type { Booking, BookingRequest, BookingStatus, BookingUpdate } from '../types';

export const bookingsApi = {
  async create(data: BookingRequest): Promise<Booking> {
    const response = await apiClient.post<Booking>('/bookings', data);
    return response.data;
  },

  async getAll(params?: { user_id?: number; status?: BookingStatus }): Promise<Booking[]> {
    const response = await apiClient.get<Booking[]>('/bookings', { params });
    return response.data;
  },

  async getMy(): Promise<Booking[]> {
    const response = await apiClient.get<Booking[]>('/bookings/my');
    return response.data;
  },

  async getById(id: number): Promise<Booking> {
    const response = await apiClient.get<Booking>(`/bookings/${id}`);
    return response.data;
  },

  async update(id: number, data: BookingUpdate): Promise<Booking> {
    const response = await apiClient.put<Booking>(`/bookings/${id}`, data);
    return response.data;
  },

  async cancel(id: number): Promise<void> {
    await apiClient.delete(`/bookings/${id}`);
  },
};
