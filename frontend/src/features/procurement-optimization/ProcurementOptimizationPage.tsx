import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { workOrdersApi } from '../../api/workOrders';
import { procurementOptimizationApi } from '../../api/procurementOptimization';
import { useApiMutation } from '../../hooks/useApiMutation';
import { Button } from '../../components/Button';
import { Select } from '../../components/Select';
import { Modal } from '../../components/Modal';
import { DataTable } from '../../components/DataTable';
import { PurchaseRequestStatusPill } from '../../components/StatusPill';
import { ErrorBanner } from '../../components/ErrorBanner';
import { LoadingState } from '../../components/LoadingState';
import { ErrorState } from '../../components/ErrorState';
import type { OptimizationResult, PartialFulfillment, PurchaseRequest, SkippedPosition } from '../../types';

export function ProcurementOptimizationPage() {
  const workOrders = useQuery({ queryKey: ['workOrders'], queryFn: workOrdersApi.getAll });

  const [workOrderId, setWorkOrderId] = useState('');
  const [pricePercent, setPricePercent] = useState(50);
  const [previewOpen, setPreviewOpen] = useState(false);

  const preview = useApiMutation(() =>
    procurementOptimizationApi.preview(workOrderId, pricePercent / 100, (100 - pricePercent) / 100),
  );

  const optimize = useApiMutation(() =>
    procurementOptimizationApi.optimize(workOrderId, pricePercent / 100, (100 - pricePercent) / 100),
  );

  const resetResults = () => {
    preview.reset();
    optimize.reset();
    setPreviewOpen(false);
  };

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
            resetResults();
          }}
        />
      )}

      <div className="panel-section-label">2. Strategija izbora dobavljača</div>
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.2rem' }}>
        <Button
          variant={pricePercent === 100 ? 'accent' : 'ghost'}
          onClick={() => {
            setPricePercent(100);
            resetResults();
          }}
        >
          Najniža cena
        </Button>
        <Button
          variant={pricePercent === 50 ? 'accent' : 'ghost'}
          onClick={() => {
            setPricePercent(50);
            resetResults();
          }}
        >
          Balans
        </Button>
        <Button
          variant={pricePercent === 0 ? 'accent' : 'ghost'}
          onClick={() => {
            setPricePercent(0);
            resetResults();
          }}
        >
          Najkraći rok
        </Button>
      </div>

      <div style={{ marginTop: '1.2rem' }}>
        <Button
          variant="accent"
          disabled={!workOrderId || preview.isPending}
          onClick={() => {
            setPreviewOpen(true);
            preview.mutate(undefined);
          }}
        >
          {preview.isPending ? 'Učitavam pregled...' : 'Prikaži pregled'}
        </Button>
      </div>

      {previewOpen && (
        <Modal title={`Pregled — ${workOrderId}`} onClose={resetResults} wide>
          {preview.isError && <ErrorBanner error={preview.error} />}
          {preview.isPending && <LoadingState />}

          {optimize.isSuccess ? (
            <>
              <p style={{ margin: '0 0 0.9rem', color: 'var(--ok)', fontSize: '0.86rem' }}>
                Zahtevi za nabavku su kreirani.
              </p>
              <OptimizationResultView result={optimize.data} />
              <div className="modal-actions">
                <Button variant="ghost" onClick={resetResults}>
                  Zatvori
                </Button>
              </div>
            </>
          ) : (
            preview.isSuccess && (
              <>
                <p style={{ margin: '0 0 0.9rem', color: 'var(--steel)', fontSize: '0.86rem' }}>
                  Predlog na osnovu trenutno poznatih količina — ništa još nije sačuvano.
                </p>
                <OptimizationResultView result={preview.data} draft />
                <ErrorBanner error={optimize.error} />
                <div className="modal-actions">
                  <Button variant="ghost" onClick={resetResults}>
                    Otkaži
                  </Button>
                  <Button variant="accent" disabled={optimize.isPending} onClick={() => optimize.mutate(undefined)}>
                    {optimize.isPending ? 'Pokrećem...' : 'Pokreni optimizaciju'}
                  </Button>
                </div>
              </>
            )
          )}
        </Modal>
      )}
    </div>
  );
}

// draft=true za pregled (dry-run): PurchaseRequest redovi jos nemaju id (nista nije sacuvano u
// bazi), pa se rowKey pravi od pozicije + ponude umesto od id-ja
function OptimizationResultView({ result, draft = false }: { result: OptimizationResult; draft?: boolean }) {
  return (
    <>
      <DataTable<PurchaseRequest>
        columns={[
          { header: 'Pozicija', render: (p) => p.technicalSheet.positionName },
          { header: 'Dobavljač', render: (p) => p.supplierMaterial.supplier.name },
          { header: 'Materijal', render: (p) => p.supplierMaterial.materialType.materialName },
          { header: 'Količina', render: (p) => p.requiredQuantity, numeric: true },
          { header: 'Cena', render: (p) => p.totalPrice.toFixed(2), numeric: true },
          { header: 'Rok', render: (p) => `${p.supplierMaterial.deliveryTime} d`, numeric: true },
          ...(draft ? [] : [{ header: 'Status', render: (p: PurchaseRequest) => <PurchaseRequestStatusPill status={p.status} /> }]),
        ]}
        rows={result.created}
        rowKey={(p) => (draft ? `${p.technicalSheet.id}-${p.supplierMaterial.id}` : p.id)}
        emptyMessage="Nijedna pozicija ovog naloga nije dobila novi zahtev za nabavku."
      />

      {result.partial.length > 0 && (
        <div className="warn-banner">
          <span className="icon">⚠</span>
          <div>
            Kombinovana raspoloživa količina svih dobavljača nije dovoljna za sledeće pozicije — {draft ? 'bila bi' : 'je'}{' '}
            napravljen zahtev za ono što je pokriveno, ostatak nedostaje:
            <ul>
              {result.partial.map((p: PartialFulfillment) => (
                <li key={p.positionName}>
                  <strong>{p.positionName}</strong> — nedostaje {p.missingQuantity.toFixed(2)}
                </li>
              ))}
            </ul>
          </div>
        </div>
      )}

      {result.skipped.length > 0 && (
        <ul style={{ marginTop: '0.9rem', paddingLeft: '1.1rem', color: 'var(--steel)', fontSize: '0.86rem' }}>
          {result.skipped.map((s: SkippedPosition) => (
            <li key={s.positionName}>
              <strong style={{ color: 'var(--ink)' }}>{s.positionName}</strong> — {s.reason}
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
