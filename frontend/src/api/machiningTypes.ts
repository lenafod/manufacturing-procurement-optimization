import { apiClient } from './client';
import type { MachiningType } from '../types';

export const machiningTypesApi = {
  getAll: () => apiClient.get<MachiningType[]>('/machining-types'),
  getById: (id: number) => apiClient.get<MachiningType>(`/machining-types/${id}`),
};
