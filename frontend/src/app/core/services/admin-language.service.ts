import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {
  AdminLanguageAPIService,
  LanguageCreateDto,
  LanguageDto,
  LanguageUpdateDto,
  Pageable,
  PagedResponseLanguageDto,
} from '../../api';

/**
 * Administrative facade over the generated `AdminLanguageAPIService`.
 * Restores a precise `void` return type on the generated delete call (typed `any`),
 * and gives components a single, stable surface for language management.
 */
@Injectable({providedIn: 'root'})
export class AdminLanguageService {
  private readonly languageApi = inject(AdminLanguageAPIService);

  /**
   * Retrieves a paginated list of languages.
   * @param pageable Pagination configuration.
   * @returns An observable emitting a paginated result of languages.
   */
  getAllLanguages(pageable: Pageable): Observable<PagedResponseLanguageDto> {
    return this.languageApi.getAllLanguages(pageable);
  }

  /**
   * Creates a language.
   * @param dto The language creation payload.
   * @returns An observable emitting the created language.
   */
  createLanguage(dto: LanguageCreateDto): Observable<LanguageDto> {
    return this.languageApi.createLanguage(dto);
  }

  /**
   * Updates a language.
   * @param id The UUID of the language.
   * @param dto The fields to update.
   * @returns An observable emitting the updated language.
   */
  updateLanguage(id: string, dto: LanguageUpdateDto): Observable<LanguageDto> {
    return this.languageApi.updateLanguage(id, dto);
  }

  /**
   * Deletes a language.
   * @param id The UUID of the language.
   * @returns An observable that completes when the delete succeeds.
   */
  deleteLanguage(id: string): Observable<void> {
    return this.languageApi.deleteLanguage(id) as Observable<void>;
  }
}
