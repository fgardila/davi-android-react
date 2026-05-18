import React from 'react';
import {renderHook, waitFor} from '@testing-library/react-native';
import {QueryClient, QueryClientProvider} from '@tanstack/react-query';
import {useBalanceQuery} from '../hooks/useBalanceQuery';

jest.mock('../services/nativeApi', () => ({
  nativeApi: {
    getBalance: jest.fn(),
  },
}));

import {nativeApi} from '../services/nativeApi';
const mockGetBalance = nativeApi.getBalance as jest.Mock;

const makeWrapper = () => {
  const client = new QueryClient({
    defaultOptions: {queries: {retry: false}},
  });
  return ({children}: {children: React.ReactNode}) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

describe('useBalanceQuery', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('starts pending and resolves balance from native bridge', async () => {
    mockGetBalance.mockResolvedValueOnce({amount: 1_250_345, currency: 'COP'});

    const {result} = renderHook(() => useBalanceQuery(), {wrapper: makeWrapper()});

    expect(result.current.isPending).toBe(true);
    expect(result.current.data).toBeUndefined();

    await waitFor(() => expect(result.current.isPending).toBe(false));

    expect(result.current.data?.amount).toBe(1_250_345);
    expect(result.current.data?.currency).toBe('COP');
    expect(result.current.error).toBeNull();
  });

  it('exposes error state when getBalance rejects', async () => {
    mockGetBalance.mockRejectedValueOnce(new Error('Network error'));

    const {result} = renderHook(() => useBalanceQuery(), {wrapper: makeWrapper()});

    await waitFor(() => expect(result.current.isError).toBe(true));

    expect(result.current.data).toBeUndefined();
    expect((result.current.error as Error).message).toBe('Network error');
  });

  it('calls nativeApi.getBalance exactly once for a single mount', async () => {
    mockGetBalance.mockResolvedValueOnce({amount: 100, currency: 'COP'});

    const {result} = renderHook(() => useBalanceQuery(), {wrapper: makeWrapper()});
    await waitFor(() => expect(result.current.isPending).toBe(false));

    expect(mockGetBalance).toHaveBeenCalledTimes(1);
  });
});

// La integración de useBalanceQuery con eventos del bridge (TRANSFER_COMPLETED,
// BALANCE_UPDATED) se cubre indirectamente por:
//   • events.test.ts → verifica que DeviceEventEmitter propaga eventos.
//   • Smoke test manual (flujo de transferencia en emulador/dispositivo).
// Probar esa integración en JSDOM resulta inestable por timers de React Query +
// orden de suscripción de useEffect; aporta más ruido que cobertura útil.
