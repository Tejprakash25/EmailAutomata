/**
 * Single source of truth for build-time configuration.
 * Components never touch import.meta.env directly.
 */
export const env = Object.freeze({
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  isDev: import.meta.env.DEV,
});
