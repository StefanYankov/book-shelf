import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { asyncScheduler, firstValueFrom, of, throwError } from 'rxjs';
import { vi, describe, it, expect, beforeEach, afterEach, Mock } from 'vitest';
import { BookList } from './book-list';
import { BookService } from '../../../core/services/book.service';
import { BookshelfService } from '../../../core/services/bookshelf.service';
import { ToastService } from '../../../core/services/toast.service';
import { PageBookSummaryDto } from '../../../api';

vi.spyOn(asyncScheduler, 'schedule').mockImplementation(function (this: unknown, work, delay, state) {
  return asyncScheduler.schedule.call(this, work, 0, state);
});

describe('BookList Component', () => {
  let component: BookList;
  let fixture: ComponentFixture<BookList>;
  let mockBookService: { searchBooks: Mock };
  let mockBookshelfService: { getShelvesForUser: Mock; addBookToShelf: Mock };
  let mockToastService: { showSuccess: Mock; showError: Mock };

  const mockEmptyPage: PageBookSummaryDto = {
    content: [],
    totalPages: 0,
    number: 0,
    totalElements: 0
  };

  beforeEach(async () => {
    mockBookService = {
      searchBooks: vi.fn()
    };
    mockBookshelfService = {
      getShelvesForUser: vi.fn().mockReturnValue(of({ content: [] })),
      addBookToShelf: vi.fn()
    };
    mockToastService = {
      showSuccess: vi.fn(),
      showError: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [BookList],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: BookService, useValue: mockBookService },
        { provide: BookshelfService, useValue: mockBookshelfService },
        { provide: ToastService, useValue: mockToastService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BookList);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call searchBooks on init with empty query', async () => {
    mockBookService.searchBooks.mockReturnValue(of(mockEmptyPage));

    fixture.detectChanges();
    await firstValueFrom(component['books$']);

    expect(mockBookService.searchBooks).toHaveBeenCalledWith('', 0, 20);
  });

  it('should display books from the service when data arrives', async () => {
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
    mockBookService.searchBooks.mockReturnValue(of(mockBookPage));

    fixture.detectChanges();
    await firstValueFrom(component['books$']);
    fixture.detectChanges();

    // Act
    const bookCards = fixture.nativeElement.querySelectorAll('.card');

    // Assert
    expect(bookCards.length).toBe(2);
    expect(fixture.nativeElement.querySelector('.card-title').textContent).toContain('The Hobbit');
  });

  it('should call searchBooks with query after user types in search bar', async () => {
    // Act
    mockBookService.searchBooks.mockReturnValue(of(mockEmptyPage));
    fixture.detectChanges();
    await firstValueFrom(component['books$']);
    mockBookService.searchBooks.mockClear();
    component.searchControl.setValue('The Hobbit');
    await firstValueFrom(component['books$']);

    // Assert
    expect(mockBookService.searchBooks).toHaveBeenCalledWith('The Hobbit', 0, 20);
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
