import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, startWith, switchMap } from 'rxjs/operators';
import { BookAPIService, Pageable, PageBookSummaryDto, PagedResponseBookshelfSummaryDto } from '../../../api';
import { BookshelfService } from '../../../core/services/bookshelf.service';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';
import { BookFormat } from '../../../core/models/book-format.enum';

/**
 * Component for browsing, searching, and filtering the book catalog.
 * Built with a fully Signal-driven reactive query pipeline.
 */
@Component({
  selector: 'app-book-list',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './book-list.html',
  styleUrls: ['./book-list.css']
})
export class BookList {
  private readonly bookApiService = inject(BookAPIService);
  private readonly bookshelfService = inject(BookshelfService);
  private readonly toastService = inject(ToastService);
  protected readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  /** Empty page used as a safe fallback for guests and on search failure. */
  private static readonly EMPTY_BOOK_PAGE: PageBookSummaryDto = {
    content: [],
    totalPages: 0,
    number: 0,
    totalElements: 0,
  };

  /** Exposes the BookFormat enum to the template. */
  protected readonly BookFormat = BookFormat;
  /** Exposes the keys of the BookFormat enum to the template for iteration. */
  protected readonly bookFormatKeys = Object.keys(BookFormat);

  /** Reactive form group for all search and filter controls. */
  protected readonly searchForm = this.fb.nonNullable.group({
    query: [''],
    format: ['' as BookFormat | ''],
    yearMin: [null as number | null],
    yearMax: [null as number | null]
  });

  /** Signal to manage the visibility of the advanced filter panel. */
  protected readonly isFilterPanelHidden = signal(true);

  /** Declarative signal representing the dynamic, paginated search results. */
  protected readonly booksResponse = toSignal(
    this.searchForm.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged((prev, curr) => JSON.stringify(prev) === JSON.stringify(curr)),
      startWith(this.searchForm.getRawValue()),
      switchMap(filters => {
        const pageable: Pageable = { page: 0, size: 20 };
        return this.bookApiService.searchBooks(
          pageable,
          filters.query || undefined,
          new Set<string>(), // Native Set alignment
          filters.format || undefined,
          filters.yearMin || undefined,
          filters.yearMax || undefined
        ).pipe(
          // Degrade gracefully: a failed search shows "no results" instead of
          // leaving the signal undefined forever (a permanent "Loading..." hang).
          catchError(err => {
            this.toastService.showError(err.error?.detail || 'Failed to load the book catalog.');
            return of(BookList.EMPTY_BOOK_PAGE);
          })
        );
      })
    )
  );

  /**
   * Signal containing the current user's bookshelves for the 'Add to Shelf' dropdown.
   * Only fetched for authenticated users; guests get an empty page so no protected
   * endpoint is called on their behalf.
   */
  protected readonly userShelves = toSignal(
    this.authService.isLoggedIn()
      ? this.bookshelfService.getShelvesForUser({ page: 0, size: 100 })
      : of({ content: [], pageNumber: 0, pageSize: 100, totalElements: 0, totalPages: 0, isLast: true } as PagedResponseBookshelfSummaryDto)
  );

  /** Toggles the visibility of the advanced filter panel. */
  protected toggleFilterPanel(): void {
    this.isFilterPanelHidden.update(hidden => !hidden);
  }

  /**
   * Adds a book to a user's shelf.
   * Exposes public visibility to grant unit testing suites execution rights.
   *
   * @param bookId The ID of the book to add.
   * @param shelfId The ID of the target shelf.
   */
  public addToShelf(bookId: string, shelfId: string): void {
    if (!bookId || !shelfId) {
      return;
    }
    this.bookshelfService.addBookToShelf(shelfId, { bookId }).subscribe({
      next: () => {
        this.toastService.showSuccess('Book added to shelf successfully!');
      },
      error: (err) => {
        this.toastService.showError(err.error?.detail || 'Failed to add book to shelf.');
      }
    });
  }
}
