import React, {useEffect, useRef, useState} from 'react';
import {
  Alert,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import BalanceCard from '../components/BalanceCard';
import ActionCard from '../components/ActionCard';
import MovementItem from '../components/MovementItem';
import SkeletonBlock from '../components/SkeletonBlock';
import EmptyState from '../components/EmptyState';
import {Colors, Spacing, Typography} from '../theme/theme';
import {nativeApi} from '../services/nativeApi';
import {useBalanceQuery} from '../hooks/useBalanceQuery';
import { useMovementsQuery } from '../hooks/useMovementsQuery';

interface HomeScreenProps {
  name: string;
  onNavigateToMovements: () => void;
}

export default function HomeScreen({name, onNavigateToMovements}: HomeScreenProps) {
  const balanceQuery = useBalanceQuery();
  const [hidden, setHidden] = useState(false);

  const amount = balanceQuery.data?.amount ?? 0;
  const balanceLoading = balanceQuery.isLoading;

  const movementsQuery = useMovementsQuery(3)

  const movements = movementsQuery.data ?? []

  const handleLogout = () => {
    Alert.alert('Cerrar sesión', '¿Seguro que deseas salir?', [
      {text: 'Cancelar', style: 'cancel'},
      {text: 'Salir', style: 'destructive', onPress: () => nativeApi.logout()},
    ]);
  };

  return (
    <View style={styles.root}>
      <ScrollView
        contentContainerStyle={styles.scroll}
        showsVerticalScrollIndicator={false}>

        {/* Red header inside the scroll so the BalanceCard sibling can
            overlap it with marginTop: -56 without being clipped */}
        <View style={styles.header}>
          <View style={styles.circle1} pointerEvents="none" />
          <View style={styles.headerContent}>
            <View>
              <Text style={styles.greeting}>Hola, {name || 'usuario'} 👋</Text>
              <Text style={styles.subGreeting}>Bienvenido a DaviPlata</Text>
            </View>
            <TouchableOpacity onPress={handleLogout} hitSlop={{top: 8, bottom: 8, left: 8, right: 8}}>
              <Text style={styles.logoutIcon}>⎋</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Balance card overlaps the header by 56dp */}
        <View style={styles.balanceWrapper}>
          <BalanceCard
            amount={amount}
            loading={balanceLoading}
            hidden={hidden}
            onToggleHidden={() => setHidden(h => !h)}
          />
        </View>

        {/* Quick actions */}
        <Text style={styles.overline}>ACCIONES RÁPIDAS</Text>
        <View style={styles.actionsRow}>
          <ActionCard
            emoji="💸"
            label="Transferir"
            onPress={() => nativeApi.openTransfer({})}
          />
          <View style={styles.actionGap} />
          <ActionCard
            emoji="📋"
            label="Movimientos"
            onPress={onNavigateToMovements}
          />
        </View>

        {/* Recent movements */}
        <View style={styles.movementsHeader}>
          <Text style={styles.sectionTitle}>Últimos movimientos</Text>
          <TouchableOpacity onPress={onNavigateToMovements}>
            <Text style={styles.seeAll}>Ver todos →</Text>
          </TouchableOpacity>
        </View>

        {movementsQuery.isLoading ? (
          <MovementsSkeleton />
        ) : movements.length === 0 ? (
          <EmptyState
            title="Sin movimientos"
            subtitle="Tus transacciones aparecerán aquí"
          />
        ) : (
          movements.map(m => <MovementItem key={m.id} movement={m} />)
        )}

        <View style={styles.bottomSpacer} />

        {/* Debug-only button to force session expiry */}
        {__DEV__ && (
          <TouchableOpacity
            style={styles.debugBtn}
            onPress={() =>
              nativeApi
                .forceSessionExpired()
                .then(() => nativeApi.getBalance())
                .catch(() => {})
            }>
            <Text style={styles.debugBtnText}>⚠ Forzar sesión expirada</Text>
          </TouchableOpacity>
        )}

      </ScrollView>
    </View>
  );
}

function MovementsSkeleton() {
  return (
    <View style={styles.skeletonList}>
      {[0, 1, 2].map(i => (
        <View key={i} style={styles.skeletonRow}>
          <SkeletonBlock style={styles.skeletonIcon} />
          <View style={styles.skeletonInfo}>
            <SkeletonBlock style={styles.skeletonLine1} />
            <SkeletonBlock style={styles.skeletonLine2} />
          </View>
          <SkeletonBlock style={styles.skeletonAmount} />
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  root: {flex: 1, backgroundColor: Colors.surfaceLight},

  // Header
  header: {
    backgroundColor: Colors.brandRed,
    paddingTop: 52,
    paddingBottom: 72,
    paddingHorizontal: Spacing.s24,
    overflow: 'hidden',
  },
  circle1: {
    position: 'absolute',
    width: 220,
    height: 220,
    borderRadius: 110,
    backgroundColor: 'rgba(255,255,255,0.06)',
    top: -60,
    right: -40,
  },
  headerContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  greeting: {...Typography.heading1, color: Colors.white},
  subGreeting: {...Typography.bodyMedium, color: 'rgba(255,255,255,0.7)', marginTop: 2},
  logoutIcon: {fontSize: 22, color: Colors.white, paddingTop: 4},

  // Scroll
  scroll: {paddingBottom: Spacing.s24},
  balanceWrapper: {
    marginTop: -56,
    marginHorizontal: Spacing.s16,
    marginBottom: Spacing.s24,
  },

  // Sections
  overline: {
    ...Typography.overline,
    color: Colors.textSecondary,
    marginHorizontal: Spacing.s24,
    marginBottom: Spacing.s12,
  },
  actionsRow: {
    flexDirection: 'row',
    paddingHorizontal: Spacing.s16,
    marginBottom: Spacing.s24,
  },
  actionGap: {width: Spacing.s12},
  movementsHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginHorizontal: Spacing.s24,
    marginBottom: Spacing.s12,
  },
  sectionTitle: {...Typography.heading3, color: Colors.textPrimary},
  seeAll: {...Typography.bodyMedium, color: Colors.brandRed, fontWeight: '600'},
  bottomSpacer: {height: Spacing.s16},

  // Skeleton
  skeletonList: {paddingTop: Spacing.s4},
  skeletonRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.white,
    marginHorizontal: Spacing.s16,
    marginBottom: Spacing.s8,
    borderRadius: 12,
    padding: Spacing.s12,
  },
  skeletonIcon: {width: 40, height: 40, borderRadius: 20, marginRight: Spacing.s12},
  skeletonInfo: {flex: 1, marginRight: Spacing.s8},
  skeletonLine1: {height: 14, width: '70%', borderRadius: 7, marginBottom: 6},
  skeletonLine2: {height: 11, width: '40%', borderRadius: 5},
  skeletonAmount: {height: 16, width: 72, borderRadius: 8},

  // Debug-only
  debugBtn: {
    marginHorizontal: Spacing.s24,
    marginTop: Spacing.s8,
    paddingVertical: Spacing.s8,
    paddingHorizontal: Spacing.s16,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: Colors.brandRed,
    alignItems: 'center',
  },
  debugBtnText: {...Typography.bodySmall, color: Colors.brandRed},
});
