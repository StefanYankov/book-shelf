import {ChangeDetectionStrategy, Component, computed, inject, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReadingChallengeService} from '../../../core/services/reading-challenge.service';
import {ToastService} from '../../../core/services/toast.service';
import {ReadingChallengeViewDto} from '../../../api';

/**
 * Page component for the user's yearly reading challenge.
 * Presents a goal-setting form when no challenge exists for the current year, and a
 * progress view once a challenge is active. Progress is adjusted with +1 / -1 steppers,
 * which set the absolute number of books read — allowing corrections, including reducing
 * progress below the goal to un-complete a challenge.
 */
@Component({
  selector: 'app-reading-challenge',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reading-challenge.html',
  styleUrl: './reading-challenge.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReadingChallenge implements OnInit {
  /** The year the challenge targets (the current calendar year). */
  protected readonly year = new Date().getFullYear();
  /** The loaded challenge, or null when none exists for the year. `undefined` while loading. */
  protected readonly challenge = signal<ReadingChallengeViewDto | null | undefined>(undefined);
  protected readonly loading = signal(true);
  /** Goal input for the set-goal form. */
  protected readonly goalInput = signal<number | null>(null);
  /** Progress percentage (0–100), clamped, for the progress bar. */
  protected readonly progressPercent = computed(() => {
    const c = this.challenge();
    if (!c || c.goal == null || c.goal <= 0 || c.booksRead == null) {
      return 0;
    }
    return Math.min(100, Math.round((c.booksRead / c.goal) * 100));
  });

  private readonly challengeService = inject(ReadingChallengeService);
  private readonly toastService = inject(ToastService);

  ngOnInit(): void {
    this.loadChallenge();
  }

  /**
   * Creates a challenge for the current year with the entered goal.
   */
  protected setGoal(): void {
    const goal = this.goalInput();
    if (goal == null || goal < 1) {
      this.toastService.showError('Please enter a goal of at least 1 book.');
      return;
    }

    this.challengeService.createChallenge(this.year, goal).subscribe({
      next: (created) => {
        this.challenge.set(created);
        this.toastService.showSuccess(`Your ${this.year} reading challenge is set!`);
      },
      error: (err) => {
        this.toastService.showError(err.error?.detail || 'Failed to create your challenge.');
      },
    });
  }

  /**
   * Adjusts progress by the given delta (+1 or -1), setting the new absolute books-read value.
   * @param delta The change to apply to the current progress.
   */
  protected adjustProgress(delta: number): void {
    const current = this.challenge();
    if (!current || current.id == null || current.booksRead == null) {
      return;
    }

    const next = current.booksRead + delta;
    if (next < 0) {
      return;
    }

    const wasCompleted = current.completed;

    this.challengeService.logProgress(current.id, next).subscribe({
      next: (updated) => {
        this.challenge.set(updated);
        if (updated.completed && !wasCompleted) {
          this.toastService.showSuccess('Challenge completed — congratulations! 🎉');
        }
      },
      error: (err) => {
        this.toastService.showError(err.error?.detail || 'Failed to update progress.');
      },
    });
  }

  private loadChallenge(): void {
    this.loading.set(true);
    this.challengeService.getChallenge(this.year).subscribe({
      next: (result) => {
        this.challenge.set(result);
        this.loading.set(false);
      },
      error: () => {
        this.toastService.showError('Failed to load your reading challenge.');
        this.loading.set(false);
      },
    });
  }
}
