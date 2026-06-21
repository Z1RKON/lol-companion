const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function validateLoginIdentifier(value: string): string | null {
  const trimmed = value.trim();
  if (!trimmed) {
    return 'Введите email или логин';
  }
  if (trimmed.includes('@')) {
    return validateEmail(trimmed);
  }
  return validateUsername(trimmed);
}

export function validateEmail(email: string): string | null {
  const trimmed = email.trim();
  if (!trimmed) {
    return 'Email обязателен';
  }
  if (!EMAIL_REGEX.test(trimmed)) {
    return 'Некорректный email';
  }
  return null;
}

export function validatePassword(password: string): string | null {
  if (!password) {
    return 'Пароль обязателен';
  }
  if (password.length < 6) {
    return 'Пароль: минимум 6 символов';
  }
  return null;
}

export function validateUsername(username: string): string | null {
  const trimmed = username.trim();
  if (!trimmed) {
    return 'Имя пользователя обязательно';
  }
  if (trimmed.length < 3 || trimmed.length > 50) {
    return 'Имя пользователя: от 3 до 50 символов';
  }
  return null;
}

export function getProfileIconUrl(
  profileIconId: number | null,
  patchVersion?: string | null,
): string {
  const iconId = profileIconId ?? 0;
  const patch = resolveDdragonVersion(patchVersion);
  return `https://ddragon.leagueoflegends.com/cdn/${patch}/img/profileicon/${iconId}.png`;
}

let latestDdragonPatch = '15.1.1';

export async function initDdragonAssets(): Promise<void> {
  try {
    const response = await fetch('https://ddragon.leagueoflegends.com/api/versions.json');
    if (!response.ok) {
      return;
    }
    const versions = (await response.json()) as string[];
    if (versions.length > 0) {
      latestDdragonPatch = versions[0];
    }
  } catch {
    // оставляем fallback
  }
}

export function resolveDdragonVersion(gameVersion?: string | null): string {
  if (gameVersion) {
    const parts = gameVersion.split('.');
    if (parts.length >= 2) {
      return `${parts[0]}.${parts[1]}.1`;
    }
  }
  return latestDdragonPatch;
}

export function getItemIconUrl(
  itemId: number,
  patchVersion?: string | null,
): string {
  if (itemId <= 0) {
    return '';
  }
  const patch = resolveDdragonVersion(patchVersion);
  return `https://ddragon.leagueoflegends.com/cdn/${patch}/img/item/${itemId}.png`;
}

export function formatGold(gold: number): string {
  return gold.toLocaleString('ru-RU');
}

export function formatRecentWinRate(
  matches: { win: boolean }[],
): string | null {
  if (matches.length === 0) {
    return null;
  }
  const wins = matches.filter((match) => match.win).length;
  const percent = (wins / matches.length) * 100;
  return `${percent.toFixed(1)}%`;
}

/** Убирает невидимые символы форматирования из Riot ID. */
export function normalizeRiotIdQuery(value: string): string {
  return value.replace(/\p{Cf}/gu, '').trim();
}

const TIER_LABELS_RU: Record<string, string> = {
  IRON: 'Железо',
  BRONZE: 'Бронза',
  SILVER: 'Серебро',
  GOLD: 'Золото',
  PLATINUM: 'Платина',
  EMERALD: 'Изумруд',
  DIAMOND: 'Алмаз',
  MASTER: 'Мастер',
  GRANDMASTER: 'Грандмастер',
  CHALLENGER: 'Претендент',
};

export function formatRankLabel(
  tier: string | null | undefined,
  rank: string | null | undefined,
  leaguePoints: number | null | undefined,
): string {
  if (!tier || tier === 'UNRANKED') {
    return 'Без ранга';
  }
  const tierLabel = TIER_LABELS_RU[tier] ?? tier;
  const division = rank ? ` ${rank}` : '';
  const lp = leaguePoints ?? 0;
  return `${tierLabel}${division} · ${lp} LP`;
}
