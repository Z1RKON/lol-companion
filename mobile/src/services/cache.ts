import AsyncStorage from '@react-native-async-storage/async-storage';
import type { LoLRegion, MatchDTO, SummonerDTO } from '../types/api';

export const AUTH_TOKEN_KEY = '@lol-companion/authToken';
export const SELECTED_REGION_KEY = '@lol-companion/selectedRegion';
export const OFFLINE_WARNING = 'Отображаются оффлайн-данные';

const SUMMONER_CACHE_PREFIX = '@lol-companion/cache/summoner:';
const SUMMONER_PUUID_CACHE_PREFIX = '@lol-companion/cache/summoner-puuid:';
const MATCHES_CACHE_PREFIX = '@lol-companion/cache/matches:';

function summonerCacheKey(name: string, region: LoLRegion): string {
  return `${SUMMONER_CACHE_PREFIX}${region}:${name.trim().toLowerCase()}`;
}

function summonerPuuidCacheKey(puuid: string): string {
  return `${SUMMONER_PUUID_CACHE_PREFIX}${puuid}`;
}

function matchesCacheKey(puuid: string): string {
  return `${MATCHES_CACHE_PREFIX}${puuid}`;
}

export async function saveAuthToken(token: string): Promise<void> {
  await AsyncStorage.setItem(AUTH_TOKEN_KEY, token);
}

export async function getAuthToken(): Promise<string | null> {
  return AsyncStorage.getItem(AUTH_TOKEN_KEY);
}

export async function clearAuthToken(): Promise<void> {
  await AsyncStorage.removeItem(AUTH_TOKEN_KEY);
}

export async function saveSelectedRegion(region: LoLRegion): Promise<void> {
  await AsyncStorage.setItem(SELECTED_REGION_KEY, region);
}

export async function getSelectedRegion(): Promise<LoLRegion | null> {
  const raw = await AsyncStorage.getItem(SELECTED_REGION_KEY);
  if (!raw) {
    return null;
  }
  return raw as LoLRegion;
}

export async function cacheSummonerProfile(
  name: string,
  region: LoLRegion,
  profile: SummonerDTO,
): Promise<void> {
  await AsyncStorage.setItem(
    summonerCacheKey(name, region),
    JSON.stringify(profile),
  );
  await cacheSummonerByPuuid(profile);
}

export async function cacheSummonerByPuuid(profile: SummonerDTO): Promise<void> {
  await AsyncStorage.setItem(
    summonerPuuidCacheKey(profile.puuid),
    JSON.stringify(profile),
  );
}

export async function getCachedSummonerByPuuid(
  puuid: string,
): Promise<SummonerDTO | null> {
  const raw = await AsyncStorage.getItem(summonerPuuidCacheKey(puuid));
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as SummonerDTO;
  } catch {
    await AsyncStorage.removeItem(summonerPuuidCacheKey(puuid));
    return null;
  }
}

export async function getCachedSummonerProfile(
  name: string,
  region: LoLRegion,
): Promise<SummonerDTO | null> {
  const raw = await AsyncStorage.getItem(summonerCacheKey(name, region));
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as SummonerDTO;
  } catch {
    await AsyncStorage.removeItem(summonerCacheKey(name, region));
    return null;
  }
}

export async function cacheMatchHistory(
  puuid: string,
  matches: MatchDTO[],
): Promise<void> {
  await AsyncStorage.setItem(
    matchesCacheKey(puuid),
    JSON.stringify(matches),
  );
}

export async function getCachedMatchHistory(
  puuid: string,
): Promise<MatchDTO[] | null> {
  const raw = await AsyncStorage.getItem(matchesCacheKey(puuid));
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as MatchDTO[];
  } catch {
    await AsyncStorage.removeItem(matchesCacheKey(puuid));
    return null;
  }
}
