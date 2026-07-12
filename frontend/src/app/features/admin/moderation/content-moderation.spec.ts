import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ContentModeration } from './content-moderation';
import { AdminModerationAPIService, BookAPIService, BookDetailsDto, BookSummaryDto, PagedResponseBookSummaryDto } from '../../../api';
import { ToastService } from '../../../core/services/toast.service';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';

describe('ContentModeration Component Tests', () => {
  let fixture: ComponentFixture<ContentModeration>;
  let component: ContentModeration;
  let mockModerationService: Record<string, ReturnType<typeof vi.fn>>;
  let mockBookService: Record<string, ReturnType<typeof vi.fn>>;
  let mockToastService: Record<string, ReturnType<typeof vi.fn>>;

  beforeEach(async () => {
    vi.useFakeTimers();

    mockModerationService = {
      moderateBook: vi.fn().mockReturnValue(of({ id: '1', title: 'Moderated Title' } as BookDetailsDto))
    };

    mockBookService = {
      searchBooks: vi.fn().mockReturnValue(of({ content: [], totalElements: 0 } as unknown as PagedResponseBookSummaryDto)),
      getBookById: vi.fn().mockReturnValue(of({ id: '1', title: 'Test Book', summary: 'Original Summary' } as BookDetailsDto))
    };

    mockToastService = {
      showSuccess: vi.fn(),
      showError: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [ContentModeration, ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: AdminModerationAPIService, useValue: mockModerationService },
        { provide: BookAPIService, useValue: mockBookService },
        { provide: ToastService, useValue: mockToastService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ContentModeration);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should fetch book details when a book is selected for moderation', () => {
    const dummySummary: BookSummaryDto = { id: '1', title: 'Test Book', authorName: 'Author' };
    component['selectBookForModeration'](dummySummary);

    expect(mockBookService['getBookById']).toHaveBeenCalledWith('1');
    expect(component['moderationForm']()).toEqual({
      id: '1',
      title: 'Test Book',
      summary: 'Original Summary'
    });
  });

  it('should moderate book and refresh search on submit', () => {
    component['moderationForm'].set({ id: '1', title: 'New Title', summary: 'New Summary' });

    component['submitBookModeration']();

    expect(mockModerationService['moderateBook']).toHaveBeenCalledWith('1', { title: 'New Title', summary: 'New Summary' });
    expect(mockToastService['showSuccess']).toHaveBeenCalled();
    expect(component['moderationForm']()).toBeNull();
  });
});
