import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '@/lib/apiClient';
import { readPage } from '@/lib/paged';
import WorkspaceNav from '@/components/layout/WorkspaceNav';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import FormField from '@/components/ui/FormField';
import EmptyState from '@/components/ui/EmptyState';
import StatusPill from '@/components/ui/StatusPill';
import Alert from '@/components/ui/Alert';
import { useApiError } from '@/lib/useApiError';

const EMPTY = { email: '', displayName: '' };

export default function RecipientList() {
  const navigate = useNavigate();
  const [page, setPage] = useState(null);
  const [status, setStatus] = useState('loading');
  const [form, setForm] = useState(EMPTY);
  const [adding, setAdding] = useState(false);
  const { capture, clear, fieldError, bannerMessage, error } = useApiError();

  const load = useCallback(() => {
    setStatus('loading');
    apiClient
      .get('/recipients?size=100&sort=createdAt,desc')
      .then((p) => { setPage(readPage(p)); setStatus('ready'); })
      .catch(() => setStatus('error'));
  }, []);

  useEffect(load, [load]);

  const update = (e) => setForm((c) => ({ ...c, [e.target.name]: e.target.value }));

  const add = async (e) => {
    e.preventDefault();
    clear();
    setAdding(true);
    try {
      await apiClient.post('/recipients', { ...form, fields: {} });
      setForm(EMPTY);
      load();
    } catch (err) {
      capture(err);
    } finally {
      setAdding(false);
    }
  };

  const remove = async (id, email) => {
    if (!window.confirm(`Remove ${email}?`)) return;
    await apiClient.delete(`/recipients/${id}`);
    load();
  };

  return (
    <section>
      <WorkspaceNav />

      <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 'var(--ea-space-6)', gap: 'var(--ea-space-4)', flexWrap: 'wrap' }}>
        <div>
          <h1 style={{ fontSize: 'var(--ea-text-xl)' }}>Recipients</h1>
          <p style={{ color: 'var(--ea-ink-300)', fontSize: 'var(--ea-text-sm)', margin: 'var(--ea-space-1) 0 0' }}>
            The people you send to, and the values that personalise each message.
          </p>
        </div>
        <Button variant="secondary" onClick={() => navigate('/recipients/import')}>Import CSV</Button>
      </div>

      <div style={{ display: 'grid', gap: 'var(--ea-space-6)' }}>
        <Card title="Add a recipient">
          <Alert tone="error">{bannerMessage}</Alert>
          <form
            onSubmit={add}
            noValidate
            data-responsive-grid
            style={{ display: 'grid', gap: 'var(--ea-space-3)', gridTemplateColumns: '1fr 1fr auto', alignItems: 'start' }}
          >
            <FormField label="Email" name="email" type="email" value={form.email} onChange={update} error={fieldError('email')} required />
            <FormField label="Name" name="displayName" value={form.displayName} onChange={update} error={fieldError('displayName')} />
            <div style={{ paddingTop: 'calc(var(--ea-text-sm) + var(--ea-space-2) + 2px)' }}>
              <Button type="submit" loading={adding} fullWidth>Add</Button>
            </div>
          </form>
        </Card>

        {status === 'error' && <Alert tone="error">{error?.message ?? 'Could not load recipients.'}</Alert>}

        {status === 'ready' && page.isEmpty && (
          <Card padded={false}>
            <EmptyState
              title="No recipients yet"
              description="Add one above, or import a CSV to bring in a whole list at once."
              actionLabel="Import CSV"
              onAction={() => navigate('/recipients/import')}
            />
          </Card>
        )}

        {status === 'ready' && !page.isEmpty && (
          <Card title={`${page.totalItems} recipient${page.totalItems === 1 ? '' : 's'}`} padded={false}>
            <div>
              {page.items.map((r) => {
                const fieldKeys = Object.keys(r.fields ?? {});
                return (
                  <div key={r.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 'var(--ea-space-4)', padding: 'var(--ea-space-3) var(--ea-space-6)', borderBottom: '1px solid var(--ea-line)' }}>
                    <div style={{ minWidth: 0 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--ea-space-3)' }}>
                        <strong style={{ color: 'var(--ea-ink-900)' }}>{r.displayName || r.email}</strong>
                        {fieldKeys.length > 0 && <StatusPill tone="neutral" label={`${fieldKeys.length} field${fieldKeys.length > 1 ? 's' : ''}`} />}
                      </div>
                      {r.displayName && (
                        <span className="ea-mono" style={{ fontSize: 'var(--ea-text-xs)', color: 'var(--ea-ink-300)' }}>{r.email}</span>
                      )}
                    </div>
                    <Button variant="ghost" onClick={() => remove(r.id, r.email)}>Remove</Button>
                  </div>
                );
              })}
            </div>
          </Card>
        )}
      </div>
    </section>
  );
}