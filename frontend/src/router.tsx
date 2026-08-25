import { createBrowserRouter } from 'react-router-dom';
import { Layout } from './components/Layout';
import { HomePage } from './features/home/HomePage';
import { ReferenceDataPage } from './features/reference-data/ReferenceDataPage';
import { SuppliersPage } from './features/suppliers/SuppliersPage';
import { SupplierDetailPage } from './features/suppliers/SupplierDetailPage';
import { InventoryPage } from './features/inventory/InventoryPage';
import { WorkOrdersPage } from './features/work-orders/WorkOrdersPage';
import { WorkOrderDetailPage } from './features/work-orders/WorkOrderDetailPage';
import { PurchaseRequestsPage } from './features/purchase-requests/PurchaseRequestsPage';
import { ProcurementOptimizationPage } from './features/procurement-optimization/ProcurementOptimizationPage';
import { ProcurementInquiriesPage } from './features/procurement-inquiries/ProcurementInquiriesPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'reference-data', element: <ReferenceDataPage /> },
      { path: 'suppliers', element: <SuppliersPage /> },
      { path: 'suppliers/:id', element: <SupplierDetailPage /> },
      { path: 'inventory', element: <InventoryPage /> },
      { path: 'work-orders', element: <WorkOrdersPage /> },
      { path: 'work-orders/:id', element: <WorkOrderDetailPage /> },
      { path: 'purchase-requests', element: <PurchaseRequestsPage /> },
      { path: 'procurement-inquiries', element: <ProcurementInquiriesPage /> },
      { path: 'procurement-optimization', element: <ProcurementOptimizationPage /> },
    ],
  },
]);
