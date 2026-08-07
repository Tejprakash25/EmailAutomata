import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { useApiError } from '@/lib/useApiError';
import FormField from '@/components/ui/FormField';
import Button from '@/components/ui/Button';
import Alert from '@/components/ui/Alert';

export default function Register() {
  const { register, isAuthenticated } = useAuth();
  const { capture, clear, fieldError, bannerMessage } = useApiError();
  const navigate = useNavigate();

  const [form, setForm] = useState({ displayName: '', email: '', password: '' });
  const [submitting, setSubmitting] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  const update = (event) =>
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));

  const submit = async (event) => {
    event.preventDefault();
    clear();
    setSubmitting(true);

    try {
      await register(form);
      navigate('/dashboard', { replace: true });
    } catch (error) {
      capture(error);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section style={{ maxWidth: 400 }}>
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
        Create account
      </p>

      <h1 style={{ fontSize: 'var(--ea-text-xl)', marginBottom: 'var(--ea-space-6)' }}>
        Start sending
      </h1>

      <Alert tone="error">{bannerMessage}</Alert>

      <form onSubmit={submit} noValidate>
        <FormField
          label="Display name"
          name="displayName"
          value={form.displayName}
          onChange={update}
          error={fieldError('displayName')}
          autoComplete="name"
          required
        />

        <FormField
          label="Email"
          name="email"
          type="email"
          value={form.email}
          onChange={update}
          error={fieldError('email')}
          autoComplete="email"
          required
        />

        <FormField
          label="Password"
          name="password"
          type="password"
          value={form.password}
          onChange={update}
          error={fieldError('password')}
          hint="At least 8 characters."
          autoComplete="new-password"
          required
        />

        <Button type="submit" loading={submitting} fullWidth>
          Create account
        </Button>
      </form>

      <p style={{ marginTop: 'var(--ea-space-6)', fontSize: 'var(--ea-text-sm)', color: 'var(--ea-ink-300)' }}>
        Already registered? <Link to="/login">Sign in</Link>
      </p>
    </section>
  );
}