import { create } from 'zustand';
import {
  addFavoriteSummoner,
  fetchFavorites,
  getErrorMessage,
  isNetworkError,
  removeFavoriteSummoner,
} from '../api/client';
import type { FavoriteSummonerDTO } from '../types/api';

export type FavoritesStatus = 'IDLE' | 'LOADING' | 'SUCCESS' | 'ERROR';

interface FavoritesStoreState {
  status: FavoritesStatus;
  errorMessage: string | null;
  favorites: FavoriteSummonerDTO[];
  loadFavorites: () => Promise<void>;
  addFavorite: (puuid: string) => Promise<boolean>;
  removeFavorite: (summonerId: number) => Promise<boolean>;
  reset: () => void;
}

export const useFavoritesStore = create<FavoritesStoreState>((set, get) => ({
  status: 'IDLE',
  errorMessage: null,
  favorites: [],

  loadFavorites: async () => {
    set({ status: 'LOADING', errorMessage: null });
    try {
      const favorites = await fetchFavorites();
      set({ status: 'SUCCESS', favorites, errorMessage: null });
    } catch (error) {
      set({
        status: 'ERROR',
        errorMessage: isNetworkError(error)
          ? 'Нет подключения к сети'
          : getErrorMessage(error),
      });
    }
  },

  addFavorite: async (puuid) => {
    try {
      await addFavoriteSummoner(puuid);
      await get().loadFavorites();
      return true;
    } catch (error) {
      set({ errorMessage: getErrorMessage(error) });
      return false;
    }
  },

  removeFavorite: async (summonerId) => {
    try {
      await removeFavoriteSummoner(summonerId);
      set({
        favorites: get().favorites.filter((f) => f.summonerId !== summonerId),
      });
      return true;
    } catch (error) {
      set({ errorMessage: getErrorMessage(error) });
      return false;
    }
  },

  reset: () => {
    set({
      status: 'IDLE',
      errorMessage: null,
      favorites: [],
    });
  },
}));
