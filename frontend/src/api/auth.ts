import { apiClient } from './client';
import type { User, UserLoginRequest, UserLoginResponse, UserRegistrationRequest } from '../types';

export const authApi = {
  async register(data: UserRegistrationRequest): Promise<User> {
    const response = await apiClient.post<User>('/auth/register', data);
    return response.data;
  },

  async login(data: UserLoginRequest): Promise<UserLoginResponse> {
    const response = await apiClient.post<UserLoginResponse>('/auth/login', data);
    return response.data;
  },
};
