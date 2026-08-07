import { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { apiClient } from '@/lib/apiClient';
import WorkspaceNav from '@/components/layout/WorkspaceNav';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Alert from '@/components/ui/Alert';
import StatusPill, { statusTone } from '@/components/ui/StatusPill';

/**
 * A single dispatch with its per-recipient delivery outcomes. This is where the
 * product's promise is visible: every recipient's individual status and, on
 * failure, the reason. Draft dispatches can be sent now or scheduled here.
 */
export default function DispatchDetail() {
  const { id } = useParams();
  const [dispatch, setDispatch] = useState(null);
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  const [scheduleAt, setScheduleAt] = useState('');

  const load = useCallback(() => {
    apiClient.get(`/dispatches/${id}`).then(setDispatch).catch((e) => setError(e.message));
  }, [id]);

  useEffect(load, [load]);

  const act = async (fn) => {
    setError(null);
    setBusy(true);
    try {
      await fn();
      load();
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  };

  const send = () => act(() => apiClient.post(`/dispatches/${id}/send`, {}));

  const schedule = () =>
    act(() =>
      apiClient.post(`/dispatches/${id}/schedule`, {
        // datetime-local yields local wall time; convert to an ISO instant.
        scheduledAt: new Date(scheduleAt).toISOString(),
      }),
    );

  const cancelSchedule = () => act(() => apiClient.post(`/dispatches/${id}/cancel-schedule`, {}));

  if (!dispatch) {
    return (
      <section>
        <WorkspaceNav />
        {error ? <Alert tone="error">{error}</Alert> : <p style={{ color: 'var(--ea-ink-300)' }}>Loading…</p>}
      </section>
    );
  }

  const pill = statusTone(dispatch.status);
  const isDraft = dispatch.status === 'DRAFT';
  const isScheduled = dispatch.status === 'SCHEDULED';

  return (
    <section>
      <WorkspaceNav />

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 'var(--ea-space-6)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--ea-space-3)' }}>
          <h1 style={{ fontSize: 'var(--ea-text-xl)' }}>Dispatch #{dispatch.id}</h1>
          <StatusPill tone={pill.tone} label={pill.label} />
        </div>
        {isDraft && <Button onClick={send} loading={busy}>Send now</Button>}
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      {isScheduled && (
        <Card title="Scheduled">
          <p style={{ margin: '0 0 var(--ea-space-3)', fontSize: 'var(--ea-text-sm)', color: 'var(--ea-ink-500)' }}>
            This dispatch will send automatically at{' '}
            <strong>{new Date(dispatch.scheduledAt).toLocaleString()}</strong>.
          </p>
          <Button variant="ghost" onClick={cancelSchedule} loading={busy}>Cancel schedule</Button>
        </Card>
      )}

      {isDraft && (
        <Card title="Schedule for later">
          <div style={{ display: 'flex', gap: 'var(--ea-space-3)', alignItems: 'center', flexWrap: 'wrap' }}>
            <input
              type="datetime-local"
              value={scheduleAt}
              onChange={(e) => setScheduleAt(e.target.value)}
              style={{
                padding: 'var(--ea-space-3)', fontFamily: 'var(--ea-font-ui)', fontSize: 'var(--ea-text-base)',
                border: '1px solid var(--ea-line)', borderRadius: 'var(--ea-radius-sm)',
                backgroundColor: 'var(--ea-paper-000)',
              }}
            />
            <Button variant="secondary" onClick={schedule} disabled={!scheduleAt || busy}>
              Schedule
            </Button>
          </div>
        </Card>
      )}

      <div style={{ marginTop: 'var(--ea-space-6)' }}>
        <Card title="Message">
          <p style={{ margin: '0 0 var(--ea-space-2)' }}>
            <span style={{ color: 'var(--ea-ink-300)', fontSize: 'var(--ea-text-sm)' }}>Subject template: </span>
            <span className="ea-mono">{dispatch.subject}</span>
          </p>
          <p style={{ margin: 0, color: 'var(--ea-ink-500)', fontSize: 'var(--ea-text-sm)', whiteSpace: 'pre-wrap' }}>
            {dispatch.body}
          </p>
        </Card>
      </div>

      <div style={{ marginTop: 'var(--ea-space-6)' }}>
        <Card title={`Recipients — ${dispatch.recipientCount}`} padded={false}>
          {(dispatch.recipients ?? []).map((r) => {
            const rp = statusTone(r.deliveryStatus);
            return (
              <div key={r.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 'var(--ea-space-4)', padding: 'var(--ea-space-3) var(--ea-space-6)', borderBottom: '1px solid var(--ea-line)' }}>
                <div style={{ minWidth: 0 }}>
                  <strong>{r.displayName || r.email}</strong>
                  <div className="ea-mono" style={{ fontSize: 'var(--ea-text-xs)', color: 'var(--ea-ink-300)' }}>
                    {r.renderedSubject}
                  </div>
                  {r.failureReason && (
                    <div style={{ fontSize: 'var(--ea-text-xs)', color: 'var(--ea-state-failed)', marginTop: 2 }}>
                      {r.failureReason}
                    </div>
                  )}
                </div>
                <StatusPill tone={rp.tone} label={rp.label} />
              </div>
            );
          })}
        </Card>
      </div>
    </section>
  );
}