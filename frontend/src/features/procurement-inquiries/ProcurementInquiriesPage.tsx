import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { workOrdersApi } from '../../api/workOrders';
import { procurementInquiriesApi } from '../../api/procurementInquiries';
import { useApiMutation } from '../../hooks/useApiMutation';
import { DataTable } from '../../components/DataTable';
import { Select } from '../../components/Select';
import { Button } from '../../components/Button';
import { Modal } from '../../components/Modal';
import { TextField } from '../../components/TextField';
import { ErrorBanner } from '../../components/ErrorBanner';
import { ProcurementInquiryStatusPill } from '../../components/StatusPill';
import { LoadingState } from '../../components/LoadingState';
import { ErrorState } from '../../components/ErrorState';
import { formatMaterialSection } from '../../utils/formatMaterialSection';
import type { ProcurementInquiry, SupplierMaterial } from '../../types';

type Tab = 'new' | 'history';

export function ProcurementInquiriesPage() {
  const [tab, setTab] = useState<Tab>('new');
  const [respondingTo, setRespondingTo] = useState<ProcurementInquiry | null>(null);

  const inquiries = useQuery({ queryKey: ['procurementInquiries'], queryFn: procurementInquiriesApi.getAll });

  return (
    <div className="card">
      <div className="card-toolbar">
        <h2>Upiti dobavljačima</h2>
      </div>

      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.1rem' }}>
        <Button variant={tab === 'new' ? 'accent' : 'ghost'} onClick={() => setTab('new')}>
          Pošalji upit
        </Button>
        <Button variant={tab === 'history' ? 'accent' : 'ghost'} onClick={() => setTab('history')}>
          Istorija {inquiries.data ? `(${inquiries.data.length})` : ''}
        </Button>
      </div>

      {tab === 'new' && <NewInquiryPanel inquiries={inquiries.data ?? []} />}

      {tab === 'history' && (
        <>
          {inquiries.isLoading && <LoadingState />}
          {inquiries.isError && <ErrorState error={inquiries.error} />}
          {inquiries.data && (
            <DataTable<ProcurementInquiry>
              columns={[
                { header: 'Pozicija', render: (i) => i.technicalSheet?.positionName ?? '— (opšti upit)' },
                { header: 'Dobavljač', render: (i) => i.supplierMaterial.supplier.name },
                { header: 'Materijal', render: (i) => i.supplierMaterial.materialType.materialName },
                { header: 'Tražena količina', render: (i) => i.requestedQuantity ?? '—', numeric: true },
                { header: 'Potvrđena količina', render: (i) => i.confirmedQuantity ?? '—', numeric: true },
                { header: 'Potvrđena cena', render: (i) => i.confirmedPrice ?? '—', numeric: true },
                { header: 'Potvrđen rok', render: (i) => (i.confirmedDeliveryTime != null ? `${i.confirmedDeliveryTime} d` : '—'), numeric: true },
                { header: 'Poslato', render: (i) => i.sentAt },
                { header: 'Status', render: (i) => <ProcurementInquiryStatusPill status={i.status} /> },
                {
                  header: 'Akcija',
                  render: (i) =>
                    i.status === 'POSLAT' ? (
                      <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                        <Button variant="accent" onClick={() => setRespondingTo(i)}>
                          Unesi odgovor
                        </Button>
                      </div>
                    ) : null,
                },
              ]}
              rows={inquiries.data}
              rowKey={(i) => i.id}
              emptyMessage="Nema poslatih upita."
            />
          )}
        </>
      )}

      {respondingTo && <RespondModal inquiry={respondingTo} onClose={() => setRespondingTo(null)} />}
    </div>
  );
}

function NewInquiryPanel({ inquiries }: { inquiries: ProcurementInquiry[] }) {
  const workOrders = useQuery({ queryKey: ['workOrders'], queryFn: workOrdersApi.getAll });
  const [workOrderId, setWorkOrderId] = useState('');
  const [technicalSheetId, setTechnicalSheetId] = useState('');

  const candidates = useQuery({
    queryKey: ['procurementInquiryCandidates', technicalSheetId],
    queryFn: () => procurementInquiriesApi.getCandidates(technicalSheetId),
    enabled: !!technicalSheetId,
  });

  const selectedWorkOrder = workOrders.data?.find((w) => w.id === workOrderId);

  const alreadySent = (offerId: number) =>
    inquiries.some((i) => i.technicalSheet?.id === technicalSheetId && i.supplierMaterial.id === offerId);

  return (
    <>
      <div className="panel-section-label" style={{ marginTop: 0 }}>
        1. Radni nalog i pozicija
      </div>
      {workOrders.isLoading && <LoadingState />}
      {workOrders.isError && <ErrorState error={workOrders.error} />}
      {workOrders.data && (
        <Select
          label="Radni nalog"
          placeholder="Izaberi radni nalog"
          options={workOrders.data.map((w) => ({ value: w.id, label: w.id }))}
          value={workOrderId}
          onChange={(e) => {
            setWorkOrderId(e.target.value);
            setTechnicalSheetId('');
          }}
        />
      )}
      {selectedWorkOrder && (
        <Select
          label="Pozicija"
          placeholder="Izaberi poziciju"
          options={selectedWorkOrder.technicalSheets.map((ts) => ({ value: ts.id, label: ts.positionName }))}
          value={technicalSheetId}
          onChange={(e) => setTechnicalSheetId(e.target.value)}
        />
      )}

      {technicalSheetId && (
        <>
          <div className="panel-section-label">2. Dobavljači koji nose ovaj materijal</div>
          {candidates.isLoading && <LoadingState />}
          {candidates.isError && <ErrorState error={candidates.error} />}
          {candidates.data && (
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
                      <SendInquiryButton
                        technicalSheetId={technicalSheetId}
                        offerId={o.id}
                        alreadySent={alreadySent(o.id)}
                      />
                    </div>
                  ),
                },
              ]}
              rows={candidates.data}
              rowKey={(o) => o.id}
              emptyMessage="Nijedan dobavljač ne nosi ovaj materijal/presek."
            />
          )}
        </>
      )}
    </>
  );
}

export function SendInquiryButton({
  technicalSheetId,
  offerId,
  alreadySent = false,
}: {
  technicalSheetId?: string;
  offerId: number;
  alreadySent?: boolean;
}) {
  const [previewOpen, setPreviewOpen] = useState(false);

  if (alreadySent) {
    return <span className="chip">Poslato</span>;
  }

  return (
    <>
      <Button variant="accent" onClick={() => setPreviewOpen(true)}>
        Pošalji upit
      </Button>
      {previewOpen && (
        <InquiryPreviewModal technicalSheetId={technicalSheetId} offerId={offerId} onClose={() => setPreviewOpen(false)} />
      )}
    </>
  );
}

function InquiryPreviewModal({
  technicalSheetId,
  offerId,
  onClose,
}: {
  technicalSheetId?: string;
  offerId: number;
  onClose: () => void;
}) {
  const preview = useQuery({
    queryKey: ['procurementInquiryPreview', technicalSheetId, offerId],
    queryFn: () => procurementInquiriesApi.preview(offerId, technicalSheetId),
  });

  const [subject, setSubject] = useState('');
  const [text, setText] = useState('');

  // popuni izmenjiva polja tek kad stigne generisan predlog - korisnik ih dalje slobodno menja
  useEffect(() => {
    if (preview.data) {
      setSubject(preview.data.subject);
      setText(preview.data.text);
    }
  }, [preview.data]);

  const sendInquiry = useApiMutation(
    () => procurementInquiriesApi.send({ technicalSheetId, supplierMaterialIds: [offerId], subject, text }),
    { invalidateKeys: [['procurementInquiries']] },
  );

  return (
    <Modal title="Pregled upita pre slanja" onClose={onClose} wide>
      {preview.isLoading && <LoadingState />}
      {preview.isError && <ErrorState error={preview.error} />}

      {sendInquiry.isSuccess ? (
        <div className="success-banner">
          <span className="icon">✓</span>
          <span>Mejl je uspešno poslat{preview.data ? ` na ${preview.data.to}` : ''}.</span>
        </div>
      ) : (
        preview.data && (
          <>
            <div className="field">
              <label>Prima</label>
              <div className="field-input">{preview.data.to}</div>
            </div>
            <TextField label="Naslov" value={subject} onChange={(e) => setSubject(e.target.value)} />
            <div className="field">
              <label htmlFor="inquiry-text">Sadržaj</label>
              <textarea
                id="inquiry-text"
                className="field-input"
                rows={12}
                value={text}
                onChange={(e) => setText(e.target.value)}
              />
            </div>
          </>
        )
      )}

      {sendInquiry.isError && (
        <div>
          <strong style={{ display: 'block', color: 'var(--crit)', fontSize: '0.85rem', marginBottom: '0.3rem' }}>
            Mejl nije poslat.
          </strong>
          <ErrorBanner error={sendInquiry.error} />
        </div>
      )}

      <div className="modal-actions">
        {sendInquiry.isSuccess ? (
          <Button variant="accent" onClick={onClose}>
            Zatvori
          </Button>
        ) : (
          <>
            <Button variant="ghost" onClick={onClose}>
              Otkaži
            </Button>
            <Button
              variant="accent"
              disabled={!preview.data || sendInquiry.isPending}
              onClick={() => sendInquiry.mutate(undefined)}
            >
              {sendInquiry.isPending ? 'Šaljem...' : 'Pošalji'}
            </Button>
          </>
        )}
      </div>
    </Modal>
  );
}

function RespondModal({ inquiry, onClose }: { inquiry: ProcurementInquiry; onClose: () => void }) {
  // cena i rok su isto tako nepouzdani kao i kolicina dok dobavljac ne odgovori - unosi se sve
  // troje odjednom, unapred popunjeno poznatim (starim) vrednostima kao polazna tacka
  const [confirmedQuantity, setConfirmedQuantity] = useState('');
  const [confirmedPrice, setConfirmedPrice] = useState(String(inquiry.supplierMaterial.pricePerUnit));
  const [confirmedDeliveryTime, setConfirmedDeliveryTime] = useState(String(inquiry.supplierMaterial.deliveryTime));

  const recordResponse = useApiMutation(
    () =>
      procurementInquiriesApi.recordResponse(
        inquiry.id,
        Number(confirmedQuantity),
        Number(confirmedPrice),
        Number(confirmedDeliveryTime),
      ),
    { invalidateKeys: [['procurementInquiries'], ['supplierMaterials']], onSuccess: onClose },
  );

  const canSubmit = confirmedQuantity !== '' && confirmedPrice !== '' && confirmedDeliveryTime !== '';

  return (
    <Modal title={`Odgovor — ${inquiry.supplierMaterial.supplier.name}`} onClose={onClose}>
      <ErrorBanner error={recordResponse.error} />
      <p style={{ margin: '0 0 0.9rem', color: 'var(--steel)', fontSize: '0.86rem' }}>
        {inquiry.technicalSheet ? `${inquiry.technicalSheet.positionName} — ` : ''}
        {inquiry.supplierMaterial.materialType.materialName}, {formatMaterialSection(inquiry.supplierMaterial.materialSectionType)}
        {inquiry.requestedQuantity != null ? `. Traženo: ${inquiry.requestedQuantity}.` : '.'}
      </p>
      <TextField
        label="Stvarna raspoloživa količina (mm)"
        type="number"
        value={confirmedQuantity}
        onChange={(e) => setConfirmedQuantity(e.target.value)}
      />
      <TextField
        label="Stvarna cena po jedinici"
        type="number"
        value={confirmedPrice}
        onChange={(e) => setConfirmedPrice(e.target.value)}
      />
      <TextField
        label="Stvaran rok isporuke (dana)"
        type="number"
        value={confirmedDeliveryTime}
        onChange={(e) => setConfirmedDeliveryTime(e.target.value)}
      />
      <div className="modal-actions">
        <Button variant="ghost" onClick={onClose}>
          Otkaži
        </Button>
        <Button variant="accent" disabled={!canSubmit || recordResponse.isPending} onClick={() => recordResponse.mutate(undefined)}>
          {recordResponse.isPending ? 'Čuvanje...' : 'Sačuvaj odgovor'}
        </Button>
      </div>
    </Modal>
  );
}
