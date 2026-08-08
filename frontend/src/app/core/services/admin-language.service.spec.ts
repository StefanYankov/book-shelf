import {TestBed} from '@angular/core/testing';
import {beforeEach, describe, expect, it, Mock, vi} from 'vitest';
import {of} from 'rxjs';
import {AdminLanguageService} from './admin-language.service';
import {AdminLanguageAPIService, LanguageDto} from '../../api';

describe('AdminLanguageService', () => {
  let service: AdminLanguageService;
  let mockLanguageApi: {
    getAllLanguages: Mock;
    createLanguage: Mock;
    updateLanguage: Mock;
    deleteLanguage: Mock;
  };

  const id = 'lang-1';

  beforeEach(() => {
    mockLanguageApi = {
      getAllLanguages: vi.fn().mockReturnValue(of({content: []})),
      createLanguage: vi.fn().mockReturnValue(of({id, name: 'English'} as LanguageDto)),
      updateLanguage: vi.fn().mockReturnValue(of({id, name: 'German'} as LanguageDto)),
      deleteLanguage: vi.fn().mockReturnValue(of(undefined)),
    };

    TestBed.configureTestingModule({
      providers: [
        AdminLanguageService,
        {provide: AdminLanguageAPIService, useValue: mockLanguageApi},
      ],
    });
    service = TestBed.inject(AdminLanguageService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAllLanguages delegates with the pageable', () => {
    const pageable = {page: 0, size: 100};
    service.getAllLanguages(pageable);
    expect(mockLanguageApi.getAllLanguages).toHaveBeenCalledWith(pageable);
  });

  it('createLanguage delegates the payload', () => {
    service.createLanguage({name: 'Bulgarian'}).subscribe();
    expect(mockLanguageApi.createLanguage).toHaveBeenCalledWith({name: 'Bulgarian'});
  });

  it('updateLanguage delegates the id and payload', () => {
    service.updateLanguage(id, {name: 'German'}).subscribe();
    expect(mockLanguageApi.updateLanguage).toHaveBeenCalledWith(id, {name: 'German'});
  });

  it('deleteLanguage delegates the id', () => {
    service.deleteLanguage(id).subscribe();
    expect(mockLanguageApi.deleteLanguage).toHaveBeenCalledWith(id);
  });
});
