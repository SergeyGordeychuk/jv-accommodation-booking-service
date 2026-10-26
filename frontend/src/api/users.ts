import { apiClient } from './client';
import type { User, UserRole } from '../types';

export const usersApi = {
  async getProfile(): Promise<User> {
    const response = await apiClient.get<User>('/users/me');
    return response.data;
  },

  async updateProfile(data: { firstName: string; lastName: string }): Promise<User> {
    const response = await apiClient.put<User>('/users/me', data);
    return response.data;
  },

  async updateRole(id: number, role: UserRole): Promise<User> {
    const response = await apiClient.put<User>(`/users/${id}/role`, { role });
    return response.data;
  },
};
