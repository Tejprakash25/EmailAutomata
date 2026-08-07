import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { apiClient } from '@/lib/apiClient';
import { readPage } from '@/lib/paged';
import WorkspaceNav from '@/components/layout/WorkspaceNav';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import EmptyState from '@/components/ui/EmptyState';
import Alert from '@/components/ui/Alert';
import StatusPill, { statusTone } from '@/components/ui/StatusPill';

const STATUSES = ['', 'DRAFT', 'SCHEDULED', 'SENT', 'FAILED'];
const LABELS = { '': 'All', DRAFT: 'Draft', SCHEDULED: 'Scheduled', SENT: 'Sent', FAILED: 'Failed' };

/**
 * Sent history. Filter and search state live in the URL, so a filtered view is
 * shareable and survives a refresh.
 */
export default function DispatchHistory() {
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();

  const status = params.get('status') ?? '';
  const search = params.get('search') ?? '';
  const pageNum = Number(params.get('page') ?? 0);

  const [searchDraft, setSearchDraft] = useState(search);
  const [page, setPage] = useState(null);
  const [state, setState] = useState('loading');

  const load = useCallback(() => {
    setState('loading');
    const qs = new URLSearchParams({ page: String(pageNum), size: '15', sort: 'createdAt,desc' });
    if (status) qs.set('status', status);
    if (search) qs.set('search', search);

    apiClient
      .get(`/dispatches/history?${qs.toString()}`)
      .then((p) => { setPage(readPage(p)); setState('ready'); })
      .catch(() => setState('error'));
  }, [status, search, pageNum]);

  useEffect(load, [load]);

  const setFilter = (next) => {
    const merged = { status, search, page: '0', ...next };
    const clean = Object.fromEntries(Object.entries(merged).filter(([, v]) => v !== '' && v != null));
    setParams(clean);
  };

  const submitSearch = (e) => {
    e.preventDefault();
    setFilter({ search: searchDraft });
  };

  return (
    <section>
      <WorkspaceNav />

      <h1 style={{ fontSize: 'var(--ea-text-xl)', marginBottom: 'var(--ea-space-2)' }}>Sent history</h1>
      <p style={{ color: 'var(--ea-ink-300)', fontSize: 'var(--ea-text-sm)', marginBottom: 'var(--ea-space-6)' }}>
        Every dispatch you've composed, with its delivery breakdown.
      </p>

      {/* Filter bar */}
      <div style={{ display: 'flex', gap: 'var(--ea-space-4)', alignItems: 'center', marginBottom: 'var(--ea-space-6)', flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', gap: 'var(--ea-space-1)', flexWrap: 'wrap' }}>
          {STATUSES.map((s) => (
            <button
              key={s || 'all'}
              onClick={() => setFilter({ status: s })}
              style={{
                padding: 'var(--ea-space-2) var(--ea-space-3)',
                fontSize: 'var(--ea-text-sm)',
                fontFamily: 'var(--ea-font-ui)',
                cursor: 'pointer',
                borderRadius: 'var(--ea-radius-pill)',
                border: `1px solid ${status === s ? 'var(--ea-signal-500)' : 'var(--ea-line)'}`,
                backgroundColor: status === s ? 'var(--ea-signal-050)' : 'var(--ea-paper-000)',
                color: status === s ? 'var(--ea-signal-600)' : 'var(--ea-ink-500)',
              }}
            >
              {LABELS[s]}
            </button>
          ))}
        </div>

        <form onSubmit={submitSearch} style={{ display: 'flex', gap: 'var(--ea-space-2)', flex: 1, minWidth: 220 }}>
          <input
            value={searchDraft}
            onChange={(e) => setSearchDraft(e.target.value)}
            placeholder="Search subject…"
            style={{
              flex: 1, padding: 'var(--ea-space-2) var(--ea-space-3)',
              border: '1px solid var(--ea-line)', borderRadius: 'var(--ea-radius-sm)',
              fontFamily: 'var(--ea-font-ui)', fontSize: 'var(--ea-text-sm)',
            }}
          />
          <Button variant="secondary" type="submit">Search</Button>
        </form>
      </div>

      {state === 'error' && <Alert tone="error">Could not load history.</Alert>}
      {state === 'loading' && <p style={{ color: 'var(--ea-ink-300)' }}>Loading…</p>}

      {state === 'ready' && page.isEmpty && (
        <Card padded={false}>
          <EmptyState
            title="Nothing here"
            description={status || search ? 'No dispatches match these filters.' : 'Compose your first dispatch to see it here.'}
            actionLabel="Compose"
            onAction={() => navigate('/compose')}
          />
        </Card>
      )}

      {state === 'ready' && !page.isEmpty && (
        <>
          <Card padded={false}>
            {page.items.map((row) => {
              const pill = statusTone(row.status);
              return (
                <div
                  key={row.id}
                  onClick={() => navigate(`/dispatches/${row.id}`)}
                  style={{
                    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                    gap: 'var(--ea-space-4)', padding: 'var(--ea-space-4) var(--ea-space-6)',
                    borderBottom: '1px solid var(--ea-line)', cursor: 'pointer',
                  }}
                >
                  <div style={{ minWidth: 0, flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--ea-space-3)' }}>
                      <StatusPill tone={pill.tone} label={pill.label} />
                      <strong style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                        {row.subject}
                      </strong>
                    </div>
                    <div className="ea-mono" style={{ fontSize: 'var(--ea-text-xs)', color: 'var(--ea-ink-300)', marginTop: 2 }}>
                      {row.recipientCount} recipient{row.recipientCount === 1 ? '' : 's'}
                      {row.sentAt && ` · sent ${new Date(row.sentAt).toLocaleString()}`}
                      {row.scheduledAt && !row.sentAt && ` · scheduled ${new Date(row.scheduledAt).toLocaleString()}`}
                    </div>
                  </div>

                  <div style={{ display: 'flex', gap: 'var(--ea-space-2)', flexShrink: 0 }}>
                    {row.delivery.sent > 0 && <StatusPill tone="sent" label={`${row.delivery.sent}`} />}
                    {row.delivery.failed > 0 && <StatusPill tone="failed" label={`${row.delivery.failed}`} />}
                    {row.delivery.pending > 0 && <StatusPill tone="pending" label={`${row.delivery.pending}`} />}
                  </div>
                </div>
              );
            })}
          </Card>

          {/* Pagination */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'var(--ea-space-4)' }}>
            <span className="ea-mono" style={{ fontSize: 'var(--ea-text-xs)', color: 'var(--ea-ink-300)' }}>
              Page {page.page + 1} of {page.totalPages} · {page.totalItems} total
            </span>
            <div style={{ display: 'flex', gap: 'var(--ea-space-2)' }}>
              <Button variant="secondary" disabled={page.isFirst} onClick={() => setFilter({ page: String(pageNum - 1) })}>
                Previous
              </Button>
              <Button variant="secondary" disabled={page.isLast} onClick={() => setFilter({ page: String(pageNum + 1) })}>
                Next
              </Button>
            </div>
          </div>
        </>
      )}
    </section>
  );
}