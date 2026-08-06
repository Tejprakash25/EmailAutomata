import { env } from '@/config/env';

/**
 * Error carrying the server's machine-readable failure code, so callers can
 * branch on `code` instead of pattern-matching on message strings.
 */
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
 * Every call in the app goes through here — one unwrapping path, one error path.
 *
 * @param {string} path   endpoint path relative to the API base, e.g. '/meta'
 * @param {RequestInit} options
 * @returns {Promise<unknown>} the unwrapped `data` payload
 */
export async function request(path, options = {}) {
  let response;

  try {
    response = await fetch(`${env.apiBaseUrl}${path}`, {
      headers: { 'Content-Type': 'application/json', ...(options.headers ?? {}) },
      ...options,
    });
  } catch {
    throw new ApiRequestError('Cannot reach the EmailAutomata service.', {
      code: 'NETWORK_UNREACHABLE',
    });
  }

  let envelope = null;
  try {
    envelope = await response.json();
  } catch {
    // Non-JSON body (proxy error page, gateway timeout) — handled below.
  }

  if (!response.ok || !envelope?.success) {
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
};
