import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { apiClient } from '@/lib/apiClient';
import { useApiError } from '@/lib/useApiError';
import { PlaceholderExtractor } from '@/lib/placeholders';
import WorkspaceNav from '@/components/layout/WorkspaceNav';
import Card from '@/components/ui/Card';
import FormField from '@/components/ui/FormField';
import Textarea from '@/components/ui/Textarea';
import Button from '@/components/ui/Button';
import Alert from '@/components/ui/Alert';
import StatusPill from '@/components/ui/StatusPill';

const EMPTY = { name: '', subject: '', body: '' };

export default function TemplateEditor() {
  const { id } = useParams();
  const isEdit = id !== undefined;
  const navigate = useNavigate();
  const { capture, clear, fieldError, bannerMessage } = useApiError();

  const [form, setForm] = useState(EMPTY);
  const [loading, setLoading] = useState(isEdit);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!isEdit) return;
    apiClient
      .get(`/templates/${id}`)
      .then((t) => setForm({ name: t.name, subject: t.subject, body: t.body }))
      .catch(capture)
      .finally(() => setLoading(false));
  }, [id, isEdit, capture]);

  // Live preview of the merge fields the server will derive on save.
  const placeholders = useMemo(
    () => PlaceholderExtractor.extract(form.subject, form.body),
    [form.subject, form.body],
  );

  const update = (e) => setForm((c) => ({ ...c, [e.target.name]: e.target.value }));

  const save = async (e) => {
    e.preventDefault();
    clear();
    setSaving(true);
    try {
      if (isEdit) {
        await apiClient.put(`/templates/${id}`, form);
      } else {
        await apiClient.post('/templates', form);
      }
      navigate('/templates');
    } catch (error) {
      capture(error);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <section>
        <WorkspaceNav />
        <p style={{ color: 'var(--ea-ink-300)' }}>Loading template…</p>
      </section>
    );
  }

  return (
    <section>
      <WorkspaceNav />

      <h1 style={{ fontSize: 'var(--ea-text-xl)', marginBottom: 'var(--ea-space-6)' }}>
        {isEdit ? 'Edit template' : 'New template'}
      </h1>

      <Alert tone="error">{bannerMessage}</Alert>

      <div style={{ display: 'grid', gap: 'var(--ea-space-6)', gridTemplateColumns: 'minmax(0, 1fr)' }}>
        <Card>
          <form onSubmit={save} noValidate>
            <FormField
              label="Template name"
              name="name"
              value={form.name}
              onChange={update}
              error={fieldError('name')}
              hint="Only you see this. Used to find the template later."
              required
            />
            <FormField
              label="Subject"
              name="subject"
              value={form.subject}
              onChange={update}
              error={fieldError('subject')}
              hint="Supports {{merge}} fields."
              required
            />
            <Textarea
              label="Body"
              name="body"
              value={form.body}
              onChange={update}
              error={fieldError('body')}
              hint="Write {{firstName}} to insert a per-recipient value."
              rows={12}
              required
            />

            <div style={{ display: 'flex', gap: 'var(--ea-space-3)', marginTop: 'var(--ea-space-2)' }}>
              <Button type="submit" loading={saving}>
                {isEdit ? 'Save changes' : 'Create template'}
              </Button>
              <Button variant="ghost" onClick={() => navigate('/templates')}>Cancel</Button>
            </div>
          </form>
        </Card>

        <Card title="Merge fields">
          {placeholders.length === 0 ? (
            <p style={{ color: 'var(--ea-ink-300)', fontSize: 'var(--ea-text-sm)', margin: 0 }}>
              None yet. Add <code>{'{{firstName}}'}</code> to the subject or body and it appears here.
            </p>
          ) : (
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 'var(--ea-space-2)' }}>
              {placeholders.map((p) => (
                <StatusPill key={p} tone="neutral" label={p} />
              ))}
            </div>
          )}
        </Card>
      </div>
    </section>
  );
}