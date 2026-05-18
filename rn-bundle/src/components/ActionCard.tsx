import React from 'react';
import {StyleSheet, Text, TouchableOpacity, View} from 'react-native';
import {Colors, Spacing, Typography} from '../theme/theme';

interface ActionCardProps {
  emoji: string;
  label: string;
  onPress: () => void;
  disabled?: boolean;
}

export default function ActionCard({emoji, label, onPress, disabled = false}: ActionCardProps) {
  return (
    <TouchableOpacity
      style={[styles.card, disabled && styles.disabled]}
      onPress={onPress}
      activeOpacity={0.75}
      disabled={disabled}>
      <View style={styles.iconCircle}>
        <Text style={styles.emoji}>{emoji}</Text>
      </View>
      <Text style={styles.label}>{label}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    flex: 1,
    backgroundColor: Colors.white,
    borderRadius: 16,
    paddingVertical: Spacing.s16,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: {width: 0, height: 2},
    shadowOpacity: 0.06,
    shadowRadius: 6,
    elevation: 2,
  },
  disabled: {opacity: 0.5},
  iconCircle: {
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: Colors.brandRedSurface,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: Spacing.s8,
  },
  emoji: {fontSize: 26},
  label: {
    ...Typography.labelStrong,
    color: Colors.textPrimary,
  },
});
