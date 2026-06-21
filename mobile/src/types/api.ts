/** Правило 3: строгие контракты ответов бэкенда — без any */

export interface SummonerDTO {
  id: number;
  puuid: string;
  summonerName: string;
  summonerLevel: number;
  profileIconId: number | null;
  tier: string;
  rank: string | null;
  leaguePoints: number;
  winRate: string;
  region: string;
}

export interface TeammateDTO {
  puuid: string;
  summonerName: string;
  profileIconId: number | null;
  gamesTogether: number;
}

export interface MatchDTO {
  matchId: string;
  gameMode: string;
  gameDurationMinutes: number;
  gameCreationTimestamp: number;
  championName: string;
  patchVersion?: string | null;
  kills: number;
  deaths: number;
  assists: number;
  kda: string;
  csScore: number;
  goldEarned: number;
  win: boolean;
  items?: number[];
}

export interface MatchParticipantDTO {
  puuid: string;
  summonerName: string;
  championName: string;
  profileIconId: number | null;
  kills: number;
  deaths: number;
  assists: number;
  kda: string;
  csScore: number;
  goldEarned: number;
  win: boolean;
  team: string;
  items: number[];
}

export interface MatchDetailDTO {
  matchId: string;
  gameMode: string;
  gameDurationMinutes: number;
  gameCreationTimestamp: number;
  patchVersion: string | null;
  participants: MatchParticipantDTO[];
}

export interface UserDTO {
  id: number;
  username: string;
  email: string;
  role: string;
  linkedRiotId: string | null;
  linkedRiotRegion: LoLRegion | string | null;
  linkedRiotPuuid: string | null;
}

export interface LinkRiotAccountRequestDTO {
  riotId: string;
  region: LoLRegion;
}

export interface AuthResponseDTO {
  accessToken: string;
  tokenType: string;
  expiresInMs: number;
  user: UserDTO;
}

export interface LoginRequestDTO {
  username: string;
  password: string;
}

export interface RegisterRequestDTO {
  username: string;
  email: string;
  password: string;
}

export interface FavoriteSummonerDTO {
  favoriteId: number;
  summonerId: number;
  puuid: string;
  summonerName: string;
  summonerLevel: number | null;
  tier: string | null;
  rank: string | null;
  leaguePoints: number | null;
  winRate: string | null;
  addedAt: string;
}

export interface ApiErrorDTO {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  code: string;
  retryAfterSeconds?: number;
}

export type LoLRegion =
  | 'RU'
  | 'EUW'
  | 'EUNE'
  | 'NA'
  | 'KR'
  | 'BR'
  | 'LAN'
  | 'LAS'
  | 'OCE'
  | 'TR'
  | 'JP';

export const LOL_REGIONS: readonly LoLRegion[] = [
  'RU',
  'EUW',
  'EUNE',
  'NA',
  'KR',
  'BR',
  'LAN',
  'LAS',
  'OCE',
  'TR',
  'JP',
];
