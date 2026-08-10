import {TestBed} from '@angular/core/testing';
import {beforeEach, describe, expect, it, Mock, vi} from 'vitest';
import {of} from 'rxjs';
import {AdminGenreService} from './admin-genre.service';
import {AdminGenreAPIService, GenreDto} from '../../api';
import {PageQuery} from '../models/page-query';

describe('AdminGenreService', () => {
  let service: AdminGenreService;
  let mockGenreApi: {
    getAllGenres: Mock;
    createGenre: Mock;
    updateGenre: Mock;
    deleteGenre: Mock;
  };
  const id = 'genre-1';

  beforeEach(() => {
    mockGenreApi = {
      getAllGenres: vi.fn().mockReturnValue(of({content: []})),
      createGenre: vi.fn().mockReturnValue(of({id, name: 'Fantasy'} as GenreDto)),
      updateGenre: vi.fn().mockReturnValue(of({id, name: 'New'} as GenreDto)),
      deleteGenre: vi.fn().mockReturnValue(of(undefined)),
    };
    TestBed.configureTestingModule({
      providers: [
        AdminGenreService,
        {provide: AdminGenreAPIService, useValue: mockGenreApi},
      ],
    });
    service = TestBed.inject(AdminGenreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAllGenres unpacks the pageable into positional page, size and sort', () => {
    const pageable: PageQuery = {page: 0, size: 100, sort: ['name,asc']};
    service.getAllGenres(pageable);
    expect(mockGenreApi.getAllGenres).toHaveBeenCalledWith(pageable.page, pageable.size, pageable.sort);
  });

  it('createGenre delegates the payload', () => {
    service.createGenre({name: 'Fantasy', description: 'Magic.'}).subscribe();
    expect(mockGenreApi.createGenre).toHaveBeenCalledWith({name: 'Fantasy', description: 'Magic.'});
  });

  it('updateGenre delegates the id and payload', () => {
    service.updateGenre(id, {name: 'New'}).subscribe();
    expect(mockGenreApi.updateGenre).toHaveBeenCalledWith(id, {name: 'New'});
  });

  it('deleteGenre delegates the id', () => {
    service.deleteGenre(id).subscribe();
    expect(mockGenreApi.deleteGenre).toHaveBeenCalledWith(id);
  });
});
