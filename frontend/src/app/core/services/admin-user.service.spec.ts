import {TestBed} from '@angular/core/testing';
import {beforeEach, describe, expect, it, Mock, vi} from 'vitest';
import {of} from 'rxjs';
import {AdminUserService} from './admin-user.service';
import {AdminAPIService, PermissionRequestDto, UserPermissionsDto} from '../../api';
import {PageQuery} from '../models/page-query';

describe('AdminUserService', () => {
  let service: AdminUserService;
  let mockAdminApiService: {
    getAllUsers: Mock;
    getUserPermissions: Mock;
    grantPermission: Mock;
    revokePermission: Mock;
    lockUser: Mock;
    unlockUser: Mock;
  };
  const userId = 'user-123';

  beforeEach(() => {
    mockAdminApiService = {
      getAllUsers: vi.fn().mockReturnValue(of({})),
      getUserPermissions: vi.fn().mockReturnValue(of({
        userId,
        permissions: new Set(['MODERATE_REVIEWS'])
      } as UserPermissionsDto)),
      grantPermission: vi.fn().mockReturnValue(of(undefined)),
      revokePermission: vi.fn().mockReturnValue(of(undefined)),
      lockUser: vi.fn().mockReturnValue(of(undefined)),
      unlockUser: vi.fn().mockReturnValue(of(undefined)),
    };
    TestBed.configureTestingModule({
      providers: [
        AdminUserService,
        {provide: AdminAPIService, useValue: mockAdminApiService},
      ],
    });
    service = TestBed.inject(AdminUserService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAllUsers', () => {
    it('should unpack the pageable into positional page, size and sort', () => {
      const pageable: PageQuery = {page: 0, size: 10, sort: ['username,asc']};
      service.getAllUsers(pageable);
      expect(mockAdminApiService.getAllUsers).toHaveBeenCalledWith(pageable.page, pageable.size, pageable.sort);
    });
  });

  describe('getUserPermissions', () => {
    it('should normalize the permission Set into a plain string array', () => {
      // Arrange handled in beforeEach (permissions = Set(['MODERATE_REVIEWS']))
      let result: string[] | undefined;
      // Act
      service.getUserPermissions(userId).subscribe(perms => (result = perms));
      // Assert
      expect(mockAdminApiService.getUserPermissions).toHaveBeenCalledWith(userId);
      expect(result).toEqual(['MODERATE_REVIEWS']);
    });

    it('should return an empty array when the user has no permissions', () => {
      // Arrange: undefined permissions field
      mockAdminApiService.getUserPermissions.mockReturnValue(of({userId} as UserPermissionsDto));
      let result: string[] | undefined;
      // Act
      service.getUserPermissions(userId).subscribe(perms => (result = perms));
      // Assert
      expect(result).toEqual([]);
    });
  });

  describe('grantPermission', () => {
    it('should delegate with a well-formed PermissionRequestDto', () => {
      service.grantPermission(userId, PermissionRequestDto.PermissionEnum.ModerateReviews, 'Trusted').subscribe();
      expect(mockAdminApiService.grantPermission).toHaveBeenCalledWith(userId, {
        permission: 'MODERATE_REVIEWS',
        reason: 'Trusted',
      });
    });
  });

  describe('revokePermission', () => {
    it('should delegate with a well-formed PermissionRequestDto', () => {
      service.revokePermission(userId, PermissionRequestDto.PermissionEnum.ModerateReviews, 'No longer needed').subscribe();
      expect(mockAdminApiService.revokePermission).toHaveBeenCalledWith(userId, {
        permission: 'MODERATE_REVIEWS',
        reason: 'No longer needed',
      });
    });
  });

  describe('lockUser', () => {
    it('should delegate a permanent lock with just the reason', () => {
      service.lockUser(userId, 'Spam').subscribe();
      expect(mockAdminApiService.lockUser).toHaveBeenCalledWith(userId, {reason: 'Spam'});
    });

    it('should delegate a temporary lock with the duration in hours', () => {
      service.lockUser(userId, 'Cooling-off', 24).subscribe();
      expect(mockAdminApiService.lockUser).toHaveBeenCalledWith(userId, {reason: 'Cooling-off', lockDurationHours: 24});
    });
  });

  describe('unlockUser', () => {
    it('should delegate with a reason DTO', () => {
      service.unlockUser(userId, 'Cleared').subscribe();
      expect(mockAdminApiService.unlockUser).toHaveBeenCalledWith(userId, {reason: 'Cleared'});
    });
  });
});
