import {TestBed} from '@angular/core/testing';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {BookshelfService} from './bookshelf.service';
import {UserShelfAPIService} from '../../api';
import {PageQuery} from '../models/page-query';
import {of} from 'rxjs';

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
    it('should unpack the pageable into positional page, size and sort', () => {
      // Arrange
      const pageable: PageQuery = {page: 0, size: 20, sort: ['name,asc']};

      // Act
      service.getShelvesForUser(pageable);

      // Assert
      expect(mockUserShelfApiService.getUserShelves).toHaveBeenCalledWith(pageable.page, pageable.size, pageable.sort);
    });
  });
});
