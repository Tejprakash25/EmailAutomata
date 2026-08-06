import { useEffect, useState } from 'react';
import { apiClient } from '@/lib/apiClient';
import StatusPill from '@/components/ui/StatusPill';

/**
 * Boot screen. Proves the full client -> proxy -> API path is wired, and gives
 * anyone cloning the repo an unambiguous signal about what is and isn't running.
 */
export default function SystemCheck() {
  const [state, setState] = useState({ status: 'checking', meta: null, error: null });

  useEffect(() => {
    let active = true;

    apiClient
      .get('/meta')
      .then((meta) => active && setState({ status: 'online', meta, error: null }))
      .catch((error) => active && setState({ status: 'offline', meta: null, error }));

    return () => {
      active = false;
    };
  }, []);

  const pill = {
    checking: { tone: 'pending', label: 'Checking' },
    online: { tone: 'sent', label: 'Connected' },
    offline: { tone: 'failed', label: 'Unreachable' },
  }[state.status];

  return (
    <section style={{ maxWidth: 640 }}>
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
        Commit 01 — Foundation
      </p>

      <h1 style={{ fontSize: 'var(--ea-text-2xl)', marginBottom: 'var(--ea-space-4)' }}>
        Compose. Schedule.<br />Account for every send.
      </h1>

      <p style={{ color: 'var(--ea-ink-500)', marginBottom: 'var(--ea-space-8)' }}>
        The EmailAutomata workspace is being built one capability at a time. This
        screen confirms the client and API are talking to each other.
      </p>

      <div
        style={{
          backgroundColor: 'var(--ea-paper-000)',
          border: '1px solid var(--ea-line)',
          borderRadius: 'var(--ea-radius-md)',
          boxShadow: 'var(--ea-shadow-sm)',
          overflow: 'hidden',
        }}
      >
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: 'var(--ea-space-4) var(--ea-space-6)',
            borderBottom: '1px solid var(--ea-line)',
          }}
        >
          <strong style={{ fontSize: 'var(--ea-text-sm)' }}>API service</strong>
          <StatusPill tone={pill.tone} label={pill.label} />
        </div>

        <dl style={{ margin: 0, padding: 'var(--ea-space-4) var(--ea-space-6)' }}>
          {state.status === 'online' &&
            [
              ['Product', state.meta.product],
              ['API version', state.meta.apiVersion],
              ['Build', state.meta.buildVersion],
              ['Environment', state.meta.environment],
              ['Server time', new Date(state.meta.serverTime).toLocaleString()],
            ].map(([label, value]) => (
              <div
                key={label}
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  gap: 'var(--ea-space-4)',
                  padding: 'var(--ea-space-2) 0',
                }}
              >
                <dt style={{ color: 'var(--ea-ink-300)', fontSize: 'var(--ea-text-sm)' }}>
                  {label}
                </dt>
                <dd className="ea-mono" style={{ margin: 0, color: 'var(--ea-ink-700)' }}>
                  {value}
                </dd>
              </div>
            ))}

          {state.status === 'checking' && (
            <p style={{ margin: 0, color: 'var(--ea-ink-300)' }}>Contacting the service…</p>
          )}

          {state.status === 'offline' && (
            <p style={{ margin: 0, color: 'var(--ea-ink-500)' }}>
              {state.error?.message} Start the API with{' '}
              <code>./mvnw spring-boot:run</code> in <code>backend/emailautomata-api</code>.
            </p>
          )}
        </dl>
      </div>
    </section>
  );
}
