export interface UserActionState {
  userId: string;
  username: string;
  type: 'LOCK' | 'UNLOCK';
}
