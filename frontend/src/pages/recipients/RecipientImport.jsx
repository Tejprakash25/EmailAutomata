import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '@/lib/apiClient';
import WorkspaceNav from '@/components/layout/WorkspaceNav';
import Card from '@/components/ui/Card';
import Textarea from '@/components/ui/Textarea';
import Button from '@/components/ui/Button';
import Alert from '@/components/ui/Alert';
import StatusPill from '@/components/ui/StatusPill';

const SAMPLE = `email,name,firstName,role
ada@example.com,Ada Lovelace,Ada,Engineer
grace@example.com,Grace Hopper,Grace,Admiral`;

export default function RecipientImport() {
  const navigate = useNavigate();
  const [csv, setCsv] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [importing, setImporting] = useState(false);

  const run = async () => {
    setError(null);
    setResult(null);
    setImporting(true);
    try {
      setResult(await apiClient.post('/recipients/import', { csv, listId: null }));
    } catch (err) {
      setError(err.message);
    } finally {
      setImporting(false);
    }
  };

  return (
    <section>
      <WorkspaceNav />

      <h1 style={{ fontSize: 'var(--ea-text-xl)', marginBottom: 'var(--ea-space-2)' }}>Import recipients</h1>
      <p style={{ color: 'var(--ea-ink-300)', fontSize: 'var(--ea-text-sm)', marginBottom: 'var(--ea-space-6)' }}>
        Paste CSV with an <code>email</code> column. A <code>name</code> column is optional;
        every other column becomes a merge field.
      </p>

      <div style={{ display: 'grid', gap: 'var(--ea-space-6)' }}>
        <Card>
          {error && <Alert tone="error">{error}</Alert>}
          <Textarea
            label="CSV"
            name="csv"
            value={csv}
            onChange={(e) => setCsv(e.target.value)}
            rows={10}
            placeholder={SAMPLE}
          />
          <div style={{ display: 'flex', gap: 'var(--ea-space-3)' }}>
            <Button onClick={run} loading={importing} disabled={!csv.trim()}>Import</Button>
            <Button variant="ghost" onClick={() => setCsv(SAMPLE)}>Use sample</Button>
            <Button variant="ghost" onClick={() => navigate('/recipients')}>Back</Button>
          </div>
        </Card>

        {result && (
          <Card title="Import result">
            <div style={{ display: 'flex', gap: 'var(--ea-space-3)', marginBottom: 'var(--ea-space-4)' }}>
              <StatusPill tone="sent" label={`${result.imported} imported`} />
              {result.skipped > 0 && <StatusPill tone="failed" label={`${result.skipped} skipped`} />}
            </div>

            {result.errors.length > 0 && (
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 'var(--ea-text-sm)' }}>
                <thead>
                  <tr style={{ textAlign: 'left', color: 'var(--ea-ink-300)' }}>
                    <th style={{ padding: 'var(--ea-space-2)' }}>Line</th>
                    <th style={{ padding: 'var(--ea-space-2)' }}>Value</th>
                    <th style={{ padding: 'var(--ea-space-2)' }}>Reason</th>
                  </tr>
                </thead>
                <tbody>
                  {result.errors.map((e, i) => (
                    <tr key={i} style={{ borderTop: '1px solid var(--ea-line)' }}>
                      <td className="ea-mono" style={{ padding: 'var(--ea-space-2)' }}>{e.line}</td>
                      <td className="ea-mono" style={{ padding: 'var(--ea-space-2)', color: 'var(--ea-ink-500)' }}>{e.value}</td>
                      <td style={{ padding: 'var(--ea-space-2)' }}>{e.reason}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}

            {result.imported > 0 && (
              <div style={{ marginTop: 'var(--ea-space-4)' }}>
                <Button onClick={() => navigate('/recipients')}>View recipients</Button>
              </div>
            )}
          </Card>
        )}
      </div>
    </section>
  );
}