import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { apiClient, tokenStore } from '@/lib/apiClient';

const AuthContext = createContext(null);

/**
 * Holds the session for the whole app.
 *
 * <p>Only the token is persisted; the profile is re-fetched from /auth/me on
 * boot. Storing the profile too would let a stale copy outlive a changed or
 * deleted account.</p>
 */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [initialising, setInitialising] = useState(true);

  useEffect(() => {
    if (!tokenStore.get()) {
      setInitialising(false);
      return;
    }

    apiClient
      .get('/auth/me')
      .then(setUser)
      .catch(() => tokenStore.clear())
      .finally(() => setInitialising(false));
  }, []);

  const adoptSession = useCallback((auth) => {
    tokenStore.set(auth.accessToken);
    setUser(auth.user);
  }, []);

  const login = useCallback(
    async (credentials) => adoptSession(await apiClient.post('/auth/login', credentials)),
    [adoptSession],
  );

  const register = useCallback(
    async (details) => adoptSession(await apiClient.post('/auth/register', details)),
    [adoptSession],
  );

  const logout = useCallback(() => {
    tokenStore.clear();
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, initialising, isAuthenticated: Boolean(user), login, register, logout }),
    [user, initialising, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}