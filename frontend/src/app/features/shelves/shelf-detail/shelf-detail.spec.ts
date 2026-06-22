import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { ShelfDetail } from './shelf-detail';
import { BookshelfService } from '../../../core/services/bookshelf.service';
import { ToastService } from '../../../core/services/toast.service';
import { of } from 'rxjs';
import { ActivatedRoute, convertToParamMap } from '@angular/router';

describe('ShelfDetail', () => {
  let component: ShelfDetail;
  let fixture: ComponentFixture<ShelfDetail>;
  let mockBookshelfService: { getShelfById: ReturnType<typeof vi.fn>; getBooksInShelf: ReturnType<typeof vi.fn>; removeBookFromShelf: ReturnType<typeof vi.fn> };
  let mockToastService: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    vi.useFakeTimers();
    // Arrange
    mockBookshelfService = {
      getShelfById: vi.fn().mockReturnValue(of({ id: 'shelf-123', name: 'Fantasy Shelf', description: 'My favorite fantasy books' })),
      getBooksInShelf: vi.fn().mockReturnValue(of({
        content: [
          { id: 'book-1', title: 'The Hobbit', authorName: 'J.R.R. Tolkien' },
          { id: 'book-2', title: 'Dune', authorName: 'Frank Herbert' }
        ],
        totalPages: 1, totalElements: 2, number: 0, size: 20
      })),
      removeBookFromShelf: vi.fn().mockReturnValue(of(null))
    };
    mockToastService = {
      showSuccess: vi.fn(),
      showError: vi.fn()
    };

    // Arrange
    await TestBed.configureTestingModule({
      imports: [ShelfDetail],
      providers: [
        { provide: BookshelfService, useValue: mockBookshelfService },
        { provide: ToastService, useValue: mockToastService },
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(convertToParamMap({ id: 'shelf-123' })),
            snapshot: {
              paramMap: convertToParamMap({ id: 'shelf-123' })
            }
          }
        }
      ]
    }).compileComponents();

    // Arrange
    fixture = TestBed.createComponent(ShelfDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should initialize and fetch data based on the route ID', () => {
    // Assert
    expect(component).toBeTruthy();
    expect(mockBookshelfService.getShelfById).toHaveBeenCalledWith('shelf-123');
    expect(mockBookshelfService.getBooksInShelf).toHaveBeenCalledWith('shelf-123', { page: 0, size: 20 });

    const shelf = component.shelf();
    expect(shelf).not.toBeNull();
    expect(shelf?.name).toBe('Fantasy Shelf');

    const books = component.books();
    expect(books).not.toBeNull();
    expect(books?.content?.length).toBe(2);
  });

  it('should render the shelf details and books in the template', () => {
    // Arrange
    const compiled = fixture.nativeElement as HTMLElement;

    // Assert
    expect(compiled.querySelector('h1')?.textContent).toContain('Fantasy Shelf');
    expect(compiled.querySelector('.lead')?.textContent).toContain('My favorite fantasy books');

    const bookTitles = Array.from(compiled.querySelectorAll('.card-title')).map(el => el.textContent?.trim());
    expect(bookTitles).toContain('The Hobbit');
    expect(bookTitles).toContain('Dune');
  });

  it('should call the service and trigger a refresh when a book is removed', () => {
    // Arrange
    mockBookshelfService.getBooksInShelf.mockClear();

    // Act
    component.removeBook('book-1');
    vi.advanceTimersByTime(0);

    // Assert
    expect(mockBookshelfService.removeBookFromShelf).toHaveBeenCalledWith('shelf-123', 'book-1');
    expect(mockBookshelfService.getBooksInShelf).toHaveBeenCalledWith('shelf-123', { page: 0, size: 20 });
    expect(mockBookshelfService.getBooksInShelf).toHaveBeenCalledTimes(1);
    expect(mockToastService.showSuccess).toHaveBeenCalledWith('Book removed from shelf.');
  });
});
