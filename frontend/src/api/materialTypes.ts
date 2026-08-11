import { apiClient } from './client';
import type { MaterialType } from '../types';

export const materialTypesApi = {
  getAll: () => apiClient.get<MaterialType[]>('/material-types'),
  getById: (id: number) => apiClient.get<MaterialType>(`/material-types/${id}`),
};
