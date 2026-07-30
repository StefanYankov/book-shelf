import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {
  AdminAPIService,
  LockUserRequestDto,
  Pageable,
  PagedResponseAdminUserViewDto,
  PermissionRequestDto,
} from '../../api';

/**
 * Service responsible for administrative user management.
 * Acts as a facade over the generated OpenAPI client (`AdminAPIService`), insulating
 * components from generator quirks: it restores precise return types (the generated
 * mutating calls are typed `any`) and normalizes the permission set into a plain
 * `string[]` for straightforward membership checks in the UI.
 */
@Injectable({providedIn: 'root'})
export class AdminUserService {
  private readonly adminApiService = inject(AdminAPIService);

  /**
   * Retrieves a paginated list of all users for administration.
   * @param pageable Pagination configuration.
   * @returns An observable emitting a paginated result of admin user views.
   */
  getAllUsers(pageable: Pageable): Observable<PagedResponseAdminUserViewDto> {
    return this.adminApiService.getAllUsers(pageable);
  }

  /**
   * Retrieves a user's currently granted permissions as a plain string array.
   * The generated DTO exposes a `Set`; this normalizes it (and the possibly-undefined
   * field) to a simple array so the UI can use straightforward membership checks.
   * @param userId The UUID of the user whose permissions are requested.
   * @returns An observable emitting the user's permission names (empty when none).
   */
  getUserPermissions(userId: string): Observable<string[]> {
    return this.adminApiService.getUserPermissions(userId).pipe(
      map(dto => Array.from(dto.permissions ?? []))
    );
  }

  /**
   * Grants a permission to a user.
   * @param userId The UUID of the target user.
   * @param permission The permission to grant.
   * @param reason The administrative reason for the action.
   * @returns An observable that completes when the grant succeeds.
   */
  grantPermission(userId: string, permission: PermissionRequestDto.PermissionEnum, reason: string): Observable<void> {
    return this.adminApiService.grantPermission(userId, {permission, reason}) as Observable<void>;
  }

  /**
   * Revokes a permission from a user.
   * @param userId The UUID of the target user.
   * @param permission The permission to revoke.
   * @param reason The administrative reason for the action.
   * @returns An observable that completes when the revoke succeeds.
   */
  revokePermission(userId: string, permission: PermissionRequestDto.PermissionEnum, reason: string): Observable<void> {
    return this.adminApiService.revokePermission(userId, {permission, reason}) as Observable<void>;
  }

  /**
   * Locks a user's account.
   * @param userId The UUID of the user to lock.
   * @param reason The administrative reason for the action.
   * @returns An observable that completes when the lock succeeds.
   */
  lockUser(userId: string, reason: string): Observable<void> {
    const dto: LockUserRequestDto = {reason};
    return this.adminApiService.lockUser(userId, dto) as Observable<void>;
  }

  /**
   * Unlocks a user's account.
   * @param userId The UUID of the user to unlock.
   * @param reason The administrative reason for the action.
   * @returns An observable that completes when the unlock succeeds.
   */
  unlockUser(userId: string, reason: string): Observable<void> {
    const dto: LockUserRequestDto = {reason};
    return this.adminApiService.unlockUser(userId, dto) as Observable<void>;
  }
}
