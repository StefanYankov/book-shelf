import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {ActivatedRoute, Router} from '@angular/router';
import {BehaviorSubject, of, throwError} from 'rxjs';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {UserList} from './user-list';
import {AdminUserService} from '../../../../core/services/admin-user.service';
import {ToastService} from '../../../../core/services/toast.service';
import {PagedResponseAdminUserViewDto, PermissionRequestDto} from '../../../../api';

/**
 * Test-only view of UserList's protected members, reached via a typed cast so tests can
 * drive the component with plain dot notation (no bracket access to mangle), without
 * widening production visibility.
 */
interface UserListInternals {
  inputReason: { set(value: string): void };
  lockType: { set(value: 'permanent' | 'temporary'): void };
  lockDurationHours: { set(value: number): void };

  currentPage(): number;

  panelPermissions(): string[] | null;

  permissionPanelUser(): { id: string; username: string } | null;

  hasPermission(permission: string): boolean;

  nextPage(): void;

  openActionForm(userId: string, username: string, type: 'LOCK' | 'UNLOCK'): void;

  submitAdministrativeAction(): void;

  openPermissionPanel(userId: string, username: string): void;

  startPermissionAction(permission: PermissionRequestDto.PermissionEnum, type: 'GRANT' | 'REVOKE'): void;

  submitPermissionAction(): void;
}

describe('UserList Component Deep-Linking Spec Tests', () => {
  let component: UserList;
  let fixture: ComponentFixture<UserList>;
  let mockAdminUserService: {
    getAllUsers: ReturnType<typeof vi.fn>;
    lockUser: ReturnType<typeof vi.fn>;
    unlockUser: ReturnType<typeof vi.fn>;
    getUserPermissions: ReturnType<typeof vi.fn>;
    grantPermission: ReturnType<typeof vi.fn>;
    revokePermission: ReturnType<typeof vi.fn>;
  };
  let mockToastService: { showSuccess: ReturnType<typeof vi.fn>; showError: ReturnType<typeof vi.fn> };
  let mockRouter: { navigate: ReturnType<typeof vi.fn> };
  let queryParamsSubject: BehaviorSubject<{ page?: string }>;

  const mockUserPage: PagedResponseAdminUserViewDto = {
    content: [{ id: '1', username: 'test', email: 'test@test.com', firstName: 'Test', lastName: 'User', isActive: true, isEmailVerified: true, role: 'ROLE_USER' }],
    pageNumber: 0,
    pageSize: 10,
    totalElements: 1,
    totalPages: 3,
    isLast: false
  };

  /** Typed access to the component's protected members. */
  const internals = (): UserListInternals => component as unknown as UserListInternals;

  beforeEach(async () => {
    mockAdminUserService = {
      getAllUsers: vi.fn().mockReturnValue(of(mockUserPage)),
      lockUser: vi.fn().mockReturnValue(of(undefined)),
      unlockUser: vi.fn().mockReturnValue(of(undefined)),
      getUserPermissions: vi.fn().mockReturnValue(of([])),
      grantPermission: vi.fn().mockReturnValue(of(undefined)),
      revokePermission: vi.fn().mockReturnValue(of(undefined))
    };
    mockToastService = {
      showSuccess: vi.fn(),
      showError: vi.fn()
    };

    queryParamsSubject = new BehaviorSubject<{ page?: string }>({ page: '0' });
    mockRouter = {
      navigate: vi.fn().mockImplementation((commands, extras) => {
        if (extras?.queryParams) {
          queryParamsSubject.next({ page: extras.queryParams.page.toString() });
        }
        return Promise.resolve(true);
      })
    };

    await TestBed.configureTestingModule({
      imports: [UserList],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: AdminUserService, useValue: mockAdminUserService},
        { provide: ToastService, useValue: mockToastService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: { queryParams: queryParamsSubject.asObservable() }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserList);
    component = fixture.componentInstance;
  });

  const stabilizeState = async () => {
    fixture.detectChanges();
    await Promise.resolve();
  };

  it('should retrieve route parameters and set page during startup', async () => {
    queryParamsSubject.next({ page: '2' });
    await stabilizeState();

    expect(internals().currentPage()).toBe(2);
    expect(mockAdminUserService.getAllUsers).toHaveBeenCalledWith({page: 2, size: 10});
  });

  it('should trigger router state changes when moving pages forward', async () => {
    await stabilizeState();
    expect(mockAdminUserService.getAllUsers).toHaveBeenCalledTimes(1);

    internals().nextPage();
    await stabilizeState();

    expect(mockRouter.navigate).toHaveBeenCalledWith([], {
      relativeTo: expect.any(Object),
      queryParams: { page: 1 },
      queryParamsHandling: 'merge'
    });
  });

  it('should invoke lockUser permanently on submit and trigger reloading routines', async () => {
    await stabilizeState();
    internals().openActionForm('1', 'test', 'LOCK');
    await stabilizeState();
    internals().inputReason.set('Violation of Terms of Service');

    internals().submitAdministrativeAction();
    await stabilizeState();

    // permanent lock -> duration is undefined
    expect(mockAdminUserService.lockUser).toHaveBeenCalledWith('1', 'Violation of Terms of Service', undefined);
    expect(mockToastService.showSuccess).toHaveBeenCalled();
  });

  it('should invoke lockUser with a duration for a temporary lock', async () => {
    await stabilizeState();
    internals().openActionForm('1', 'test', 'LOCK');
    await stabilizeState();
    internals().inputReason.set('Cooling-off period');
    internals().lockType.set('temporary');
    internals().lockDurationHours.set(48);

    internals().submitAdministrativeAction();
    await stabilizeState();

    expect(mockAdminUserService.lockUser).toHaveBeenCalledWith('1', 'Cooling-off period', 48);
    expect(mockToastService.showSuccess).toHaveBeenCalled();
  });

  it('should reject a temporary lock submitted with a non-positive duration', async () => {
    await stabilizeState();
    internals().openActionForm('1', 'test', 'LOCK');
    await stabilizeState();
    internals().inputReason.set('Cooling-off period');
    internals().lockType.set('temporary');
    internals().lockDurationHours.set(0);

    internals().submitAdministrativeAction();
    await stabilizeState();

    expect(mockAdminUserService.lockUser).not.toHaveBeenCalled();
    expect(mockToastService.showError).toHaveBeenCalled();
  });

  describe('Permission management', () => {
    it('should lazily load a user\'s permissions when the panel opens', async () => {
      mockAdminUserService.getUserPermissions.mockReturnValue(of(['MODERATE_REVIEWS']));
      await stabilizeState();

      internals().openPermissionPanel('1', 'test');
      await stabilizeState();

      expect(mockAdminUserService.getUserPermissions).toHaveBeenCalledWith('1');
      expect(internals().panelPermissions()).toEqual(['MODERATE_REVIEWS']);
      expect(internals().hasPermission('MODERATE_REVIEWS')).toBe(true);
    });

    it('should grant a permission and refresh the panel', async () => {
      // Initially none, then granted after refresh
      mockAdminUserService.getUserPermissions
        .mockReturnValueOnce(of([]))
        .mockReturnValueOnce(of(['MODERATE_REVIEWS']));
      await stabilizeState();

      internals().openPermissionPanel('1', 'test');
      await stabilizeState();

      internals().startPermissionAction(PermissionRequestDto.PermissionEnum.ModerateReviews, 'GRANT');
      internals().inputReason.set('Trusted contributor');
      internals().submitPermissionAction();
      await stabilizeState();

      expect(mockAdminUserService.grantPermission).toHaveBeenCalledWith('1', 'MODERATE_REVIEWS', 'Trusted contributor');
      expect(mockToastService.showSuccess).toHaveBeenCalled();
      // Panel refreshed with the newly granted permission
      expect(internals().panelPermissions()).toEqual(['MODERATE_REVIEWS']);
    });

    it('should revoke a permission and refresh the panel', async () => {
      mockAdminUserService.getUserPermissions
        .mockReturnValueOnce(of(['MODERATE_REVIEWS']))
        .mockReturnValueOnce(of([]));
      await stabilizeState();

      internals().openPermissionPanel('1', 'test');
      await stabilizeState();

      internals().startPermissionAction(PermissionRequestDto.PermissionEnum.ModerateReviews, 'REVOKE');
      internals().inputReason.set('No longer needed');
      internals().submitPermissionAction();
      await stabilizeState();

      expect(mockAdminUserService.revokePermission).toHaveBeenCalledWith('1', 'MODERATE_REVIEWS', 'No longer needed');
      expect(internals().panelPermissions()).toEqual([]);
    });

    it('should reject a permission action submitted without a reason', async () => {
      mockAdminUserService.getUserPermissions.mockReturnValue(of([]));
      await stabilizeState();
      internals().openPermissionPanel('1', 'test');
      await stabilizeState();

      internals().startPermissionAction(PermissionRequestDto.PermissionEnum.ModerateReviews, 'GRANT');
      internals().inputReason.set('   '); // blank
      internals().submitPermissionAction();
      await stabilizeState();

      expect(mockToastService.showError).toHaveBeenCalled();
      expect(mockAdminUserService.grantPermission).not.toHaveBeenCalled();
    });

    it('should close the panel and surface an error when permission loading fails', async () => {
      mockAdminUserService.getUserPermissions.mockReturnValue(throwError(() => ({error: {detail: 'boom'}})));
      await stabilizeState();

      internals().openPermissionPanel('1', 'test');
      await stabilizeState();

      expect(mockToastService.showError).toHaveBeenCalledWith('Failed to load user permissions.');
      expect(internals().permissionPanelUser()).toBeNull();
    });
  });
});
