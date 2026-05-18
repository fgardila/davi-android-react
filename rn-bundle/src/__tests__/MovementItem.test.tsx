import React from 'react';
import {render} from '@testing-library/react-native';
import MovementItem from '../components/MovementItem';
import {MovementItem as MovementItemType} from '../bridge/types';

const baseMovement: MovementItemType = {
  id: 'mov-1',
  type: 'CREDIT',
  status: 'COMPLETED',
  amount: 150_000,
  description: 'Recibido de Camila Ruiz',
  occurredAtMillis: Date.now() - 60 * 60 * 1000, // hace 1h
};

describe('MovementItem', () => {
  it('renders description and amount', () => {
    const {getByText} = render(<MovementItem movement={baseMovement} />);
    expect(getByText('Recibido de Camila Ruiz')).toBeTruthy();
    // El monto formateado: "+ $ 150.000"
    expect(getByText(/150\.000/)).toBeTruthy();
  });

  it('renders CREDIT with down arrow', () => {
    const {getByText} = render(<MovementItem movement={baseMovement} />);
    // CREDIT muestra ↓
    expect(getByText('↓')).toBeTruthy();
  });

  it('renders DEBIT with up arrow', () => {
    const {getByText} = render(
      <MovementItem movement={{...baseMovement, type: 'DEBIT'}} />,
    );
    // DEBIT muestra ↑
    expect(getByText('↑')).toBeTruthy();
  });

  it('does NOT show status badge for COMPLETED', () => {
    const {queryByText} = render(<MovementItem movement={baseMovement} />);
    expect(queryByText('Pendiente')).toBeNull();
    expect(queryByText('Fallido')).toBeNull();
  });

  it('shows "Pendiente" badge for PENDING status', () => {
    const {getByText} = render(
      <MovementItem movement={{...baseMovement, status: 'PENDING'}} />,
    );
    expect(getByText('Pendiente')).toBeTruthy();
  });

  it('shows "Fallido" badge for FAILED status', () => {
    const {getByText} = render(
      <MovementItem movement={{...baseMovement, status: 'FAILED'}} />,
    );
    expect(getByText('Fallido')).toBeTruthy();
  });

  it('applies strikethrough to amount when FAILED', () => {
    const {getByText} = render(
      <MovementItem
        movement={{...baseMovement, status: 'FAILED', type: 'DEBIT', amount: 35_000}}
      />,
    );
    const amount = getByText(/35\.000/);
    // El style puede ser un array (style + style condicional); aplanamos para checar.
    const flattened = Array.isArray(amount.props.style)
      ? Object.assign({}, ...amount.props.style)
      : amount.props.style;
    expect(flattened.textDecorationLine).toBe('line-through');
  });

  it('does NOT apply strikethrough to amount when COMPLETED', () => {
    const {getByText} = render(<MovementItem movement={baseMovement} />);
    const amount = getByText(/150\.000/);
    const flattened = Array.isArray(amount.props.style)
      ? Object.assign({}, ...amount.props.style)
      : amount.props.style;
    expect(flattened.textDecorationLine).not.toBe('line-through');
  });

  it('renders relative date', () => {
    const {getByText} = render(<MovementItem movement={baseMovement} />);
    // 1h ago → "Hoy"
    expect(getByText('Hoy')).toBeTruthy();
  });
});
