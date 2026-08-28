import { apiClient } from './client';
import type { InquiryEmailPreview, ProcurementInquiry, SupplierMaterial } from '../types';

export interface SendInquiriesPayload {
  technicalSheetId?: string;
  supplierMaterialIds: number[];
  subject?: string;
  text?: string;
}

export const procurementInquiriesApi = {
  getAll: () => apiClient.get<ProcurementInquiry[]>('/procurement-inquiries'),
  getCandidates: (technicalSheetId: string) =>
    apiClient.get<SupplierMaterial[]>(`/procurement-inquiries/candidates/${encodeURIComponent(technicalSheetId)}`),
  send: (payload: SendInquiriesPayload) => apiClient.post<ProcurementInquiry[]>('/procurement-inquiries', payload),
  preview: (supplierMaterialId: number, technicalSheetId?: string) =>
    apiClient.get<InquiryEmailPreview>(
      `/procurement-inquiries/preview?supplierMaterialId=${supplierMaterialId}` +
        (technicalSheetId ? `&technicalSheetId=${encodeURIComponent(technicalSheetId)}` : ''),
    ),
  recordResponse: (id: number, confirmedQuantity: number, confirmedPrice: number, confirmedDeliveryTime: number) =>
    apiClient.patch<ProcurementInquiry>(
      `/procurement-inquiries/${id}/respond?confirmedQuantity=${confirmedQuantity}` +
        `&confirmedPrice=${confirmedPrice}&confirmedDeliveryTime=${confirmedDeliveryTime}`,
    ),
};
