export const BridgeEvents = {
  SESSION_EXPIRED: 'SESSION_EXPIRED',
  TRANSFER_COMPLETED: 'TRANSFER_COMPLETED',
  BALANCE_UPDATED: 'BALANCE_UPDATED',
} as const;

export type BridgeEventName = (typeof BridgeEvents)[keyof typeof BridgeEvents];

export interface BalancePayload {
  amount: number;
  currency: string;
}

export type MovementStatus = 'COMPLETED' | 'PENDING' | 'FAILED';

export interface MovementItem {
  id: string;
  type: 'DEBIT' | 'CREDIT';
  status: MovementStatus;
  amount: number;
  description: string;
  occurredAtMillis: number;
}

export interface MovementsPayload {
  items: MovementItem[];
  total: number;
  page: number;
}

export interface TransferPayload {
  toPhone?: string;
  amount?: number;
}

export interface TransferCompletedPayload {
  newBalance: number;
}
