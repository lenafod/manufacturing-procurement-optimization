import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { purchaseRequestsApi } from '../../api/purchaseRequests';
import { useApiMutation } from '../../hooks/useApiMutation';
import { DataTable } from '../../components/DataTable';
import { Select } from '../../components/Select';
import { Button } from '../../components/Button';
import { ErrorBanner } from '../../components/ErrorBanner';
import { PurchaseRequestStatusPill, StatusPill } from '../../components/StatusPill';
import { LoadingState } from '../../components/LoadingState';
import { ErrorState } from '../../components/ErrorState';
import type { PurchaseRequest, PurchaseRequestStatus } from '../../types';

type StatusFilter = PurchaseRequestStatus | 'ALL';

const STATUS_OPTIONS: { value: StatusFilter; label: string }[] = [
  { value: 'ALL', label: 'Svi statusi' },
  { value: 'CREATED', label: 'Kreiran' },
  { value: 'SENT', label: 'Poslat' },
  { value: 'IN_DELIVERY', label: 'U isporuci' },
  { value: 'DELIVERED', label: 'Isporučen' },
  { value: 'CANCELED', label: 'Otkazan' },
];

// sledeci korak u toku CREATED -> SENT -> IN_DELIVERY -> DELIVERED (nema ga za DELIVERED/CANCELED)
const NEXT_STEP: Partial<Record<PurchaseRequestStatus, { status: PurchaseRequestStatus; label: string }>> = {
  CREATED: { status: 'SENT', label: 'Pošalji' },
  SENT: { status: 'IN_DELIVERY', label: 'U isporuci' },
  IN_DELIVERY: { status: 'DELIVERED', label: 'Isporučeno' },
};
const CANCELABLE_STATUSES: PurchaseRequestStatus[] = ['CREATED', 'SENT', 'IN_DELIVERY'];

export function PurchaseRequestsPage() {
  const [status, setStatus] = useState<StatusFilter>('CREATED');

  const purchaseRequests = useQuery({
    queryKey: ['purchaseRequests', status],
    queryFn: () => (status === 'ALL' ? purchaseRequestsApi.getAll() : purchaseRequestsApi.getByStatus(status)),
  });

  const overdue = useQuery({ queryKey: ['purchaseRequests', 'overdue'], queryFn: purchaseRequestsApi.getOverdue });
  const overdueIds = new Set((overdue.data ?? []).map((p) => p.id));

  return (
    <div className="card">
      <div className="card-toolbar">
        <h2>Zahtevi za nabavku</h2>
        <Select
          label="Status"
          options={STATUS_OPTIONS.map((o) => ({ value: o.value, label: o.label }))}
          value={status}
          onChange={(e) => setStatus(e.target.value as StatusFilter)}
          style={{ minWidth: '160px' }}
        />
      </div>

      {overdue.data && overdue.data.length > 0 && (
        <div className="warn-banner">
          <span className="icon">⚠</span>
          <div>
            <strong>{overdue.data.length}</strong> {overdue.data.length === 1 ? 'zahtev kasni' : 'zahteva kasni'} sa
            očekivanom isporukom.
            <ul>
              {overdue.data.map((p) => (
                <li key={p.id}>
                  {p.technicalSheet.positionName}: očekivano {p.expectedDeliveryDate}
                </li>
              ))}
            </ul>
          </div>
        </div>
      )}

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
            {
              header: 'Očekivana isporuka',
              render: (p) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  {p.expectedDeliveryDate}
                  {overdueIds.has(p.id) && <StatusPill tone="crit">KASNI</StatusPill>}
                </div>
              ),
            },
            { header: 'Akcija', render: (p) => <StatusActions request={p} /> },
          ]}
          rows={purchaseRequests.data}
          rowKey={(p) => p.id}
          emptyMessage={status === 'ALL' ? 'Nema nijednog zahteva za nabavku.' : 'Nema zahteva za nabavku sa ovim statusom.'}
        />
      )}
    </div>
  );
}

function StatusActions({ request }: { request: PurchaseRequest }) {
  const updateStatus = useApiMutation((newStatus: PurchaseRequestStatus) => purchaseRequestsApi.updateStatus(request.id, newStatus), {
    invalidateKeys: [['purchaseRequests']],
  });

  const nextStep = NEXT_STEP[request.status];
  const canCancel = CANCELABLE_STATUSES.includes(request.status);

  if (!nextStep && !canCancel) {
    return null;
  }

  return (
    <div style={{ display: 'flex', gap: '0.4rem', justifyContent: 'flex-end' }}>
      {updateStatus.isError && <ErrorBanner error={updateStatus.error} />}
      {nextStep && (
        <Button
          variant="accent"
          disabled={updateStatus.isPending}
          onClick={() => updateStatus.mutate(nextStep.status)}
        >
          {nextStep.label}
        </Button>
      )}
      {canCancel && (
        <Button variant="ghost" disabled={updateStatus.isPending} onClick={() => updateStatus.mutate('CANCELED')}>
          Otkaži
        </Button>
      )}
    </div>
  );
}
