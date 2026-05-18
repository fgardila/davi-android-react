import React from 'react';
import {StyleSheet, Text, View} from 'react-native';
import {Colors, Spacing, Typography} from '../theme/theme';

interface EmptyStateProps {
  title?: string;
  subtitle?: string;
}

export default function EmptyState({
  title = 'Sin movimientos',
  subtitle = 'Tus transacciones aparecerán aquí',
}: EmptyStateProps) {
  return (
    <View style={styles.container}>
      <Text style={styles.icon}>📭</Text>
      <Text style={styles.title}>{title}</Text>
      <Text style={styles.subtitle}>{subtitle}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    paddingVertical: Spacing.s48,
    paddingHorizontal: Spacing.s32,
  },
  icon: {fontSize: 48, marginBottom: Spacing.s16},
  title: {
    ...Typography.heading3,
    color: Colors.textPrimary,
    marginBottom: Spacing.s8,
    textAlign: 'center',
  },
  subtitle: {
    ...Typography.bodyMedium,
    color: Colors.textSecondary,
    textAlign: 'center',
  },
});
