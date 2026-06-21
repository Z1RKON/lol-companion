import { create } from 'zustand';
import {
  fetchMatchHistory,
  fetchMySummonerProfile,
  fetchSummonerByPuuid,
  fetchTeammates,
  getErrorMessage,
  isNetworkError,
  searchSummonerByName,
} from '../api/client';
import {
  cacheMatchHistory,
  cacheSummonerByPuuid,
  cacheSummonerProfile,
  getCachedMatchHistory,
  getCachedSummonerByPuuid,
  getCachedSummonerProfile,
  getSelectedRegion,
  OFFLINE_WARNING,
  saveSelectedRegion,
} from '../services/cache';
import {
  LOL_REGIONS,
  type LoLRegion,
  type MatchDTO,
  type SummonerDTO,
  type TeammateDTO,
} from '../types/api';
import { normalizeRiotIdQuery } from '../utils/validation';

export type ProfileStatus = 'IDLE' | 'LOADING' | 'SUCCESS' | 'ERROR';

interface SummonerStoreState {
  status: ProfileStatus;
  matchesStatus: ProfileStatus;
  teammatesStatus: ProfileStatus;
  errorMessage: string | null;
  matchesErrorMessage: string | null;
  teammatesErrorMessage: string | null;
  offlineWarning: string | null;
  selectedRegion: LoLRegion;
  summoner: SummonerDTO | null;
  matches: MatchDTO[];
  teammates: TeammateDTO[];
  hydrateRegion: () => Promise<void>;
  setRegion: (region: LoLRegion) => Promise<void>;
  searchSummoner: (name: string) => Promise<void>;
  loadSummonerByPuuid: (puuid: string) => Promise<void>;
  loadMyLinkedProfile: () => Promise<boolean>;
  loadMatches: (count?: number) => Promise<void>;
  loadTeammates: (matches?: number, limit?: number) => Promise<void>;
  reset: () => void;
}

const initialState = {
  status: 'IDLE' as ProfileStatus,
  matchesStatus: 'IDLE' as ProfileStatus,
  teammatesStatus: 'IDLE' as ProfileStatus,
  errorMessage: null,
  matchesErrorMessage: null,
  teammatesErrorMessage: null,
  offlineWarning: null,
  selectedRegion: 'RU' as LoLRegion,
  summoner: null,
  matches: [] as MatchDTO[],
  teammates: [] as TeammateDTO[],
};

function isLoLRegion(value: string): value is LoLRegion {
  return (LOL_REGIONS as readonly string[]).includes(value);
}

export const useSummonerStore = create<SummonerStoreState>((set, get) => ({
  ...initialState,

  hydrateRegion: async () => {
    const saved = await getSelectedRegion();
    if (saved && isLoLRegion(saved)) {
      set({ selectedRegion: saved });
    }
  },

  setRegion: async (region) => {
    await saveSelectedRegion(region);
    set({ selectedRegion: region });
  },

  searchSummoner: async (name: string) => {
    const trimmed = normalizeRiotIdQuery(name);
    const { selectedRegion } = get();

    if (!trimmed) {
      set({
        status: 'ERROR',
        errorMessage: 'Введите игровое имя призывателя',
        offlineWarning: null,
      });
      return;
    }

    set({
      status: 'LOADING',
      errorMessage: null,
      offlineWarning: null,
      summoner: null,
      matches: [],
      teammates: [],
      matchesStatus: 'IDLE',
      matchesErrorMessage: null,
      teammatesStatus: 'IDLE',
      teammatesErrorMessage: null,
    });

    try {
      const profile = await searchSummonerByName(trimmed, selectedRegion);
      await cacheSummonerProfile(trimmed, selectedRegion, profile);
      set({
        status: 'SUCCESS',
        summoner: profile,
        errorMessage: null,
        offlineWarning: null,
      });
    } catch (error) {
      if (isNetworkError(error)) {
        const cached = await getCachedSummonerProfile(trimmed, selectedRegion);
        if (cached) {
          set({
            status: 'SUCCESS',
            summoner: cached,
            errorMessage: null,
            offlineWarning: OFFLINE_WARNING,
          });
          return;
        }
      }

      set({
        status: 'ERROR',
        errorMessage: getErrorMessage(error),
        offlineWarning: null,
      });
    }
  },

  loadSummonerByPuuid: async (puuid: string) => {
    const trimmed = puuid.trim();
    if (!trimmed) {
      set({
        status: 'ERROR',
        errorMessage: 'Некорректный PUUID',
        offlineWarning: null,
      });
      return;
    }

    set({
      status: 'LOADING',
      errorMessage: null,
      offlineWarning: null,
      summoner: null,
      matches: [],
      teammates: [],
      matchesStatus: 'IDLE',
      matchesErrorMessage: null,
      teammatesStatus: 'IDLE',
      teammatesErrorMessage: null,
    });

    try {
      const profile = await fetchSummonerByPuuid(trimmed);
      await cacheSummonerByPuuid(profile);
      if (isLoLRegion(profile.region)) {
        await saveSelectedRegion(profile.region);
        set({ selectedRegion: profile.region });
      }
      set({
        status: 'SUCCESS',
        summoner: profile,
        errorMessage: null,
        offlineWarning: null,
      });
    } catch (error) {
      if (isNetworkError(error)) {
        const cached = await getCachedSummonerByPuuid(trimmed);
        if (cached) {
          set({
            status: 'SUCCESS',
            summoner: cached,
            errorMessage: null,
            offlineWarning: OFFLINE_WARNING,
          });
          return;
        }
      }

      set({
        status: 'ERROR',
        errorMessage: getErrorMessage(error),
        offlineWarning: null,
      });
    }
  },

  loadMyLinkedProfile: async () => {
    set({
      status: 'LOADING',
      errorMessage: null,
      offlineWarning: null,
      summoner: null,
      matches: [],
      teammates: [],
      matchesStatus: 'IDLE',
      matchesErrorMessage: null,
      teammatesStatus: 'IDLE',
      teammatesErrorMessage: null,
    });

    try {
      const profile = await fetchMySummonerProfile();
      if (isLoLRegion(profile.region)) {
        await saveSelectedRegion(profile.region);
        set({ selectedRegion: profile.region });
      }
      await cacheSummonerByPuuid(profile);
      if (profile.summonerName && isLoLRegion(profile.region)) {
        await cacheSummonerProfile(profile.summonerName, profile.region, profile);
      }
      set({
        status: 'SUCCESS',
        summoner: profile,
        errorMessage: null,
        offlineWarning: null,
      });
      return true;
    } catch (error) {
      if (isNetworkError(error)) {
        set({
          status: 'ERROR',
          errorMessage: 'Нет подключения к серверу',
        });
        return false;
      }
      set({
        status: 'ERROR',
        errorMessage: getErrorMessage(error),
      });
      return false;
    }
  },

  loadMatches: async (count = 20) => {
    const { summoner } = get();
    if (!summoner) {
      return;
    }

    set({
      matchesStatus: 'LOADING',
      matchesErrorMessage: null,
    });

    try {
      const matches = await fetchMatchHistory(summoner.puuid, count);
      await cacheMatchHistory(summoner.puuid, matches);
      set({
        matchesStatus: 'SUCCESS',
        matches,
        matchesErrorMessage: null,
        offlineWarning: null,
      });
    } catch (error) {
      if (isNetworkError(error)) {
        const cached = await getCachedMatchHistory(summoner.puuid);
        if (cached) {
          set({
            matchesStatus: 'SUCCESS',
            matches: cached,
            matchesErrorMessage: null,
            offlineWarning: OFFLINE_WARNING,
          });
          return;
        }
      }

      set({
        matchesStatus: 'ERROR',
        matchesErrorMessage: getErrorMessage(error),
      });
    }
  },

  loadTeammates: async (matches = 20, limit = 20) => {
    const { summoner } = get();
    if (!summoner) {
      return;
    }

    set({
      teammatesStatus: 'LOADING',
      teammatesErrorMessage: null,
    });

    try {
      const teammates = await fetchTeammates(summoner.puuid, matches, limit);
      set({
        teammatesStatus: 'SUCCESS',
        teammates,
        teammatesErrorMessage: null,
      });
    } catch (error) {
      set({
        teammatesStatus: 'ERROR',
        teammatesErrorMessage: getErrorMessage(error),
        teammates: [],
      });
    }
  },

  reset: () => {
    set({ ...initialState, selectedRegion: get().selectedRegion });
  },
}));
