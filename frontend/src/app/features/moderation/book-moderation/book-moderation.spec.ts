import {ComponentFixture, TestBed} from '@angular/core/testing';
import {BookModeration} from './book-moderation';
import {
  BookAPIService,
  BookDetailsDto,
  BookSummaryDto,
  ModerationAPIService,
  PagedResponseBookSummaryDto
} from '../../../api';
import {ToastService} from '../../../core/services/toast.service';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {of} from 'rxjs';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';

/**
 * Test-only view of BookModeration's protected members, reached via a typed cast so tests can
 * drive the component with plain dot notation without widening production visibility.
 */
interface BookModerationInternals {
  moderationForm: {
    (): { id: string; title: string; summary: string } | null;
    set(value: { id: string; title: string; summary: string } | null): void;
  };

  selectBookForModeration(book: BookSummaryDto): void;

  submitBookModeration(): void;
}

describe('BookModeration Component Tests', () => {
  let fixture: ComponentFixture<BookModeration>;
  let component: BookModeration;
  let mockModerationService: Record<string, ReturnType<typeof vi.fn>>;
  let mockBookService: Record<string, ReturnType<typeof vi.fn>>;
  let mockToastService: Record<string, ReturnType<typeof vi.fn>>;

  /** Typed access to the component's protected members. */
  const internals = (): BookModerationInternals => component as unknown as BookModerationInternals;

  beforeEach(async () => {
    vi.useFakeTimers();

    mockModerationService = {
      moderateBook: vi.fn().mockReturnValue(of({id: '1', title: 'Moderated Title'} as BookDetailsDto))
    };

    mockBookService = {
      searchBooks: vi.fn().mockReturnValue(of({
        content: [],
        totalElements: 0
      } as unknown as PagedResponseBookSummaryDto)),
      getBookById: vi.fn().mockReturnValue(of({
        id: '1',
        title: 'Test Book',
        summary: 'Original Summary'
      } as BookDetailsDto))
    };

    mockToastService = {
      showSuccess: vi.fn(),
      showError: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [BookModeration, ReactiveFormsModule],
      providers: [
        FormBuilder,
        {provide: ModerationAPIService, useValue: mockModerationService},
        {provide: BookAPIService, useValue: mockBookService},
        {provide: ToastService, useValue: mockToastService}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BookModeration);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should fetch book details when a book is selected for moderation', () => {
    // Arrange
    const dummySummary: BookSummaryDto = {id: '1', title: 'Test Book', authorName: 'Author'};

    // Act
    internals().selectBookForModeration(dummySummary);

    // Assert
    expect(mockBookService['getBookById']).toHaveBeenCalledWith('1');
    expect(internals().moderationForm()).toEqual({
      id: '1',
      title: 'Test Book',
      summary: 'Original Summary'
    });
  });

  it('should moderate book and refresh search on submit', () => {
    // Arrange
    internals().moderationForm.set({id: '1', title: 'New Title', summary: 'New Summary'});

    // Act
    internals().submitBookModeration();

    // Assert
    expect(mockModerationService['moderateBook']).toHaveBeenCalledWith('1', {
      title: 'New Title',
      summary: 'New Summary'
    });
    expect(mockToastService['showSuccess']).toHaveBeenCalled();
    expect(internals().moderationForm()).toBeNull();
  });
});
