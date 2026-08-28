import { apiClient } from './client';
import type { WorkOrder } from '../types';

export interface WorkOrderFilters {
  id?: string;
  materialTypeId?: number;
  positionName?: string;
}

export const workOrdersApi = {
  getAll: () => apiClient.get<WorkOrder[]>('/work-orders'),
  search: (filters: WorkOrderFilters) => {
    const params = new URLSearchParams();
    if (filters.id) params.set('id', filters.id);
    if (filters.materialTypeId) params.set('materialTypeId', String(filters.materialTypeId));
    if (filters.positionName) params.set('positionName', filters.positionName);
    const query = params.toString();
    return apiClient.get<WorkOrder[]>(`/work-orders${query ? `?${query}` : ''}`);
  },
  getById: (id: string) => apiClient.get<WorkOrder>(`/work-orders/${id}`),
  create: (workOrder: { id: string }) => apiClient.post<WorkOrder>('/work-orders', workOrder),
  pdfUrl: (id: string) => `/api/work-orders/${encodeURIComponent(id)}/pdf`,
};
