import Button from '@/components/ui/Button';

/**
 * Shown when a collection is empty. Treats "nothing here yet" as a designed
 * state with a next action, rather than a blank panel.
 */
export default function EmptyState({ title, description, actionLabel, onAction }) {
  return (
    <div
      style={{
        textAlign: 'center',
        padding: 'var(--ea-space-12) var(--ea-space-6)',
        color: 'var(--ea-ink-300)',
      }}
    >
      <h3 style={{ fontSize: 'var(--ea-text-lg)', color: 'var(--ea-ink-700)', marginBottom: 'var(--ea-space-2)' }}>
        {title}
      </h3>
      {description && (
        <p style={{ maxWidth: 360, margin: '0 auto var(--ea-space-6)' }}>{description}</p>
      )}
      {actionLabel && onAction && (
        <Button onClick={onAction}>{actionLabel}</Button>
      )}
    </div>
  );
}