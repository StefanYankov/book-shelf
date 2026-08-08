import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {
  AdminAuthorAPIService,
  AuthorCreateDto,
  AuthorDetailsDto,
  AuthorUpdateDto,
  Pageable,
  PagedResponseAuthorSummaryDto,
} from '../../api';

/**
 * Administrative facade over the generated `AdminAuthorAPIService`.
 * Restores a precise `void` return type on the generated delete call (typed `any`),
 * and exposes the multipart create (author part plus an optional image) as a single method.
 */
@Injectable({providedIn: 'root'})
export class AdminAuthorService {
  private readonly authorApi = inject(AdminAuthorAPIService);

  /**
   * Retrieves a paginated list of authors in summary form.
   * @param pageable Pagination configuration.
   * @returns An observable emitting a paginated result of author summaries.
   */
  getAllAuthors(pageable: Pageable): Observable<PagedResponseAuthorSummaryDto> {
    return this.authorApi.getAllAuthors(pageable);
  }

  /**
   * Creates an author, with an optional profile image.
   * @param dto   The author creation payload (name and optional summary).
   * @param image The optional profile image file.
   * @returns An observable emitting the created author's details.
   */
  createAuthor(dto: AuthorCreateDto, image?: Blob): Observable<AuthorDetailsDto> {
    return this.authorApi.createAuthor(dto, image);
  }

  /**
   * Updates an author's name and summary.
   * @param id  The UUID of the author.
   * @param dto The fields to update.
   * @returns An observable emitting the updated author's details.
   */
  updateAuthor(id: string, dto: AuthorUpdateDto): Observable<AuthorDetailsDto> {
    return this.authorApi.updateAuthor(id, dto);
  }

  /**
   * Deletes an author.
   * @param id The UUID of the author.
   * @returns An observable that completes when the delete succeeds.
   */
  deleteAuthor(id: string): Observable<void> {
    return this.authorApi.deleteAuthor(id) as Observable<void>;
  }
}
