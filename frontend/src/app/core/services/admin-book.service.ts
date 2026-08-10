import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AdminBookAPIService, BookCreateDto, BookDetailsDto, BookUpdateDto,} from '../../api';

/**
 * Administrative facade over the generated `AdminBookAPIService`.
 * Restores a precise `void` return type on the generated delete call (typed `any`),
 * and exposes the multipart create (book part plus an optional cover image) as a single method.
 */
@Injectable({providedIn: 'root'})
export class AdminBookService {
  private readonly bookApi = inject(AdminBookAPIService);

  /**
   * Creates a book, with an optional cover image.
   * @param dto   The book creation payload.
   * @param image The optional cover image file.
   * @returns An observable emitting the created book's details.
   */
  createBook(dto: BookCreateDto, image?: Blob): Observable<BookDetailsDto> {
    return this.bookApi.createBook(dto, image);
  }

  /**
   * Partially updates an existing book.
   * @param id  The UUID of the book.
   * @param dto The fields to update.
   * @returns An observable emitting the updated book's details.
   */
  updateBook(id: string, dto: BookUpdateDto): Observable<BookDetailsDto> {
    return this.bookApi.updateBook(id, dto);
  }

  /**
   * Deletes a book.
   * @param id The UUID of the book.
   * @returns An observable that completes when the delete succeeds.
   */
  deleteBook(id: string): Observable<void> {
    return this.bookApi.deleteBook(id) as Observable<void>;
  }
}
