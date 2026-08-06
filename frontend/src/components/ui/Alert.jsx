/**
 * Inline message banner. Tones map onto the same semantic palette as
 * StatusPill, so a failure looks like a failure everywhere in the product.
 */
const TONES = {
  error: { fg: 'var(--ea-state-failed)', bg: 'var(--ea-state-failed-bg)' },
  success: { fg: 'var(--ea-state-sent)', bg: 'var(--ea-state-sent-bg)' },
  info: { fg: 'var(--ea-ink-500)', bg: 'var(--ea-paper-200)' },
};

export default function Alert({ tone = 'info', children }) {
  if (!children) return null;

  const { fg, bg } = TONES[tone] ?? TONES.info;

  return (
    <div
      role={tone === 'error' ? 'alert' : 'status'}
      style={{
        backgroundColor: bg,
        color: fg,
        border: `1px solid ${fg}25`,
        borderRadius: 'var(--ea-radius-md)',
        padding: 'var(--ea-space-3) var(--ea-space-4)',
        fontSize: 'var(--ea-text-sm)',
        marginBottom: 'var(--ea-space-4)',
      }}
    >
      {children}
    </div>
  );
}