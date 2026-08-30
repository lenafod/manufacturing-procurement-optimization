import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { inventoryApi } from '../../api/inventory';
import { materialTypesApi } from '../../api/materialTypes';
import { materialSectionTypesApi } from '../../api/materialSectionTypes';
import { supplierMaterialsApi } from '../../api/supplierMaterials';
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
import { groupSectionsByShape } from '../../utils/groupSections';
import { SendInquiryButton } from '../procurement-inquiries/ProcurementInquiriesPage';
import type { Inventory, SupplierMaterial } from '../../types';

export function InventoryPage() {
  const [modalOpen, setModalOpen] = useState(false);
  const [materialsOpen, setMaterialsOpen] = useState(false);

  const inventory = useQuery({ queryKey: ['inventory'], queryFn: inventoryApi.getAll });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
      <CheckStockCard />

      <div className="card">
        <div className="card-toolbar">
          <button type="button" className="disclosure-toggle" onClick={() => setMaterialsOpen((open) => !open)}>
            <span className={`disclosure-arrow ${materialsOpen ? 'open' : ''}`}>▸</span>
            <h2 style={{ margin: 0 }}>Magacin{inventory.data ? ` (${inventory.data.length})` : ''}</h2>
          </button>
          <Button variant="accent" onClick={() => setModalOpen(true)}>
            + Nova stavka
          </Button>
        </div>

        {materialsOpen && (
          <>
            {inventory.isLoading && <LoadingState />}
            {inventory.isError && <ErrorState error={inventory.error} />}
            {inventory.data && (
              <DataTable<Inventory>
                columns={[
                  { header: 'Materijal', render: (i) => i.materialType.materialName },
                  { header: 'Presek', render: (i) => formatMaterialSection(i.materialSectionType) },
                  { header: 'Količina (mm)', render: (i) => i.quantity, numeric: true },
                ]}
                rows={[...inventory.data].sort((a, b) => a.materialType.materialName.localeCompare(b.materialType.materialName))}
                rowKey={(i) => i.id}
                emptyMessage="Magacin je prazan. Dodaj prvu stavku."
              />
            )}
          </>
        )}
      </div>

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
        options={[...(materialTypes.data ?? [])]
          .sort((a, b) => a.materialName.localeCompare(b.materialName))
          .map((m) => ({ value: String(m.id), label: m.materialName }))}
        value={materialTypeId}
        onChange={(e) => setMaterialTypeId(e.target.value)}
      />
      <Select
        label="Presek"
        placeholder="Izaberi presek"
        groups={groupSectionsByShape(materialSectionTypes.data ?? [])}
        value={materialSectionTypeId}
        onChange={(e) => setMaterialSectionTypeId(e.target.value)}
      />
      <TextField
        label="Količina (mm)"
        hint="ukupna dužina materijala na lageru, u milimetrima"
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
  const supplierMaterials = useQuery({ queryKey: ['supplierMaterials'], queryFn: supplierMaterialsApi.getAll });

  const [materialTypeId, setMaterialTypeId] = useState('');
  const [materialSectionTypeId, setMaterialSectionTypeId] = useState('');
  const [requiredQuantity, setRequiredQuantity] = useState('');
  const [result, setResult] = useState<boolean | null>(null);
  const [suppliersModalOpen, setSuppliersModalOpen] = useState(false);

  const selectedMaterialType = materialTypes.data?.find((m) => String(m.id) === materialTypeId);
  const selectedMaterialSection = materialSectionTypes.data?.find((s) => String(s.id) === materialSectionTypeId);

  const checkStock = useApiMutation(
    () => inventoryApi.check(selectedMaterialType!.materialName, Number(materialSectionTypeId), Number(requiredQuantity)),
    {
      onSuccess: (data) => {
        setResult(data);
        if (!data) setSuppliersModalOpen(true);
      },
    },
  );

  const canCheck = materialTypeId && materialSectionTypeId && requiredQuantity;

  const candidateOffers = (supplierMaterials.data ?? []).filter(
    (o) => String(o.materialType.id) === materialTypeId && String(o.materialSectionType.id) === materialSectionTypeId,
  );

  return (
    <div className="card">
      <div className="card-toolbar">
        <h2>Provera stanja</h2>
      </div>
      <ErrorBanner error={checkStock.error} />
      <Select
        label="Vrsta materijala"
        placeholder="Izaberi materijal"
        options={[...(materialTypes.data ?? [])]
          .sort((a, b) => a.materialName.localeCompare(b.materialName))
          .map((m) => ({ value: String(m.id), label: m.materialName }))}
        value={materialTypeId}
        onChange={(e) => {
          setMaterialTypeId(e.target.value);
          setResult(null);
        }}
      />
      <Select
        label="Presek"
        placeholder="Izaberi presek"
        groups={groupSectionsByShape(materialSectionTypes.data ?? [])}
        value={materialSectionTypeId}
        onChange={(e) => {
          setMaterialSectionTypeId(e.target.value);
          setResult(null);
        }}
      />
      <TextField
        label="Potrebna količina (mm)"
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

      {suppliersModalOpen && (
        <SuppliersForMaterialModal
          materialName={selectedMaterialType?.materialName ?? ''}
          sectionLabel={selectedMaterialSection ? formatMaterialSection(selectedMaterialSection) : ''}
          requiredQuantity={Number(requiredQuantity)}
          offers={candidateOffers}
          onClose={() => setSuppliersModalOpen(false)}
        />
      )}
    </div>
  );
}

function SuppliersForMaterialModal({
  materialName,
  sectionLabel,
  requiredQuantity,
  offers,
  onClose,
}: {
  materialName: string;
  sectionLabel: string;
  requiredQuantity: number;
  offers: SupplierMaterial[];
  onClose: () => void;
}) {
  return (
    <Modal title="Nema dovoljno materijala na lageru" onClose={onClose} wide>
      <p style={{ margin: '0 0 0.9rem', color: 'var(--steel)', fontSize: '0.86rem' }}>
        Za <strong style={{ color: 'var(--ink)' }}>{materialName}</strong> ({sectionLabel}) treba{' '}
        <strong style={{ color: 'var(--ink)' }}>{requiredQuantity} mm</strong>, a magacin nema dovoljno. Ovi dobavljači
        prodaju taj materijal i presek. Pošalji upit direktno odavde.
      </p>
      <DataTable<SupplierMaterial>
        columns={[
          { header: 'Dobavljač', render: (o) => o.supplier.name },
          { header: 'Cena/jed.', render: (o) => o.pricePerUnit, numeric: true },
          { header: 'Rok isporuke', render: (o) => `${o.deliveryTime} d`, numeric: true },
          { header: 'Poznata količina', render: (o) => o.availableQuantity, numeric: true },
          {
            header: 'Akcija',
            render: (o) => (
              <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                <SendInquiryButton offerId={o.id} />
              </div>
            ),
          },
        ]}
        rows={offers}
        rowKey={(o) => o.id}
        emptyMessage="Nijedan dobavljač ne nosi ovaj materijal i presek."
      />
      <div className="modal-actions">
        <Button variant="ghost" onClick={onClose}>
          Zatvori
        </Button>
      </div>
    </Modal>
  );
}
