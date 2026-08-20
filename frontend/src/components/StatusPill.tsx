import type { ReactNode } from 'react';
import type { PurchaseRequestStatus } from '../types';

type Tone = 'neutral' | 'ok' | 'warn' | 'crit';

export function StatusPill({ tone, children }: { tone: Tone; children: ReactNode }) {
  return <span className={`pill pill-${tone}`}>{children}</span>;
}

const PURCHASE_REQUEST_STATUS_LABEL: Record<PurchaseRequestStatus, string> = {
  CREATED: 'Kreiran',
  SENT: 'Poslat',
  IN_DELIVERY: 'U isporuci',
  DELIVERED: 'Isporučen',
  CANCELED: 'Otkazan',
};

const PURCHASE_REQUEST_STATUS_TONE: Record<PurchaseRequestStatus, Tone> = {
  CREATED: 'neutral',
  SENT: 'warn',
  IN_DELIVERY: 'warn',
  DELIVERED: 'ok',
  CANCELED: 'crit',
};

export function PurchaseRequestStatusPill({ status }: { status: PurchaseRequestStatus }) {
  return <StatusPill tone={PURCHASE_REQUEST_STATUS_TONE[status]}>{PURCHASE_REQUEST_STATUS_LABEL[status]}</StatusPill>;
}
