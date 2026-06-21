import { create } from 'zustand';
import { fetchMatchDetails, getErrorMessage, isNetworkError } from '../api/client';
import type { MatchDetailDTO } from '../types/api';

export type MatchDetailStatus = 'IDLE' | 'LOADING' | 'SUCCESS' | 'ERROR';

interface MatchDetailStoreState {
  status: MatchDetailStatus;
  errorMessage: string | null;
  detail: MatchDetailDTO | null;
  loadMatchDetail: (matchId: string) => Promise<void>;
  reset: () => void;
}

export const useMatchDetailStore = create<MatchDetailStoreState>((set) => ({
  status: 'IDLE',
  errorMessage: null,
  detail: null,

  loadMatchDetail: async (matchId) => {
    set({ status: 'LOADING', errorMessage: null, detail: null });
    try {
      const detail = await fetchMatchDetails(matchId);
      set({ status: 'SUCCESS', detail, errorMessage: null });
    } catch (error) {
      set({
        status: 'ERROR',
        errorMessage: isNetworkError(error)
          ? 'Нет подключения к сети'
          : getErrorMessage(error),
      });
    }
  },

  reset: () => {
    set({ status: 'IDLE', errorMessage: null, detail: null });
  },
}));
