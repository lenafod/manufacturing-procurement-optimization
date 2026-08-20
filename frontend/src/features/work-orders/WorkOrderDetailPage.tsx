import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { workOrdersApi } from '../../api/workOrders';
import { technicalSheetsApi, type CreateTechnicalSheetPayload } from '../../api/technicalSheets';
import { materialTypesApi } from '../../api/materialTypes';
import { materialSectionTypesApi } from '../../api/materialSectionTypes';
import { technicalProcessingsApi } from '../../api/technicalProcessings';
import { surfaceProtectionsApi } from '../../api/surfaceProtections';
import { machiningTypesApi } from '../../api/machiningTypes';
import { useApiMutation } from '../../hooks/useApiMutation';
import { Button } from '../../components/Button';
import { SlidePanel } from '../../components/SlidePanel';
import { TextField } from '../../components/TextField';
import { Select } from '../../components/Select';
import { ErrorBanner } from '../../components/ErrorBanner';
import { LoadingState } from '../../components/LoadingState';
import { ErrorState } from '../../components/ErrorState';
import { formatMaterialSection } from '../../utils/formatMaterialSection';

export function WorkOrderDetailPage() {
  const { id } = useParams<{ id: string }>();
  const workOrderId = id!;
  const [panelOpen, setPanelOpen] = useState(false);

  const workOrder = useQuery({
    queryKey: ['workOrders', workOrderId],
    queryFn: () => workOrdersApi.getById(workOrderId),
  });

  if (workOrder.isLoading) return <LoadingState />;
  if (workOrder.isError) return <ErrorState error={workOrder.error} />;
  if (!workOrder.data) return null;

  const positions = workOrder.data.technicalSheets;

  return (
    <div className="card">
      <div className="card-toolbar">
        <div>
          <h2 className="mono" style={{ marginBottom: '0.2rem' }}>
            {workOrder.data.id}
          </h2>
          <span style={{ color: 'var(--steel)', fontSize: '0.85rem' }}>
            {positions.length} {positions.length === 1 ? 'pozicija' : 'pozicija'} dodato
          </span>
        </div>
        <Button
          variant="accent"
          disabled={positions.length === 0}
          onClick={() => window.open(workOrdersApi.pdfUrl(workOrderId), '_blank')}
        >
          Generiši radni nalog (PDF)
        </Button>
      </div>

      {positions.length === 0 && <p className="empty-note">Još nema pozicija na ovom nalogu.</p>}

      {positions.map((sheet) => (
        <div
          key={sheet.id}
          className="position-card"
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: '0.75rem',
            border: '1px solid var(--line)',
            padding: '0.75rem 0.9rem',
            marginBottom: '0.6rem',
          }}
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.15rem' }}>
            <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>{sheet.positionName}</span>
            <span className="mono" style={{ fontSize: '0.78rem', color: 'var(--steel)' }}>
              {sheet.sheetId} · {sheet.sheetVersion} · {sheet.quantity} kom
            </span>
          </div>
          <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap', justifyContent: 'flex-end' }}>
            <span className="chip">
              {sheet.materialType.materialName} · {formatMaterialSection(sheet.materialSectionType)}
            </span>
            {sheet.prepLength != null && <span className="chip">pripremak {sheet.prepLength} mm</span>}
            {sheet.partMass != null && <span className="chip">masa {sheet.partMass} g</span>}
          </div>
        </div>
      ))}

      <div style={{ marginTop: '0.75rem' }}>
        <Button variant="ghost" onClick={() => setPanelOpen(true)}>
          + Dodaj poziciju
        </Button>
      </div>

      {panelOpen && <AddPositionPanel workOrderId={workOrderId} onClose={() => setPanelOpen(false)} />}
    </div>
  );
}

function AddPositionPanel({ workOrderId, onClose }: { workOrderId: string; onClose: () => void }) {
  const materialTypes = useQuery({ queryKey: ['materialTypes'], queryFn: materialTypesApi.getAll });
  const materialSectionTypes = useQuery({
    queryKey: ['materialSectionTypes'],
    queryFn: materialSectionTypesApi.getAll,
  });
  const technicalProcessings = useQuery({
    queryKey: ['technicalProcessings'],
    queryFn: technicalProcessingsApi.getAll,
  });
  const surfaceProtections = useQuery({
    queryKey: ['surfaceProtections'],
    queryFn: surfaceProtectionsApi.getAll,
  });
  const machiningTypes = useQuery({ queryKey: ['machiningTypes'], queryFn: machiningTypesApi.getAll });

  const [positionName, setPositionName] = useState('');
  const [sheetId, setSheetId] = useState('');
  const [sheetVersion, setSheetVersion] = useState('v1');
  const [quantity, setQuantity] = useState('');
  const [materialTypeId, setMaterialTypeId] = useState('');
  const [materialSectionTypeId, setMaterialSectionTypeId] = useState('');
  const [partLength, setPartLength] = useState('');
  const [technicalAllowance, setTechnicalAllowance] = useState('');
  const [technicalProcessingId, setTechnicalProcessingId] = useState('');
  const [surfaceProtectionId, setSurfaceProtectionId] = useState('');
  const [machiningTypeId, setMachiningTypeId] = useState('');

  const createSheet = useApiMutation(technicalSheetsApi.create, {
    invalidateKeys: [['workOrders', workOrderId], ['workOrders']],
    onSuccess: onClose,
  });

  const canSubmit =
    positionName &&
    sheetId &&
    sheetVersion &&
    quantity &&
    materialTypeId &&
    materialSectionTypeId &&
    partLength &&
    technicalAllowance &&
    technicalProcessingId &&
    surfaceProtectionId &&
    machiningTypeId;

  function submit() {
    // TechnicalSheet.id nema auto-generisanje na backend-u - sastavljamo ga od sheetId + verzije,
    // pošto su to polja koja korisnik već unosi kao ljudski čitljiv identifikator lista.
    const payload: CreateTechnicalSheetPayload = {
      id: `${sheetId}-${sheetVersion}`,
      quantity: Number(quantity),
      sheetId,
      sheetVersion,
      workOrder: { id: workOrderId },
      positionName,
      materialType: { id: Number(materialTypeId) },
      materialSectionType: { id: Number(materialSectionTypeId) },
      partLength: Number(partLength),
      technicalAllowance: Number(technicalAllowance),
      technicalProcessing: { id: Number(technicalProcessingId) },
      surfaceProtection: { id: Number(surfaceProtectionId) },
      machiningType: { id: Number(machiningTypeId) },
    };
    createSheet.mutate(payload);
  }

  return (
    <SlidePanel title="Nova pozicija" onClose={onClose}>
      <ErrorBanner error={createSheet.error} />

      <div className="panel-section-label">Osnovni podaci</div>
      <div className="panel-grid">
        <TextField label="Naziv pozicije" value={positionName} onChange={(e) => setPositionName(e.target.value)} fullWidth />
        <TextField label="Količina (kom)" type="number" value={quantity} onChange={(e) => setQuantity(e.target.value)} />
        <TextField label="Šifra lista" mono value={sheetId} onChange={(e) => setSheetId(e.target.value)} />
        <TextField label="Verzija" mono value={sheetVersion} onChange={(e) => setSheetVersion(e.target.value)} />
      </div>

      <div className="panel-section-label">Materijal</div>
      <div className="panel-grid">
        <Select
          label="Vrsta materijala"
          placeholder="Izaberi materijal"
          fullWidth
          options={(materialTypes.data ?? []).map((m) => ({ value: String(m.id), label: m.materialName }))}
          value={materialTypeId}
          onChange={(e) => setMaterialTypeId(e.target.value)}
        />
        <Select
          label="Presek"
          placeholder="Izaberi presek"
          fullWidth
          options={(materialSectionTypes.data ?? []).map((s) => ({
            value: String(s.id),
            label: formatMaterialSection(s),
          }))}
          value={materialSectionTypeId}
          onChange={(e) => setMaterialSectionTypeId(e.target.value)}
        />
        <TextField
          label="Dužina izratka (mm)"
          type="number"
          value={partLength}
          onChange={(e) => setPartLength(e.target.value)}
        />
        <TextField
          label="Tehnički dodatak (mm)"
          type="number"
          value={technicalAllowance}
          onChange={(e) => setTechnicalAllowance(e.target.value)}
        />
      </div>

      <div className="panel-section-label">Obrada</div>
      <div className="panel-grid">
        <Select
          label="Tehnička obrada"
          placeholder="Izaberi obradu"
          fullWidth
          options={(technicalProcessings.data ?? []).map((t) => ({ value: String(t.id), label: t.name }))}
          value={technicalProcessingId}
          onChange={(e) => setTechnicalProcessingId(e.target.value)}
        />
        <Select
          label="Površinska zaštita"
          placeholder="Izaberi zaštitu"
          fullWidth
          options={(surfaceProtections.data ?? []).map((s) => ({ value: String(s.id), label: s.name }))}
          value={surfaceProtectionId}
          onChange={(e) => setSurfaceProtectionId(e.target.value)}
        />
        <Select
          label="Mašinska obrada"
          placeholder="Izaberi obradu"
          fullWidth
          options={(machiningTypes.data ?? []).map((m) => ({ value: String(m.id), label: m.name }))}
          value={machiningTypeId}
          onChange={(e) => setMachiningTypeId(e.target.value)}
        />
      </div>

      <div className="modal-actions" style={{ marginTop: '1rem' }}>
        <Button variant="ghost" onClick={onClose}>
          Otkaži
        </Button>
        <Button variant="accent" disabled={!canSubmit || createSheet.isPending} onClick={submit}>
          {createSheet.isPending ? 'Čuvanje...' : 'Sačuvaj poziciju'}
        </Button>
      </div>
    </SlidePanel>
  );
}
