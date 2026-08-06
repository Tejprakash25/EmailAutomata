/**
 * The EmailAutomata mark: a sealed-envelope glyph paired with the wordmark.
 * The diagonal fold uses the signal accent — the only place colour appears
 * in the identity.
 */
export default function Wordmark({ size = 'md' }) {
  const glyph = size === 'lg' ? 40 : 28;
  const type = size === 'lg' ? 'var(--ea-text-xl)' : 'var(--ea-text-lg)';

  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 'var(--ea-space-3)' }}>
      <svg width={glyph} height={glyph} viewBox="0 0 32 32" role="img" aria-label="EmailAutomata">
        <rect
          x="2.5" y="6.5" width="27" height="19" rx="3"
          fill="none" stroke="var(--ea-ink-900)" strokeWidth="2"
        />
        <path
          d="M3.5 8.5 L16 18 L28.5 8.5"
          fill="none" stroke="var(--ea-signal-500)" strokeWidth="2"
          strokeLinecap="round" strokeLinejoin="round"
        />
      </svg>
      <span
        style={{
          fontFamily: 'var(--ea-font-display)',
          fontSize: type,
          fontWeight: 600,
          letterSpacing: '-0.02em',
          color: 'var(--ea-ink-900)',
        }}
      >
        EmailAutomata
      </span>
    </span>
  );
}
