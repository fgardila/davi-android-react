import {DeviceEventEmitter} from 'react-native';
import {addBridgeListener} from '../bridge/events';
import {BridgeEvents} from '../bridge/types';

describe('addBridgeListener', () => {
  it('invokes handler with emitted payload', () => {
    const handler = jest.fn();
    const unsubscribe = addBridgeListener(BridgeEvents.TRANSFER_COMPLETED, handler);

    DeviceEventEmitter.emit(BridgeEvents.TRANSFER_COMPLETED, {newBalance: 999});

    expect(handler).toHaveBeenCalledTimes(1);
    expect(handler).toHaveBeenCalledWith({newBalance: 999});
    unsubscribe();
  });

  it('returns an unsubscribe function that detaches the listener', () => {
    const handler = jest.fn();
    const unsubscribe = addBridgeListener(BridgeEvents.SESSION_EXPIRED, handler);

    unsubscribe();
    DeviceEventEmitter.emit(BridgeEvents.SESSION_EXPIRED, {});

    expect(handler).not.toHaveBeenCalled();
  });

  it('multiple listeners coexist independently', () => {
    const handlerA = jest.fn();
    const handlerB = jest.fn();
    const unA = addBridgeListener(BridgeEvents.BALANCE_UPDATED, handlerA);
    const unB = addBridgeListener(BridgeEvents.BALANCE_UPDATED, handlerB);

    DeviceEventEmitter.emit(BridgeEvents.BALANCE_UPDATED, {amount: 1});

    expect(handlerA).toHaveBeenCalledTimes(1);
    expect(handlerB).toHaveBeenCalledTimes(1);

    unA();
    DeviceEventEmitter.emit(BridgeEvents.BALANCE_UPDATED, {amount: 2});

    // A ya no recibe, pero B sigue suscrito.
    expect(handlerA).toHaveBeenCalledTimes(1);
    expect(handlerB).toHaveBeenCalledTimes(2);

    unB();
  });

  it('only fires for the subscribed event, not others', () => {
    const handler = jest.fn();
    const unsubscribe = addBridgeListener(BridgeEvents.TRANSFER_COMPLETED, handler);

    DeviceEventEmitter.emit(BridgeEvents.SESSION_EXPIRED, {});
    DeviceEventEmitter.emit(BridgeEvents.BALANCE_UPDATED, {});

    expect(handler).not.toHaveBeenCalled();
    unsubscribe();
  });
});
