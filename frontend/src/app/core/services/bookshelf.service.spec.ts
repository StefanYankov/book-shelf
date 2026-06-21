import { TestBed } from '@angular/core/testing';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { BookshelfService } from './bookshelf.service';
import { UserShelfAPIService } from '../../api';
import { of } from 'rxjs';

describe('BookshelfService', () => {
  let service: BookshelfService;
  let mockUserShelfApiService: { getUserShelves: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    mockUserShelfApiService = {
      getUserShelves: vi.fn().mockReturnValue(of({}))
    };

    TestBed.configureTestingModule({
      providers: [
        BookshelfService,
        { provide: UserShelfAPIService, useValue: mockUserShelfApiService }
      ]
    });
    service = TestBed.inject(BookshelfService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getShelvesForUser', () => {
    it('should call the API service with the correct parameters', () => {
      // Arrange
      const pageable = { page: 0, size: 20, sort: ['name,asc'] };

      // Act
      service.getShelvesForUser(pageable);

      // Assert
      expect(mockUserShelfApiService.getUserShelves).toHaveBeenCalledWith(pageable);
    });
  });
});
