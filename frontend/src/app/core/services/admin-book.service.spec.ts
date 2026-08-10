import {TestBed} from '@angular/core/testing';
import {beforeEach, describe, expect, it, Mock, vi} from 'vitest';
import {of} from 'rxjs';
import {AdminBookService} from './admin-book.service';
import {AdminBookAPIService, BookCreateDto, BookDetailsDto, BookUpdateDto} from '../../api';

describe('AdminBookService', () => {
  let service: AdminBookService;
  let mockBookApi: {
    createBook: Mock;
    updateBook: Mock;
    deleteBook: Mock;
  };
  const id = 'book-1';

  const createDto: BookCreateDto = {
    title: 'Dune',
    isbn: '978-0441013593',
    pages: 412,
    yearPublished: 1965,
    summary: 'A desert planet.',
    format: 'HARDCOVER',
    authorId: 'author-1',
    languageId: 'lang-1',
    publisherId: 'pub-1',
    genreIds: new Set(['genre-1']),
  };

  beforeEach(() => {
    mockBookApi = {
      createBook: vi.fn().mockReturnValue(of({id, title: 'Dune'} as BookDetailsDto)),
      updateBook: vi.fn().mockReturnValue(of({id, title: 'Dune Messiah'} as BookDetailsDto)),
      deleteBook: vi.fn().mockReturnValue(of(undefined)),
    };
    TestBed.configureTestingModule({
      providers: [
        AdminBookService,
        {provide: AdminBookAPIService, useValue: mockBookApi},
      ],
    });
    service = TestBed.inject(AdminBookService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('createBook delegates the payload without an image', () => {
    service.createBook(createDto).subscribe();
    expect(mockBookApi.createBook).toHaveBeenCalledWith(createDto, undefined);
  });

  it('createBook delegates the payload with a cover image', () => {
    const image = new Blob(['x'], {type: 'image/jpeg'});
    service.createBook(createDto, image).subscribe();
    expect(mockBookApi.createBook).toHaveBeenCalledWith(createDto, image);
  });

  it('updateBook delegates the id and payload', () => {
    const updateDto: BookUpdateDto = {title: 'Dune Messiah'};
    service.updateBook(id, updateDto).subscribe();
    expect(mockBookApi.updateBook).toHaveBeenCalledWith(id, updateDto);
  });

  it('deleteBook delegates the id', () => {
    service.deleteBook(id).subscribe();
    expect(mockBookApi.deleteBook).toHaveBeenCalledWith(id);
  });
});
