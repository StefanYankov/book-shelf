/**
 * Lightweight pagination/sorting query used by the core facade services.
 * Replaces the previously generated `Pageable` model, which no longer exists after
 * the API flattened pagination into discrete `page` / `size` / `sort` query parameters.
 * The facades unpack this into those positional arguments when calling the generated client.
 */
export interface PageQuery {
  /** Zero-based page index. */
  page?: number;
  /** Page size. */
  size?: number;
  /** Sorting criteria, each in the form `property,(asc|desc)`. */
  sort?: string[];
}
