import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, startWith, switchMap, of } from 'rxjs';
import { BookService } from '../../../core/services/book.service';
import { BookshelfService } from '../../../core/services/bookshelf.service';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';
import { PageBookSummaryDto, AddBookToBookshelfDto, PageBookshelfSummaryDto } from '../../../api';

@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './book-list.html',
  styleUrls: ['./book-list.css']
})
export class BookList {
  private readonly bookService = inject(BookService);
  private readonly bookshelfService = inject(BookshelfService);
  private readonly toastService = inject(ToastService);
  public readonly authService = inject(AuthService);

  // --- Book Search State ---
  searchControl = new FormControl('');
  private books$ = this.searchControl.valueChanges.pipe(
    startWith(''),
    debounceTime(300),
    distinctUntilChanged(),
    switchMap(query => this.bookService.searchBooks(query || '', 0, 20))
  );
  books = toSignal(this.books$, {
    initialValue: { content: [], totalPages: 0, number: 0 } as PageBookSummaryDto
  });

  // --- User Shelves State ---
  // Only fetch shelves if the user is logged in
  userShelves = toSignal(
    this.authService.isLoggedIn()
      ? this.bookshelfService.getShelvesForUser({ page: 0, size: 100 })
      : of({ content: [], totalPages: 0, number: 0, totalElements: 0 } as PageBookshelfSummaryDto)
  );

  /**
   * Adds a book to the selected shelf.
   * @param bookId The UUID of the book to add.
   * @param shelfId The UUID of the target shelf.
   */
  addToShelf(bookId: string, shelfId: string) {
    if (!bookId || !shelfId) return;

    const addBookDto: AddBookToBookshelfDto = { bookId };
    this.bookshelfService.addBookToShelf(shelfId, addBookDto).subscribe({
      next: () => {
        this.toastService.showSuccess('Book added to shelf successfully!');
      },
      error: (err) => {
        this.toastService.showError(err.error?.detail || 'Failed to add book to shelf.');
      }
    });
  }
}
