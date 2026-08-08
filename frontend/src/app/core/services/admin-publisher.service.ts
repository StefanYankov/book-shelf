import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {
  AdminPublisherAPIService,
  Pageable,
  PagedResponsePublisherDto,
  PublisherCreateDto,
  PublisherDto,
  PublisherUpdateDto,
} from '../../api';

/**
 * Administrative facade over the generated `AdminPublisherAPIService`.
 * Restores a precise `void` return type on the generated delete call (typed `any`),
 * and gives components a single, stable surface for publisher management.
 */
@Injectable({providedIn: 'root'})
export class AdminPublisherService {
  private readonly publisherApi = inject(AdminPublisherAPIService);

  /**
   * Retrieves a paginated list of publishers.
   * @param pageable Pagination configuration.
   * @returns An observable emitting a paginated result of publishers.
   */
  getAllPublishers(pageable: Pageable): Observable<PagedResponsePublisherDto> {
    return this.publisherApi.getAllPublishers(pageable);
  }

  /**
   * Creates a publisher.
   * @param dto The publisher creation payload.
   * @returns An observable emitting the created publisher.
   */
  createPublisher(dto: PublisherCreateDto): Observable<PublisherDto> {
    return this.publisherApi.createPublisher(dto);
  }

  /**
   * Updates a publisher.
   * @param id The UUID of the publisher.
   * @param dto The fields to update.
   * @returns An observable emitting the updated publisher.
   */
  updatePublisher(id: string, dto: PublisherUpdateDto): Observable<PublisherDto> {
    return this.publisherApi.updatePublisher(id, dto);
  }

  /**
   * Deletes a publisher.
   * @param id The UUID of the publisher.
   * @returns An observable that completes when the delete succeeds.
   */
  deletePublisher(id: string): Observable<void> {
    return this.publisherApi.deletePublisher(id) as Observable<void>;
  }
}
