import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { workOrdersApi } from '../../api/workOrders';
import { procurementOptimizationApi } from '../../api/procurementOptimization';
import { useApiMutation } from '../../hooks/useApiMutation';
import { Button } from '../../components/Button';
import { Select } from '../../components/Select';
import { WeightBalanceSlider } from '../../components/WeightBalanceSlider';
import { DataTable } from '../../components/DataTable';
import { PurchaseRequestStatusPill } from '../../components/StatusPill';
import { ErrorBanner } from '../../components/ErrorBanner';
import { LoadingState } from '../../components/LoadingState';
import { ErrorState } from '../../components/ErrorState';
import type { PurchaseRequest } from '../../types';

export function ProcurementOptimizationPage() {
  const workOrders = useQuery({ queryKey: ['workOrders'], queryFn: workOrdersApi.getAll });

  const [workOrderId, setWorkOrderId] = useState('');
  const [pricePercent, setPricePercent] = useState(50);

  const optimize = useApiMutation(() =>
    procurementOptimizationApi.optimize(workOrderId, pricePercent / 100, (100 - pricePercent) / 100),
  );

  return (
    <div className="card">
      <div className="card-toolbar">
        <h2>Optimizacija nabavke</h2>
      </div>

      <div className="panel-section-label" style={{ marginTop: 0 }}>
        1. Radni nalog
      </div>
      {workOrders.isLoading && <LoadingState />}
      {workOrders.isError && <ErrorState error={workOrders.error} />}
      {workOrders.data && (
        <Select
          label="Izaberi radni nalog"
          placeholder="Izaberi radni nalog"
          options={workOrders.data.map((w) => ({
            value: w.id,
            label: `${w.id} · ${w.technicalSheets.length} ${w.technicalSheets.length === 1 ? 'pozicija' : 'pozicije'}`,
          }))}
          value={workOrderId}
          onChange={(e) => {
            setWorkOrderId(e.target.value);
            optimize.reset();
          }}
        />
      )}

      <div className="panel-section-label">2. Odnos kriterijuma</div>
      <WeightBalanceSlider pricePercent={pricePercent} onChange={setPricePercent} />

      <div style={{ marginTop: '1.2rem' }}>
        <ErrorBanner error={optimize.error} />
        <Button variant="accent" disabled={!workOrderId || optimize.isPending} onClick={() => optimize.mutate(undefined)}>
          {optimize.isPending ? 'Optimizujem...' : 'Pokreni optimizaciju'}
        </Button>
      </div>

      {optimize.isSuccess && (
        <>
          <div className="panel-section-label">3. Rezultat</div>
          <DataTable<PurchaseRequest>
            columns={[
              { header: 'Pozicija', render: (p) => p.technicalSheet.positionName },
              { header: 'Dobavljač', render: (p) => p.supplierMaterial.supplier.name },
              { header: 'Materijal', render: (p) => p.supplierMaterial.materialType.materialName },
              { header: 'Cena', render: (p) => p.totalPrice.toFixed(2), numeric: true },
              { header: 'Rok', render: (p) => `${p.supplierMaterial.deliveryTime} d`, numeric: true },
              { header: 'Status', render: (p) => <PurchaseRequestStatusPill status={p.status} /> },
            ]}
            rows={optimize.data}
            rowKey={(p) => p.id}
            emptyMessage="Sve pozicije ovog naloga već imaju dovoljno zaliha — nema potrebe za nabavkom."
          />
        </>
      )}
    </div>
  );
}
