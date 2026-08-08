import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {
  AdminGenreAPIService,
  GenreCreateDto,
  GenreDto,
  GenreUpdateDto,
  Pageable,
  PagedResponseGenreDto,
} from '../../api';

/**
 * Administrative facade over the generated `AdminGenreAPIService`.
 * Restores a precise `void` return type on the generated delete call (typed `any`),
 * and gives components a single, stable surface for genre management.
 */
@Injectable({providedIn: 'root'})
export class AdminGenreService {
  private readonly genreApi = inject(AdminGenreAPIService);

  /**
   * Retrieves a paginated list of genres.
   * @param pageable Pagination configuration.
   * @returns An observable emitting a paginated result of genres.
   */
  getAllGenres(pageable: Pageable): Observable<PagedResponseGenreDto> {
    return this.genreApi.getAllGenres(pageable);
  }

  /**
   * Creates a genre.
   * @param dto The genre creation payload.
   * @returns An observable emitting the created genre.
   */
  createGenre(dto: GenreCreateDto): Observable<GenreDto> {
    return this.genreApi.createGenre(dto);
  }

  /**
   * Updates a genre.
   * @param id The UUID of the genre.
   * @param dto The fields to update.
   * @returns An observable emitting the updated genre.
   */
  updateGenre(id: string, dto: GenreUpdateDto): Observable<GenreDto> {
    return this.genreApi.updateGenre(id, dto);
  }

  /**
   * Deletes a genre.
   * @param id The UUID of the genre.
   * @returns An observable that completes when the delete succeeds.
   */
  deleteGenre(id: string): Observable<void> {
    return this.genreApi.deleteGenre(id) as Observable<void>;
  }
}
