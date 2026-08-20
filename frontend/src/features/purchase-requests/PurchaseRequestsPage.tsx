import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { purchaseRequestsApi } from '../../api/purchaseRequests';
import { DataTable } from '../../components/DataTable';
import { Select } from '../../components/Select';
import { PurchaseRequestStatusPill } from '../../components/StatusPill';
import { LoadingState } from '../../components/LoadingState';
import { ErrorState } from '../../components/ErrorState';
import type { PurchaseRequest, PurchaseRequestStatus } from '../../types';

const STATUS_OPTIONS: { value: PurchaseRequestStatus; label: string }[] = [
  { value: 'CREATED', label: 'Kreiran' },
  { value: 'SENT', label: 'Poslat' },
  { value: 'IN_DELIVERY', label: 'U isporuci' },
  { value: 'DELIVERED', label: 'Isporučen' },
  { value: 'CANCELED', label: 'Otkazan' },
];

export function PurchaseRequestsPage() {
  const [status, setStatus] = useState<PurchaseRequestStatus>('CREATED');

  const purchaseRequests = useQuery({
    queryKey: ['purchaseRequests', status],
    queryFn: () => purchaseRequestsApi.getByStatus(status),
  });

  return (
    <div className="card">
      <div className="card-toolbar">
        <h2>Zahtevi za nabavku</h2>
        <Select
          label="Status"
          options={STATUS_OPTIONS.map((o) => ({ value: o.value, label: o.label }))}
          value={status}
          onChange={(e) => setStatus(e.target.value as PurchaseRequestStatus)}
          style={{ minWidth: '160px' }}
        />
      </div>

      {purchaseRequests.isLoading && <LoadingState />}
      {purchaseRequests.isError && <ErrorState error={purchaseRequests.error} />}
      {purchaseRequests.data && (
        <DataTable<PurchaseRequest>
          columns={[
            { header: 'Pozicija', render: (p) => p.technicalSheet.positionName },
            { header: 'Dobavljač', render: (p) => p.supplierMaterial.supplier.name },
            { header: 'Materijal', render: (p) => p.supplierMaterial.materialType.materialName },
            { header: 'Količina', render: (p) => p.requiredQuantity, numeric: true },
            { header: 'Cena', render: (p) => p.totalPrice.toFixed(2), numeric: true },
            { header: 'Status', render: (p) => <PurchaseRequestStatusPill status={p.status} /> },
            { header: 'Kreiran', render: (p) => p.createdAt },
            { header: 'Očekivana isporuka', render: (p) => p.expectedDeliveryDate },
          ]}
          rows={purchaseRequests.data}
          rowKey={(p) => p.id}
          emptyMessage="Nema zahteva za nabavku sa ovim statusom."
        />
      )}
    </div>
  );
}
