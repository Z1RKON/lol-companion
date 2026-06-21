import { create } from 'zustand';
import {
  fetchCurrentUser,
  getErrorMessage,
  linkRiotAccount,
  loginUser,
  registerUser,
  unlinkRiotAccount,
} from '../api/client';
import { clearAuthToken, getAuthToken, saveAuthToken } from '../services/cache';
import { useFavoritesStore } from './useFavoritesStore';
import { useSummonerStore } from './useSummonerStore';
import type { LoLRegion, UserDTO } from '../types/api';

export type AuthStatus = 'IDLE' | 'LOADING' | 'SUCCESS' | 'ERROR';

interface AuthStoreState {
  status: AuthStatus;
  isHydrating: boolean;
  errorMessage: string | null;
  token: string | null;
  user: UserDTO | null;
  isAuthenticated: boolean;
  hydrateSession: () => Promise<void>;
  login: (identifier: string, password: string) => Promise<boolean>;
  register: (username: string, email: string, password: string) => Promise<boolean>;
  linkRiot: (riotId: string, region: LoLRegion) => Promise<boolean>;
  unlinkRiot: () => Promise<boolean>;
  refreshUser: () => Promise<void>;
  logout: () => Promise<void>;
}

export const useAuthStore = create<AuthStoreState>((set) => ({
  status: 'IDLE',
  isHydrating: true,
  errorMessage: null,
  token: null,
  user: null,
  isAuthenticated: false,

  hydrateSession: async () => {
    set({ isHydrating: true });
    try {
      const token = await getAuthToken();
      if (!token) {
        set({
          isHydrating: false,
          isAuthenticated: false,
          token: null,
          user: null,
          status: 'IDLE',
        });
        return;
      }

      const user = await fetchCurrentUser();
      set({
        isHydrating: false,
        isAuthenticated: true,
        token,
        user,
        status: 'SUCCESS',
        errorMessage: null,
      });
    } catch {
      await clearAuthToken();
      set({
        isHydrating: false,
        isAuthenticated: false,
        token: null,
        user: null,
        status: 'IDLE',
        errorMessage: null,
      });
    }
  },

  login: async (identifier, password) => {
    set({ status: 'LOADING', errorMessage: null });
    try {
      const response = await loginUser({
        username: identifier.trim(),
        password,
      });
      await saveAuthToken(response.accessToken);
      set({
        status: 'SUCCESS',
        token: response.accessToken,
        user: response.user,
        isAuthenticated: true,
        errorMessage: null,
      });
      return true;
    } catch (error) {
      set({
        status: 'ERROR',
        errorMessage: getErrorMessage(error),
        isAuthenticated: false,
      });
      return false;
    }
  },

  register: async (username, email, password) => {
    set({ status: 'LOADING', errorMessage: null });
    try {
      const response = await registerUser({ username, email, password });
      await saveAuthToken(response.accessToken);
      set({
        status: 'SUCCESS',
        token: response.accessToken,
        user: response.user,
        isAuthenticated: true,
        errorMessage: null,
      });
      return true;
    } catch (error) {
      set({
        status: 'ERROR',
        errorMessage: getErrorMessage(error),
        isAuthenticated: false,
      });
      return false;
    }
  },

  linkRiot: async (riotId, region) => {
    set({ status: 'LOADING', errorMessage: null });
    try {
      const user = await linkRiotAccount({ riotId, region });
      await useSummonerStore.getState().setRegion(region);
      set({ status: 'SUCCESS', user, errorMessage: null });
      return true;
    } catch (error) {
      set({ status: 'ERROR', errorMessage: getErrorMessage(error) });
      return false;
    }
  },

  unlinkRiot: async () => {
    set({ status: 'LOADING', errorMessage: null });
    try {
      const user = await unlinkRiotAccount();
      set({ status: 'SUCCESS', user, errorMessage: null });
      return true;
    } catch (error) {
      set({ status: 'ERROR', errorMessage: getErrorMessage(error) });
      return false;
    }
  },

  refreshUser: async () => {
    try {
      const user = await fetchCurrentUser();
      set({ user });
    } catch {
      // ignore — session hydrate handles auth errors
    }
  },

  logout: async () => {
    set({ status: 'LOADING' });
    await clearAuthToken();
    useSummonerStore.getState().reset();
    useFavoritesStore.getState().reset();
    set({
      status: 'IDLE',
      errorMessage: null,
      token: null,
      user: null,
      isAuthenticated: false,
    });
  },
}));
