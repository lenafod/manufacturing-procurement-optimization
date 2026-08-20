import type { SelectHTMLAttributes } from 'react';

export interface SelectOption {
  value: string;
  label: string;
}

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string;
  options: SelectOption[];
  placeholder?: string;
  hint?: string;
  error?: boolean;
  fullWidth?: boolean;
}

export function Select({
  label,
  options,
  placeholder,
  hint,
  error,
  fullWidth,
  className,
  id,
  ...rest
}: SelectProps) {
  const selectId = id ?? label.replace(/\s+/g, '-').toLowerCase();
  return (
    <div className={`field ${fullWidth ? 'full' : ''}`.trim()}>
      <label htmlFor={selectId}>{label}</label>
      <select id={selectId} className={`${error ? 'err' : ''} ${className ?? ''}`.trim()} {...rest}>
        {placeholder && <option value="">{placeholder}</option>}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {hint && <span className="hint">{hint}</span>}
    </div>
  );
}
