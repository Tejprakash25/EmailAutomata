import { Link } from 'react-router-dom';
import Wordmark from '@/components/brand/Wordmark';
import Button from '@/components/ui/Button';
import { useAuth } from '@/context/AuthContext';

/**
 * Persistent application frame: masthead, content well, footer rule.
 */
export default function AppShell({ children }) {
  const { isAuthenticated, user, logout } = useAuth();

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
          <Link to={isAuthenticated ? '/dashboard' : '/'} style={{ textDecoration: 'none' }}>
            <Wordmark />
          </Link>

          {isAuthenticated ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--ea-space-4)' }}>
              <span
                className="ea-mono"
                data-responsive-hide-mobile
                style={{ color: 'var(--ea-ink-300)', fontSize: 'var(--ea-text-xs)' }}
              >
                {user?.email}
              </span>
              <Button variant="ghost" onClick={logout}>Sign out</Button>
            </div>
          ) : (
            <span
              className="ea-mono"
              style={{ color: 'var(--ea-ink-300)', fontSize: 'var(--ea-text-xs)' }}
            >
              v0.1.0
            </span>
          )}
        </div>
      </header>

      <main
        style={{
          flex: 1,
          width: '100%',
          maxWidth: 'var(--ea-container)',
          margin: '0 auto',
          padding: 'var(--ea-space-8) var(--ea-space-6)',
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