import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { workOrdersApi } from '../../api/workOrders';
import { purchaseRequestsApi } from '../../api/purchaseRequests';
import { procurementInquiriesApi } from '../../api/procurementInquiries';
import { DataTable } from '../../components/DataTable';
import { PurchaseRequestStatusPill } from '../../components/StatusPill';
import { LoadingState } from '../../components/LoadingState';
import type { PurchaseRequest } from '../../types';

const ACTIVE_STATUSES = new Set(['CREATED', 'SENT', 'IN_DELIVERY']);

export function HomePage() {
  const navigate = useNavigate();

  const workOrders = useQuery({ queryKey: ['workOrders'], queryFn: workOrdersApi.getAll });
  const purchaseRequests = useQuery({ queryKey: ['purchaseRequests', 'ALL'], queryFn: purchaseRequestsApi.getAll });
  const overdue = useQuery({ queryKey: ['purchaseRequests', 'overdue'], queryFn: purchaseRequestsApi.getOverdue });
  const inquiries = useQuery({ queryKey: ['procurementInquiries'], queryFn: procurementInquiriesApi.getAll });

  const isLoading = workOrders.isLoading || purchaseRequests.isLoading || overdue.isLoading || inquiries.isLoading;

  const activeRequestCount = purchaseRequests.data?.filter((p) => ACTIVE_STATUSES.has(p.status)).length ?? 0;
  const overdueCount = overdue.data?.length ?? 0;
  const pendingInquiryCount = inquiries.data?.filter((i) => i.status === 'POSLAT').length ?? 0;

  const recentRequests = [...(purchaseRequests.data ?? [])]
    .sort((a, b) => (a.createdAt < b.createdAt ? 1 : a.createdAt > b.createdAt ? -1 : b.id - a.id))
    .slice(0, 5);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.1rem' }}>
      <div>
        <h1 style={{ margin: 0 }}>Manufacturing Procurement Optimization</h1>
      </div>

      {isLoading ? (
        <LoadingState />
      ) : (
        <div className="stat-grid">
          <StatTile label="Radni nalozi" value={workOrders.data?.length ?? 0} onClick={() => navigate('/work-orders')} />
          <StatTile
            label="Zahtevi na čekanju"
            value={activeRequestCount}
            onClick={() => navigate('/purchase-requests')}
          />
          <StatTile
            label="Kasne isporuke"
            value={overdueCount}
            tone={overdueCount > 0 ? 'crit' : 'ok'}
            onClick={() => navigate('/purchase-requests')}
          />
          <StatTile
            label="Upiti bez odgovora"
            value={pendingInquiryCount}
            tone={pendingInquiryCount > 0 ? 'warn' : 'ok'}
            onClick={() => navigate('/procurement-inquiries')}
          />
        </div>
      )}

      <div className="card">
        <div className="card-toolbar">
          <h2>Poslednji zahtevi za nabavku</h2>
        </div>
        {purchaseRequests.isLoading && <LoadingState />}
        {purchaseRequests.data && (
          <DataTable<PurchaseRequest>
            columns={[
              { header: 'Pozicija', render: (p) => p.technicalSheet.positionName },
              { header: 'Dobavljač', render: (p) => p.supplierMaterial.supplier.name },
              { header: 'Kreiran', render: (p) => p.createdAt },
              { header: 'Status', render: (p) => <PurchaseRequestStatusPill status={p.status} /> },
            ]}
            rows={recentRequests}
            rowKey={(p) => p.id}
            onRowClick={() => navigate('/purchase-requests')}
            emptyMessage="Još nema zahteva za nabavku."
          />
        )}
      </div>
    </div>
  );
}

function StatTile({
  label,
  value,
  tone = 'neutral',
  onClick,
}: {
  label: string;
  value: number;
  tone?: 'neutral' | 'ok' | 'warn' | 'crit';
  onClick: () => void;
}) {
  return (
    <button type="button" className={`stat-tile stat-tile-${tone}`} onClick={onClick}>
      <span className="stat-tile-value">{value}</span>
      <span className="stat-tile-label">{label}</span>
    </button>
  );
}
