import { resolveDdragonVersion } from './validation';

type ChampionEntry = {
  id: string;
  key: string;
  name: string;
};

const FALLBACK_PATCH = '14.6.1';

let catalogById = new Map<string, ChampionEntry>();
let catalogByKeyLower = new Map<string, ChampionEntry>();
let loadedPatch: string | null = null;
let loadPromise: Promise<void> | null = null;

/** Загружает ru_RU каталог чемпионов Data Dragon (кэш в памяти). */
export async function ensureChampionCatalog(
  patchVersion?: string | null,
): Promise<void> {
  const patch = resolveDdragonVersion(patchVersion);
  if (loadedPatch === patch && catalogById.size > 0) {
    return;
  }

  if (loadPromise && loadedPatch === patch) {
    await loadPromise;
    return;
  }

  loadedPatch = patch;
  loadPromise = fetchCatalog(patch).finally(() => {
    loadPromise = null;
  });
  await loadPromise;
}

async function fetchCatalog(patch: string): Promise<void> {
  const urls = [
    `https://ddragon.leagueoflegends.com/cdn/${patch}/data/ru_RU/champion.json`,
    `https://ddragon.leagueoflegends.com/cdn/${FALLBACK_PATCH}/data/ru_RU/champion.json`,
  ];

  for (const url of urls) {
    try {
      const response = await fetch(url);
      if (!response.ok) {
        continue;
      }
      const payload = (await response.json()) as {
        data?: Record<string, ChampionEntry>;
      };
      if (!payload.data) {
        continue;
      }

      catalogById = new Map();
      catalogByKeyLower = new Map();

      for (const entry of Object.values(payload.data)) {
        catalogById.set(entry.id, entry);
        catalogByKeyLower.set(entry.id.toLowerCase(), entry);
        catalogByKeyLower.set(entry.key, entry);
        catalogByKeyLower.set(entry.name.toLowerCase(), entry);
      }
      return;
    } catch {
      // пробуем fallback URL
    }
  }
}

function findChampionEntry(championKey: string): ChampionEntry | undefined {
  if (!championKey || championKey === 'Unknown') {
    return undefined;
  }

  const direct = catalogById.get(championKey);
  if (direct) {
    return direct;
  }

  const lower = catalogByKeyLower.get(championKey.toLowerCase());
  if (lower) {
    return lower;
  }

  return undefined;
}

export function getChampionIconUrl(
  championKey: string,
  patchVersion?: string | null,
): string {
  const patch = resolveDdragonVersion(patchVersion);
  const entry = findChampionEntry(championKey);
  const id = entry?.id ?? championKey;
  return `https://ddragon.leagueoflegends.com/cdn/${patch}/img/champion/${id}.png`;
}

export function getChampionNameRu(championKey: string): string {
  if (!championKey || championKey === 'Unknown') {
    return 'Неизвестно';
  }
  return findChampionEntry(championKey)?.name ?? championKey;
}

export function isChampionCatalogReady(): boolean {
  return catalogById.size > 0;
}
