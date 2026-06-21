import { useEffect, useState } from 'react';
import { ensureChampionCatalog, isChampionCatalogReady } from '../utils/champions';

/** Подгружает ru_RU каталог чемпионов и триггерит перерисовку экрана. */
export function useChampionCatalog(patchVersion?: string | null): boolean {
  const [ready, setReady] = useState(isChampionCatalogReady());

  useEffect(() => {
    let active = true;
    void ensureChampionCatalog(patchVersion).then(() => {
      if (active) {
        setReady(true);
      }
    });
    return () => {
      active = false;
    };
  }, [patchVersion]);

  return ready;
}
