import type { SelectHTMLAttributes } from 'react';

export interface SelectOption {
  value: string;
  label: string;
}

export interface SelectOptionGroup {
  label: string;
  options: SelectOption[];
}

interface SelectProps extends Omit<SelectHTMLAttributes<HTMLSelectElement>, 'children'> {
  label: string;
  options?: SelectOption[];
  groups?: SelectOptionGroup[];
  placeholder?: string;
  hint?: string;
  error?: boolean;
  fullWidth?: boolean;
}

// options za ravnu listu, groups za dvonivosku (npr. presek: oblik -> dimenzije) preko <optgroup>
export function Select({
  label,
  options,
  groups,
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
        {groups
          ? groups.map((group) => (
              <optgroup key={group.label} label={group.label}>
                {group.options.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </optgroup>
            ))
          : (options ?? []).map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
      </select>
      {hint && <span className="hint">{hint}</span>}
    </div>
  );
}
