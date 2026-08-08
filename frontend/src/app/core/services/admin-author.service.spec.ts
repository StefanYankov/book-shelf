import {TestBed} from '@angular/core/testing';
import {beforeEach, describe, expect, it, Mock, vi} from 'vitest';
import {of} from 'rxjs';
import {AdminAuthorService} from './admin-author.service';
import {AdminAuthorAPIService, AuthorDetailsDto} from '../../api';

describe('AdminAuthorService', () => {
  let service: AdminAuthorService;
  let mockAuthorApi: {
    getAllAuthors: Mock;
    createAuthor: Mock;
    updateAuthor: Mock;
    deleteAuthor: Mock;
  };

  const id = 'author-1';

  beforeEach(() => {
    mockAuthorApi = {
      getAllAuthors: vi.fn().mockReturnValue(of({content: []})),
      createAuthor: vi.fn().mockReturnValue(of({id, name: 'Tolkien'} as AuthorDetailsDto)),
      updateAuthor: vi.fn().mockReturnValue(of({id, name: 'Tolkien'} as AuthorDetailsDto)),
      deleteAuthor: vi.fn().mockReturnValue(of(undefined)),
    };

    TestBed.configureTestingModule({
      providers: [
        AdminAuthorService,
        {provide: AdminAuthorAPIService, useValue: mockAuthorApi},
      ],
    });
    service = TestBed.inject(AdminAuthorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAllAuthors delegates with the pageable', () => {
    const pageable = {page: 0, size: 100};
    service.getAllAuthors(pageable);
    expect(mockAuthorApi.getAllAuthors).toHaveBeenCalledWith(pageable);
  });

  it('createAuthor delegates the payload without an image', () => {
    service.createAuthor({name: 'Tolkien', summary: 'Bio'}).subscribe();
    expect(mockAuthorApi.createAuthor).toHaveBeenCalledWith({name: 'Tolkien', summary: 'Bio'}, undefined);
  });

  it('createAuthor delegates the payload with an image', () => {
    const image = new Blob(['x'], {type: 'image/jpeg'});
    service.createAuthor({name: 'Tolkien', summary: 'Bio'}, image).subscribe();
    expect(mockAuthorApi.createAuthor).toHaveBeenCalledWith({name: 'Tolkien', summary: 'Bio'}, image);
  });

  it('updateAuthor delegates the id and payload', () => {
    service.updateAuthor(id, {name: 'Tolkien', summary: 'New'}).subscribe();
    expect(mockAuthorApi.updateAuthor).toHaveBeenCalledWith(id, {name: 'Tolkien', summary: 'New'});
  });

  it('deleteAuthor delegates the id', () => {
    service.deleteAuthor(id).subscribe();
    expect(mockAuthorApi.deleteAuthor).toHaveBeenCalledWith(id);
  });
});
