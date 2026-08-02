import {TestBed} from '@angular/core/testing';
import {beforeEach, describe, expect, it, Mock, vi} from 'vitest';
import {firstValueFrom, of, throwError} from 'rxjs';
import {ReadingChallengeService} from './reading-challenge.service';
import {ReadingChallengeAPIService, ReadingChallengeViewDto} from '../../api';

describe('ReadingChallengeService', () => {
  let service: ReadingChallengeService;
  let mockApi: {
    getChallenge: Mock;
    createChallenge: Mock;
    logProgress: Mock;
  };

  const challenge: ReadingChallengeViewDto = {
    id: 'challenge-1',
    userId: 'user-1',
    year: 2026,
    goal: 30,
    booksRead: 5,
    completed: false,
  };

  beforeEach(() => {
    mockApi = {
      getChallenge: vi.fn().mockReturnValue(of(challenge)),
      createChallenge: vi.fn().mockReturnValue(of(challenge)),
      logProgress: vi.fn().mockReturnValue(of(challenge)),
    };

    TestBed.configureTestingModule({
      providers: [
        ReadingChallengeService,
        {provide: ReadingChallengeAPIService, useValue: mockApi},
      ],
    });
    service = TestBed.inject(ReadingChallengeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getChallenge', () => {
    it('should call the API with the year and return the challenge', async () => {
      // Act
      const result = await firstValueFrom(service.getChallenge(2026));

      // Assert
      expect(mockApi.getChallenge).toHaveBeenCalledWith(2026);
      expect(result).toEqual(challenge);
    });

    it('should map a 404 response to null (no challenge yet)', async () => {
      // Arrange
      mockApi.getChallenge.mockReturnValue(throwError(() => ({status: 404})));

      // Act
      const result = await firstValueFrom(service.getChallenge(2026));

      // Assert
      expect(result).toBeNull();
    });

    it('should propagate non-404 errors', async () => {
      // Arrange
      mockApi.getChallenge.mockReturnValue(throwError(() => ({status: 500})));

      // Act & Assert
      await expect(firstValueFrom(service.getChallenge(2026))).rejects.toEqual({status: 500});
    });
  });

  describe('createChallenge', () => {
    it('should call the API with a well-formed create DTO', () => {
      // Act
      service.createChallenge(2026, 30).subscribe();

      // Assert
      expect(mockApi.createChallenge).toHaveBeenCalledWith({year: 2026, goal: 30});
    });
  });

  describe('logProgress', () => {
    it('should call the API with the challenge id and a well-formed progress DTO', () => {
      // Act
      service.logProgress('challenge-1', 3).subscribe();

      // Assert
      expect(mockApi.logProgress).toHaveBeenCalledWith('challenge-1', {booksRead: 3});
    });
  });
});
