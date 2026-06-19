import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { BookAPIService, BookDetailsDto, PageBookSummaryDto } from '../../api';

/**
 * @Injectable
 * Service for managing book-related data interactions with the backend API.
 *
 * This service acts as a facade over the auto-generated BookAPIService,
 * providing a clean and simplified interface for the application's components.
 * It encapsulates the logic for API calls, data mapping, and error handling.
 */
@Injectable({
  providedIn: 'root'
})
export class BookService {
  private bookApi = inject(BookAPIService);

  /**
   * Retrieves a paginated list of all books.
   *
   * @param page The page number to retrieve (0-indexed).
   * @param size The number of items per page.
   * @returns An Observable emitting a paginated result of book summaries.
   */
  getAllBooks(page: number, size: number): Observable<PageBookSummaryDto> {
    return this.bookApi.getAllBooks({ page, size });
  }

  /**
   * Searches for books based on a query string.
   *
   * @param query The search term to filter books by title or author.
   * @param page The page number to retrieve (0-indexed).
   * @param size The number of items per page.
   * @returns An Observable emitting a paginated result of book summaries.
   */
  searchBooks(query: string, page: number, size: number): Observable<PageBookSummaryDto> {
    return this.bookApi.searchBooks({ page, size }, query);
  }

  /**
   * Retrieves the details for a single book by its ID.
   *
   * @param id The UUID of the book.
   * @returns An Observable emitting the detailed book information.
   */
  getBookById(id: string): Observable<BookDetailsDto> {
    return this.bookApi.getBookById(id);
  }
}
