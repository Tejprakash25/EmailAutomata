import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '@/lib/apiClient';
import { readPage } from '@/lib/paged';
import WorkspaceNav from '@/components/layout/WorkspaceNav';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import EmptyState from '@/components/ui/EmptyState';
import StatusPill from '@/components/ui/StatusPill';
import Alert from '@/components/ui/Alert';

export default function TemplateList() {
  const navigate = useNavigate();
  const [state, setState] = useState({ status: 'loading', page: null, error: null });

  const load = useCallback(() => {
    setState((s) => ({ ...s, status: 'loading' }));
    apiClient
      .get('/templates?size=50&sort=updatedAt,desc')
      .then((page) => setState({ status: 'ready', page: readPage(page), error: null }))
      .catch((error) => setState({ status: 'error', page: null, error }));
  }, []);

  useEffect(load, [load]);

  const remove = async (id, name) => {
    if (!window.confirm(`Delete "${name}"? This cannot be undone.`)) return;
    try {
      await apiClient.delete(`/templates/${id}`);
      load();
    } catch (error) {
      setState((s) => ({ ...s, error }));
    }
  };

  return (
    <section>
      <WorkspaceNav />

      <div
        style={{
          display: 'flex',
          alignItems: 'baseline',
          justifyContent: 'space-between',
          marginBottom: 'var(--ea-space-6)',
        }}
      >
        <div>
          <h1 style={{ fontSize: 'var(--ea-text-xl)' }}>Templates</h1>
          <p style={{ color: 'var(--ea-ink-300)', fontSize: 'var(--ea-text-sm)', margin: 'var(--ea-space-1) 0 0' }}>
            Reusable subjects and bodies with <code>{'{{merge}}'}</code> fields.
          </p>
        </div>
        <Button onClick={() => navigate('/templates/new')}>New template</Button>
      </div>

      {state.error && <Alert tone="error">{state.error.message}</Alert>}

      {state.status === 'loading' && (
        <p style={{ color: 'var(--ea-ink-300)' }}>Loading templates…</p>
      )}

      {state.status === 'ready' && state.page.isEmpty && (
        <Card padded={false}>
          <EmptyState
            title="No templates yet"
            description="Create your first template to reuse a subject and body across many sends."
            actionLabel="New template"
            onAction={() => navigate('/templates/new')}
          />
        </Card>
      )}

      {state.status === 'ready' && !state.page.isEmpty && (
        <div style={{ display: 'grid', gap: 'var(--ea-space-3)' }}>
          {state.page.items.map((t) => (
            <Card key={t.id} padded={false}>
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  gap: 'var(--ea-space-4)',
                  padding: 'var(--ea-space-4) var(--ea-space-6)',
                }}
              >
                <div style={{ minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--ea-space-3)' }}>
                    <strong style={{ color: 'var(--ea-ink-900)' }}>{t.name}</strong>
                    {t.placeholderCount > 0 && (
                      <StatusPill tone="neutral" label={`${t.placeholderCount} field${t.placeholderCount > 1 ? 's' : ''}`} />
                    )}
                  </div>
                  <p
                    style={{
                      margin: 'var(--ea-space-1) 0 0',
                      color: 'var(--ea-ink-300)',
                      fontSize: 'var(--ea-text-sm)',
                      whiteSpace: 'nowrap',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                    }}
                  >
                    {t.subject}
                  </p>
                </div>

                <div style={{ display: 'flex', gap: 'var(--ea-space-2)', flexShrink: 0 }}>
                  <Button variant="secondary" onClick={() => navigate(`/templates/${t.id}`)}>Edit</Button>
                  <Button variant="ghost" onClick={() => remove(t.id, t.name)}>Delete</Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </section>
  );
}