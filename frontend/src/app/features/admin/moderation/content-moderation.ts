import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AdminModerationAPIService, BookAPIService, BookUpdateDto, PagedResponseBookSummaryDto, BookSummaryDto, BookDetailsDto } from '../../../api';
import { toSignal } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged, startWith, switchMap } from 'rxjs/operators';
import {ToastService} from '../../../core/services/toast.service';

@Component({
  selector: 'app-content-moderation',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './content-moderation.html',
  styleUrl: './content-moderation.css',
})
export class ContentModeration {
  private readonly moderationApiService = inject(AdminModerationAPIService);
  private readonly bookApiService = inject(BookAPIService);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  protected activeTab = signal<'BOOKS' | 'SHELVES'>('BOOKS');
  protected moderationForm = signal<{ id: string, title: string, summary: string } | null>(null);

  // --- Search Functionality ---
  protected searchControl = this.fb.control('');
  protected searchResults = toSignal(
    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      startWith(''),
      switchMap(query => this.bookApiService.searchBooks({ page: 0, size: 10 }, query || undefined))
    ),
    { initialValue: { content: [], totalElements: 0, totalPages: 0, pageNumber: 0, pageSize: 10, isLast: true } as PagedResponseBookSummaryDto }
  );

  /**
   * Sets up a book for moderation by populating the form.
   * Fetches full details to retrieve the existing summary.
   * @param book The book summary to be moderated.
   */
  protected selectBookForModeration(book: BookSummaryDto): void {
    if (!book.id || !book.title) {
      this.toast.showError('Cannot moderate a book with missing ID or title.');
      return;
    }

    this.bookApiService.getBookById(book.id).subscribe({
      next: (details: BookDetailsDto) => {
        this.moderationForm.set({
          id: details.id!,
          title: details.title!,
          summary: details.summary || ''
        });
      },
      error: () => this.toast.showError('Failed to fetch book details for moderation.')
    });
  }

  /**
   * Submits the moderation update for a book.
   */
  protected submitBookModeration(): void {
    const form = this.moderationForm();
    if (!form) return;

    const dto: BookUpdateDto = { title: form.title, summary: form.summary };

    this.moderationApiService.moderateBook(form.id, dto).subscribe({
      next: (updatedBook: BookDetailsDto) => {
        this.toast.showSuccess(`Book "${updatedBook.title}" moderated successfully.`);
        this.moderationForm.set(null);
        this.searchControl.setValue(this.searchControl.value);
      },
      error: () => this.toast.showError('Book moderation action failed.')
    });
  }

  /**
   * Cancels the current moderation action.
   */
  protected cancelModeration(): void {
    this.moderationForm.set(null);
  }
}
