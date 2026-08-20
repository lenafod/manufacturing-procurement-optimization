import type { ButtonHTMLAttributes } from 'react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'accent' | 'ghost';
}

export function Button({ variant = 'ghost', className, ...rest }: ButtonProps) {
  const variantClass = variant === 'accent' ? 'btn-accent' : 'btn-ghost';
  return <button type="button" className={`btn ${variantClass} ${className ?? ''}`.trim()} {...rest} />;
}
