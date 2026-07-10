import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminAPIService, LockUserRequestDto, PagedResponseAdminUserViewDto } from '../../../../api';
import { Subject, switchMap, tap } from 'rxjs';

/**
 * @fileoverview Component for displaying and managing a paginated list of users in the admin panel.
 */

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-list.html',
  styleUrl: './user-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserList implements OnInit {
  private readonly adminApiService = inject(AdminAPIService);

  // State Signals
  data = signal<PagedResponseAdminUserViewDto | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  // Pagination
  currentPage = signal(0);
  private loadUsers$ = new Subject<void>();

  ngOnInit(): void {
    this.loadUsers$.pipe(
      tap(() => this.loading.set(true)),
      switchMap(() => this.adminApiService.getAllUsers({ page: this.currentPage(), size: 10 }))
    ).subscribe({
      next: (response) => {
        this.data.set(response);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load users.');
        this.loading.set(false);
      }
    });

    this.loadUsers();
  }

  /**
   * Triggers the user loading pipeline.
   */
  loadUsers(): void {
    this.loadUsers$.next();
  }

  /**
   * Handles the action to lock a user's account.
   * @param userId The ID of the user to lock.
   */
  onLockUser(userId: string): void {
    const reason = prompt('Please provide a reason for locking the user:');
    if (reason) {
      const dto: LockUserRequestDto = { reason };
      this.adminApiService.lockUser(userId, dto).subscribe(() => this.loadUsers());
    }
  }

  /**
   * Handles the action to unlock a user's account.
   * @param userId The ID of the user to unlock.
   */
  onUnlockUser(userId: string): void {
    const reason = prompt('Please provide a reason for unlocking the user:');
    if (reason) {
      const dto: LockUserRequestDto = { reason };
      this.adminApiService.unlockUser(userId, dto).subscribe(() => this.loadUsers());
    }
  }

  /**
   * Navigates to the next page of users.
   */
  nextPage(): void {
    if (this.data()?.isLast) return;
    this.currentPage.update(page => page + 1);
    this.loadUsers();
  }

  /**
   * Navigates to the previous page of users.
   */
  previousPage(): void {
    if (this.currentPage() === 0) return;
    this.currentPage.update(page => page - 1);
    this.loadUsers();
  }
}
