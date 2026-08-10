import {TestBed} from '@angular/core/testing';
import {beforeEach, describe, expect, it, Mock, vi} from 'vitest';
import {of} from 'rxjs';
import {BookService} from './book.service';
import {BookAPIService, BookDetailsDto} from '../../api';

describe('BookService', () => {
  let service: BookService;
  let mockBookApi: {
    getAllBooks: Mock;
    searchBooks: Mock;
    getBookById: Mock;
  };
  const id = 'book-1';

  beforeEach(() => {
    mockBookApi = {
      getAllBooks: vi.fn().mockReturnValue(of({content: []})),
      searchBooks: vi.fn().mockReturnValue(of({content: []})),
      getBookById: vi.fn().mockReturnValue(of({id, title: 'Dune'} as BookDetailsDto)),
    };
    TestBed.configureTestingModule({
      providers: [
        BookService,
        {provide: BookAPIService, useValue: mockBookApi},
      ],
    });
    service = TestBed.inject(BookService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAllBooks', () => {
    it('should delegate page and size positionally', () => {
      service.getAllBooks(0, 20).subscribe();
      expect(mockBookApi.getAllBooks).toHaveBeenCalledWith(0, 20);
    });
  });

  describe('searchBooks', () => {
    it('should delegate the query with page and size in their positional slots', () => {
      service.searchBooks('Dune', 0, 10).subscribe();
      // searchBooks positional args: (query, genres, format, yearMin, yearMax, page, size, sort)
      expect(mockBookApi.searchBooks).toHaveBeenCalledWith(
        'Dune', undefined, undefined, undefined, undefined, 0, 10);
    });
  });

  describe('getBookById', () => {
    it('should delegate the id', () => {
      service.getBookById(id).subscribe();
      expect(mockBookApi.getBookById).toHaveBeenCalledWith(id);
    });
  });
});
