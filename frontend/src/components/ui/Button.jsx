/**
 * Action button. The signal accent is reserved for the primary variant, which
 * is why there is at most one on any screen.
 */
const VARIANTS = {
  primary: {
    bg: 'var(--ea-signal-500)',
    fg: 'var(--ea-paper-000)',
    border: 'var(--ea-signal-500)',
  },
  secondary: {
    bg: 'var(--ea-paper-000)',
    fg: 'var(--ea-ink-700)',
    border: 'var(--ea-line)',
  },
  ghost: {
    bg: 'transparent',
    fg: 'var(--ea-ink-500)',
    border: 'transparent',
  },
};

export default function Button({
  children,
  variant = 'primary',
  type = 'button',
  onClick,
  disabled = false,
  loading = false,
  fullWidth = false,
}) {
  const tone = VARIANTS[variant] ?? VARIANTS.primary;
  const inactive = disabled || loading;

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={inactive}
      style={{
        width: fullWidth ? '100%' : undefined,
        padding: 'var(--ea-space-3) var(--ea-space-6)',
        backgroundColor: tone.bg,
        color: tone.fg,
        border: `1px solid ${tone.border}`,
        borderRadius: 'var(--ea-radius-sm)',
        fontFamily: 'var(--ea-font-ui)',
        fontSize: 'var(--ea-text-sm)',
        fontWeight: 500,
        cursor: inactive ? 'not-allowed' : 'pointer',
        opacity: inactive ? 0.6 : 1,
        transition: `opacity var(--ea-transition)`,
      }}
    >
      {loading ? 'Working…' : children}
    </button>
  );
}