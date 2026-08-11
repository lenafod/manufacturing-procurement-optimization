import { apiClient } from './client';
import type { WorkOrder } from '../types';

export const workOrdersApi = {
  getAll: () => apiClient.get<WorkOrder[]>('/work-orders'),
  getById: (id: string) => apiClient.get<WorkOrder>(`/work-orders/${id}`),
  create: (workOrder: { id: string }) => apiClient.post<WorkOrder>('/work-orders', workOrder),
};
