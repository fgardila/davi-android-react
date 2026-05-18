import React, {useRef, useState} from 'react';
import {
  FlatList,
  RefreshControl,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import AppBarRed from '../components/AppBarRed';
import FilterChip from '../components/FilterChip';
import MovementItem from '../components/MovementItem';
import SkeletonBlock from '../components/SkeletonBlock';
import EmptyState from '../components/EmptyState';
import {Colors, Spacing, Typography} from '../theme/theme';
import {formatCOP} from '../utils/formatCurrency';
import {useMovementsQuery} from '../hooks/useMovementsQuery';

type FilterType = 'ALL' | 'DEBIT' | 'CREDIT';

interface MovementsScreenProps {
  onBack: () => void;
}

export default function MovementsScreen({onBack}: MovementsScreenProps) {
  const movementsQuery = useMovementsQuery();
  const [filter, setFilter] = useState<FilterType>('ALL');
  const [search, setSearch] = useState('');
  const [searchActive, setSearchActive] = useState(false);
  const searchInputRef = useRef<TextInput>(null);
  const searchDebounce = useRef<ReturnType<typeof setTimeout>>(undefined);

  const onRefresh = () => { movementsQuery.refetch(); };

  const handleSearchChange = (text: string) => {
    clearTimeout(searchDebounce.current);
    searchDebounce.current = setTimeout(() => setSearch(text), 300);
  };

  const filtered = (movementsQuery.data ?? []).filter(m => {
    if (filter === 'DEBIT' && m.type !== 'DEBIT') return false;
    if (filter === 'CREDIT' && m.type !== 'CREDIT') return false;
    if (search.length > 0 && !m.description.toLowerCase().includes(search.toLowerCase())) return false;
    return true;
  });

  const totalIn = filtered.filter(m => m.type === 'CREDIT').reduce((s, m) => s + m.amount, 0);
  const totalOut = filtered.filter(m => m.type === 'DEBIT').reduce((s, m) => s + m.amount, 0);

  const searchAction = (
    <TouchableOpacity
      onPress={() => { setSearchActive(true); setTimeout(() => searchInputRef.current?.focus(), 100); }}
      hitSlop={{top: 8, bottom: 8, left: 8, right: 8}}>
      <Text style={styles.searchIcon}>🔍</Text>
    </TouchableOpacity>
  );

  return (
    <View style={styles.root}>
      {searchActive ? (
        <View style={styles.searchBar}>
          <TextInput
            ref={searchInputRef}
            style={styles.searchInput}
            placeholder="Buscar movimiento..."
            placeholderTextColor="rgba(255,255,255,0.5)"
            onChangeText={handleSearchChange}
            autoFocus
            returnKeyType="search"
          />
          <TouchableOpacity
            onPress={() => { setSearchActive(false); setSearch(''); }}
            style={styles.cancelBtn}>
            <Text style={styles.cancelText}>Cancelar</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <AppBarRed title="Movimientos" onBack={onBack} rightAction={searchAction} />
      )}

      {/* Filter chips (sticky) */}
      <View style={styles.filtersContainer}>
        <View style={styles.filtersRow}>
          {(['ALL', 'DEBIT', 'CREDIT'] as FilterType[]).map(f => (
            <FilterChip
              key={f}
              label={f === 'ALL' ? 'Todos' : f === 'DEBIT' ? 'Débitos' : 'Créditos'}
              active={filter === f}
              onPress={() => setFilter(f)}
            />
          ))}
        </View>
      </View>

      {/* Summary */}
      <View style={styles.summary}>
        <View style={styles.summaryItem}>
          <Text style={styles.summaryLabel}>Ingresos</Text>
          <Text style={[styles.summaryAmount, {color: Colors.creditGreen}]}>
            {formatCOP(totalIn)}
          </Text>
        </View>
        <View style={styles.summaryDivider} />
        <View style={styles.summaryItem}>
          <Text style={styles.summaryLabel}>Gastos</Text>
          <Text style={[styles.summaryAmount, {color: Colors.debitRed}]}>
            {formatCOP(totalOut)}
          </Text>
        </View>
      </View>

      {movementsQuery.isPending ? (
        <MovementsSkeleton />
      ) : (
        <FlatList
          data={filtered}
          keyExtractor={item => item.id}
          renderItem={({item}) => <MovementItem movement={item} />}
          refreshControl={
            <RefreshControl
              refreshing={movementsQuery.isFetching && !movementsQuery.isPending}
              onRefresh={onRefresh}
              tintColor={Colors.brandRed}
              colors={[Colors.brandRed]}
            />
          }
          ListEmptyComponent={
            <EmptyState
              title="Sin resultados"
              subtitle={search ? `Ningún movimiento coincide con "${search}"` : 'No hay movimientos aún'}
            />
          }
          contentContainerStyle={styles.listContent}
        />
      )}
    </View>
  );
}

function MovementsSkeleton() {
  return (
    <View style={styles.skeletonList}>
      {[0, 1, 2, 3, 4].map(i => (
        <View key={i} style={styles.skeletonRow}>
          <SkeletonBlock style={styles.skeletonIcon} />
          <View style={styles.skeletonInfo}>
            <SkeletonBlock style={styles.skeletonLine1} />
            <SkeletonBlock style={styles.skeletonLine2} />
          </View>
          <SkeletonBlock style={styles.skeletonAmountBlock} />
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  root: {flex: 1, backgroundColor: Colors.surfaceLight},

  // Search bar (replaces AppBar when active)
  searchBar: {
    backgroundColor: Colors.brandRed,
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: 52,
    paddingBottom: Spacing.s16,
    paddingHorizontal: Spacing.s16,
    gap: Spacing.s12,
  },
  searchInput: {
    flex: 1,
    color: Colors.white,
    ...Typography.bodyLarge,
    borderBottomWidth: 1.5,
    borderBottomColor: 'rgba(255,255,255,0.5)',
    paddingVertical: Spacing.s4,
  },
  cancelBtn: {paddingVertical: Spacing.s4},
  cancelText: {...Typography.bodyMedium, color: Colors.white, fontWeight: '600'},
  searchIcon: {fontSize: 18, color: Colors.white},

  // Filters
  filtersContainer: {
    backgroundColor: Colors.white,
    borderBottomWidth: 1,
    borderBottomColor: Colors.strokeDefault,
  },
  filtersRow: {
    flexDirection: 'row',
    gap: Spacing.s8,
    padding: Spacing.s12,
  },

  // Summary
  summary: {
    flexDirection: 'row',
    backgroundColor: Colors.white,
    marginBottom: Spacing.s8,
    borderBottomWidth: 1,
    borderBottomColor: Colors.strokeDefault,
  },
  summaryItem: {flex: 1, alignItems: 'center', paddingVertical: Spacing.s12},
  summaryDivider: {width: 1, backgroundColor: Colors.strokeDefault, marginVertical: Spacing.s8},
  summaryLabel: {...Typography.bodySmall, color: Colors.textSecondary, marginBottom: 2},
  summaryAmount: {...Typography.currencyBody},

  // List
  listContent: {paddingTop: Spacing.s4, paddingBottom: Spacing.s32},

  // Skeleton
  skeletonList: {paddingTop: Spacing.s8},
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
  skeletonLine1: {height: 14, width: '65%', borderRadius: 7, marginBottom: 6},
  skeletonLine2: {height: 11, width: '35%', borderRadius: 5},
  skeletonAmountBlock: {height: 16, width: 72, borderRadius: 8},
});
