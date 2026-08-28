// Odslikava com.mpo.enums i com.mpo.entity sa backend-a.

export type SectionShape = 'ROUND' | 'RECTANGULAR' | 'HEXAGONAL' | 'PIPE' | 'CUBE';

export type PurchaseRequestStatus =
  | 'CREATED'
  | 'SENT'
  | 'IN_DELIVERY'
  | 'DELIVERED'
  | 'CANCELED';

export type ProcurementInquiryStatus = 'POSLAT' | 'ODGOVOREN';

export interface MaterialType {
  id: number;
  materialName: string;
  density: number;
}

export interface MaterialSectionType {
  id: number;
  typeName: SectionShape;
  dim1: number;
  dim2: number | null;
  usesDim2: boolean;
}

export interface MachiningType {
  id: number;
  name: string;
}

export interface SurfaceProtection {
  id: number;
  name: string;
}

export interface TechnicalProcessing {
  id: number;
  name: string;
}

export interface Supplier {
  id: number;
  name: string;
  address: string;
  phoneNumber: string;
  email: string;
}

export interface SupplierMaterial {
  id: number;
  supplier: Supplier;
  materialType: MaterialType;
  materialSectionType: MaterialSectionType;
  pricePerUnit: number;
  deliveryTime: number;
  availableQuantity: number;
}

export interface Inventory {
  id: number;
  materialType: MaterialType;
  materialSectionType: MaterialSectionType;
  quantity: number;
}

export interface WorkOrder {
  id: string;
  technicalSheets: TechnicalSheet[];
}

export interface TechnicalSheet {
  id: string;
  quantity: number;
  sheetId: string;
  sheetVersion: string;
  workOrder?: WorkOrder;
  positionName: string;
  materialType: MaterialType;
  materialSectionType: MaterialSectionType;
  partLength: number;
  technicalAllowance: number;
  positionSurface: number | null;
  technicalProcessing: TechnicalProcessing;
  surfaceProtection: SurfaceProtection;
  machiningType: MachiningType;
  prepLength: number | null;
  partMass: number | null;
  blankMass: number | null;
  removedMass: number | null;
}

export interface PurchaseRequest {
  id: number;
  technicalSheet: TechnicalSheet;
  supplierMaterial: SupplierMaterial;
  requiredQuantity: number;
  totalPrice: number;
  status: PurchaseRequestStatus;
  createdAt: string;
  expectedDeliveryDate: string;
  actualDeliveryDate: string | null;
}

export interface SkippedPosition {
  positionName: string;
  reason: string;
}

export interface PartialFulfillment {
  positionName: string;
  missingQuantity: number;
}

export interface InquiryEmailPreview {
  to: string;
  subject: string;
  text: string;
}

export interface ProcurementInquiry {
  id: number;
  technicalSheet: TechnicalSheet | null;
  supplierMaterial: SupplierMaterial;
  requestedQuantity: number | null;
  confirmedQuantity: number | null;
  confirmedPrice: number | null;
  confirmedDeliveryTime: number | null;
  status: ProcurementInquiryStatus;
  sentAt: string;
  respondedAt: string | null;
}

export interface OptimizationResult {
  created: PurchaseRequest[];
  partial: PartialFulfillment[];
  skipped: SkippedPosition[];
}
