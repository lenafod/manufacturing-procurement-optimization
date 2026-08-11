import { createBrowserRouter } from 'react-router-dom';
import { Layout } from './components/Layout';
import { HomePage } from './features/home/HomePage';
import { ReferenceDataPage } from './features/reference-data/ReferenceDataPage';
import { SuppliersPage } from './features/suppliers/SuppliersPage';
import { InventoryPage } from './features/inventory/InventoryPage';
import { WorkOrdersPage } from './features/work-orders/WorkOrdersPage';
import { PurchaseRequestsPage } from './features/purchase-requests/PurchaseRequestsPage';
import { ProcurementOptimizationPage } from './features/procurement-optimization/ProcurementOptimizationPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'reference-data', element: <ReferenceDataPage /> },
      { path: 'suppliers', element: <SuppliersPage /> },
      { path: 'inventory', element: <InventoryPage /> },
      { path: 'work-orders', element: <WorkOrdersPage /> },
      { path: 'purchase-requests', element: <PurchaseRequestsPage /> },
      { path: 'procurement-optimization', element: <ProcurementOptimizationPage /> },
    ],
  },
]);
