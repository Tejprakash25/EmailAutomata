import { useAuth } from '@/context/AuthContext';
import WorkspaceNav from '@/components/layout/WorkspaceNav';

/**
 * Placeholder home for an authenticated session. Replaced with real statistics
 * in the dashboard commit.
 */
export default function Dashboard() {
  const { user } = useAuth();

  return (
    <section>
      <WorkspaceNav />

      <p
        className="ea-mono"
        style={{
          color: 'var(--ea-signal-600)',
          fontSize: 'var(--ea-text-xs)',
          letterSpacing: '0.12em',
          textTransform: 'uppercase',
          margin: '0 0 var(--ea-space-3)',
        }}
      >
        Workspace
      </p>

      <h1 style={{ fontSize: 'var(--ea-text-2xl)', marginBottom: 'var(--ea-space-4)' }}>
        Good to see you, {user?.displayName?.split(' ')[0]}
      </h1>

      <p style={{ color: 'var(--ea-ink-500)' }}>
        Templates, recipients, and the delivery ledger arrive in the commits ahead.
      </p>
    </section>
  );
}