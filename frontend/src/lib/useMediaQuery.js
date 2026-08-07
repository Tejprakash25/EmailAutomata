import { useEffect, useState } from 'react';

/**
 * Subscribes to a CSS media query and re-renders on change. The one primitive
 * the app uses to make layout decisions in JS, so "is this mobile?" is answered
 * the same way everywhere.
 */
export function useMediaQuery(query) {
  const [matches, setMatches] = useState(
    () => typeof window !== 'undefined' && window.matchMedia(query).matches,
  );

  useEffect(() => {
    const mql = window.matchMedia(query);
    const onChange = (e) => setMatches(e.matches);
    mql.addEventListener('change', onChange);
    setMatches(mql.matches);
    return () => mql.removeEventListener('change', onChange);
  }, [query]);

  return matches;
}

/** Shared breakpoint. Below this the app switches to single-column layouts. */
export const MOBILE_QUERY = '(max-width: 720px)';