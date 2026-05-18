import React, {useEffect, useRef, useState} from 'react';
import {
  Animated,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import {Colors, Spacing, Typography} from '../theme/theme';
import {formatCOP} from '../utils/formatCurrency';
import SkeletonBlock from './SkeletonBlock';
import {useReduceMotion} from '../hooks/useReduceMotion';

interface BalanceCardProps {
  amount: number | null;
  loading: boolean;
  hidden: boolean;
  onToggleHidden: () => void;
}

export default function BalanceCard({amount, loading, hidden, onToggleHidden}: BalanceCardProps) {
  const reduceMotion = useReduceMotion();
  const animatedValue = useRef(new Animated.Value(0)).current;
  const [displayAmount, setDisplayAmount] = useState(0);
  const prevAmount = useRef<number | null>(null);

  useEffect(() => {
    if (amount === null) return;
    if (reduceMotion) {
      prevAmount.current = amount;
      setDisplayAmount(amount);
      return;
    }
    const from = prevAmount.current ?? 0;
    prevAmount.current = amount;
    animatedValue.setValue(from);
    const listener = animatedValue.addListener(({value}) => setDisplayAmount(value));
    Animated.timing(animatedValue, {
      toValue: amount,
      duration: 800,
      useNativeDriver: false,
    }).start(() => animatedValue.removeListener(listener));
    return () => animatedValue.removeListener(listener);
  }, [amount, animatedValue, reduceMotion]);

  return (
    <View style={styles.card}>
      {/* Decorative circles */}
      <View style={styles.circle1} pointerEvents="none" />
      <View style={styles.circle2} pointerEvents="none" />

      <Text style={styles.label}>SALDO DISPONIBLE</Text>

      {loading ? (
        <View style={styles.skeletonRow}>
          <SkeletonBlock style={styles.skeletonAmount} light />
        </View>
      ) : (
        <TouchableOpacity onPress={onToggleHidden} activeOpacity={0.7} style={styles.amountRow}>
          <Text style={styles.amount}>
            {hidden ? '••••••••' : formatCOP(Math.round(displayAmount))}
          </Text>
          <Text style={styles.eyeIcon}>{hidden ? '👁' : '👁‍🗨'}</Text>
        </TouchableOpacity>
      )}

      <Text style={styles.currency}>Pesos colombianos · COP</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: Colors.brandRedDark,
    borderRadius: 20,
    padding: Spacing.s20,
    overflow: 'hidden',
    ...{
      shadowColor: Colors.brandRed,
      shadowOffset: {width: 0, height: 6},
      shadowOpacity: 0.4,
      shadowRadius: 14,
      elevation: 12,
    },
  },
  circle1: {
    position: 'absolute',
    width: 200,
    height: 200,
    borderRadius: 100,
    backgroundColor: 'rgba(255,255,255,0.07)',
    top: -60,
    right: -40,
  },
  circle2: {
    position: 'absolute',
    width: 140,
    height: 140,
    borderRadius: 70,
    backgroundColor: 'rgba(255,255,255,0.05)',
    bottom: -50,
    left: -20,
  },
  label: {
    ...Typography.overline,
    color: 'rgba(255,255,255,0.7)',
    marginBottom: Spacing.s8,
  },
  amountRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.s8,
    marginBottom: Spacing.s4,
  },
  amount: {
    ...Typography.currencyDisplay,
    color: Colors.white,
  },
  eyeIcon: {fontSize: 16, marginTop: 4},
  skeletonRow: {marginVertical: Spacing.s8},
  skeletonAmount: {height: 36, width: 180, borderRadius: 8},
  currency: {
    ...Typography.bodySmall,
    color: 'rgba(255,255,255,0.55)',
    marginTop: Spacing.s4,
  },
});
