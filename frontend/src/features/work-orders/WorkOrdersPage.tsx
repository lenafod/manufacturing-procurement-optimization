import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { workOrdersApi } from '../../api/workOrders';
import { materialTypesApi } from '../../api/materialTypes';
import { useApiMutation } from '../../hooks/useApiMutation';
import { DataTable } from '../../components/DataTable';
import { Button } from '../../components/Button';
import { Modal } from '../../components/Modal';
import { TextField } from '../../components/TextField';
import { Select } from '../../components/Select';
import { ErrorBanner } from '../../components/ErrorBanner';
import { LoadingState } from '../../components/LoadingState';
import { ErrorState } from '../../components/ErrorState';
import type { WorkOrder } from '../../types';

export function WorkOrdersPage() {
  const navigate = useNavigate();
  const [modalOpen, setModalOpen] = useState(false);
  const [idFilter, setIdFilter] = useState('');
  const [materialTypeId, setMaterialTypeId] = useState('');
  const [positionFilter, setPositionFilter] = useState('');

  const materialTypes = useQuery({ queryKey: ['materialTypes'], queryFn: materialTypesApi.getAll });

  const workOrders = useQuery({
    queryKey: ['workOrders', 'search', idFilter, materialTypeId, positionFilter],
    queryFn: () =>
      workOrdersApi.search({
        id: idFilter || undefined,
        materialTypeId: materialTypeId ? Number(materialTypeId) : undefined,
        positionName: positionFilter || undefined,
      }),
  });

  const hasFilters = idFilter || materialTypeId || positionFilter;

  return (
    <div className="card">
      <div className="card-toolbar">
        <h2>Radni nalozi</h2>
        <Button variant="accent" onClick={() => setModalOpen(true)}>
          + Novi radni nalog
        </Button>
      </div>

      <div className="panel-grid" style={{ marginBottom: '0.9rem' }}>
        <TextField
          label="Pretraga po ID-ju"
          placeholder="npr. RN-2026"
          value={idFilter}
          onChange={(e) => setIdFilter(e.target.value)}
        />
        <Select
          label="Materijal"
          placeholder="Svi materijali"
          options={(materialTypes.data ?? []).map((m) => ({ value: String(m.id), label: m.materialName }))}
          value={materialTypeId}
          onChange={(e) => setMaterialTypeId(e.target.value)}
        />
        <TextField
          label="Pretraga po poziciji"
          placeholder="naziv pozicije"
          value={positionFilter}
          onChange={(e) => setPositionFilter(e.target.value)}
        />
      </div>

      {workOrders.isLoading && <LoadingState />}
      {workOrders.isError && <ErrorState error={workOrders.error} />}
      {workOrders.data && (
        <DataTable<WorkOrder>
          columns={[
            { header: 'ID radnog naloga', render: (w) => w.id },
            { header: 'Pozicija', render: (w) => w.technicalSheets.length, numeric: true },
            {
              header: 'Ukupno komada',
              render: (w) => w.technicalSheets.reduce((sum, sheet) => sum + sheet.quantity, 0),
              numeric: true,
            },
          ]}
          rows={workOrders.data}
          rowKey={(w) => w.id}
          emptyMessage={hasFilters ? 'Nijedan radni nalog ne odgovara filterima.' : 'Još nema radnih naloga. Kreiraj prvi.'}
          onRowClick={(w) => navigate(`/work-orders/${w.id}`)}
        />
      )}

      {modalOpen && <NewWorkOrderModal onClose={() => setModalOpen(false)} onCreated={(id) => navigate(`/work-orders/${id}`)} />}
    </div>
  );
}

function NewWorkOrderModal({ onClose, onCreated }: { onClose: () => void; onCreated: (id: string) => void }) {
  const [id, setId] = useState('');

  const createWorkOrder = useApiMutation(workOrdersApi.create, {
    invalidateKeys: [['workOrders']],
    onSuccess: (workOrder) => onCreated(workOrder.id),
  });

  return (
    <Modal title="Novi radni nalog" onClose={onClose}>
      <ErrorBanner error={createWorkOrder.error} />
      <TextField
        label="ID radnog naloga"
        hint="Poslovni ključ, mora biti jedinstven"
        mono
        autoFocus
        error={createWorkOrder.isError}
        value={id}
        onChange={(e) => setId(e.target.value)}
        placeholder="npr. RN-2026-014"
      />
      <div className="modal-actions">
        <Button variant="ghost" onClick={onClose}>
          Otkaži
        </Button>
        <Button
          variant="accent"
          disabled={!id || createWorkOrder.isPending}
          onClick={() => createWorkOrder.mutate({ id })}
        >
          {createWorkOrder.isPending ? 'Kreiranje...' : 'Kreiraj'}
        </Button>
      </div>
    </Modal>
  );
}
