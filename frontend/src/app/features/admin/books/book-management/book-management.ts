import {Component, computed, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {toSignal} from '@angular/core/rxjs-interop';
import {startWith, Subject, switchMap} from 'rxjs';
import {AdminBookService} from '../../../../core/services/admin-book.service';
import {BookService} from '../../../../core/services/book.service';
import {AdminAuthorService} from '../../../../core/services/admin-author.service';
import {AdminLanguageService} from '../../../../core/services/admin-language.service';
import {AdminPublisherService} from '../../../../core/services/admin-publisher.service';
import {AdminGenreService} from '../../../../core/services/admin-genre.service';
import {ToastService} from '../../../../core/services/toast.service';
import {BookCreateDto, BookSummaryDto, BookUpdateDto, PagedResponseBookSummaryDto,} from '../../../../api';
import {BOOK_FORMAT_DISPLAY_LABELS, BookFormat} from '../../../../core/models/book-format.enum';

/**
 * Administrative management page for books: a paginated list with an inline create/edit form
 * and a per-row delete confirmation. Creation accepts an optional cover image; author, language,
 * publisher, and genres are chosen from existing records. All mutations refresh the list and
 * surface a toast.
 */
@Component({
  selector: 'app-book-management',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './book-management.html',
  styleUrl: './book-management.css',
})
export class BookManagement {
  private static readonly LOOKUP_PAGE = {page: 0, size: 200};

  /** The id of the book being edited, or null when the form is in create mode. */
  protected readonly editingId = signal<string | null>(null);
  /** Whether the create/edit form is visible. */
  protected readonly formOpen = signal(false);
  /** The id awaiting delete confirmation, or null. */
  protected readonly confirmingDeleteId = signal<string | null>(null);
  /** The selected cover image file for a create, or null. Editing does not change the image. */
  protected readonly selectedImage = signal<File | null>(null);
  /** The form heading, reflecting create vs edit mode. */
  protected readonly formTitle = computed(() => (this.editingId() === null ? 'Create Book' : 'Edit Book'));
  /** Whether the image input is shown (create mode only). */
  protected readonly imageInputVisible = computed(() => this.editingId() === null);

  /** Exposes the BookFormat enum values and labels to the template. */
  protected readonly bookFormats = Object.values(BookFormat);
  protected readonly formatLabels = BOOK_FORMAT_DISPLAY_LABELS;

  private readonly bookAdminService = inject(AdminBookService);
  private readonly bookService = inject(BookService);
  private readonly authorService = inject(AdminAuthorService);
  /** Lookup lists that populate the form dropdowns. */
  protected readonly authors = toSignal(this.authorService.getAllAuthors(BookManagement.LOOKUP_PAGE));
  private readonly languageService = inject(AdminLanguageService);
  protected readonly languages = toSignal(this.languageService.getAllLanguages(BookManagement.LOOKUP_PAGE));
  private readonly publisherService = inject(AdminPublisherService);
  protected readonly publishers = toSignal(this.publisherService.getAllPublishers(BookManagement.LOOKUP_PAGE));
  private readonly genreService = inject(AdminGenreService);
  protected readonly genres = toSignal(this.genreService.getAllGenres(BookManagement.LOOKUP_PAGE));
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);
  protected readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
    isbn: [''],
    pages: [null as number | null, [Validators.required, Validators.min(1)]],
    yearPublished: [null as number | null, [Validators.required]],
    summary: ['', [Validators.required, Validators.maxLength(2000)]],
    format: ['' as BookFormat | '', [Validators.required]],
    authorId: ['', [Validators.required]],
    languageId: ['', [Validators.required]],
    publisherId: ['', [Validators.required]],
    genreIds: [[] as string[], [Validators.required]],
  });
  private readonly refresh$ = new Subject<void>();
  /** The current page of book summaries, refreshed after each mutation. */
  protected readonly books = toSignal(
    this.refresh$.pipe(
      startWith(null),
      switchMap(() => this.bookService.getAllBooks(0, 100))
    ),
    {initialValue: {} as PagedResponseBookSummaryDto}
  );

  /** Opens the form in create mode. */
  protected startCreate(): void {
    this.editingId.set(null);
    this.form.reset({genreIds: []});
    this.selectedImage.set(null);
    this.formOpen.set(true);
  }

  /**
   * Opens the form in edit mode. The list uses the lean BookSummaryDto, so the full details are
   * fetched to populate every field, including the referenced author, language, publisher, genres,
   * and format.
   */
  protected startEdit(book: BookSummaryDto): void {
    if (!book.id) {
      return;
    }
    this.editingId.set(book.id);
    this.selectedImage.set(null);
    this.formOpen.set(true);

    this.bookService.getBookById(book.id).subscribe({
      next: (details) => this.form.setValue({
        title: details.title ?? '',
        isbn: details.isbn ?? '',
        pages: details.pages ?? null,
        yearPublished: details.yearPublished ?? null,
        summary: details.summary ?? '',
        format: (details.format as BookFormat) ?? '',
        authorId: details.author?.id ?? '',
        languageId: details.language?.id ?? '',
        publisherId: details.publisher?.id ?? '',
        genreIds: Array.from(details.genres ?? []).map(g => g.id!).filter(Boolean),
      }),
      error: (err) => this.toast.showError(err.error?.detail || 'Failed to load book details.'),
    });
  }

  /** Records the chosen cover image file from the file input. */
  protected onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedImage.set(input.files?.[0] ?? null);
  }

  /** Closes and resets the form. */
  protected cancelForm(): void {
    this.formOpen.set(false);
    this.editingId.set(null);
    this.selectedImage.set(null);
    this.form.reset({genreIds: []});
  }

  /** Creates or updates a book depending on the current mode. */
  protected submit(): void {
    if (this.form.invalid) {
      return;
    }
    const value = this.form.getRawValue();
    const id = this.editingId();

    // The generated DTO types genreIds as Set<string>, but a JS Set does not serialize to a JSON
    // array (JSON.stringify(new Set([...])) yields "{}"). The runtime value must therefore be a
    // plain array, cast here to satisfy the generated type while producing a valid JSON payload.
    const genreIds = value.genreIds as unknown as Set<string>;

    if (id === null) {
      const createDto: BookCreateDto = {
        title: value.title,
        isbn: value.isbn || undefined,
        pages: value.pages!,
        yearPublished: value.yearPublished!,
        summary: value.summary,
        format: value.format as BookFormat,
        authorId: value.authorId,
        languageId: value.languageId,
        publisherId: value.publisherId,
        genreIds,
      };
      this.bookAdminService.createBook(createDto, this.selectedImage() ?? undefined).subscribe({
        next: () => this.onMutationSuccess('Book created.'),
        error: (err) => this.toast.showError(err.error?.detail || 'Failed to save the book.'),
      });
    } else {
      const updateDto: BookUpdateDto = {
        title: value.title,
        isbn: value.isbn || undefined,
        pages: value.pages!,
        yearPublished: value.yearPublished!,
        summary: value.summary,
        format: value.format as BookFormat,
        authorId: value.authorId,
        languageId: value.languageId,
        publisherId: value.publisherId,
        genreIds,
      };
      this.bookAdminService.updateBook(id, updateDto).subscribe({
        next: () => this.onMutationSuccess('Book updated.'),
        error: (err) => this.toast.showError(err.error?.detail || 'Failed to save the book.'),
      });
    }
  }

  /** Marks a book for delete confirmation. */
  protected requestDelete(id: string): void {
    this.confirmingDeleteId.set(id);
  }

  /** Cancels a pending delete confirmation. */
  protected cancelDelete(): void {
    this.confirmingDeleteId.set(null);
  }

  /** Deletes the confirmed book and refreshes the list. */
  protected confirmDelete(id: string): void {
    this.bookAdminService.deleteBook(id).subscribe({
      next: () => {
        this.toast.showSuccess('Book deleted.');
        this.confirmingDeleteId.set(null);
        this.refresh$.next();
      },
      error: (err) => this.toast.showError(err.error?.detail || 'Failed to delete the book.'),
    });
  }

  /** Shared success path for create and update: toast, close the form, refresh the list. */
  private onMutationSuccess(message: string): void {
    this.toast.showSuccess(message);
    this.cancelForm();
    this.refresh$.next();
  }
}
