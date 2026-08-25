import { apiClient } from './client';
import type { OptimizationResult } from '../types';

export const procurementOptimizationApi = {
  optimize: (workOrderId: string, weightPrice: number, weightDeliveryTime: number) =>
    apiClient.get<OptimizationResult>(
      `/procurement-optimization/optimize/${encodeURIComponent(workOrderId)}` +
        `?weightPrice=${weightPrice}&weightDeliveryTime=${weightDeliveryTime}`,
    ),
  preview: (workOrderId: string, weightPrice: number, weightDeliveryTime: number) =>
    apiClient.get<OptimizationResult>(
      `/procurement-optimization/preview/${encodeURIComponent(workOrderId)}` +
        `?weightPrice=${weightPrice}&weightDeliveryTime=${weightDeliveryTime}`,
    ),
};
