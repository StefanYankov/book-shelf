import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AddBookToBookshelfDto,
  BookshelfCreateDto,
  BookshelfDetailsDto,
  BookshelfUpdateDto,
  Pageable,
  PagedResponseBookshelfSummaryDto,
  PagedResponseBookSummaryDto,
  UserShelfAPIService,
} from '../../api';

/**
 * Service responsible for managing user bookshelves.
 * Acts as a facade over the generated OpenAPI client (`UserShelfAPIService`).
 */
@Injectable({ providedIn: 'root' })
export class BookshelfService {
  private readonly userShelfApiService = inject(UserShelfAPIService);

  /**
   * Retrieves a paginated list of bookshelves belonging to the currently authenticated user.
   * @param pageable Pagination and sorting configuration.
   * @returns An observable emitting a paginated result of bookshelf summaries.
   */
  getShelvesForUser(pageable: Pageable): Observable<PagedResponseBookshelfSummaryDto> {
    return this.userShelfApiService.getUserShelves(pageable);
  }

  /**
   * Creates a new bookshelf for the currently authenticated user.
   * @param createDto Data required to create the bookshelf.
   * @returns An observable emitting the detailed view of the newly created bookshelf.
   */
  createShelf(createDto: BookshelfCreateDto): Observable<BookshelfDetailsDto> {
    return this.userShelfApiService.createShelf(createDto);
  }

  /**
   * Retrieves the detailed view of a specific bookshelf.
   * @param shelfId The UUID of the bookshelf to retrieve.
   * @returns An observable emitting the detailed bookshelf data.
   */
  getShelfById(shelfId: string): Observable<BookshelfDetailsDto> {
    return this.userShelfApiService.getShelfById(shelfId);
  }

  /**
   * Retrieves a paginated list of books contained within a specific bookshelf.
   * @param shelfId The UUID of the bookshelf.
   * @param pageable Pagination and sorting configuration.
   * @returns An observable emitting a paginated result of book summaries.
   */
  getBooksInShelf(shelfId: string, pageable: Pageable): Observable<PagedResponseBookSummaryDto> {
    return this.userShelfApiService.getBooksInShelf(shelfId, pageable);
  }

  /**
   * Updates an existing bookshelf.
   * @param shelfId The UUID of the bookshelf to update.
   * @param updateDto Data required to update the bookshelf.
   * @returns An observable emitting the updated detailed view of the bookshelf.
   */
  updateShelf(shelfId: string, updateDto: BookshelfUpdateDto): Observable<BookshelfDetailsDto> {
    return this.userShelfApiService.updateShelf(shelfId, updateDto);
  }

  /**
   * Deletes a specific bookshelf.
   * @param shelfId The UUID of the bookshelf to delete.
   * @returns An observable that completes when the deletion is successful.
   */
  deleteShelf(shelfId: string): Observable<void> {
    return this.userShelfApiService.deleteShelf(shelfId);
  }

  /**
   * Adds a book to a specific bookshelf.
   * @param shelfId The UUID of the bookshelf.
   * @param addBookDto Data containing the ID of the book to add.
   * @returns An observable that completes when the book is added successfully.
   */
  addBookToShelf(shelfId: string, addBookDto: AddBookToBookshelfDto): Observable<void> {
    return this.userShelfApiService.addBookToShelf(shelfId, addBookDto);
  }

  /**
   * Removes a book from a specific bookshelf.
   * @param shelfId The UUID of the bookshelf.
   * @param bookId The UUID of the book to remove.
   * @returns An observable that completes when the book is removed successfully.
   */
  removeBookFromShelf(shelfId: string, bookId: string): Observable<void> {
    return this.userShelfApiService.removeBookFromShelf(shelfId, bookId);
  }
}
