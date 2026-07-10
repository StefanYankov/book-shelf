// NOTE: This is a manual copy of the backend enum for template convenience, avodin the overhead needed for the `npm run generate:api` to generate it automatically . Keep in sync!
export enum BookFormat {
  PAPERBACK = 'PAPERBACK',
  HARDCOVER = 'HARDCOVER',
  DIGITAL = 'DIGITAL'
}

// Map database values to UI display strings
export const BOOK_FORMAT_DISPLAY_LABELS: Record<BookFormat, string> = {
  [BookFormat.PAPERBACK]: 'Paperback',
  [BookFormat.HARDCOVER]: 'Hardcover',
  [BookFormat.DIGITAL]: 'E-Book / Digital'
};
