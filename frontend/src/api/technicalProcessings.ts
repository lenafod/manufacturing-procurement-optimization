import { apiClient } from './client';
import type { TechnicalProcessing } from '../types';

export const technicalProcessingsApi = {
  getAll: () => apiClient.get<TechnicalProcessing[]>('/technical-processings'),
  getById: (id: number) => apiClient.get<TechnicalProcessing>(`/technical-processings/${id}`),
};
