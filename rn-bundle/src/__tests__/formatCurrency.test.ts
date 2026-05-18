import {formatCOP, formatMovementAmount, formatRelativeDate} from '../utils/formatCurrency';

describe('formatCOP', () => {
  it('formats whole numbers in COP', () => {
    const result = formatCOP(1_250_345);
    // es-CO locale: $ 1.250.345 (space after $ sign, dots as thousands sep)
    expect(result).toContain('1.250.345');
    expect(result).toContain('$');
  });

  it('omits decimals by default', () => {
    expect(formatCOP(50_000)).not.toContain(',');
  });

  it('shows decimals when requested', () => {
    const result = formatCOP(50_000.5, true);
    // Colombian locale uses comma as decimal separator: 50.000,50
    expect(result).toContain(',50');
  });

  it('formats zero', () => {
    const result = formatCOP(0);
    expect(result).toContain('0');
  });
});

describe('formatMovementAmount', () => {
  it('prefixes debit with minus', () => {
    const result = formatMovementAmount(50_000, 'DEBIT');
    expect(result.startsWith('-')).toBe(true);
    expect(result).toContain('50.000');
  });

  it('prefixes credit with plus', () => {
    const result = formatMovementAmount(150_000, 'CREDIT');
    expect(result.startsWith('+')).toBe(true);
    expect(result).toContain('150.000');
  });
});

describe('formatRelativeDate', () => {
  it('returns Hoy for timestamps within the last 24h', () => {
    const nowMinus1h = Date.now() - 60 * 60 * 1000;
    expect(formatRelativeDate(nowMinus1h)).toBe('Hoy');
  });

  it('returns Ayer for timestamps 1-2 days ago', () => {
    const yesterday = Date.now() - 25 * 60 * 60 * 1000;
    expect(formatRelativeDate(yesterday)).toBe('Ayer');
  });

  it('returns formatted date for older timestamps', () => {
    const old = new Date(2025, 4, 13).getTime(); // 13 May 2025
    const result = formatRelativeDate(old);
    expect(result).toMatch(/\d+ [A-Z]+/);
  });
});
