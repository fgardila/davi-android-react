import {DeviceEventEmitter} from 'react-native';
import {BridgeEventName} from './types';

export function addBridgeListener<T>(
  event: BridgeEventName,
  handler: (payload: T) => void,
) {
  const sub = DeviceEventEmitter.addListener(event, handler);
  return () => sub.remove();
}
