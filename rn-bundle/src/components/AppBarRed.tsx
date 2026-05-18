import React from 'react';
import {
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import {Colors, Spacing, Typography} from '../theme/theme';

interface AppBarRedProps {
  title: string;
  onBack?: () => void;
  rightAction?: React.ReactNode;
}

export default function AppBarRed({title, onBack, rightAction}: AppBarRedProps) {
  return (
    <View style={styles.bar}>
      <View style={styles.left}>
        {onBack ? (
          <TouchableOpacity onPress={onBack} style={styles.backBtn} hitSlop={{top: 8, bottom: 8, left: 8, right: 8}}>
            <Text style={styles.backArrow}>←</Text>
          </TouchableOpacity>
        ) : (
          <View style={styles.placeholder} />
        )}
      </View>

      <Text style={styles.title} numberOfLines={1}>{title}</Text>

      <View style={styles.right}>
        {rightAction ?? <View style={styles.placeholder} />}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  bar: {
    backgroundColor: Colors.brandRed,
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: 24,
    paddingBottom: Spacing.s16,
    paddingHorizontal: Spacing.s8,
  },
  left: {width: 48, alignItems: 'flex-start'},
  right: {width: 48, alignItems: 'flex-end'},
  backBtn: {padding: Spacing.s8},
  backArrow: {fontSize: 22, color: Colors.white, fontWeight: '600'},
  title: {
    ...Typography.heading3,
    color: Colors.white,
    flex: 1,
    textAlign: 'center',
  },
  placeholder: {width: 32},
});
