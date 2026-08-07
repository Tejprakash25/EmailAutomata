import { env } from '@/config/env';

const TOKEN_KEY = 'emailautomata.accessToken';

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

export class ApiRequestError extends Error {
  constructor(message, { code = 'UNKNOWN_ERROR', status = 0, details = null } = {}) {
    super(message);
    this.name = 'ApiRequestError';
    this.code = code;
    this.status = status;
    this.details = details;
  }
}

/**
 * Thin fetch wrapper that understands the EmailAutomata response envelope.
 * Attaches the bearer token when one is held, and clears it on a 401 so a
 * stale session cannot loop the app through repeated failures.
 */
export async function request(path, options = {}) {
  const token = tokenStore.get();
  let response;

  try {
    response = await fetch(`${env.apiBaseUrl}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.headers ?? {}),
      },
    });
  } catch {
    throw new ApiRequestError('Cannot reach the EmailAutomata service.', {
      code: 'NETWORK_UNREACHABLE',
    });
  }

  if (response.status === 204) return null;

  let envelope = null;
  try {
    envelope = await response.json();
  } catch {
    // Non-JSON body — handled below.
  }

  if (!response.ok || !envelope?.success) {
    if (response.status === 401) tokenStore.clear();

    throw new ApiRequestError(
      envelope?.error?.message ?? 'The request could not be completed.',
      {
        code: envelope?.error?.code ?? 'UNEXPECTED_RESPONSE',
        status: response.status,
        details: envelope?.error?.details ?? null,
      },
    );
  }

  return envelope.data;
}

export const apiClient = {
  get: (path) => request(path, { method: 'GET' }),
  post: (path, body) => request(path, { method: 'POST', body: JSON.stringify(body) }),
  put: (path, body) => request(path, { method: 'PUT', body: JSON.stringify(body) }),
  patch: (path, body) => request(path, { method: 'PATCH', body: JSON.stringify(body) }),
  delete: (path) => request(path, { method: 'DELETE' }),
};