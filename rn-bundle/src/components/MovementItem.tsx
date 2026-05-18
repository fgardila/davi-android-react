import React from 'react';
import {StyleSheet, Text, View} from 'react-native';
import {Colors, Spacing, Typography} from '../theme/theme';
import {MovementItem as MovementItemType, MovementStatus} from '../bridge/types';
import {formatMovementAmount, formatRelativeDate} from '../utils/formatCurrency';

interface MovementItemProps {
  movement: MovementItemType;
}

const STATUS_META: Record<Exclude<MovementStatus, 'COMPLETED'>, {label: string; bg: string; fg: string}> = {
  PENDING: {label: 'Pendiente', bg: '#FEF3C7', fg: '#92400E'},
  FAILED:  {label: 'Fallido',   bg: '#FEE2E2', fg: '#991B1B'},
};

export default function MovementItem({movement}: MovementItemProps) {
  const isDebit = movement.type === 'DEBIT';
  const isFailed = movement.status === 'FAILED';
  const statusMeta = movement.status !== 'COMPLETED' ? STATUS_META[movement.status] : null;
  const amountColor = isFailed
    ? Colors.textDisabled
    : isDebit ? Colors.debitRed : Colors.creditGreen;

  return (
    <View style={styles.row}>
      <View
        style={[
          styles.iconContainer,
          {backgroundColor: isDebit ? Colors.debitSurface : Colors.creditSurface},
        ]}>
        <Text style={[styles.arrow, {color: isDebit ? Colors.debitRed : Colors.creditGreen}]}>
          {isDebit ? '↑' : '↓'}
        </Text>
      </View>

      <View style={styles.info}>
        <Text
          style={[styles.description, isFailed && styles.descriptionFailed]}
          numberOfLines={2}>
          {movement.description}
        </Text>
        <View style={styles.metaRow}>
          <Text style={styles.date}>{formatRelativeDate(movement.occurredAtMillis)}</Text>
          {statusMeta && (
            <View style={[styles.badge, {backgroundColor: statusMeta.bg}]}>
              <Text style={[styles.badgeText, {color: statusMeta.fg}]}>{statusMeta.label}</Text>
            </View>
          )}
        </View>
      </View>

      <Text
        style={[
          styles.amount,
          {color: amountColor},
          isFailed && styles.amountFailed,
        ]}>
        {formatMovementAmount(movement.amount, movement.type)}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.white,
    borderRadius: 12,
    padding: Spacing.s12,
    marginHorizontal: Spacing.s16,
    marginBottom: Spacing.s8,
    shadowColor: '#000',
    shadowOffset: {width: 0, height: 1},
    shadowOpacity: 0.04,
    shadowRadius: 3,
    elevation: 1,
  },
  iconContainer: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: Spacing.s12,
  },
  arrow: {
    fontSize: 18,
    fontWeight: '700',
  },
  info: {
    flex: 1,
    marginRight: Spacing.s8,
  },
  description: {
    ...Typography.bodyMedium,
    color: Colors.textPrimary,
  },
  descriptionFailed: {
    color: Colors.textSecondary,
  },
  metaRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 2,
    gap: Spacing.s8,
  },
  date: {
    ...Typography.bodySmall,
    color: Colors.textSecondary,
  },
  badge: {
    paddingHorizontal: Spacing.s8,
    paddingVertical: 2,
    borderRadius: 999,
  },
  badgeText: {
    ...Typography.bodySmall,
    fontWeight: '600',
    fontSize: 11,
  },
  amount: {
    ...Typography.currencyBody,
  },
  amountFailed: {
    textDecorationLine: 'line-through',
  },
});
