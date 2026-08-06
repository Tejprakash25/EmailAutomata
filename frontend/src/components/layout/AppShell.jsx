import Wordmark from '@/components/brand/Wordmark';

/**
 * Persistent application frame: masthead, content well, footer rule.
 * Every future page renders inside this shell, so chrome stays consistent
 * as features land.
 */
export default function AppShell({ children }) {
  return (
    <div style={{ minHeight: '100%', display: 'flex', flexDirection: 'column' }}>
      <header
        style={{
          borderBottom: '1px solid var(--ea-line)',
          backgroundColor: 'var(--ea-paper-000)',
        }}
      >
        <div
          style={{
            maxWidth: 'var(--ea-container)',
            margin: '0 auto',
            padding: 'var(--ea-space-4) var(--ea-space-6)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: 'var(--ea-space-4)',
          }}
        >
          <Wordmark />
          <span
            className="ea-mono"
            style={{ color: 'var(--ea-ink-300)', fontSize: 'var(--ea-text-xs)' }}
          >
            v0.1.0
          </span>
        </div>
      </header>

      <main
        style={{
          flex: 1,
          width: '100%',
          maxWidth: 'var(--ea-container)',
          margin: '0 auto',
          padding: 'var(--ea-space-12) var(--ea-space-6)',
        }}
      >
        {children}
      </main>

      <footer
        style={{
          borderTop: '1px solid var(--ea-line)',
          padding: 'var(--ea-space-4) var(--ea-space-6)',
          textAlign: 'center',
          color: 'var(--ea-ink-300)',
          fontSize: 'var(--ea-text-sm)',
        }}
      >
        EmailAutomata — compose, schedule, account for every send.
      </footer>
    </div>
  );
}
