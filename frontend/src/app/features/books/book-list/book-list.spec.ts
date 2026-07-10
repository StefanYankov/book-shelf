import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi, describe, it, expect, beforeEach, afterEach, Mock } from 'vitest';
import { BookList } from './book-list';
import { BookAPIService, PageBookSummaryDto } from '../../../api';
import { BookshelfService } from '../../../core/services/bookshelf.service';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';

describe('BookList Component Unit Tests', () => {
  let component: BookList;
  let fixture: ComponentFixture<BookList>;
  let mockBookApiService: { searchBooks: Mock };
  let mockBookshelfService: { getShelvesForUser: Mock; addBookToShelf: Mock };
  let mockToastService: { showSuccess: Mock; showError: Mock };
  let mockAuthService: { isLoggedIn: Mock };

  const mockEmptyPage: PageBookSummaryDto = {
    content: [],
    totalPages: 0,
    number: 0,
    totalElements: 0
  };

  beforeEach(async () => {
    vi.useFakeTimers();

    mockBookApiService = {
      searchBooks: vi.fn().mockReturnValue(of(mockEmptyPage))
    };
    mockBookshelfService = {
      getShelvesForUser: vi.fn().mockReturnValue(of({ content: [] })),
      addBookToShelf: vi.fn()
    };
    mockToastService = {
      showSuccess: vi.fn(),
      showError: vi.fn()
    };
    mockAuthService = {
      isLoggedIn: vi.fn().mockReturnValue(true)
    };

    await TestBed.configureTestingModule({
      imports: [BookList],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: BookAPIService, useValue: mockBookApiService },
        { provide: BookshelfService, useValue: mockBookshelfService },
        { provide: ToastService, useValue: mockToastService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BookList);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  it('should create cleanly', () => {
    expect(component).toBeTruthy();
  });

  it('should call searchBooks on init with default criteria', () => {
    mockBookApiService.searchBooks.mockReturnValue(of(mockEmptyPage));

    fixture.detectChanges();
    vi.advanceTimersByTime(300);

    expect(mockBookApiService.searchBooks).toHaveBeenCalledWith(
      { page: 0, size: 20 },
      undefined,
      expect.any(Set),
      undefined,
      undefined,
      undefined
    );
  });

  it('should display books from the service when data arrives', () => {
    // Arrange
    const mockBookPage: PageBookSummaryDto = {
      content: [
        { id: '1', title: 'The Hobbit', authorName: 'J.R.R. Tolkien', coverImageUrl: '' },
        { id: '2', title: 'The Lord of the Rings', authorName: 'J.R.R. Tolkien', coverImageUrl: '' }
      ],
      totalPages: 1,
      number: 0,
      totalElements: 2
    };
    mockBookApiService.searchBooks.mockReturnValue(of(mockBookPage));

    component['searchForm'].patchValue({ query: 'Hobbit' });
    fixture.detectChanges();

    vi.advanceTimersByTime(300);
    fixture.detectChanges();

    // Act
    const bookCards = fixture.nativeElement.querySelectorAll('.card');

    // Assert
    expect(bookCards.length).toBe(2);
    expect(fixture.nativeElement.querySelector('.card-title').textContent).toContain('The Hobbit');
  });

  it('should call searchBooks with query after user types in search bar', () => {
    // Act
    mockBookApiService.searchBooks.mockReturnValue(of(mockEmptyPage));
    fixture.detectChanges();
    vi.advanceTimersByTime(300);

    mockBookApiService.searchBooks.mockClear();
    component['searchForm'].patchValue({ query: 'The Hobbit' });

    fixture.detectChanges();

    expect(mockBookApiService.searchBooks).not.toHaveBeenCalled();

    vi.advanceTimersByTime(300);

    // Assert
    expect(mockBookApiService.searchBooks).toHaveBeenCalledWith(
      { page: 0, size: 20 },
      'The Hobbit',
      expect.any(Set),
      undefined,
      undefined,
      undefined
    );
  });

  describe('addToShelf', () => {
    it('should show success toast on successful addition', () => {
      // Arrange
      mockBookshelfService.addBookToShelf.mockReturnValue(of(null));
      const bookId = 'book-1';
      const shelfId = 'shelf-1';

      // Act
      component.addToShelf(bookId, shelfId);

      // Assert
      expect(mockBookshelfService.addBookToShelf).toHaveBeenCalledWith(shelfId, { bookId });
      expect(mockToastService.showSuccess).toHaveBeenCalledWith('Book added to shelf successfully!');
    });

    it('should show error toast on failed addition', () => {
      // Arrange
      const errorResponse = { error: { detail: 'Book already in shelf' } };
      mockBookshelfService.addBookToShelf.mockReturnValue(throwError(() => errorResponse));
      const bookId = 'book-1';
      const shelfId = 'shelf-1';

      // Act
      component.addToShelf(bookId, shelfId);

      // Assert
      expect(mockBookshelfService.addBookToShelf).toHaveBeenCalledWith(shelfId, { bookId });
      expect(mockToastService.showError).toHaveBeenCalledWith('Book already in shelf');
    });

    it('should do nothing if bookId or shelfId is missing', () => {
      // Act
      component.addToShelf('', 'shelf-1');
      component.addToShelf('book-1', '');

      // Assert
      expect(mockBookshelfService.addBookToShelf).not.toHaveBeenCalled();
    });
  });
});
