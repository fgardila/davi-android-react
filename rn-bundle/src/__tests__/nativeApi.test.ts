// El wrapper nativeApi.ts captura NativeModules.DaviPlataBridge en tiempo de import.
// Estrategia: importar el NativeModules real, inyectar el stub, y luego require()
// nativeApi para forzar que evalúe con el bridge ya inyectado. Sin jest.mock
// para evitar romper el preset 'react-native' (que mockea muchas APIs internas).

import {NativeModules} from 'react-native';

const mockBridge = {
  getBalance: jest.fn(),
  getMovements: jest.fn(),
  openTransfer: jest.fn(),
  logout: jest.fn(),
  forceSessionExpired: jest.fn(),
};
(NativeModules as Record<string, unknown>).DaviPlataBridge = mockBridge;

// require POST-injection — nativeApi captura el bridge en el primer evaluation.
// eslint-disable-next-line @typescript-eslint/no-var-requires
const {nativeApi} = require('../services/nativeApi');

describe('nativeApi wrapper', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('getBalance delegates to bridge', () => {
    mockBridge.getBalance.mockResolvedValueOnce({amount: 100, currency: 'COP'});
    nativeApi.getBalance();
    expect(mockBridge.getBalance).toHaveBeenCalledTimes(1);
  });

  it('getMovements forwards page and size', () => {
    nativeApi.getMovements(2, 30);
    expect(mockBridge.getMovements).toHaveBeenCalledWith(2, 30);
  });

  it('getMovements uses default size when omitted', () => {
    nativeApi.getMovements(0);
    expect(mockBridge.getMovements).toHaveBeenCalledWith(0, 20);
  });

  it('openTransfer forwards payload', () => {
    const payload = {toPhone: '3009876543', amount: 50_000};
    nativeApi.openTransfer(payload);
    expect(mockBridge.openTransfer).toHaveBeenCalledWith(payload);
  });

  it('openTransfer uses empty payload by default', () => {
    nativeApi.openTransfer();
    expect(mockBridge.openTransfer).toHaveBeenCalledWith({});
  });

  it('logout delegates to bridge', () => {
    nativeApi.logout();
    expect(mockBridge.logout).toHaveBeenCalledTimes(1);
  });

  it('forceSessionExpired delegates to bridge', () => {
    nativeApi.forceSessionExpired();
    expect(mockBridge.forceSessionExpired).toHaveBeenCalledTimes(1);
  });
});
