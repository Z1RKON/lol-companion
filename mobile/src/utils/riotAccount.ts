import type { UserDTO } from '../types/api';

export function hasLinkedRiotAccount(user: UserDTO | null | undefined): boolean {
  return Boolean(user?.linkedRiotId && user?.linkedRiotPuuid);
}
