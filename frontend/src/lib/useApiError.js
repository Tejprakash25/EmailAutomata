import { useCallback, useState } from 'react';

/**
 * Holds the failure state of one API interaction.
 *
 * <p>Splits a rejected ApiRequestError into the two things a form needs: a
 * banner message, and a field-to-message map that inputs can look themselves
 * up in. Every form in the app uses this, so validation display is consistent
 * without each screen re-implementing it.</p>
 */
export function useApiError() {
  const [error, setError] = useState(null);

  const capture = useCallback((caught) => {
    setError({
      code: caught?.code ?? 'UNKNOWN_ERROR',
      message: caught?.message ?? 'Something went wrong.',
      fields: caught?.details ?? {},
    });
  }, []);

  const clear = useCallback(() => setError(null), []);

  return {
    error,
    capture,
    clear,
    /** Field-level message for `name`, or undefined if that field is fine. */
    fieldError: (name) => error?.fields?.[name],
    /**
     * Banner text — suppressed when every message is already attached to a
     * field, so the user never reads the same complaint twice.
     */
    bannerMessage:
      error && Object.keys(error.fields ?? {}).length === 0 ? error.message : null,
  };
}