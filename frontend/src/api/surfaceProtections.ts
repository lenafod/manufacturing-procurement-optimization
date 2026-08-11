import { apiClient } from './client';
import type { SurfaceProtection } from '../types';

export const surfaceProtectionsApi = {
  getAll: () => apiClient.get<SurfaceProtection[]>('/surface-protections'),
  getById: (id: number) => apiClient.get<SurfaceProtection>(`/surface-protections/${id}`),
};
