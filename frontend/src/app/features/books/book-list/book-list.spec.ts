import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { asyncScheduler, firstValueFrom, of } from 'rxjs';
import { vi, describe, it, expect, beforeEach, afterEach, Mock } from 'vitest';
import { BookList } from './book-list';
import { BookService } from '../../../core/services/book.service';
import { PageBookSummaryDto } from '../../../api';

// Intercept the async scheduler for testing purposes globally across this spec file
// This forces any delayed execution (like debounceTime) to fire immediately (0ms)
vi.spyOn(asyncScheduler, 'schedule').mockImplementation(function (this: unknown, work, delay, state) {
  return asyncScheduler.schedule.call(this, work, 0, state);
});

describe('BookList Component', () => {
  let component: BookList;
  let fixture: ComponentFixture<BookList>;
  let mockBookService: { searchBooks: Mock };

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

    await TestBed.configureTestingModule({
      imports: [BookList],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: BookService, useValue: mockBookService }
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
    // Arrange
    mockBookService.searchBooks.mockReturnValue(of(mockEmptyPage));

    // Act
    fixture.detectChanges();
    // With the scheduler mocked, firstValueFrom will resolve immediately
    // without hitting the 5000ms timeout
    await firstValueFrom(component['books$']);

    // Assert
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

    // Act
    fixture.detectChanges();
    await firstValueFrom(component['books$']);
    fixture.detectChanges();

    // Assert
    const bookCards = fixture.nativeElement.querySelectorAll('.book-card');
    expect(bookCards.length).toBe(2);
    expect(fixture.nativeElement.querySelector('.book-title').textContent).toContain('The Hobbit');
  });

  it('should call searchBooks with query after user types in search bar', async () => {
    // Arrange
    mockBookService.searchBooks.mockReturnValue(of(mockEmptyPage));

    fixture.detectChanges();
    await firstValueFrom(component['books$']);
    mockBookService.searchBooks.mockClear();

    // Act
    component.searchControl.setValue('The Hobbit');
    await firstValueFrom(component['books$']);

    // Assert
    expect(mockBookService.searchBooks).toHaveBeenCalledWith('The Hobbit', 0, 20);
  });
});
