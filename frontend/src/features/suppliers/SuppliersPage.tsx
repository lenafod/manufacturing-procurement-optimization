import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { suppliersApi } from '../../api/suppliers';
import { useApiMutation } from '../../hooks/useApiMutation';
import { DataTable } from '../../components/DataTable';
import { Button } from '../../components/Button';
import { Modal } from '../../components/Modal';
import { TextField } from '../../components/TextField';
import { ErrorBanner } from '../../components/ErrorBanner';
import { LoadingState } from '../../components/LoadingState';
import { ErrorState } from '../../components/ErrorState';
import type { Supplier } from '../../types';

export function SuppliersPage() {
  const navigate = useNavigate();
  const [modalOpen, setModalOpen] = useState(false);

  const suppliers = useQuery({ queryKey: ['suppliers'], queryFn: suppliersApi.getAll });

  return (
    <div className="card">
      <div className="card-toolbar">
        <h2>Dobavljači</h2>
        <Button variant="accent" onClick={() => setModalOpen(true)}>
          + Novi dobavljač
        </Button>
      </div>

      {suppliers.isLoading && <LoadingState />}
      {suppliers.isError && <ErrorState error={suppliers.error} />}
      {suppliers.data && (
        <DataTable<Supplier>
          columns={[
            { header: 'Naziv', render: (s) => s.name },
            { header: 'Adresa', render: (s) => s.address },
            { header: 'Telefon', render: (s) => s.phoneNumber },
            { header: 'Email', render: (s) => s.email },
          ]}
          rows={suppliers.data}
          rowKey={(s) => s.id}
          emptyMessage="Još nema dobavljača — dodaj prvog."
          onRowClick={(s) => navigate(`/suppliers/${s.id}`)}
        />
      )}

      {modalOpen && <NewSupplierModal onClose={() => setModalOpen(false)} />}
    </div>
  );
}

function NewSupplierModal({ onClose }: { onClose: () => void }) {
  const [name, setName] = useState('');
  const [address, setAddress] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [email, setEmail] = useState('');

  const createSupplier = useApiMutation(suppliersApi.create, {
    invalidateKeys: [['suppliers']],
    onSuccess: onClose,
  });

  return (
    <Modal title="Novi dobavljač" onClose={onClose}>
      <ErrorBanner error={createSupplier.error} />
      <TextField label="Naziv" value={name} onChange={(e) => setName(e.target.value)} autoFocus />
      <TextField label="Adresa" value={address} onChange={(e) => setAddress(e.target.value)} />
      <TextField label="Telefon" value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} />
      <TextField label="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
      <div className="modal-actions">
        <Button variant="ghost" onClick={onClose}>
          Otkaži
        </Button>
        <Button
          variant="accent"
          disabled={!name || createSupplier.isPending}
          onClick={() => createSupplier.mutate({ name, address, phoneNumber, email })}
        >
          {createSupplier.isPending ? 'Čuvanje...' : 'Kreiraj'}
        </Button>
      </div>
    </Modal>
  );
}
