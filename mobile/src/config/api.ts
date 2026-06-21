import { getExpoGoProjectConfig } from 'expo';
import Constants from 'expo-constants';
import { Platform } from 'react-native';

const API_PORT = 8080;
const API_PATH = '/api';

/**
 * Базовый URL бэкенда.
 *
 * Приоритет:
 * 1. EXPO_PUBLIC_API_URL в mobile/.env
 * 2. IP Metro/Expo Go (тот же, что в QR-коде) — для телефона в LAN
 * 3. Android-эмулятор: 10.0.2.2
 * 4. iOS-симулятор / web: localhost
 *
 * Пример .env: EXPO_PUBLIC_API_URL=http://192.168.0.101:8080/api
 */
export function resolveApiBaseUrl(): string {
  const fromEnv = process.env.EXPO_PUBLIC_API_URL?.trim();
  if (fromEnv) {
    return fromEnv.replace(/\/$/, '');
  }

  const expoLanHost = getExpoDevLanHost();
  if (expoLanHost) {
    return `http://${expoLanHost}:${API_PORT}${API_PATH}`;
  }

  if (Platform.OS === 'android') {
    return `http://10.0.2.2:${API_PORT}${API_PATH}`;
  }

  return `http://localhost:${API_PORT}${API_PATH}`;
}

/** Хост ПК из Expo Go (например 192.168.0.101:8081 → 192.168.0.101). */
function getExpoDevLanHost(): string | null {
  const debuggerHost =
    getExpoGoProjectConfig()?.debuggerHost ??
    Constants.expoGoConfig?.debuggerHost ??
    (Constants.manifest2?.extra as { expoGo?: { debuggerHost?: string } } | undefined)
      ?.expoGo?.debuggerHost;

  if (!debuggerHost) {
    const hostUri = Constants.expoConfig?.hostUri;
    if (hostUri) {
      try {
        const normalized = hostUri.includes('://') ? hostUri : `http://${hostUri}`;
        const hostname = new URL(normalized).hostname;
        return isUsableLanHost(hostname) ? hostname : null;
      } catch {
        return null;
      }
    }
    return null;
  }

  const withoutScheme = debuggerHost.replace(/^https?:\/\//, '');
  const host = withoutScheme.split(':')[0]?.trim() ?? '';
  return isUsableLanHost(host) ? host : null;
}

function isUsableLanHost(host: string): boolean {
  if (!host) {
    return false;
  }
  if (host === 'localhost' || host === '127.0.0.1') {
    return false;
  }
  return true;
}
