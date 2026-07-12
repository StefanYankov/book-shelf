import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, BehaviorSubject } from 'rxjs';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { UserList } from './user-list';
import { AdminAPIService, PagedResponseAdminUserViewDto } from '../../../../api';
import { ToastService } from '../../../../core/services/toast.service';

describe('UserList Component Deep-Linking Spec Tests', () => {
  let component: UserList;
  let fixture: ComponentFixture<UserList>;
  let mockAdminApiService: {
    getAllUsers: ReturnType<typeof vi.fn>;
    lockUser: ReturnType<typeof vi.fn>;
    unlockUser: ReturnType<typeof vi.fn>;
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

  beforeEach(async () => {
    mockAdminApiService = {
      getAllUsers: vi.fn().mockReturnValue(of(mockUserPage)),
      lockUser: vi.fn().mockReturnValue(of(undefined)),
      unlockUser: vi.fn().mockReturnValue(of(undefined))
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
        { provide: AdminAPIService, useValue: mockAdminApiService },
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

    expect(component['currentPage']()).toBe(2);
    expect(mockAdminApiService.getAllUsers).toHaveBeenCalledWith({ page: 2, size: 10 });
  });

  it('should trigger router state changes when moving pages forward', async () => {
    await stabilizeState();
    expect(mockAdminApiService.getAllUsers).toHaveBeenCalledTimes(1);

    component['nextPage']();
    await stabilizeState();

    expect(mockRouter.navigate).toHaveBeenCalledWith([], {
      relativeTo: expect.any(Object),
      queryParams: { page: 1 },
      queryParamsHandling: 'merge'
    });
  });

  it('should invoke lockUser on submit and trigger reloading routines', async () => {
    await stabilizeState();
    component['openActionForm']('1', 'test', 'LOCK');
    await stabilizeState(); // Wait for modal to be visible
    component['inputReason'].set('Violation of Terms of Service');

    component['submitAdministrativeAction']();
    await stabilizeState();

    expect(mockAdminApiService.lockUser).toHaveBeenCalledWith('1', { reason: 'Violation of Terms of Service' });
    expect(mockToastService.showSuccess).toHaveBeenCalled();
  });
});
