import { useEffect, useMemo, useState } from 'react';
import { apiClient } from '@/lib/apiClient';
import { readPage } from '@/lib/paged';
import { useApiError } from '@/lib/useApiError';
import WorkspaceNav from '@/components/layout/WorkspaceNav';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Alert from '@/components/ui/Alert';
import StatusPill from '@/components/ui/StatusPill';

/**
 * Compose joins a template to a set of recipients and produces a draft
 * dispatch. The readiness summary returned by the server is surfaced
 * immediately, so unresolved recipients are visible before any send.
 */
export default function Compose() {
  const [templates, setTemplates] = useState([]);
  const [recipients, setRecipients] = useState([]);
  const [templateId, setTemplateId] = useState('');
  const [selected, setSelected] = useState(() => new Set());
  const [result, setResult] = useState(null);
  const [composing, setComposing] = useState(false);
  const { capture, clear, bannerMessage } = useApiError();

  useEffect(() => {
    apiClient.get('/templates?size=100&sort=name,asc').then((p) => setTemplates(readPage(p).items));
    apiClient.get('/recipients?size=200&sort=email,asc').then((p) => setRecipients(readPage(p).items));
  }, []);

  const toggle = (id) =>
    setSelected((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });

  const selectedTemplate = useMemo(
    () => templates.find((t) => String(t.id) === String(templateId)),
    [templates, templateId],
  );

  const compose = async () => {
    clear();
    setResult(null);
    setComposing(true);
    try {
      const res = await apiClient.post('/dispatches/compose', {
        templateId: Number(templateId),
        recipientIds: [...selected],
        listId: null,
      });
      setResult(res);
    } catch (err) {
      capture(err);
    } finally {
      setComposing(false);
    }
  };

  const canCompose = templateId && selected.size > 0 && !composing;

  return (
    <section>
      <WorkspaceNav />

      <h1 style={{ fontSize: 'var(--ea-text-xl)', marginBottom: 'var(--ea-space-2)' }}>Compose</h1>
      <p style={{ color: 'var(--ea-ink-300)', fontSize: 'var(--ea-text-sm)', marginBottom: 'var(--ea-space-6)' }}>
        Pick a template and recipients. We render each message and flag anyone
        missing a required field before you send.
      </p>

      <Alert tone="error">{bannerMessage}</Alert>

      <div style={{ display: 'grid', gap: 'var(--ea-space-6)' }}>
        <Card title="Template">
          <select
            value={templateId}
            onChange={(e) => setTemplateId(e.target.value)}
            style={{
              width: '100%', padding: 'var(--ea-space-3)', fontSize: 'var(--ea-text-base)',
              border: '1px solid var(--ea-line)', borderRadius: 'var(--ea-radius-sm)',
              backgroundColor: 'var(--ea-paper-000)', fontFamily: 'var(--ea-font-ui)',
            }}
          >
            <option value="">Select a template…</option>
            {templates.map((t) => (
              <option key={t.id} value={t.id}>{t.name}</option>
            ))}
          </select>
          {selectedTemplate && (
            <p style={{ marginTop: 'var(--ea-space-3)', fontSize: 'var(--ea-text-sm)', color: 'var(--ea-ink-500)' }}>
              Subject: <span className="ea-mono">{selectedTemplate.subject}</span>
            </p>
          )}
        </Card>

        <Card title={`Recipients${selected.size ? ` — ${selected.size} selected` : ''}`} padded={false}>
          {recipients.length === 0 ? (
            <p style={{ padding: 'var(--ea-space-6)', color: 'var(--ea-ink-300)' }}>
              No recipients yet. Add some on the Recipients tab first.
            </p>
          ) : (
            recipients.map((r) => (
              <label
                key={r.id}
                style={{
                  display: 'flex', alignItems: 'center', gap: 'var(--ea-space-3)',
                  padding: 'var(--ea-space-3) var(--ea-space-6)', borderBottom: '1px solid var(--ea-line)',
                  cursor: 'pointer',
                }}
              >
                <input type="checkbox" checked={selected.has(r.id)} onChange={() => toggle(r.id)} />
                <span style={{ flex: 1 }}>
                  <strong>{r.displayName || r.email}</strong>
                  {r.displayName && (
                    <span className="ea-mono" style={{ marginLeft: 'var(--ea-space-2)', fontSize: 'var(--ea-text-xs)', color: 'var(--ea-ink-300)' }}>{r.email}</span>
                  )}
                </span>
                <span className="ea-mono" style={{ fontSize: 'var(--ea-text-xs)', color: 'var(--ea-ink-300)' }}>
                  {Object.keys(r.fields ?? {}).length} fields
                </span>
              </label>
            ))
          )}
        </Card>

        <div>
          <Button onClick={compose} disabled={!canCompose} loading={composing}>
            Compose draft
          </Button>
        </div>

        {result && (
          <Card title="Draft composed">
            <div style={{ display: 'flex', gap: 'var(--ea-space-3)', marginBottom: 'var(--ea-space-4)' }}>
              <StatusPill tone="sent" label={`${result.preview.readyRecipients} ready`} />
              {result.preview.unresolved.length > 0 && (
                <StatusPill tone="failed" label={`${result.preview.unresolved.length} unresolved`} />
              )}
            </div>

            {result.preview.unresolved.length > 0 ? (
              <>
                <p style={{ fontSize: 'var(--ea-text-sm)', color: 'var(--ea-ink-500)', marginBottom: 'var(--ea-space-3)' }}>
                  These recipients are missing fields the template needs:
                </p>
                <ul style={{ margin: 0, paddingLeft: 'var(--ea-space-6)', fontSize: 'var(--ea-text-sm)' }}>
                  {result.preview.unresolved.map((u) => (
                    <li key={u.email} style={{ marginBottom: 'var(--ea-space-1)' }}>
                      <span className="ea-mono">{u.email}</span> — missing{' '}
                      <span style={{ color: 'var(--ea-state-failed)' }}>{u.missingFields.join(', ')}</span>
                    </li>
                  ))}
                </ul>
              </>
            ) : (
              <p style={{ fontSize: 'var(--ea-text-sm)', color: 'var(--ea-state-sent)', margin: 0 }}>
                Every recipient rendered cleanly. This draft is ready to send.
              </p>
            )}

            <p style={{ marginTop: 'var(--ea-space-4)', fontSize: 'var(--ea-text-xs)', color: 'var(--ea-ink-300)' }}>
              Draft #{result.dispatch.id} saved. Sending and scheduling arrive in the next commits.
            </p>
          </Card>
        )}
      </div>
    </section>
  );
}