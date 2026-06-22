import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { BookDetail } from './book-detail';
import { BookService } from '../../../core/services/book.service';
import { BookshelfService } from '../../../core/services/bookshelf.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { BookDetailsDto, PageBookshelfSummaryDto } from '../../../api';

describe('BookDetail Component', () => {
  let component: BookDetail;
  let fixture: ComponentFixture<BookDetail>;
  let mockBookService: { getBookById: ReturnType<typeof vi.fn> };
  let mockBookshelfService: { getShelvesForUser: ReturnType<typeof vi.fn>; addBookToShelf: ReturnType<typeof vi.fn> };
  let mockAuthService: { isLoggedIn: ReturnType<typeof vi.fn> };
  let mockToastService: { showSuccess: ReturnType<typeof vi.fn> };

  const mockBook: BookDetailsDto = {
    id: '123',
    title: 'Test Book',
    author: { id: 'a1', name: 'Test Author' },
    summary: 'A great book.',
    coverImageUrl: '',
    isbn: '12345',
    pages: 100,
    yearPublished: 2024,
    format: 'PAPERBACK',
    language: { id: 'l1', name: 'English' },
    publisher: { id: 'p1', name: 'Test Publisher' },
    genres: new Set([{ id: 'g1', name: 'Fantasy' }])
  };

  const mockShelves: PageBookshelfSummaryDto = {
    content: [
      { id: 'shelf-1', name: 'My Favorites' },
      { id: 'shelf-2', name: 'To Read' }
    ],
    totalElements: 2, totalPages: 1, number: 0, size: 20
  };

  beforeEach(async () => {
    mockBookService = {
      getBookById: vi.fn().mockReturnValue(of(mockBook))
    };
    mockBookshelfService = {
      getShelvesForUser: vi.fn().mockReturnValue(of(mockShelves)),
      addBookToShelf: vi.fn().mockReturnValue(of(null))
    };
    mockAuthService = {
      isLoggedIn: vi.fn()
    };
    mockToastService = {
      showSuccess: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [BookDetail],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { paramMap: of(new Map([['id', '123']])) } },
        { provide: BookService, useValue: mockBookService },
        { provide: BookshelfService, useValue: mockBookshelfService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: ToastService, useValue: mockToastService }
      ]
    }).compileComponents();
  });

  const setupComponent = (isLoggedIn: boolean) => {
    mockAuthService.isLoggedIn.mockReturnValue(isLoggedIn);
    fixture = TestBed.createComponent(BookDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  };

  it('should create', () => {
    setupComponent(false);
    expect(component).toBeTruthy();
  });

  it('should call getBookById with the correct ID from the route', () => {
    setupComponent(false);
    expect(mockBookService.getBookById).toHaveBeenCalledWith('123');
  });

  it('should render the book details in the template', () => {
    setupComponent(false);
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Test Book');
    expect(compiled.querySelector('h5')?.textContent).toContain('by Test Author');
  });

  describe('Add to Shelf UI', () => {
    it('should NOT display the Add to Shelf button for guest users', () => {
      setupComponent(false);
      const button = fixture.nativeElement.querySelector('#addToShelfDropdown');
      expect(button).toBeNull();
    });

    it('should display the Add to Shelf button for logged-in users', () => {
      setupComponent(true);
      const button = fixture.nativeElement.querySelector('#addToShelfDropdown');
      expect(button).not.toBeNull();
    });

    it('should populate the dropdown with the user\'s shelves', () => {
      setupComponent(true);
      const dropdownItems = fixture.nativeElement.querySelectorAll('.dropdown-item');
      expect(dropdownItems.length).toBe(2);
      expect(dropdownItems[0].textContent).toContain('My Favorites');
      expect(dropdownItems[1].textContent).toContain('To Read');
    });

    it('should call the correct service method when a shelf is clicked', () => {
      setupComponent(true);
      const dropdownItem = fixture.nativeElement.querySelector('.dropdown-item') as HTMLElement;

      dropdownItem.click();

      expect(mockBookshelfService.addBookToShelf).toHaveBeenCalledWith('shelf-1', { bookId: '123' });
      expect(mockToastService.showSuccess).toHaveBeenCalledWith('Book added to shelf successfully!');
    });
  });
});
