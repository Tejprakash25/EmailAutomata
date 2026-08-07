/**
 * Client mirror of the backend PlaceholderExtractor.
 *
 * The pattern is kept byte-for-byte identical to the server's so the editor's
 * live preview matches exactly what the server will persist. If one side
 * changes, both must — this is the deliberate cost of a real-time preview.
 */
const TOKEN = /\{\{\s*([a-zA-Z][a-zA-Z0-9_]*)\s*\}\}/g;

export const PlaceholderExtractor = {
  extract(...fragments) {
    const found = new Set();
    for (const fragment of fragments) {
      if (!fragment) continue;
      for (const match of fragment.matchAll(TOKEN)) {
        found.add(match[1]);
      }
    }
    return [...found];
  },
};