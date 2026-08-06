/**
 * Delivery-state pill. Defined once, in Commit 1, so Pending / Sent / Failed
 * look identical everywhere they appear later — history, dashboard, detail.
 */
const TONES = {
  pending: { fg: 'var(--ea-state-pending)', bg: 'var(--ea-state-pending-bg)' },
  sent: { fg: 'var(--ea-state-sent)', bg: 'var(--ea-state-sent-bg)' },
  failed: { fg: 'var(--ea-state-failed)', bg: 'var(--ea-state-failed-bg)' },
  neutral: { fg: 'var(--ea-ink-300)', bg: 'var(--ea-paper-200)' },
};

export default function StatusPill({ tone = 'neutral', label }) {
  const { fg, bg } = TONES[tone] ?? TONES.neutral;

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: 'var(--ea-space-2)',
        padding: '3px 10px',
        borderRadius: 'var(--ea-radius-pill)',
        backgroundColor: bg,
        color: fg,
        fontFamily: 'var(--ea-font-mono)',
        fontSize: 'var(--ea-text-xs)',
        fontWeight: 500,
        textTransform: 'uppercase',
        letterSpacing: '0.06em',
      }}
    >
      <span
        aria-hidden="true"
        style={{ width: 6, height: 6, borderRadius: '50%', backgroundColor: fg }}
      />
      {label}
    </span>
  );
}
