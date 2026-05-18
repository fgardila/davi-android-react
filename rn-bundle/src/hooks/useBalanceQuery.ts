import {useQuery, useQueryClient} from '@tanstack/react-query';
import {useEffect} from 'react';
import {nativeApi} from '../services/nativeApi';
import {addBridgeListener} from '../bridge/events';
import {BalancePayload, BridgeEvents, TransferCompletedPayload} from '../bridge/types';
import {MOVEMENTS_KEY} from './useMovementsQuery';

const BALANCE_KEY = ['balance'] as const;

export function useBalanceQuery() {
  const qc = useQueryClient();

  const balanceQuery = useQuery<BalancePayload>({
    queryKey: BALANCE_KEY,
    queryFn: () => nativeApi.getBalance(),
    staleTime: 5 * 60 * 1000, // 5 minutos
  });

  useEffect(() => {
    const offTransfer = addBridgeListener<TransferCompletedPayload>(
      BridgeEvents.TRANSFER_COMPLETED,
      payload => {
        qc.setQueryData<BalancePayload>(BALANCE_KEY, prev => ({
          amount: payload.newBalance,
          currency: prev?.currency ?? 'COP',
        }));
        qc.invalidateQueries({queryKey: MOVEMENTS_KEY});
      },
    );
    const offBalance = addBridgeListener<{amount: number}>(
      BridgeEvents.BALANCE_UPDATED,
      payload =>
        qc.setQueryData<BalancePayload>(BALANCE_KEY, prev => ({
          amount: payload.amount,
          currency: prev?.currency ?? 'COP',
        })),
    );
    return () => {
      offTransfer();
      offBalance();
    };
  }, [qc]);

  return balanceQuery
}
