import React from 'react';
import {renderHook, waitFor} from '@testing-library/react-native';
import {QueryClient, QueryClientProvider} from '@tanstack/react-query';
import {useMovementsQuery} from '../hooks/useMovementsQuery';
import {MovementItem} from '../bridge/types';

jest.mock('../services/nativeApi', () => ({
  nativeApi: {
    getMovements: jest.fn(),
  },
}));

import {nativeApi} from '../services/nativeApi';
const mockGetMovements = nativeApi.getMovements as jest.Mock;

const makeWrapper = () => {
  const client = new QueryClient({defaultOptions: {queries: {retry: false}}});
  return ({children}: {children: React.ReactNode}) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

const sampleItem: MovementItem = {
  id: 'mov-1',
  type: 'CREDIT',
  status: 'COMPLETED',
  amount: 100_000,
  description: 'x',
  occurredAtMillis: Date.now(),
};

describe('useMovementsQuery', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('starts pending and resolves with items array', async () => {
    mockGetMovements.mockResolvedValueOnce({items: [sampleItem], total: 1, page: 0});

    const {result} = renderHook(() => useMovementsQuery(20), {wrapper: makeWrapper()});

    expect(result.current.isPending).toBe(true);

    await waitFor(() => expect(result.current.isPending).toBe(false));

    expect(result.current.data).toEqual([sampleItem]);
    expect(result.current.error).toBeNull();
  });

  it('calls getMovements with page 0 and provided size', async () => {
    mockGetMovements.mockResolvedValueOnce({items: [], total: 0, page: 0});

    renderHook(() => useMovementsQuery(50), {wrapper: makeWrapper()});

    await waitFor(() => expect(mockGetMovements).toHaveBeenCalled());
    expect(mockGetMovements).toHaveBeenCalledWith(0, 50);
  });

  it('exposes error state when getMovements rejects', async () => {
    mockGetMovements.mockRejectedValueOnce(new Error('SESSION_EXPIRED'));

    const {result} = renderHook(() => useMovementsQuery(), {wrapper: makeWrapper()});

    await waitFor(() => expect(result.current.isError).toBe(true));
    expect((result.current.error as Error).message).toBe('SESSION_EXPIRED');
  });

  it('returns empty data when items list is empty', async () => {
    mockGetMovements.mockResolvedValueOnce({items: [], total: 0, page: 0});

    const {result} = renderHook(() => useMovementsQuery(), {wrapper: makeWrapper()});

    await waitFor(() => expect(result.current.isPending).toBe(false));
    expect(result.current.data).toEqual([]);
  });
});
