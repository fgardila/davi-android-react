import React from 'react';
import {StyleSheet, Text, TouchableOpacity} from 'react-native';
import {Colors, Spacing, Typography} from '../theme/theme';

interface FilterChipProps {
  label: string;
  active: boolean;
  onPress: () => void;
}

export default function FilterChip({label, active, onPress}: FilterChipProps) {
  return (
    <TouchableOpacity
      style={[styles.chip, active && styles.chipActive]}
      onPress={onPress}
      activeOpacity={0.7}>
      <Text style={[styles.label, active && styles.labelActive]}>{label}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  chip: {
    paddingHorizontal: Spacing.s12,
    paddingVertical: Spacing.s4 + 2,
    borderRadius: 999,
    borderWidth: 1.5,
    borderColor: Colors.strokeDefault,
    backgroundColor: Colors.white,
  },
  chipActive: {
    backgroundColor: Colors.brandRed,
    borderColor: Colors.brandRed,
  },
  label: {
    ...Typography.bodySmall,
    color: Colors.textSecondary,
    fontWeight: '500',
  },
  labelActive: {
    color: Colors.white,
    fontWeight: '700',
  },
});
