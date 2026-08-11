import { apiClient } from './client';
import type { PurchaseRequest, PurchaseRequestStatus } from '../types';

export const purchaseRequestsApi = {
  getByStatus: (status: PurchaseRequestStatus) =>
    apiClient.get<PurchaseRequest[]>(`/purchase-requests/by-status?status=${status}`),
};
