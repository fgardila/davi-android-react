import React, {useEffect, useRef} from 'react';
import {Animated, StyleSheet, View, ViewStyle} from 'react-native';
import {Colors} from '../theme/theme';
import {useReduceMotion} from '../hooks/useReduceMotion';

interface SkeletonBlockProps {
  style?: ViewStyle;
  light?: boolean;
}

export default function SkeletonBlock({style, light = false}: SkeletonBlockProps) {
  const reduceMotion = useReduceMotion();
  const shimmer = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    if (reduceMotion) return;
    const anim = Animated.loop(
      Animated.sequence([
        Animated.timing(shimmer, {toValue: 1, duration: 900, useNativeDriver: true}),
        Animated.timing(shimmer, {toValue: 0, duration: 900, useNativeDriver: true}),
      ]),
    );
    anim.start();
    return () => anim.stop();
  }, [shimmer, reduceMotion]);

  const base = light ? 'rgba(255,255,255,0.2)' : Colors.surfaceMedium;

  if (reduceMotion) {
    return <View style={[styles.block, {backgroundColor: base, opacity: 0.7}, style]} />;
  }

  const opacity = shimmer.interpolate({inputRange: [0, 1], outputRange: [0.5, 1]});
  return (
    <Animated.View
      style={[styles.block, {backgroundColor: base, opacity}, style]}
    />
  );
}

const styles = StyleSheet.create({
  block: {
    borderRadius: 6,
  },
});
