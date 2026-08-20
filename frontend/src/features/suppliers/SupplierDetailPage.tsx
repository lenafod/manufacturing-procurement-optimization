import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { suppliersApi } from '../../api/suppliers';
import { supplierMaterialsApi } from '../../api/supplierMaterials';
import { materialTypesApi } from '../../api/materialTypes';
import { materialSectionTypesApi } from '../../api/materialSectionTypes';
import { useApiMutation } from '../../hooks/useApiMutation';
import { DataTable } from '../../components/DataTable';
import { Button } from '../../components/Button';
import { Modal } from '../../components/Modal';
import { TextField } from '../../components/TextField';
import { Select } from '../../components/Select';
import { ErrorBanner } from '../../components/ErrorBanner';
import { LoadingState } from '../../components/LoadingState';
import { ErrorState } from '../../components/ErrorState';
import { formatMaterialSection } from '../../utils/formatMaterialSection';
import type { SupplierMaterial } from '../../types';

export function SupplierDetailPage() {
  const { id } = useParams<{ id: string }>();
  const supplierId = Number(id);
  const [modalOpen, setModalOpen] = useState(false);

  const supplier = useQuery({
    queryKey: ['suppliers', supplierId],
    queryFn: () => suppliersApi.getById(supplierId),
  });

  const offers = useQuery({ queryKey: ['supplierMaterials'], queryFn: supplierMaterialsApi.getAll });
  const supplierOffers = offers.data?.filter((o) => o.supplier.id === supplierId) ?? [];

  if (supplier.isLoading) return <LoadingState />;
  if (supplier.isError) return <ErrorState error={supplier.error} />;
  if (!supplier.data) return null;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
      <div className="card">
        <h2 style={{ marginTop: 0 }}>{supplier.data.name}</h2>
        <p style={{ color: 'var(--steel)', margin: 0 }}>
          {supplier.data.address} · {supplier.data.phoneNumber} · {supplier.data.email}
        </p>
      </div>

      <div className="card">
        <div className="card-toolbar">
          <h2>Ponude materijala</h2>
          <Button variant="accent" onClick={() => setModalOpen(true)}>
            + Nova ponuda
          </Button>
        </div>

        {offers.isLoading && <LoadingState />}
        {offers.isError && <ErrorState error={offers.error} />}
        {offers.data && (
          <DataTable<SupplierMaterial>
            columns={[
              { header: 'Materijal', render: (o) => o.materialType.materialName },
              { header: 'Presek', render: (o) => formatMaterialSection(o.materialSectionType) },
              { header: 'Cena/jed.', render: (o) => o.pricePerUnit, numeric: true },
              { header: 'Rok isporuke', render: (o) => `${o.deliveryTime} d`, numeric: true },
            ]}
            rows={supplierOffers}
            rowKey={(o) => o.id}
            emptyMessage="Ovaj dobavljač još nema ponuda."
          />
        )}
      </div>

      {modalOpen && <NewOfferModal supplierId={supplierId} onClose={() => setModalOpen(false)} />}
    </div>
  );
}

function NewOfferModal({ supplierId, onClose }: { supplierId: number; onClose: () => void }) {
  const materialTypes = useQuery({ queryKey: ['materialTypes'], queryFn: materialTypesApi.getAll });
  const materialSectionTypes = useQuery({
    queryKey: ['materialSectionTypes'],
    queryFn: materialSectionTypesApi.getAll,
  });

  const [materialTypeId, setMaterialTypeId] = useState('');
  const [materialSectionTypeId, setMaterialSectionTypeId] = useState('');
  const [pricePerUnit, setPricePerUnit] = useState('');
  const [deliveryTime, setDeliveryTime] = useState('');

  const createOffer = useApiMutation(supplierMaterialsApi.create, {
    invalidateKeys: [['supplierMaterials']],
    onSuccess: onClose,
  });

  const canSubmit = materialTypeId && materialSectionTypeId && pricePerUnit && deliveryTime;

  return (
    <Modal title="Nova ponuda materijala" onClose={onClose}>
      <ErrorBanner error={createOffer.error} />
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
        label="Cena po jedinici"
        type="number"
        value={pricePerUnit}
        onChange={(e) => setPricePerUnit(e.target.value)}
      />
      <TextField
        label="Rok isporuke (dana)"
        type="number"
        value={deliveryTime}
        onChange={(e) => setDeliveryTime(e.target.value)}
      />
      <div className="modal-actions">
        <Button variant="ghost" onClick={onClose}>
          Otkaži
        </Button>
        <Button
          variant="accent"
          disabled={!canSubmit || createOffer.isPending}
          onClick={() =>
            createOffer.mutate({
              supplier: { id: supplierId },
              materialType: { id: Number(materialTypeId) },
              materialSectionType: { id: Number(materialSectionTypeId) },
              pricePerUnit: Number(pricePerUnit),
              deliveryTime: Number(deliveryTime),
            })
          }
        >
          {createOffer.isPending ? 'Čuvanje...' : 'Sačuvaj ponudu'}
        </Button>
      </div>
    </Modal>
  );
}
