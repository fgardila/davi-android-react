export function formatCOP(amount: number, showDecimals = false): string {
  const options: Intl.NumberFormatOptions = {
    style: 'currency',
    currency: 'COP',
    minimumFractionDigits: showDecimals ? 2 : 0,
    maximumFractionDigits: showDecimals ? 2 : 0,
  };
  return new Intl.NumberFormat('es-CO', options).format(amount);
}

export function formatMovementAmount(amount: number, type: 'DEBIT' | 'CREDIT'): string {
  const formatted = formatCOP(amount);
  return type === 'DEBIT' ? `-${formatted}` : `+${formatted}`;
}

export function formatRelativeDate(millis: number): string {
  const now = Date.now();
  const diff = now - millis;
  const day = 86_400_000;

  if (diff < day) return 'Hoy';
  if (diff < day * 2) return 'Ayer';

  const date = new Date(millis);
  const months = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];
  return `${date.getDate()} ${months[date.getMonth()].toUpperCase()}`;
}
