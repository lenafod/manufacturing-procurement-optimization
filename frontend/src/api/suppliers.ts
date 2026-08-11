import { apiClient } from './client';
import type { Supplier } from '../types';

export const suppliersApi = {
  getAll: () => apiClient.get<Supplier[]>('/suppliers'),
  getById: (id: number) => apiClient.get<Supplier>(`/suppliers/${id}`),
  create: (supplier: Omit<Supplier, 'id'>) => apiClient.post<Supplier>('/suppliers', supplier),
};
