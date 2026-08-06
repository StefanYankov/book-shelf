import {Component, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {
  BookAPIService,
  BookDetailsDto,
  BookSummaryDto,
  BookUpdateDto,
  ModerationAPIService,
  PagedResponseBookSummaryDto
} from '../../../api';
import {toSignal} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, startWith, switchMap} from 'rxjs/operators';
import {ToastService} from '../../../core/services/toast.service';

/**
 * Book moderation page for users holding the MODERATE_BOOKS permission (non-administrators).
 * Provides only book curation — shelf moderation and deletion remain in the administrator-only
 * content moderation view. Kept separate from that admin view so each surface exposes exactly the
 * capabilities its audience is authorized for.
 */
@Component({
  selector: 'app-book-moderation',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './book-moderation.html',
  styleUrl: './book-moderation.css',
})
export class BookModeration {
  protected moderationForm = signal<{ id: string, title: string, summary: string } | null>(null);
  private readonly moderationApiService = inject(ModerationAPIService);
  private readonly bookApiService = inject(BookAPIService);
  protected searchResults = toSignal(
    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      startWith(''),
      switchMap(query => this.bookApiService.searchBooks({page: 0, size: 10}, query || undefined))
    ),
    {
      initialValue: {
        content: [],
        totalElements: 0,
        totalPages: 0,
        pageNumber: 0,
        pageSize: 10,
        isLast: true
      } as PagedResponseBookSummaryDto
    }
  );
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);
  // --- Search Functionality ---
  protected searchControl = this.fb.control('');

  /**
   * Loads a book's full details into the moderation form.
   * @param book The book summary chosen from the search results.
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
   * Submits the moderated title and summary for the selected book.
   */
  protected submitBookModeration(): void {
    const form = this.moderationForm();
    if (!form) return;

    const dto: BookUpdateDto = {title: form.title, summary: form.summary};

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
