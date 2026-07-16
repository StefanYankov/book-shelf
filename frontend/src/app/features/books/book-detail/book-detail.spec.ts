import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { BookDetail } from './book-detail';
import { BookAPIService, UserShelfAPIService } from '../../../api';
import { ReviewService } from '../../../core/services/review.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { BookDetailsDto, PagedResponseBookshelfSummaryDto, PagedResponseReviewViewDto, ReviewViewDto } from '../../../api';

/**
 * Test-only view of BookDetail's protected coordination members, reached via a typed
 * cast so we can drive the review add/edit logic directly with dot notation, without
 * widening production visibility.
 */
interface BookDetailInternals {
  editingReview(): ReviewViewDto | null;
  addFormOpen(): boolean;
  myReview(): ReviewViewDto | null;
  showAddForm(): boolean;
  onReviewsLoaded(reviews: ReviewViewDto[]): void;
  openAddForm(): void;
  onAddCancelled(): void;
  onEditRequested(review: ReviewViewDto): void;
  onEditCancelled(): void;
  onReviewSaved(): void;
}

describe('BookDetail Component', () => {
  let component: BookDetail;
  let fixture: ComponentFixture<BookDetail>;
  let mockBookApiService: { getBookById: ReturnType<typeof vi.fn> };
  let mockUserShelfApiService: { getUserShelves: ReturnType<typeof vi.fn>; addBookToShelf: ReturnType<typeof vi.fn> };
  let mockReviewService: {
    getReviewsForTarget: ReturnType<typeof vi.fn>;
    addReview: ReturnType<typeof vi.fn>;
    updateReview: ReturnType<typeof vi.fn>;
    deleteReview: ReturnType<typeof vi.fn>;
  };
  let mockAuthService: {
    isLoggedIn: ReturnType<typeof vi.fn>;
    userId: ReturnType<typeof vi.fn>;
    userRole: ReturnType<typeof vi.fn>;
  };
  let mockToastService: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };

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

  const mockShelves: PagedResponseBookshelfSummaryDto = {
    content: [
      { id: 'shelf-1', name: 'My Favorites' },
      { id: 'shelf-2', name: 'To Read' }
    ],
    totalElements: 2, totalPages: 1, pageNumber: 0, pageSize: 20
  };

  const reviewFactory = (overrides: Partial<ReviewViewDto> = {}): ReviewViewDto => ({
    id: 'review-1',
    title: 'Great',
    comment: 'Loved it',
    rating: 5,
    userId: 'user-1',
    username: 'alice',
    targetId: '123',
    targetType: 'BOOK',
    createdAt: '2026-07-13T10:00:00Z',
    updatedAt: '2026-07-13T10:00:00Z',
    ...overrides,
  });

  const pageOf = (content: ReviewViewDto[]): PagedResponseReviewViewDto => ({
    content,
    pageNumber: 0,
    pageSize: 5,
    totalElements: content.length,
    totalPages: 1,
    isLast: true,
  });

  /** Typed access to BookDetail's protected coordination members. */
  const internals = (): BookDetailInternals => component as unknown as BookDetailInternals;

  beforeEach(async () => {
    mockBookApiService = {
      getBookById: vi.fn().mockReturnValue(of(mockBook))
    };
    mockUserShelfApiService = {
      getUserShelves: vi.fn().mockReturnValue(of(mockShelves)),
      addBookToShelf: vi.fn().mockReturnValue(of(null))
    };
    mockReviewService = {
      getReviewsForTarget: vi.fn().mockReturnValue(of(pageOf([]))),
      addReview: vi.fn(),
      updateReview: vi.fn(),
      deleteReview: vi.fn().mockReturnValue(of(undefined)),
    };
    mockAuthService = {
      isLoggedIn: vi.fn().mockReturnValue(false),
      userId: vi.fn().mockReturnValue(null),
      userRole: vi.fn().mockReturnValue(null),
    };
    mockToastService = {
      showSuccess: vi.fn(),
      showError: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [BookDetail],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { paramMap: of(new Map([['id', '123']])) } },
        { provide: BookAPIService, useValue: mockBookApiService },
        { provide: UserShelfAPIService, useValue: mockUserShelfApiService },
        { provide: ReviewService, useValue: mockReviewService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: ToastService, useValue: mockToastService }
      ]
    }).compileComponents();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  /** Creates the component with the given auth state and runs initial change detection. */
  const setupComponent = (isLoggedIn: boolean) => {
    mockAuthService.isLoggedIn.mockReturnValue(isLoggedIn);
    fixture = TestBed.createComponent(BookDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  };

  // ---------------------------------------------------------------------------
  // Rendering & data load
  // ---------------------------------------------------------------------------

  it('should create', () => {
    // Arrange & Act
    setupComponent(false);

    // Assert
    expect(component).toBeTruthy();
  });

  it('should call getBookById with the correct ID from the route', () => {
    // Arrange & Act
    setupComponent(false);

    // Assert
    expect(mockBookApiService.getBookById).toHaveBeenCalledWith('123');
  });

  it('should render the book details in the template', () => {
    // Arrange & Act
    setupComponent(false);

    // Assert
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Test Book');
    expect(compiled.querySelector('h5')?.textContent).toContain('by Test Author');
  });

  // ---------------------------------------------------------------------------
  // Add to Shelf UI
  // ---------------------------------------------------------------------------

  describe('Add to Shelf UI', () => {
    it('should NOT display the Add to Shelf button for guest users', () => {
      // Arrange & Act
      setupComponent(false);

      // Assert
      const button = fixture.nativeElement.querySelector('#addToShelfDropdown');
      expect(button).toBeNull();
    });

    it('should display the Add to Shelf button for logged-in users', () => {
      // Arrange & Act
      setupComponent(true);

      // Assert
      const button = fixture.nativeElement.querySelector('#addToShelfDropdown');
      expect(button).not.toBeNull();
    });

    it("should populate the dropdown with the user's shelves", () => {
      // Arrange & Act
      setupComponent(true);

      // Assert
      const dropdownItems = fixture.nativeElement.querySelectorAll('.dropdown-item');
      expect(dropdownItems.length).toBe(2);
      expect(dropdownItems[0].textContent).toContain('My Favorites');
      expect(dropdownItems[1].textContent).toContain('To Read');
    });

    it('should call the service and show a success toast when a shelf is clicked', () => {
      // Arrange
      setupComponent(true);
      const dropdownItem = fixture.nativeElement.querySelector('.dropdown-item') as HTMLElement;

      // Act
      dropdownItem.click();

      // Assert
      expect(mockUserShelfApiService.addBookToShelf).toHaveBeenCalledWith('shelf-1', { bookId: '123' });
      expect(mockToastService.showSuccess).toHaveBeenCalledWith('Book added to shelf successfully!');
    });

    it('should do nothing when bookId or shelfId is missing', () => {
      // Arrange
      setupComponent(true);
      mockUserShelfApiService.addBookToShelf.mockClear();

      // Act
      component.addToShelf('', 'shelf-1');
      component.addToShelf('123', '');

      // Assert
      expect(mockUserShelfApiService.addBookToShelf).not.toHaveBeenCalled();
    });
  });

  // ---------------------------------------------------------------------------
  // Review coordination — "(b)" one-per-book logic + add-form toggle
  // ---------------------------------------------------------------------------

  describe('Review coordination', () => {
    it('should not offer the add-review affordance to a guest', () => {
      // Arrange & Act
      setupComponent(false);
      internals().onReviewsLoaded([]);
      fixture.detectChanges();

      // Assert
      expect(internals().showAddForm()).toBe(false);
      expect(fixture.nativeElement.querySelector('app-review-form')).toBeNull();
      expect(fixture.nativeElement.textContent).not.toContain('Write a review');
    });

    it('should offer the "Write a review" button (not the form) to a logged-in non-reviewer', () => {
      // Arrange
      mockAuthService.userId.mockReturnValue('user-1');
      setupComponent(true);

      // Act: reviews load, none belong to the current user
      internals().onReviewsLoaded([reviewFactory({ userId: 'someone-else' })]);
      fixture.detectChanges();

      // Assert: button shows, form does not (collapsed by default)
      expect(internals().showAddForm()).toBe(true);
      expect(fixture.nativeElement.textContent).toContain('Write a review');
      expect(fixture.nativeElement.querySelector('app-review-form')).toBeNull();
    });

    it('should expand the form when "Write a review" is opened, and collapse on cancel', () => {
      // Arrange
      mockAuthService.userId.mockReturnValue('user-1');
      setupComponent(true);
      internals().onReviewsLoaded([reviewFactory({ userId: 'someone-else' })]);
      fixture.detectChanges();

      // Act: open the form
      internals().openAddForm();
      fixture.detectChanges();

      // Assert: form is now present
      expect(internals().addFormOpen()).toBe(true);
      expect(fixture.nativeElement.querySelector('app-review-form')).not.toBeNull();

      // Act: cancel collapses it back to the button
      internals().onAddCancelled();
      fixture.detectChanges();

      // Assert
      expect(internals().addFormOpen()).toBe(false);
      expect(fixture.nativeElement.querySelector('app-review-form')).toBeNull();
      expect(fixture.nativeElement.textContent).toContain('Write a review');
    });

    it('should detect the user\'s own review and offer no add affordance (one-per-book)', async () => {
      // Arrange: the loaded reviews include one authored by the current user
      mockAuthService.userId.mockReturnValue('user-1');
      mockReviewService.getReviewsForTarget.mockReturnValue(of(pageOf([reviewFactory({ userId: 'user-1' })])));
      setupComponent(true);

      // Act: allow the child list's async load + reviewsLoaded emission to propagate
      await fixture.whenStable();
      fixture.detectChanges();

      // Assert
      expect(internals().myReview()).toEqual(reviewFactory({ userId: 'user-1' }));
      expect(internals().showAddForm()).toBe(false);
      expect(fixture.nativeElement.textContent).not.toContain('Write a review');
      expect(fixture.nativeElement.querySelector('app-review-form')).toBeNull();
    });

    it('should enter edit mode (and suppress the add affordance) when the list requests an edit', () => {
      // Arrange
      mockAuthService.userId.mockReturnValue('user-1');
      setupComponent(true);
      const target = reviewFactory({ userId: 'user-1', title: 'Editing this' });

      // Act
      internals().onEditRequested(target);
      fixture.detectChanges();

      // Assert
      expect(internals().editingReview()).toEqual(target);
      expect(internals().showAddForm()).toBe(false);
      expect(fixture.nativeElement.querySelector('app-review-form')).not.toBeNull();
    });

    it('should clear edit mode when an edit is cancelled', () => {
      // Arrange
      setupComponent(true);
      internals().onEditRequested(reviewFactory());

      // Act
      internals().onEditCancelled();

      // Assert
      expect(internals().editingReview()).toBeNull();
    });

    it('should clear edit/add mode and reload the list after a successful save', async () => {
      // Arrange
      mockAuthService.userId.mockReturnValue('user-1');
      setupComponent(true);
      await fixture.whenStable(); // initial child list load
      const callsBeforeSave = mockReviewService.getReviewsForTarget.mock.calls.length;
      internals().openAddForm();

      // Act
      internals().onReviewSaved();
      await fixture.whenStable();

      // Assert
      expect(internals().editingReview()).toBeNull();
      expect(internals().addFormOpen()).toBe(false);
      expect(mockReviewService.getReviewsForTarget.mock.calls.length).toBeGreaterThan(callsBeforeSave);
    });
  });
});
