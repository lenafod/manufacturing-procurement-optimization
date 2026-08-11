import { apiClient } from './client';
import type { PurchaseRequest } from '../types';

export const procurementOptimizationApi = {
  optimize: (workOrderId: string, weightPrice: number, weightDeliveryTime: number) =>
    apiClient.get<PurchaseRequest[]>(
      `/procurement-optimization/optimize/${encodeURIComponent(workOrderId)}` +
        `?weightPrice=${weightPrice}&weightDeliveryTime=${weightDeliveryTime}`,
    ),
};
