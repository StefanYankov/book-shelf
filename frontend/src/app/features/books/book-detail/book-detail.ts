import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { switchMap, of } from 'rxjs';
import { BookAPIService } from '../../../api';
import { UserShelfAPIService } from '../../../api';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';
import { AddBookToBookshelfDto, PagedResponseBookshelfSummaryDto } from '../../../api';

@Component({
  selector: 'app-book-detail',
  imports: [CommonModule],
  templateUrl: './book-detail.html',
  styleUrls: ['./book-detail.css'],
})
export class BookDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly bookApiService = inject(BookAPIService);
  private readonly userShelfApiService = inject(UserShelfAPIService);
  private readonly toastService = inject(ToastService);
  public readonly authService = inject(AuthService);

  book = toSignal(
    this.route.paramMap.pipe(
      switchMap(params => this.bookApiService.getBookById(params.get('id')!))
    )
  );

  // --- User Shelves State ---
  userShelves = toSignal(
    this.authService.isLoggedIn()
      ? this.userShelfApiService.getUserShelves({ page: 0, size: 100 })
      : of({ content: [], totalPages: 0, pageNumber: 0, totalElements: 0, pageSize: 20, isLast: true } as PagedResponseBookshelfSummaryDto)
  );

  /**
   * Adds a book to the selected shelf.
   * @param bookId The UUID of the book to add.
   * @param shelfId The UUID of the target shelf.
   */
  addToShelf(bookId: string, shelfId: string) {
    if (!bookId || !shelfId) return;

    const addBookDto: AddBookToBookshelfDto = { bookId };
    this.userShelfApiService.addBookToShelf(shelfId, addBookDto).subscribe({
      next: () => {
        this.toastService.showSuccess('Book added to shelf successfully!');
      },
      error: (err) => {
        this.toastService.showError(err.error?.detail || 'Failed to add book to shelf.');
      }
    });
  }
}
