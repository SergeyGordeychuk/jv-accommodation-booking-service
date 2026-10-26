import { apiClient } from './client';
import type { Payment, PaymentRequest } from '../types';

export const paymentsApi = {
  async createPaymentSession(data: PaymentRequest): Promise<Payment> {
    const response = await apiClient.post<Payment>('/payments', data);
    return response.data;
  },

  async getAll(userId?: number): Promise<Payment[]> {
    const response = await apiClient.get<Payment[]>('/payments', {
      params: userId ? { user_id: userId } : undefined,
    });
    return response.data;
  },

  async handleSuccess(sessionId: string): Promise<Payment> {
    const response = await apiClient.get<Payment>('/payments/success', {
      params: { session_id: sessionId },
    });
    return response.data;
  },

  async handleCancel(sessionId: string): Promise<string> {
    const response = await apiClient.get<string>('/payments/cancel', {
      params: { session_id: sessionId },
    });
    return response.data;
  },
};
