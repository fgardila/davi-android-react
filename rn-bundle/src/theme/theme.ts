import {StyleSheet} from 'react-native';

export const Colors = {
  brandRed: '#DA0026',
  brandRedDark: '#A8001C',
  brandRedSurface: 'rgba(218,0,38,0.08)',
  white: '#FFFFFF',
  surfaceLight: '#F5F5F5',
  surfaceMedium: '#EEEEEE',
  textPrimary: '#1A1A1A',
  textSecondary: '#6B6B6B',
  strokeDefault: '#E0E0E0',
  debitRed: '#E53935',
  creditGreen: '#2E7D32',
  debitSurface: '#FFEBEE',
  creditSurface: '#E8F5E9',
} as const;

export const Spacing = {
  s4: 4,
  s8: 8,
  s12: 12,
  s16: 16,
  s20: 20,
  s24: 24,
  s32: 32,
  s48: 48,
} as const;

export const Typography = StyleSheet.create({
  heading1: {fontFamily: 'sans-serif', fontWeight: '700', fontSize: 24, lineHeight: 32},
  heading2: {fontFamily: 'sans-serif', fontWeight: '700', fontSize: 20, lineHeight: 28},
  heading3: {fontFamily: 'sans-serif', fontWeight: '600', fontSize: 16, lineHeight: 24},
  bodyLarge: {fontFamily: 'sans-serif', fontWeight: '400', fontSize: 16, lineHeight: 24},
  bodyMedium: {fontFamily: 'sans-serif', fontWeight: '400', fontSize: 14, lineHeight: 20},
  bodySmall: {fontFamily: 'sans-serif', fontWeight: '400', fontSize: 12, lineHeight: 16},
  labelStrong: {fontFamily: 'sans-serif', fontWeight: '600', fontSize: 14, lineHeight: 20},
  overline: {fontFamily: 'sans-serif', fontWeight: '600', fontSize: 10, lineHeight: 16, letterSpacing: 1.5},
  currencyDisplay: {fontFamily: 'monospace', fontWeight: '700', fontSize: 32, lineHeight: 40},
  currencyBody: {fontFamily: 'monospace', fontWeight: '600', fontSize: 16, lineHeight: 24},
});
