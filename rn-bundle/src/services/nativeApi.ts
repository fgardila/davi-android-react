import {NativeModules} from 'react-native';
import {BalancePayload, MovementsPayload, TransferPayload} from '../bridge/types';

interface DaviPlataBridgeInterface {
  getBalance(): Promise<BalancePayload>;
  getMovements(page: number, size: number): Promise<MovementsPayload>;
  openTransfer(payload: TransferPayload): void;
  logout(): Promise<void>;
  forceSessionExpired(): Promise<void>;
}

const bridge = NativeModules.DaviPlataBridge as DaviPlataBridgeInterface;

export const nativeApi = {
  getBalance: (): Promise<BalancePayload> => bridge.getBalance(),
  getMovements: (page: number, size: number = 20): Promise<MovementsPayload> =>
    bridge.getMovements(page, size),
  openTransfer: (payload: TransferPayload = {}): void => bridge.openTransfer(payload),
  logout: (): Promise<void> => bridge.logout(),
  forceSessionExpired: (): Promise<void> => bridge.forceSessionExpired(),
};
