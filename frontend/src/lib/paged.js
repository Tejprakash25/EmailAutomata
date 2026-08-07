/**
 * Spring's Page<T> shape, normalised to what the UI actually consumes.
 * Isolates every list screen from the server's pagination envelope, so a
 * change there touches one file rather than every list.
 */
export function readPage(page) {
  return {
    items: page?.content ?? [],
    page: page?.number ?? 0,
    size: page?.size ?? 0,
    totalItems: page?.totalElements ?? 0,
    totalPages: page?.totalPages ?? 0,
    isFirst: page?.first ?? true,
    isLast: page?.last ?? true,
    isEmpty: (page?.content ?? []).length === 0,
  };
}