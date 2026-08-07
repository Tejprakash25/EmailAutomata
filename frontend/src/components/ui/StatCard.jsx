/**
 * A single headline figure. The value takes display type and, for the delivery
 * rate, the signal accent — the one place a number is emphasised as an outcome.
 */
export default function StatCard({ label, value, suffix, accent = false, hint }) {
  return (
    <div
      style={{
        backgroundColor: 'var(--ea-paper-000)',
        border: '1px solid var(--ea-line)',
        borderRadius: 'var(--ea-radius-md)',
        boxShadow: 'var(--ea-shadow-sm)',
        padding: 'var(--ea-space-6)',
      }}
    >
      <div
        className="ea-mono"
        style={{
          fontSize: 'var(--ea-text-xs)',
          textTransform: 'uppercase',
          letterSpacing: '0.08em',
          color: 'var(--ea-ink-300)',
          marginBottom: 'var(--ea-space-2)',
        }}
      >
        {label}
      </div>
      <div
        style={{
          fontFamily: 'var(--ea-font-display)',
          fontSize: 'var(--ea-text-2xl)',
          fontWeight: 600,
          lineHeight: 1,
          color: accent ? 'var(--ea-signal-600)' : 'var(--ea-ink-900)',
        }}
      >
        {value}
        {suffix && <span style={{ fontSize: 'var(--ea-text-lg)', color: 'var(--ea-ink-300)' }}>{suffix}</span>}
      </div>
      {hint && (
        <div style={{ fontSize: 'var(--ea-text-xs)', color: 'var(--ea-ink-300)', marginTop: 'var(--ea-space-2)' }}>
          {hint}
        </div>
      )}
    </div>
  );
}