import { hasLinkedRiotAccount } from '../riotAccount';
import type { UserDTO } from '../../types/api';

describe('riotAccount', () => {
  it('hasLinkedRiotAccount false без данных', () => {
    expect(hasLinkedRiotAccount(null)).toBe(false);
    expect(hasLinkedRiotAccount(undefined)).toBe(false);
  });

  it('hasLinkedRiotAccount true при полной привязке', () => {
    const user: UserDTO = {
      id: 1,
      username: 'player',
      email: 'p@mail.com',
      role: 'USER',
      linkedRiotId: 'Player#TAG',
      linkedRiotRegion: 'RU',
      linkedRiotPuuid: 'puuid-123',
    };
    expect(hasLinkedRiotAccount(user)).toBe(true);
  });

  it('hasLinkedRiotAccount false при частичной привязке', () => {
    const user: UserDTO = {
      id: 1,
      username: 'player',
      email: 'p@mail.com',
      role: 'USER',
      linkedRiotId: 'Player#TAG',
      linkedRiotRegion: 'RU',
      linkedRiotPuuid: null,
    };
    expect(hasLinkedRiotAccount(user)).toBe(false);
  });
});
