import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';

/**
 * Gates a route behind an active session.
 *
 * <p>Renders nothing while the session is being restored — redirecting during
 * that window would bounce an authenticated user to the login screen on every
 * refresh.</p>
 */
export default function ProtectedRoute({ children }) {
  const { isAuthenticated, initialising } = useAuth();
  const location = useLocation();

  if (initialising) {
    return (
      <p style={{ color: 'var(--ea-ink-300)' }}>Restoring your session…</p>
    );
  }

  if (!isAuthenticated) {
    // The attempted destination travels with the redirect so login can return
    // the user where they were headed.
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }

  return children;
}