import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { UserList } from './user-list';
import { AdminAPIService, PagedResponseAdminUserViewDto } from '../../../../api';
import { of, throwError } from 'rxjs';
import { vi, describe, it, expect, beforeEach } from 'vitest';

describe('UserList Component Specs', () => {
  let component: UserList;
  let fixture: ComponentFixture<UserList>;
  let mockAdminApiService: {
    getAllUsers: ReturnType<typeof vi.fn>;
    lockUser: ReturnType<typeof vi.fn>;
    unlockUser: ReturnType<typeof vi.fn>;
  };

  const mockUserPage: PagedResponseAdminUserViewDto = {
    content: [{ id: '1', username: 'test', email: 'test@test.com', firstName: 'Test', lastName: 'User', isActive: true, isEmailVerified: true, role: 'ROLE_USER' }],
    pageNumber: 0,
    pageSize: 10,
    totalElements: 1,
    totalPages: 1,
    isLast: true
  };

  beforeEach(async () => {
    mockAdminApiService = {
      getAllUsers: vi.fn().mockReturnValue(of(mockUserPage)),
      lockUser: vi.fn().mockReturnValue(of(undefined)),
      unlockUser: vi.fn().mockReturnValue(of(undefined))
    };

    await TestBed.configureTestingModule({
      imports: [UserList],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AdminAPIService, useValue: mockAdminApiService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserList);
    component = fixture.componentInstance;
  });

  const stabilizeState = async () => {
    fixture.detectChanges();
    await Promise.resolve();
  };

  it('should create and load users on init', async () => {
    await stabilizeState();
    expect(component).toBeTruthy();
    expect(mockAdminApiService.getAllUsers).toHaveBeenCalledWith({ page: 0, size: 10 });
    expect(component.data()?.content?.length).toBe(1);
    expect(component.loading()).toBe(false);
  });

  it('should handle error on user load', async () => {
    mockAdminApiService.getAllUsers.mockReturnValue(throwError(() => new Error('API Error')));
    await stabilizeState();
    expect(component.error()).toBe('Failed to load users.');
    expect(component.loading()).toBe(false);
  });

  it('should call lockUser and reload data on confirmation', async () => {
    window.prompt = vi.fn().mockReturnValue('Test reason');
    await stabilizeState();
    component.onLockUser('1');
    expect(mockAdminApiService.lockUser).toHaveBeenCalledWith('1', { reason: 'Test reason' });
    await stabilizeState();
    expect(mockAdminApiService.getAllUsers).toHaveBeenCalledTimes(2);
  });

  it('should not call lockUser if prompt is cancelled', async () => {
    window.prompt = vi.fn().mockReturnValue(null);
    await stabilizeState();
    component.onLockUser('1');
    expect(mockAdminApiService.lockUser).not.toHaveBeenCalled();
  });

  it('should call unlockUser and reload data on confirmation', async () => {
    window.prompt = vi.fn().mockReturnValue('Test reason');
    await stabilizeState();
    component.onUnlockUser('1');
    expect(mockAdminApiService.unlockUser).toHaveBeenCalledWith('1', { reason: 'Test reason' });
    await stabilizeState();
    expect(mockAdminApiService.getAllUsers).toHaveBeenCalledTimes(2);
  });

  it('should navigate to the next page', async () => {
    // 1. Stabilize the initial load configuration first
    await stabilizeState();
    expect(mockAdminApiService.getAllUsers).toHaveBeenCalledTimes(1);

    // 2. Clear call history to isolate step metrics safely
    mockAdminApiService.getAllUsers.mockClear();

    // 3. Mutate the target parameters and step execution forward
    component.data.set({ ...mockUserPage, isLast: false });
    component.nextPage();
    await stabilizeState();

    expect(component.currentPage()).toBe(1);
    expect(mockAdminApiService.getAllUsers).toHaveBeenCalledTimes(1); // Cleared baseline calls means 1 fresh action invocation
  });

  it('should not navigate to the next page if on the last page', async () => {
    component.data.set({ ...mockUserPage, isLast: true });
    await stabilizeState();
    component.nextPage();
    await stabilizeState();
    expect(component.currentPage()).toBe(0);
    expect(mockAdminApiService.getAllUsers).toHaveBeenCalledTimes(1);
  });

  it('should navigate to the previous page', async () => {
    component.currentPage.set(1);
    await stabilizeState();
    component.previousPage();
    await stabilizeState();
    expect(component.currentPage()).toBe(0);
    expect(mockAdminApiService.getAllUsers).toHaveBeenCalledTimes(2);
  });

  it('should not navigate to the previous page if on the first page', async () => {
    await stabilizeState();
    component.previousPage();
    await stabilizeState();
    expect(component.currentPage()).toBe(0);
    expect(mockAdminApiService.getAllUsers).toHaveBeenCalledTimes(1);
  });
});
