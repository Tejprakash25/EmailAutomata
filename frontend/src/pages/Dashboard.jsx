import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '@/lib/apiClient';
import { useAuth } from '@/context/AuthContext';
import { useMediaQuery, MOBILE_QUERY } from '@/lib/useMediaQuery';
import WorkspaceNav from '@/components/layout/WorkspaceNav';
import StatCard from '@/components/ui/StatCard';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import StatusPill, { statusTone } from '@/components/ui/StatusPill';

/**
 * Authenticated home. A real summary of the account, computed server-side.
 */
export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const isMobile = useMediaQuery(MOBILE_QUERY);
  const [stats, setStats] = useState(null);
  const [recent, setRecent] = useState([]);

  useEffect(() => {
    apiClient.get('/dashboard/stats').then(setStats).catch(() => setStats(null));
    apiClient
      .get('/dispatches/history?size=5&sort=createdAt,desc')
      .then((p) => setRecent(p?.content ?? []))
      .catch(() => setRecent([]));
  }, []);

  const firstName = user?.displayName?.split(' ')[0];

  return (
    <section>
      <WorkspaceNav />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 'var(--ea-space-4)', marginBottom: 'var(--ea-space-8)', flexWrap: 'wrap' }}>
        <div>
          <p className="ea-mono" style={{ color: 'var(--ea-signal-600)', fontSize: 'var(--ea-text-xs)', letterSpacing: '0.12em', textTransform: 'uppercase', margin: '0 0 var(--ea-space-2)' }}>
            Workspace
          </p>
          <h1 style={{ fontSize: 'var(--ea-text-2xl)' }}>Good to see you, {firstName}</h1>
        </div>
        <Button onClick={() => navigate('/compose')}>New dispatch</Button>
      </div>

      {/* Stat grid */}
      <div
        style={{
          display: 'grid',
          gap: 'var(--ea-space-4)',
          gridTemplateColumns: isMobile ? '1fr 1fr' : 'repeat(4, 1fr)',
          marginBottom: 'var(--ea-space-8)',
        }}
      >
        <StatCard label="Dispatches" value={stats?.totalDispatches ?? '—'} />
        <StatCard label="Recipients" value={stats?.totalRecipients ?? '—'} />
        <StatCard label="Messages sent" value={stats?.messagesSent ?? '—'} />
        <StatCard
          label="Delivery rate"
          value={stats ? stats.deliveryRate : '—'}
          suffix={stats ? '%' : ''}
          accent
          hint={stats ? `${stats.messagesFailed} failed · ${stats.messagesPending} pending` : undefined}
        />
      </div>

      <div style={{ display: 'grid', gap: 'var(--ea-space-6)', gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr' }}>
        {/* Status breakdown */}
        <Card title="By status">
          {stats ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--ea-space-3)' }}>
              {Object.entries(stats.statusBreakdown).map(([status, count]) => {
                const pill = statusTone(status);
                return (
                  <div key={status} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <StatusPill tone={pill.tone} label={pill.label} />
                    <span className="ea-mono" style={{ color: 'var(--ea-ink-700)' }}>{count}</span>
                  </div>
                );
              })}
            </div>
          ) : (
            <p style={{ color: 'var(--ea-ink-300)', margin: 0 }}>Loading…</p>
          )}
        </Card>

        {/* Recent activity */}
        <Card title="Recent activity" action={<Button variant="ghost" onClick={() => navigate('/history')}>View all</Button>} padded={false}>
          {recent.length === 0 ? (
            <p style={{ padding: 'var(--ea-space-6)', color: 'var(--ea-ink-300)', margin: 0 }}>
              No dispatches yet. Compose your first one.
            </p>
          ) : (
            recent.map((row) => {
              const pill = statusTone(row.status);
              return (
                <div
                  key={row.id}
                  onClick={() => navigate(`/dispatches/${row.id}`)}
                  style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 'var(--ea-space-3)', padding: 'var(--ea-space-3) var(--ea-space-6)', borderBottom: '1px solid var(--ea-line)', cursor: 'pointer' }}
                >
                  <span style={{ minWidth: 0, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', fontSize: 'var(--ea-text-sm)' }}>
                    {row.subject}
                  </span>
                  <StatusPill tone={pill.tone} label={pill.label} />
                </div>
              );
            })
          )}
        </Card>
      </div>
    </section>
  );
}