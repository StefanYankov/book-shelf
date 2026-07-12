import { ChangeDetectionStrategy, Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, Subscription } from 'rxjs';
import { switchMap, tap, map, distinctUntilChanged } from 'rxjs/operators';
import { AdminAPIService, LockUserRequestDto, PagedResponseAdminUserViewDto } from '../../../../api';
import { ToastService } from '../../../../core/services/toast.service';
import { UserActionState } from '../../../../core/models/user-action-state.model';

/**
 * Component for displaying and managing a paginated list of users in the admin panel.
 * Synchronizes pagination state directly to route query parameters to support bookmarking.
 */
@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-list.html',
  styleUrl: './user-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserList implements OnInit, OnDestroy {
  private readonly adminApiService = inject(AdminAPIService);
  private readonly toastService = inject(ToastService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly data = signal<PagedResponseAdminUserViewDto | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly activeAction = signal<UserActionState | null>(null);
  protected readonly inputReason = signal<string>('');
  protected readonly currentPage = signal(0);

  private readonly loadUsers$ = new Subject<void>();
  private queryParamsSubscription?: Subscription;

  ngOnInit(): void {
    // Pipeline 1: Administrative user loader stream
    this.loadUsers$.pipe(
      tap(() => this.loading.set(true)),
      switchMap(() => this.adminApiService.getAllUsers({ page: this.currentPage(), size: 10 }))
    ).subscribe({
      next: (response) => {
        this.data.set(response);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load user directory.');
        this.loading.set(false);
      }
    });

    // Pipeline 2: Query parameter observer syncing router states
    this.queryParamsSubscription = this.route.queryParams.pipe(
      map(params => {
        const page = parseInt(params['page'], 10);
        return isNaN(page) ? 0 : page;
      }),
      distinctUntilChanged(),
      tap(page => this.currentPage.set(page)),
      tap(() => this.loadUsers())
    ).subscribe();
  }

  ngOnDestroy(): void {
    this.queryParamsSubscription?.unsubscribe();
  }

  /**
   * Triggers the user loading pipeline.
   */
  protected loadUsers(): void {
    this.loadUsers$.next();
  }

  /**
   * Initiates the local interaction context modal form.
   */
  protected openActionForm(userId: string, username: string, type: 'LOCK' | 'UNLOCK'): void {
    this.inputReason.set('');
    this.activeAction.set({ userId, username, type });
  }

  /**
   * Closes the dynamic interaction panel.
   */
  protected cancelAction(): void {
    this.activeAction.set(null);
    this.inputReason.set('');
  }

  /**
   * Submits the state transition request securely with the inputted feedback metadata.
   */
  protected submitAdministrativeAction(): void {
    const action = this.activeAction();
    const reason = this.inputReason().trim();

    if (!action || !reason) {
      this.toastService.showError('An administrative reason must be supplied.');
      return;
    }

    const dto: LockUserRequestDto = { reason };

    const apiCall = action.type === 'LOCK'
      ? this.adminApiService.lockUser(action.userId, dto)
      : this.adminApiService.unlockUser(action.userId, dto);

    apiCall.subscribe({
      next: () => {
        this.toastService.showSuccess(`User ${action.username} ${action.type.toLowerCase()}ed successfully.`);
        this.cancelAction();
        this.loadUsers();
      },
      error: (err) => {
        this.toastService.showError(err.error?.detail || `Failed to ${action.type.toLowerCase()} user.`);
      }
    });
  }

  /**
   * Triggers navigation route change to the next directory index.
   */
  protected nextPage(): void {
    if (this.data()?.isLast) return;
    this.updatePageRoute(this.currentPage() + 1);
  }

  /**
   * Triggers navigation route change to the previous directory index.
   */
  protected previousPage(): void {
    if (this.currentPage() === 0) return;
    this.updatePageRoute(this.currentPage() - 1);
  }

  private updatePageRoute(page: number): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page },
      queryParamsHandling: 'merge'
    });
  }
}
