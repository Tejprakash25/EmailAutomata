import { useId } from 'react';

/**
 * Labelled input wired for accessibility and server-side validation.
 *
 * <p>Accepts the message straight out of the API's error.details map, which is
 * why field names in DTOs and form inputs are kept identical — the mapping
 * needs no translation layer.</p>
 */
export default function FormField({
  label,
  name,
  type = 'text',
  value,
  onChange,
  error,
  hint,
  autoComplete,
  required = false,
  disabled = false,
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
        {required && (
          <span aria-hidden="true" style={{ color: 'var(--ea-signal-500)' }}> *</span>
        )}
      </label>

      <input
        id={id}
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        disabled={disabled}
        placeholder={placeholder}
        autoComplete={autoComplete}
        aria-invalid={Boolean(error)}
        aria-describedby={describedBy}
        style={{
          width: '100%',
          padding: 'var(--ea-space-3)',
          fontFamily: 'var(--ea-font-ui)',
          fontSize: 'var(--ea-text-base)',
          color: 'var(--ea-ink-900)',
          backgroundColor: disabled ? 'var(--ea-paper-200)' : 'var(--ea-paper-000)',
          border: `1px solid ${error ? 'var(--ea-state-failed)' : 'var(--ea-line)'}`,
          borderRadius: 'var(--ea-radius-sm)',
          outline: 'none',
          transition: `border-color var(--ea-transition)`,
        }}
      />

      {error && (
        <p
          id={`${id}-error`}
          style={{
            margin: 'var(--ea-space-2) 0 0',
            fontSize: 'var(--ea-text-xs)',
            color: 'var(--ea-state-failed)',
          }}
        >
          {error}
        </p>
      )}

      {!error && hint && (
        <p
          id={`${id}-hint`}
          style={{
            margin: 'var(--ea-space-2) 0 0',
            fontSize: 'var(--ea-text-xs)',
            color: 'var(--ea-ink-300)',
          }}
        >
          {hint}
        </p>
      )}
    </div>
  );
}