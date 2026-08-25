import { apiClient } from './client';
import type { SupplierMaterial } from '../types';

export interface CreateSupplierMaterialPayload {
  supplier: { id: number };
  materialType: { id: number };
  materialSectionType: { id: number };
  pricePerUnit: number;
  deliveryTime: number;
  availableQuantity: number;
}

export interface UpdateSupplierMaterialPayload {
  pricePerUnit: number;
  deliveryTime: number;
  availableQuantity: number;
}

export const supplierMaterialsApi = {
  getAll: () => apiClient.get<SupplierMaterial[]>('/supplier-materials'),
  getById: (id: number) => apiClient.get<SupplierMaterial>(`/supplier-materials/${id}`),
  create: (payload: CreateSupplierMaterialPayload) =>
    apiClient.post<SupplierMaterial>('/supplier-materials', payload),
  update: (id: number, payload: UpdateSupplierMaterialPayload) =>
    apiClient.put<SupplierMaterial>(`/supplier-materials/${id}`, payload),
  findOptimal: (
    materialTypeName: string,
    materialSectionTypeId: number,
    weightPrice: number,
    weightDeliveryTime: number,
  ) =>
    apiClient.get<SupplierMaterial>(
      `/supplier-materials/optimal?materialTypeName=${encodeURIComponent(materialTypeName)}` +
        `&materialSectionTypeId=${materialSectionTypeId}` +
        `&weightPrice=${weightPrice}&weightDeliveryTime=${weightDeliveryTime}`,
    ),
};
