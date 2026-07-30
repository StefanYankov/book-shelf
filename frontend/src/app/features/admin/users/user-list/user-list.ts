import {ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {Subject, Subscription} from 'rxjs';
import {distinctUntilChanged, map, switchMap, tap} from 'rxjs/operators';
import {PagedResponseAdminUserViewDto, PermissionRequestDto} from '../../../../api';
import {AdminUserService} from '../../../../core/services/admin-user.service';
import {ToastService} from '../../../../core/services/toast.service';
import {UserActionState} from '../../../../core/models/user-action-state.model';
import {PermissionActionState} from '../../../../core/models/permission-action-state.model';

/**
 * Component for displaying and managing a paginated list of users in the admin panel.
 * Synchronizes pagination state directly to route query parameters to support bookmarking.
 * Supports lock/unlock and on-demand fine-grained permission management.
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
  /** The catalog of all known permissions, derived from the generated enum (future-proof). */
  protected readonly allPermissions = Object.values(PermissionRequestDto.PermissionEnum);
  private readonly toastService = inject(ToastService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly data = signal<PagedResponseAdminUserViewDto | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly activeAction = signal<UserActionState | null>(null);
  protected readonly inputReason = signal<string>('');
  protected readonly currentPage = signal(0);

  // --- Permission management (on-demand) state ---
  /** The user whose permission panel is open, or null when closed. */
  protected readonly permissionPanelUser = signal<{ id: string; username: string } | null>(null);
  /** The open user's currently granted permissions, or null while loading. */
  protected readonly panelPermissions = signal<string[] | null>(null);
  /** An in-progress grant/revoke awaiting a reason, or null. */
  protected readonly activePermissionAction = signal<PermissionActionState | null>(null);
  private readonly adminUserService = inject(AdminUserService);

  private readonly loadUsers$ = new Subject<void>();
  private queryParamsSubscription?: Subscription;

  ngOnInit(): void {
    // Pipeline 1: Administrative user loader stream
    this.loadUsers$.pipe(
      tap(() => this.loading.set(true)),
        switchMap(() => this.adminUserService.getAllUsers({page: this.currentPage(), size: 10}))
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
   * Initiates the local interaction context modal form for lock/unlock.
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
   * Submits the lock/unlock state transition request with the inputted reason metadata.
   */
  protected submitAdministrativeAction(): void {
    const action = this.activeAction();
    const reason = this.inputReason().trim();

    if (!action || !reason) {
      this.toastService.showError('An administrative reason must be supplied.');
      return;
    }

    const apiCall = action.type === 'LOCK'
        ? this.adminUserService.lockUser(action.userId, reason)
        : this.adminUserService.unlockUser(action.userId, reason);

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

  // --- Permission management ---

  /**
   * Opens the permission panel for a user and lazily loads their current permissions.
   */
  protected openPermissionPanel(userId: string, username: string): void {
    this.permissionPanelUser.set({id: userId, username});
    this.panelPermissions.set(null); // loading
    this.adminUserService.getUserPermissions(userId).subscribe({
      next: (permissions) => this.panelPermissions.set(permissions),
      error: () => {
        this.toastService.showError('Failed to load user permissions.');
        this.closePermissionPanel();
      }
    });
  }

  /**
   * Closes the permission panel and clears its state.
   */
  protected closePermissionPanel(): void {
    this.permissionPanelUser.set(null);
    this.panelPermissions.set(null);
    this.activePermissionAction.set(null);
    this.inputReason.set('');
  }

  /**
   * Whether the open user currently holds the given permission.
   */
  protected hasPermission(permission: string): boolean {
    return this.panelPermissions()?.includes(permission) ?? false;
  }

  /**
   * Begins a grant/revoke by capturing the intent and prompting for a reason.
   */
  protected startPermissionAction(permission: PermissionRequestDto.PermissionEnum, type: 'GRANT' | 'REVOKE'): void {
    const user = this.permissionPanelUser();
    if (!user) return;
    this.inputReason.set('');
    this.activePermissionAction.set({userId: user.id, username: user.username, permission, type});
  }

  /**
   * Cancels the pending permission grant/revoke (returns to the permission list).
   */
  protected cancelPermissionAction(): void {
    this.activePermissionAction.set(null);
    this.inputReason.set('');
  }

  /**
   * Submits the pending grant/revoke with the supplied reason, then refreshes the panel.
   */
  protected submitPermissionAction(): void {
    const action = this.activePermissionAction();
    const reason = this.inputReason().trim();

    if (!action || !reason) {
      this.toastService.showError('An administrative reason must be supplied.');
      return;
    }

    const apiCall = action.type === 'GRANT'
        ? this.adminUserService.grantPermission(action.userId, action.permission, reason)
        : this.adminUserService.revokePermission(action.userId, action.permission, reason);

    apiCall.subscribe({
      next: () => {
        this.toastService.showSuccess(`Permission ${action.type === 'GRANT' ? 'granted to' : 'revoked from'} ${action.username}.`);
        this.activePermissionAction.set(null);
        this.inputReason.set('');
        // Re-fetch to refresh the panel's current-state display.
        this.adminUserService.getUserPermissions(action.userId).subscribe({
          next: (permissions) => this.panelPermissions.set(permissions)
        });
      },
      error: (err) => {
        this.toastService.showError(err.error?.detail || 'Failed to update permission.');
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
