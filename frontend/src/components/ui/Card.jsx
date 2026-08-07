/**
 * Paper surface with an optional header row. The one container primitive every
 * content block sits in, so spacing and borders stay uniform product-wide.
 */
export default function Card({ title, action, children, padded = true }) {
  return (
    <div
      style={{
        backgroundColor: 'var(--ea-paper-000)',
        border: '1px solid var(--ea-line)',
        borderRadius: 'var(--ea-radius-md)',
        boxShadow: 'var(--ea-shadow-sm)',
        overflow: 'hidden',
      }}
    >
      {(title || action) && (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: 'var(--ea-space-4)',
            padding: 'var(--ea-space-4) var(--ea-space-6)',
            borderBottom: '1px solid var(--ea-line)',
          }}
        >
          {title && (
            <h2 style={{ fontSize: 'var(--ea-text-lg)', margin: 0 }}>{title}</h2>
          )}
          {action}
        </div>
      )}
      <div style={{ padding: padded ? 'var(--ea-space-6)' : 0 }}>{children}</div>
    </div>
  );
}