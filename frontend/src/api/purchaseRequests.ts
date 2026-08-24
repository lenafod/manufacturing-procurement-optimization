import { apiClient } from './client';
import type { PurchaseRequest, PurchaseRequestStatus } from '../types';

export const purchaseRequestsApi = {
  getAll: () => apiClient.get<PurchaseRequest[]>('/purchase-requests'),
  getByStatus: (status: PurchaseRequestStatus) =>
    apiClient.get<PurchaseRequest[]>(`/purchase-requests/by-status?status=${status}`),
  getOverdue: () => apiClient.get<PurchaseRequest[]>('/purchase-requests/overdue'),
  updateStatus: (id: number, status: PurchaseRequestStatus) =>
    apiClient.patch<PurchaseRequest>(`/purchase-requests/${id}/status?status=${status}`),
};
