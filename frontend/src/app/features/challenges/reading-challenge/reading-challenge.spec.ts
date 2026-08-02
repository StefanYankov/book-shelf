import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of, throwError} from 'rxjs';
import {beforeEach, describe, expect, it, Mock, vi} from 'vitest';
import {ReadingChallenge} from './reading-challenge';
import {ReadingChallengeService} from '../../../core/services/reading-challenge.service';
import {ToastService} from '../../../core/services/toast.service';
import {ReadingChallengeViewDto} from '../../../api';

interface ReadingChallengeInternals {
  goalInput: { set(v: number | null): void };

  challenge(): ReadingChallengeViewDto | null | undefined;

  loading(): boolean;

  progressPercent(): number;

  setGoal(): void;

  adjustProgress(delta: number): void;
}

describe('ReadingChallenge', () => {
  let fixture: ComponentFixture<ReadingChallenge>;
  let component: ReadingChallenge;
  let mockService: { getChallenge: Mock; createChallenge: Mock; logProgress: Mock };
  let mockToast: { showSuccess: Mock; showError: Mock };

  const year = new Date().getFullYear();

  const challenge = (booksRead: number, completed: boolean): ReadingChallengeViewDto => ({
    id: 'challenge-1',
    userId: 'user-1',
    year,
    goal: 30,
    booksRead,
    completed,
  });

  const internals = (): ReadingChallengeInternals => component as unknown as ReadingChallengeInternals;

  beforeEach(async () => {
    mockService = {
      getChallenge: vi.fn().mockReturnValue(of(null)),
      createChallenge: vi.fn().mockReturnValue(of(challenge(0, false))),
      logProgress: vi.fn().mockReturnValue(of(challenge(6, false))),
    };
    mockToast = {showSuccess: vi.fn(), showError: vi.fn()};

    await TestBed.configureTestingModule({
      imports: [ReadingChallenge],
      providers: [
        {provide: ReadingChallengeService, useValue: mockService},
        {provide: ToastService, useValue: mockToast},
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReadingChallenge);
    component = fixture.componentInstance;
  });

  it('loads the current year challenge on init', () => {
    // Act
    fixture.detectChanges();

    // Assert
    expect(mockService.getChallenge).toHaveBeenCalledWith(year);
  });

  it('shows the set-goal state when no challenge exists', () => {
    // Arrange
    mockService.getChallenge.mockReturnValue(of(null));

    // Act
    fixture.detectChanges();

    // Assert
    expect(internals().challenge()).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Set your');
  });

  it('shows the progress state when a challenge exists', () => {
    // Arrange
    mockService.getChallenge.mockReturnValue(of(challenge(5, false)));

    // Act
    fixture.detectChanges();

    // Assert
    expect(internals().challenge()).not.toBeNull();
    expect(internals().progressPercent()).toBe(17);
  });

  describe('setGoal', () => {
    it('creates a challenge and shows success', () => {
      // Arrange
      fixture.detectChanges();
      internals().goalInput.set(30);

      // Act
      internals().setGoal();

      // Assert
      expect(mockService.createChallenge).toHaveBeenCalledWith(year, 30);
      expect(mockToast.showSuccess).toHaveBeenCalled();
      expect(internals().challenge()).toEqual(challenge(0, false));
    });

    it('rejects a missing or invalid goal', () => {
      // Arrange
      fixture.detectChanges();
      internals().goalInput.set(0);

      // Act
      internals().setGoal();

      // Assert
      expect(mockService.createChallenge).not.toHaveBeenCalled();
      expect(mockToast.showError).toHaveBeenCalled();
    });

    it('surfaces an error when creation fails', () => {
      // Arrange
      mockService.createChallenge.mockReturnValue(throwError(() => ({error: {detail: 'Duplicate'}})));
      fixture.detectChanges();
      internals().goalInput.set(30);

      // Act
      internals().setGoal();

      // Assert
      expect(mockToast.showError).toHaveBeenCalledWith('Duplicate');
    });
  });

  describe('adjustProgress', () => {
    beforeEach(() => {
      mockService.getChallenge.mockReturnValue(of(challenge(5, false)));
      fixture.detectChanges();
    });

    it('increments by setting the new absolute value (current + 1)', () => {
      // Arrange: current is 5

      // Act
      internals().adjustProgress(1);

      // Assert
      expect(mockService.logProgress).toHaveBeenCalledWith('challenge-1', 6);
      expect(internals().challenge()).toEqual(challenge(6, false));
    });

    it('decrements by setting the new absolute value (current - 1)', () => {
      // Arrange: current is 5
      mockService.logProgress.mockReturnValue(of(challenge(4, false)));

      // Act
      internals().adjustProgress(-1);

      // Assert
      expect(mockService.logProgress).toHaveBeenCalledWith('challenge-1', 4);
    });

    it('does not decrement below zero', () => {
      // Arrange: a challenge at 0 books read
      mockService.getChallenge.mockReturnValue(of(challenge(0, false)));
      fixture = TestBed.createComponent(ReadingChallenge);
      component = fixture.componentInstance;
      fixture.detectChanges();

      // Act
      internals().adjustProgress(-1);

      // Assert
      expect(mockService.logProgress).not.toHaveBeenCalled();
    });

    it('celebrates only on the transition into completed', () => {
      // Arrange: incrementing reaches the goal
      mockService.logProgress.mockReturnValue(of(challenge(30, true)));

      // Act
      internals().adjustProgress(1);

      // Assert
      expect(mockToast.showSuccess).toHaveBeenCalledWith('Challenge completed — congratulations! 🎉');
    });

    it('surfaces an error when the update fails', () => {
      // Arrange
      mockService.logProgress.mockReturnValue(throwError(() => ({error: {detail: 'boom'}})));

      // Act
      internals().adjustProgress(1);

      // Assert
      expect(mockToast.showError).toHaveBeenCalledWith('boom');
    });
  });
});
