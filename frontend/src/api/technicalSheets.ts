import { apiClient } from './client';
import type { TechnicalSheet } from '../types';

export interface CreateTechnicalSheetPayload {
  id: string;
  quantity: number;
  sheetId: string;
  sheetVersion: string;
  workOrder: { id: string };
  positionName: string;
  materialType: { id: number };
  materialSectionType: { id: number };
  partLength: number;
  technicalAllowance: number;
  technicalProcessing: { id: number };
  surfaceProtection: { id: number };
  machiningType: { id: number };
}

export const technicalSheetsApi = {
  getBySheetId: (sheetId: string, sortDirection: 'asc' | 'desc') =>
    apiClient.get<TechnicalSheet[]>(
      `/technical-sheets/by-sheet-id?sheetId=${encodeURIComponent(sheetId)}&sortDirection=${sortDirection}`,
    ),
  getByIdAndVersion: (id: string, version: string) =>
    apiClient.get<TechnicalSheet>(
      `/technical-sheets/by-id-and-version?id=${encodeURIComponent(id)}&version=${encodeURIComponent(version)}`,
    ),
  create: (payload: CreateTechnicalSheetPayload) =>
    apiClient.post<TechnicalSheet>('/technical-sheets', payload),
  pdfUrl: (id: string, version: string) =>
    `/api/technical-sheets/by-id-and-version/pdf?id=${encodeURIComponent(id)}&version=${encodeURIComponent(version)}`,
};
