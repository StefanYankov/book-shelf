import {TestBed} from '@angular/core/testing';
import {beforeEach, describe, expect, it, Mock, vi} from 'vitest';
import {of} from 'rxjs';
import {AdminPublisherService} from './admin-publisher.service';
import {AdminPublisherAPIService, PublisherDto} from '../../api';

describe('AdminPublisherService', () => {
  let service: AdminPublisherService;
  let mockPublisherApi: {
    getAllPublishers: Mock;
    createPublisher: Mock;
    updatePublisher: Mock;
    deletePublisher: Mock;
  };

  const id = 'pub-1';

  beforeEach(() => {
    mockPublisherApi = {
      getAllPublishers: vi.fn().mockReturnValue(of({content: []})),
      createPublisher: vi.fn().mockReturnValue(of({id, name: 'Penguin Books'} as PublisherDto)),
      updatePublisher: vi.fn().mockReturnValue(of({id, name: 'Doubleday'} as PublisherDto)),
      deletePublisher: vi.fn().mockReturnValue(of(undefined)),
    };

    TestBed.configureTestingModule({
      providers: [
        AdminPublisherService,
        {provide: AdminPublisherAPIService, useValue: mockPublisherApi},
      ],
    });
    service = TestBed.inject(AdminPublisherService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAllPublishers delegates with the pageable', () => {
    const pageable = {page: 0, size: 100};
    service.getAllPublishers(pageable);
    expect(mockPublisherApi.getAllPublishers).toHaveBeenCalledWith(pageable);
  });

  it('createPublisher delegates the payload', () => {
    service.createPublisher({name: 'Doubleday'}).subscribe();
    expect(mockPublisherApi.createPublisher).toHaveBeenCalledWith({name: 'Doubleday'});
  });

  it('updatePublisher delegates the id and payload', () => {
    service.updatePublisher(id, {name: 'Doubleday'}).subscribe();
    expect(mockPublisherApi.updatePublisher).toHaveBeenCalledWith(id, {name: 'Doubleday'});
  });

  it('deletePublisher delegates the id', () => {
    service.deletePublisher(id).subscribe();
    expect(mockPublisherApi.deletePublisher).toHaveBeenCalledWith(id);
  });
});
