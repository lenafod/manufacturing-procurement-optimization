import { apiClient } from './client';
import type { SectionShape, SupplierMaterial } from '../types';

export interface CreateSupplierMaterialPayload {
  supplier: { id: number };
  materialType: { id: number };
  materialSectionType: { id: number };
  pricePerUnit: number;
  deliveryTime: number;
}

export const supplierMaterialsApi = {
  getAll: () => apiClient.get<SupplierMaterial[]>('/supplier-materials'),
  getById: (id: number) => apiClient.get<SupplierMaterial>(`/supplier-materials/${id}`),
  create: (payload: CreateSupplierMaterialPayload) =>
    apiClient.post<SupplierMaterial>('/supplier-materials', payload),
  findOptimal: (
    materialTypeName: string,
    materialSectionTypeName: SectionShape,
    weightPrice: number,
    weightDeliveryTime: number,
  ) =>
    apiClient.get<SupplierMaterial>(
      `/supplier-materials/optimal?materialTypeName=${encodeURIComponent(materialTypeName)}` +
        `&materialSectionTypeName=${materialSectionTypeName}` +
        `&weightPrice=${weightPrice}&weightDeliveryTime=${weightDeliveryTime}`,
    ),
};
