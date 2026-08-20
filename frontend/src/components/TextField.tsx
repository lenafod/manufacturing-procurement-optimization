import type { InputHTMLAttributes } from 'react';

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  hint?: string;
  error?: boolean;
  mono?: boolean;
  fullWidth?: boolean;
}

export function TextField({ label, hint, error, mono, fullWidth, className, id, ...rest }: TextFieldProps) {
  const inputId = id ?? label.replace(/\s+/g, '-').toLowerCase();
  return (
    <div className={`field ${fullWidth ? 'full' : ''}`.trim()}>
      <label htmlFor={inputId}>{label}</label>
      <input
        id={inputId}
        className={`field-input ${mono ? 'mono' : ''} ${error ? 'err' : ''} ${className ?? ''}`.trim()}
        {...rest}
      />
      {hint && <span className="hint">{hint}</span>}
    </div>
  );
}
