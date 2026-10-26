import { apiClient } from './client';
import type { Accommodation, AccommodationPatchRequest, AccommodationRequest } from '../types';

export const accommodationsApi = {
  async getAll(): Promise<Accommodation[]> {
    const response = await apiClient.get<Accommodation[]>('/accommodations');
    return response.data;
  },

  async getById(id: number): Promise<Accommodation> {
    const response = await apiClient.get<Accommodation>(`/accommodations/${id}`);
    return response.data;
  },

  async create(data: AccommodationRequest): Promise<Accommodation> {
    const response = await apiClient.post<Accommodation>('/accommodations', data);
    return response.data;
  },

  async update(id: number, data: AccommodationRequest): Promise<Accommodation> {
    const response = await apiClient.put<Accommodation>(`/accommodations/${id}`, data);
    return response.data;
  },

  async patch(id: number, data: AccommodationPatchRequest): Promise<Accommodation> {
    const response = await apiClient.patch<Accommodation>(`/accommodations/${id}`, data);
    return response.data;
  },

  async delete(id: number): Promise<void> {
    await apiClient.delete(`/accommodations/${id}`);
  },

  async uploadImage(id: number, file: File): Promise<Accommodation> {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post<Accommodation>(`/accommodations/${id}/image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
};
