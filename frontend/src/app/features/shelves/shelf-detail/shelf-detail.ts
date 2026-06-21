import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BookshelfService } from '../../../core/services/bookshelf.service';
import { ToastService } from '../../../core/services/toast.service';
import { switchMap, Subject, startWith, map } from 'rxjs';

@Component({
  selector: 'app-shelf-detail',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './shelf-detail.html',
  styleUrl: './shelf-detail.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ShelfDetail {
  private readonly bookshelfService = inject(BookshelfService);
  private readonly toastService = inject(ToastService);
  private readonly route = inject(ActivatedRoute);
  private readonly shelfId$ = this.route.paramMap.pipe(
    map(params => params.get('id')!)
  );

  shelf = toSignal(
    this.shelfId$.pipe(
      switchMap(id => this.bookshelfService.getShelfById(id))
    ),
    { initialValue: null }
  );

  // --- Books in Shelf State Management ---
  private readonly refreshBooks$ = new Subject<void>();

  books = toSignal(
    this.shelfId$.pipe(
      switchMap(id => this.refreshBooks$.pipe(
        startWith(null),
        switchMap(() => this.bookshelfService.getBooksInShelf(id, { page: 0, size: 20 }))
      ))
    ),
    { initialValue: null }
  );

  /**
   * Removes a book from the current shelf and triggers a refresh of the book list.
   * @param bookId The UUID of the book to remove.
   */
  removeBook(bookId: string) {
    const currentShelfId = this.route.snapshot.paramMap.get('id');
    if (!currentShelfId) return;

    this.bookshelfService.removeBookFromShelf(currentShelfId, bookId).subscribe({
      next: () => {
        this.toastService.showSuccess('Book removed from shelf.');
        this.refreshBooks$.next();
      },
      error: (err) => {
        this.toastService.showError(err.error?.detail || 'Failed to remove book.');
      }
    });
  }
}
