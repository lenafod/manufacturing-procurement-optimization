import { apiClient } from './client';
import type { MaterialSectionType } from '../types';

export const materialSectionTypesApi = {
  getAll: () => apiClient.get<MaterialSectionType[]>('/material-section-types'),
  getById: (id: number) => apiClient.get<MaterialSectionType>(`/material-section-types/${id}`),
};
