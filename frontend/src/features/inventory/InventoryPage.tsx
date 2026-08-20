import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { inventoryApi } from '../../api/inventory';
import { materialTypesApi } from '../../api/materialTypes';
import { materialSectionTypesApi } from '../../api/materialSectionTypes';
import { useApiMutation } from '../../hooks/useApiMutation';
import { DataTable } from '../../components/DataTable';
import { Button } from '../../components/Button';
import { Modal } from '../../components/Modal';
import { TextField } from '../../components/TextField';
import { Select } from '../../components/Select';
import { StatusPill } from '../../components/StatusPill';
import { ErrorBanner } from '../../components/ErrorBanner';
import { LoadingState } from '../../components/LoadingState';
import { ErrorState } from '../../components/ErrorState';
import { formatMaterialSection } from '../../utils/formatMaterialSection';
import type { Inventory } from '../../types';

export function InventoryPage() {
  const [modalOpen, setModalOpen] = useState(false);

  const inventory = useQuery({ queryKey: ['inventory'], queryFn: inventoryApi.getAll });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
      <div className="card">
        <div className="card-toolbar">
          <h2>Magacin</h2>
          <Button variant="accent" onClick={() => setModalOpen(true)}>
            + Nova stavka
          </Button>
        </div>

        {inventory.isLoading && <LoadingState />}
        {inventory.isError && <ErrorState error={inventory.error} />}
        {inventory.data && (
          <DataTable<Inventory>
            columns={[
              { header: 'Materijal', render: (i) => i.materialType.materialName },
              { header: 'Presek', render: (i) => formatMaterialSection(i.materialSectionType) },
              { header: 'Količina', render: (i) => i.quantity, numeric: true },
            ]}
            rows={inventory.data}
            rowKey={(i) => i.id}
            emptyMessage="Magacin je prazan — dodaj prvu stavku."
          />
        )}
      </div>

      <CheckStockCard />

      {modalOpen && <NewInventoryModal onClose={() => setModalOpen(false)} />}
    </div>
  );
}

function NewInventoryModal({ onClose }: { onClose: () => void }) {
  const materialTypes = useQuery({ queryKey: ['materialTypes'], queryFn: materialTypesApi.getAll });
  const materialSectionTypes = useQuery({
    queryKey: ['materialSectionTypes'],
    queryFn: materialSectionTypesApi.getAll,
  });

  const [materialTypeId, setMaterialTypeId] = useState('');
  const [materialSectionTypeId, setMaterialSectionTypeId] = useState('');
  const [quantity, setQuantity] = useState('');

  const createInventory = useApiMutation(inventoryApi.create, {
    invalidateKeys: [['inventory']],
    onSuccess: onClose,
  });

  const canSubmit = materialTypeId && materialSectionTypeId && quantity;

  return (
    <Modal title="Nova stavka magacina" onClose={onClose}>
      <ErrorBanner error={createInventory.error} />
      <Select
        label="Vrsta materijala"
        placeholder="Izaberi materijal"
        options={(materialTypes.data ?? []).map((m) => ({ value: String(m.id), label: m.materialName }))}
        value={materialTypeId}
        onChange={(e) => setMaterialTypeId(e.target.value)}
      />
      <Select
        label="Presek"
        placeholder="Izaberi presek"
        options={(materialSectionTypes.data ?? []).map((s) => ({
          value: String(s.id),
          label: formatMaterialSection(s),
        }))}
        value={materialSectionTypeId}
        onChange={(e) => setMaterialSectionTypeId(e.target.value)}
      />
      <TextField
        label="Količina"
        hint="ukupna dužina materijala na lageru"
        type="number"
        value={quantity}
        onChange={(e) => setQuantity(e.target.value)}
      />
      <div className="modal-actions">
        <Button variant="ghost" onClick={onClose}>
          Otkaži
        </Button>
        <Button
          variant="accent"
          disabled={!canSubmit || createInventory.isPending}
          onClick={() =>
            createInventory.mutate({
              materialType: { id: Number(materialTypeId) },
              materialSectionType: { id: Number(materialSectionTypeId) },
              quantity: Number(quantity),
            })
          }
        >
          {createInventory.isPending ? 'Čuvanje...' : 'Sačuvaj'}
        </Button>
      </div>
    </Modal>
  );
}

function CheckStockCard() {
  const materialTypes = useQuery({ queryKey: ['materialTypes'], queryFn: materialTypesApi.getAll });
  const materialSectionTypes = useQuery({
    queryKey: ['materialSectionTypes'],
    queryFn: materialSectionTypesApi.getAll,
  });

  const [materialTypeId, setMaterialTypeId] = useState('');
  const [materialSectionTypeId, setMaterialSectionTypeId] = useState('');
  const [requiredQuantity, setRequiredQuantity] = useState('');
  const [result, setResult] = useState<boolean | null>(null);

  const selectedMaterialType = materialTypes.data?.find((m) => String(m.id) === materialTypeId);

  const checkStock = useApiMutation(
    () => inventoryApi.check(selectedMaterialType!.materialName, Number(materialSectionTypeId), Number(requiredQuantity)),
    { onSuccess: (data) => setResult(data) },
  );

  const canCheck = materialTypeId && materialSectionTypeId && requiredQuantity;

  return (
    <div className="card">
      <div className="card-toolbar">
        <h2>Provera stanja</h2>
      </div>
      <ErrorBanner error={checkStock.error} />
      <Select
        label="Vrsta materijala"
        placeholder="Izaberi materijal"
        options={(materialTypes.data ?? []).map((m) => ({ value: String(m.id), label: m.materialName }))}
        value={materialTypeId}
        onChange={(e) => {
          setMaterialTypeId(e.target.value);
          setResult(null);
        }}
      />
      <Select
        label="Presek"
        placeholder="Izaberi presek"
        options={(materialSectionTypes.data ?? []).map((s) => ({
          value: String(s.id),
          label: formatMaterialSection(s),
        }))}
        value={materialSectionTypeId}
        onChange={(e) => {
          setMaterialSectionTypeId(e.target.value);
          setResult(null);
        }}
      />
      <TextField
        label="Potrebna količina"
        type="number"
        value={requiredQuantity}
        onChange={(e) => {
          setRequiredQuantity(e.target.value);
          setResult(null);
        }}
      />
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginTop: '0.3rem' }}>
        <Button
          variant="accent"
          disabled={!canCheck || checkStock.isPending}
          onClick={() => checkStock.mutate(undefined)}
        >
          {checkStock.isPending ? 'Proveravam...' : 'Proveri'}
        </Button>
        {result !== null && (
          <StatusPill tone={result ? 'ok' : 'crit'}>{result ? 'Ima dovoljno na stanju' : 'Nema dovoljno na stanju'}</StatusPill>
        )}
      </div>
    </div>
  );
}
