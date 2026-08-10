import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of, throwError} from 'rxjs';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {BookManagement} from './book-management';
import {AdminBookService} from '../../../../core/services/admin-book.service';
import {BookService} from '../../../../core/services/book.service';
import {AdminAuthorService} from '../../../../core/services/admin-author.service';
import {AdminLanguageService} from '../../../../core/services/admin-language.service';
import {AdminPublisherService} from '../../../../core/services/admin-publisher.service';
import {AdminGenreService} from '../../../../core/services/admin-genre.service';
import {ToastService} from '../../../../core/services/toast.service';
import {BookDetailsDto, BookSummaryDto, PagedResponseBookSummaryDto} from '../../../../api';

/**
 * Test-only view of BookManagement's protected members, reached via a typed cast so tests can
 * drive the component with plain dot notation without widening production visibility.
 */
interface BookManagementInternals {
  form: {
    setValue(value: Record<string, unknown>): void;
    patchValue(value: Record<string, unknown>): void;
    getRawValue(): Record<string, unknown>;
    invalid: boolean;
  };

  editingId(): string | null;

  formOpen(): boolean;

  confirmingDeleteId(): string | null;

  imageInputVisible(): boolean;

  selectedImage(): File | null;

  startCreate(): void;

  startEdit(book: BookSummaryDto): void;

  onImageSelected(event: Event): void;

  cancelForm(): void;

  submit(): void;

  requestDelete(id: string): void;

  cancelDelete(): void;

  confirmDelete(id: string): void;
}

describe('BookManagement Component Tests', () => {
  let component: BookManagement;
  let fixture: ComponentFixture<BookManagement>;

  let mockBookAdminService: {
    createBook: ReturnType<typeof vi.fn>;
    updateBook: ReturnType<typeof vi.fn>;
    deleteBook: ReturnType<typeof vi.fn>;
  };
  let mockBookService: {
    getAllBooks: ReturnType<typeof vi.fn>;
    getBookById: ReturnType<typeof vi.fn>;
  };
  let mockAuthorService: { getAllAuthors: ReturnType<typeof vi.fn> };
  let mockLanguageService: { getAllLanguages: ReturnType<typeof vi.fn> };
  let mockPublisherService: { getAllPublishers: ReturnType<typeof vi.fn> };
  let mockGenreService: { getAllGenres: ReturnType<typeof vi.fn> };
  let mockToast: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };

  // --- Object Mother factories ---
  const booksPage: PagedResponseBookSummaryDto = {
    content: [{id: 'b1', title: 'Dune', authorName: 'Frank Herbert', coverImageUrl: 'https://cdn/d.jpg'}],
    totalElements: 1,
    totalPages: 1,
    pageNumber: 0,
    pageSize: 100,
    isLast: true,
  };

  const bookDetails: BookDetailsDto = {
    id: 'b1',
    title: 'Dune',
    isbn: '978-0441013593',
    pages: 412,
    yearPublished: 1965,
    summary: 'A desert planet.',
    format: 'HARDCOVER',
    author: {id: 'author-1', name: 'Frank Herbert'},
    language: {id: 'lang-1', name: 'English'},
    publisher: {id: 'pub-1', name: 'Chilton Books'},
    genres: new Set([{id: 'genre-1', name: 'Science Fiction'}]),
    coverImageUrl: 'https://cdn/d.jpg',
  };

  const lookupPage = <T>(content: T[]) => ({
    content,
    totalElements: content.length,
    totalPages: 1,
    pageNumber: 0,
    pageSize: 200,
    isLast: true
  });

  /** Fills the form with a complete, valid create payload. */
  const fillValidForm = (): void => {
    internals().form.setValue({
      title: 'Dune',
      isbn: '978-0441013593',
      pages: 412,
      yearPublished: 1965,
      summary: 'A desert planet.',
      format: 'HARDCOVER',
      authorId: 'author-1',
      languageId: 'lang-1',
      publisherId: 'pub-1',
      genreIds: ['genre-1'],
    });
  };

  /** Typed access to the component's protected members. */
  const internals = (): BookManagementInternals => component as unknown as BookManagementInternals;

  beforeEach(async () => {
    mockBookAdminService = {
      createBook: vi.fn().mockReturnValue(of(bookDetails)),
      updateBook: vi.fn().mockReturnValue(of(bookDetails)),
      deleteBook: vi.fn().mockReturnValue(of(undefined)),
    };
    mockBookService = {
      getAllBooks: vi.fn().mockReturnValue(of(booksPage)),
      getBookById: vi.fn().mockReturnValue(of(bookDetails)),
    };
    mockAuthorService = {
      getAllAuthors: vi.fn().mockReturnValue(of(lookupPage([{
        id: 'author-1',
        name: 'Frank Herbert'
      }])))
    };
    mockLanguageService = {getAllLanguages: vi.fn().mockReturnValue(of(lookupPage([{id: 'lang-1', name: 'English'}])))};
    mockPublisherService = {
      getAllPublishers: vi.fn().mockReturnValue(of(lookupPage([{
        id: 'pub-1',
        name: 'Chilton Books'
      }])))
    };
    mockGenreService = {
      getAllGenres: vi.fn().mockReturnValue(of(lookupPage([{
        id: 'genre-1',
        name: 'Science Fiction'
      }])))
    };
    mockToast = {showSuccess: vi.fn(), showError: vi.fn()};

    await TestBed.configureTestingModule({
      imports: [BookManagement],
      providers: [
        {provide: AdminBookService, useValue: mockBookAdminService},
        {provide: BookService, useValue: mockBookService},
        {provide: AdminAuthorService, useValue: mockAuthorService},
        {provide: AdminLanguageService, useValue: mockLanguageService},
        {provide: AdminPublisherService, useValue: mockPublisherService},
        {provide: AdminGenreService, useValue: mockGenreService},
        {provide: ToastService, useValue: mockToast},
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BookManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create, load the book list, and populate the lookups', () => {
    // Assert
    expect(component).toBeTruthy();
    expect(mockBookService.getAllBooks).toHaveBeenCalled();
    expect(mockAuthorService.getAllAuthors).toHaveBeenCalled();
    expect(mockLanguageService.getAllLanguages).toHaveBeenCalled();
    expect(mockPublisherService.getAllPublishers).toHaveBeenCalled();
    expect(mockGenreService.getAllGenres).toHaveBeenCalled();
  });

  it('startCreate opens the form in create mode with the image input visible', () => {
    // Act
    internals().startCreate();

    // Assert
    expect(internals().formOpen()).toBe(true);
    expect(internals().editingId()).toBeNull();
    expect(internals().imageInputVisible()).toBe(true);
  });

  it('startEdit opens edit mode, fetches details, and pre-fills every field', () => {
    // Act
    internals().startEdit({id: 'b1', title: 'Dune', authorName: 'Frank Herbert', coverImageUrl: 'https://cdn/d.jpg'});

    // Assert
    expect(internals().formOpen()).toBe(true);
    expect(internals().editingId()).toBe('b1');
    expect(internals().imageInputVisible()).toBe(false);
    expect(mockBookService.getBookById).toHaveBeenCalledWith('b1');
    expect(internals().form.getRawValue()).toEqual({
      title: 'Dune',
      isbn: '978-0441013593',
      pages: 412,
      yearPublished: 1965,
      summary: 'A desert planet.',
      format: 'HARDCOVER',
      authorId: 'author-1',
      languageId: 'lang-1',
      publisherId: 'pub-1',
      genreIds: ['genre-1'],
    });
  });

  it('startEdit surfaces an error toast when details cannot be loaded', () => {
    // Arrange
    mockBookService.getBookById.mockReturnValue(throwError(() => ({error: {detail: 'Not found'}})));

    // Act
    internals().startEdit({id: 'b1', title: 'Dune', authorName: 'Frank Herbert', coverImageUrl: undefined});

    // Assert
    expect(mockToast.showError).toHaveBeenCalledWith('Not found');
  });

  it('onImageSelected records the chosen file', () => {
    // Arrange
    const file = new File(['x'], 'cover.jpg', {type: 'image/jpeg'});
    const event = {target: {files: [file]}} as unknown as Event;

    // Act
    internals().onImageSelected(event);

    // Assert
    expect(internals().selectedImage()).toBe(file);
  });

  it('submit creates a book with the selected image, converts genreIds to a Set, and refreshes', () => {
    // Arrange
    const file = new File(['x'], 'cover.jpg', {type: 'image/jpeg'});
    internals().startCreate();
    fillValidForm();
    internals().onImageSelected({target: {files: [file]}} as unknown as Event);

    // Act
    internals().submit();

    // Assert
    expect(mockBookAdminService.createBook).toHaveBeenCalledTimes(1);
    const [dtoArg, imageArg] = mockBookAdminService.createBook.mock.calls[0];
    expect(dtoArg.title).toBe('Dune');
    expect(Array.isArray(dtoArg.genreIds)).toBe(true);
    expect(dtoArg.genreIds).toEqual(['genre-1']);
    expect(imageArg).toBe(file);
    expect(mockToast.showSuccess).toHaveBeenCalledWith('Book created.');
    expect(mockBookService.getAllBooks).toHaveBeenCalledTimes(2);
    expect(internals().formOpen()).toBe(false);
  });

  it('submit creates a book without an image when none is chosen', () => {
    // Arrange
    internals().startCreate();
    fillValidForm();

    // Act
    internals().submit();

    // Assert
    const [, imageArg] = mockBookAdminService.createBook.mock.calls[0];
    expect(imageArg).toBeUndefined();
  });

  it('submit updates a book when in edit mode', () => {
    // Arrange
    internals().startEdit({id: 'b1', title: 'Dune', authorName: 'Frank Herbert', coverImageUrl: undefined});
    internals().form.patchValue({title: 'Dune Messiah'});

    // Act
    internals().submit();

    // Assert
    expect(mockBookAdminService.updateBook).toHaveBeenCalledTimes(1);
    const [idArg, dtoArg] = mockBookAdminService.updateBook.mock.calls[0];
    expect(idArg).toBe('b1');
    expect(dtoArg.title).toBe('Dune Messiah');
    expect(Array.isArray(dtoArg.genreIds)).toBe(true);
    expect(mockToast.showSuccess).toHaveBeenCalledWith('Book updated.');
  });

  it('submit does nothing when the form is invalid', () => {
    // Arrange: open create mode but leave the form empty (invalid)
    internals().startCreate();

    // Act
    internals().submit();

    // Assert
    expect(mockBookAdminService.createBook).not.toHaveBeenCalled();
    expect(mockBookAdminService.updateBook).not.toHaveBeenCalled();
  });

  it('submit surfaces an error toast on create failure', () => {
    // Arrange
    mockBookAdminService.createBook.mockReturnValue(throwError(() => ({error: {detail: 'Bad reference'}})));
    internals().startCreate();
    fillValidForm();

    // Act
    internals().submit();

    // Assert
    expect(mockToast.showError).toHaveBeenCalledWith('Bad reference');
  });

  it('requestDelete then confirmDelete deletes and refreshes', () => {
    // Act
    internals().requestDelete('b1');

    // Assert intermediate state
    expect(internals().confirmingDeleteId()).toBe('b1');

    // Act
    internals().confirmDelete('b1');

    // Assert
    expect(mockBookAdminService.deleteBook).toHaveBeenCalledWith('b1');
    expect(mockToast.showSuccess).toHaveBeenCalledWith('Book deleted.');
    expect(internals().confirmingDeleteId()).toBeNull();
    expect(mockBookService.getAllBooks).toHaveBeenCalledTimes(2);
  });

  it('confirmDelete surfaces an error toast on failure', () => {
    // Arrange
    mockBookAdminService.deleteBook.mockReturnValue(throwError(() => ({error: {detail: 'In use'}})));

    // Act
    internals().requestDelete('b1');
    internals().confirmDelete('b1');

    // Assert
    expect(mockToast.showError).toHaveBeenCalledWith('In use');
  });

  it('cancelDelete clears the pending confirmation', () => {
    // Act
    internals().requestDelete('b1');
    internals().cancelDelete();

    // Assert
    expect(internals().confirmingDeleteId()).toBeNull();
    expect(mockBookAdminService.deleteBook).not.toHaveBeenCalled();
  });
});
