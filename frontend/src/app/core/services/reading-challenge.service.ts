import {inject, Injectable} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {ReadingChallengeAPIService, ReadingChallengeCreateDto, ReadingChallengeViewDto,} from '../../api';

/**
 * Service responsible for managing the user's reading challenge.
 * Acts as a facade over the generated OpenAPI client (`ReadingChallengeAPIService`),
 * normalizing a 404 on lookup into a null result — representing the expected
 * "no challenge for this year yet" state rather than an error.
 */
@Injectable({providedIn: 'root'})
export class ReadingChallengeService {
  private readonly readingChallengeApiService = inject(ReadingChallengeAPIService);

  /**
   * Retrieves the current user's challenge for a given year.
   * A 404 response is treated as the absence of a challenge and mapped to null,
   * allowing the UI to present the "set a goal" flow instead of an error.
   * @param year The challenge year.
   * @returns An observable emitting the challenge, or null when none exists.
   */
  getChallenge(year: number): Observable<ReadingChallengeViewDto | null> {
    return this.readingChallengeApiService.getChallenge(year).pipe(
      catchError((err: HttpErrorResponse) => {
        if (err.status === 404) {
          return of(null);
        }
        throw err;
      })
    );
  }

  /**
   * Creates a reading challenge for the currently authenticated user.
   * @param year The target year of the challenge.
   * @param goal The number of books the user aims to read.
   * @returns An observable emitting the created challenge.
   */
  createChallenge(year: number, goal: number): Observable<ReadingChallengeViewDto> {
    const dto: ReadingChallengeCreateDto = {year, goal};
    return this.readingChallengeApiService.createChallenge(dto);
  }

  /**
   * Logs reading progress against an existing challenge.
   * @param challengeId The UUID of the challenge.
   * @param booksRead The number of books to add to the current progress.
   * @returns An observable emitting the updated challenge.
   */
  logProgress(challengeId: string, booksRead: number): Observable<ReadingChallengeViewDto> {
    return this.readingChallengeApiService.logProgress(challengeId, {booksRead});
  }
}
