import React, {useEffect, useState} from 'react';
import HomeScreen from './src/screens/HomeScreen';
import MovementsScreen from './src/screens/MovementsScreen';
import {BackHandler} from 'react-native';
import {QueryClientProvider} from '@tanstack/react-query';
import {queryClient} from './src/lib/queryClient';
import {addBridgeListener} from './src/bridge/events';
import {BridgeEvents} from './src/bridge/types';

export type Screen = 'HOME' | 'MOVEMENTS';

interface AppProps {
  screen?: Screen;
  name?: string;
}

export default function App({screen: initialScreen = 'HOME', name = ''}: AppProps) {
  const [currentScreen, setCurrentScreen] = useState<Screen>(initialScreen);

  useEffect(() => {
    return addBridgeListener(BridgeEvents.SESSION_EXPIRED, () => {
      queryClient.clear();
      console.log('Sesión expirada, limpiando cache y volviendo a Home');
    });
  }, []);

  useEffect(() => {
    if (currentScreen !== 'MOVEMENTS') return;

    console.log('[BackHandler] registrando listener');
    const subscription = BackHandler.addEventListener('hardwareBackPress', () => {
      console.log('[BackHandler] back presionado en MOVEMENTS');
      setCurrentScreen('HOME');
      return true;
    });

    return () => {
      console.log('[BackHandler] removiendo listener');
      subscription.remove();
    };
  }, [currentScreen]);
  
  return (
    <QueryClientProvider client={queryClient}>
      {currentScreen === 'MOVEMENTS' ? (
        <MovementsScreen onBack={() => setCurrentScreen('HOME')} />
      ) : (
        <HomeScreen
          name={name}
          onNavigateToMovements={() => setCurrentScreen('MOVEMENTS')}
        />
      )}
    </QueryClientProvider>
  );
}
