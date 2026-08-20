import { apiClient } from './client';
import type { Inventory } from '../types';

export interface CreateInventoryPayload {
  materialType: { id: number };
  materialSectionType: { id: number };
  quantity: number;
}

export const inventoryApi = {
  getAll: () => apiClient.get<Inventory[]>('/inventory'),
  getById: (id: number) => apiClient.get<Inventory>(`/inventory/${id}`),
  create: (payload: CreateInventoryPayload) => apiClient.post<Inventory>('/inventory', payload),
  check: (materialTypeName: string, materialSectionTypeId: number, requiredQuantity: number) =>
    apiClient.get<boolean>(
      `/inventory/check?materialTypeName=${encodeURIComponent(materialTypeName)}` +
        `&materialSectionTypeId=${materialSectionTypeId}&requiredQuantity=${requiredQuantity}`,
    ),
};
