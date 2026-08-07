import { useId } from 'react';

/**
 * Multi-line counterpart to FormField, wired the same way for validation and
 * accessibility so the two compose consistently in a form.
 */
export default function Textarea({
  label,
  name,
  value,
  onChange,
  error,
  hint,
  rows = 8,
  required = false,
  placeholder,
}) {
  const id = useId();
  const describedBy = error ? `${id}-error` : hint ? `${id}-hint` : undefined;

  return (
    <div style={{ marginBottom: 'var(--ea-space-4)' }}>
      <label
        htmlFor={id}
        style={{
          display: 'block',
          fontSize: 'var(--ea-text-sm)',
          fontWeight: 500,
          color: 'var(--ea-ink-700)',
          marginBottom: 'var(--ea-space-2)',
        }}
      >
        {label}
        {required && <span aria-hidden="true" style={{ color: 'var(--ea-signal-500)' }}> *</span>}
      </label>

      <textarea
        id={id}
        name={name}
        value={value}
        onChange={onChange}
        rows={rows}
        placeholder={placeholder}
        aria-invalid={Boolean(error)}
        aria-describedby={describedBy}
        style={{
          width: '100%',
          padding: 'var(--ea-space-3)',
          fontFamily: 'var(--ea-font-ui)',
          fontSize: 'var(--ea-text-base)',
          lineHeight: 1.6,
          color: 'var(--ea-ink-900)',
          backgroundColor: 'var(--ea-paper-000)',
          border: `1px solid ${error ? 'var(--ea-state-failed)' : 'var(--ea-line)'}`,
          borderRadius: 'var(--ea-radius-sm)',
          outline: 'none',
          resize: 'vertical',
        }}
      />

      {error && (
        <p id={`${id}-error`} style={{ margin: 'var(--ea-space-2) 0 0', fontSize: 'var(--ea-text-xs)', color: 'var(--ea-state-failed)' }}>
          {error}
        </p>
      )}
      {!error && hint && (
        <p id={`${id}-hint`} style={{ margin: 'var(--ea-space-2) 0 0', fontSize: 'var(--ea-text-xs)', color: 'var(--ea-ink-300)' }}>
          {hint}
        </p>
      )}
    </div>
  );
}