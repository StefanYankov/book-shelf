import {PermissionRequestDto} from '../../api';

/**
 * Transient state for an in-progress permission grant/revoke action, captured while
 * the admin supplies a reason. Mirrors the lock/unlock action-state pattern.
 */
export interface PermissionActionState {
  userId: string;
  username: string;
  permission: PermissionRequestDto.PermissionEnum;
  type: 'GRANT' | 'REVOKE';
}
