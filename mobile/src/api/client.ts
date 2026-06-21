import axios, { AxiosError } from 'axios';
import { getAuthToken } from '../services/cache';
import { resolveApiBaseUrl } from '../config/api';
import type {
  ApiErrorDTO,
  AuthResponseDTO,
  FavoriteSummonerDTO,
  LinkRiotAccountRequestDTO,
  LoginRequestDTO,
  MatchDetailDTO,
  MatchDTO,
  RegisterRequestDTO,
  SummonerDTO,
  TeammateDTO,
  UserDTO,
  LoLRegion,
} from '../types/api';

export const apiClient = axios.create({
  baseURL: resolveApiBaseUrl(),
  timeout: 15_000,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(async (config) => {
  const token = await getAuthToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export function isNetworkError(error: unknown): boolean {
  if (!axios.isAxiosError(error)) {
    return false;
  }
  return error.message === 'Network Error' || error.code === 'ERR_NETWORK';
}

export async function loginUser(payload: LoginRequestDTO): Promise<AuthResponseDTO> {
  const { data } = await apiClient.post<AuthResponseDTO>('/auth/login', payload);
  return data;
}

export async function registerUser(
  payload: RegisterRequestDTO,
): Promise<AuthResponseDTO> {
  const { data } = await apiClient.post<AuthResponseDTO>('/auth/register', payload);
  return data;
}

export async function fetchCurrentUser(): Promise<UserDTO> {
  const { data } = await apiClient.get<UserDTO>('/auth/me');
  return data;
}

export async function linkRiotAccount(
  payload: LinkRiotAccountRequestDTO,
): Promise<UserDTO> {
  const { data } = await apiClient.put<UserDTO>('/auth/me/riot-account', {
    riotId: payload.riotId.trim(),
    region: payload.region,
  });
  return data;
}

export async function unlinkRiotAccount(): Promise<UserDTO> {
  const { data } = await apiClient.delete<UserDTO>('/auth/me/riot-account');
  return data;
}

export async function fetchMySummonerProfile(): Promise<SummonerDTO> {
  const { data } = await apiClient.get<SummonerDTO>('/auth/me/summoner');
  return data;
}

export async function searchSummonerByName(
  name: string,
  region: LoLRegion = 'RU',
): Promise<SummonerDTO> {
  const trimmed = name.trim();
  if (!trimmed) {
    throw new Error('Введите игровое имя призывателя');
  }
  const { data } = await apiClient.get<SummonerDTO>('/summoner/search', {
    params: { name: trimmed, region },
  });
  return data;
}

export async function fetchSummonerByName(name: string): Promise<SummonerDTO> {
  return searchSummonerByName(name);
}

export async function fetchSummonerByPuuid(puuid: string): Promise<SummonerDTO> {
  const trimmed = puuid.trim();
  if (!trimmed) {
    throw new Error('Некорректный PUUID');
  }
  const { data } = await apiClient.get<SummonerDTO>(
    `/summoner/${encodeURIComponent(trimmed)}`,
  );
  return data;
}

export async function fetchMatchHistory(
  puuid: string,
  count = 20,
): Promise<MatchDTO[]> {
  const { data } = await apiClient.get<MatchDTO[]>(
    `/summoner/${encodeURIComponent(puuid)}/matches`,
    { params: { count } },
  );
  return data;
}

export async function fetchTeammates(
  puuid: string,
  matches = 20,
  limit = 20,
): Promise<TeammateDTO[]> {
  const { data } = await apiClient.get<TeammateDTO[]>(
    `/summoner/${encodeURIComponent(puuid)}/teammates`,
    { params: { matches, limit } },
  );
  return data;
}

export async function fetchMatchDetails(matchId: string): Promise<MatchDetailDTO> {
  const { data } = await apiClient.get<MatchDetailDTO>(
    `/summoner/matches/${encodeURIComponent(matchId)}`,
  );
  return data;
}

export async function fetchFavorites(): Promise<FavoriteSummonerDTO[]> {
  const { data } = await apiClient.get<FavoriteSummonerDTO[]>('/summoner/favorites');
  return data;
}

export async function addFavoriteSummoner(
  puuid: string,
): Promise<FavoriteSummonerDTO> {
  const trimmed = puuid.trim();
  if (!trimmed) {
    throw new Error('Некорректный PUUID');
  }
  const { data } = await apiClient.post<FavoriteSummonerDTO>('/summoner/favorites', {
    puuid: trimmed,
  });
  return data;
}

export async function removeFavoriteSummoner(summonerId: number): Promise<void> {
  await apiClient.delete(`/summoner/favorites/${summonerId}`);
}

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiErrorDTO>;
    if (axiosError.response?.data?.message) {
      return axiosError.response.data.message;
    }
    if (isNetworkError(error)) {
      const apiUrl = resolveApiBaseUrl();
      return (
        `Не удалось подключиться к серверу (${apiUrl}). ` +
        'Запустите backend на ПК, подключите телефон к той же Wi‑Fi и в Expo выберите LAN (не Tunnel). ' +
        'При необходимости укажите IP в mobile/.env: EXPO_PUBLIC_API_URL=http://ВАШ_IP:8080/api'
      );
    }
    return axiosError.message || 'Ошибка запроса';
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'Неизвестная ошибка';
}
