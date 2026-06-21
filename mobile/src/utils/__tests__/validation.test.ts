import {
  formatGold,
  formatRankLabel,
  formatRecentWinRate,
  getItemIconUrl,
  getProfileIconUrl,
  normalizeRiotIdQuery,
  resolveDdragonVersion,
  validateEmail,
  validateLoginIdentifier,
  validatePassword,
  validateUsername,
} from '../validation';

describe('validation', () => {
  it('validateEmail принимает корректный адрес', () => {
    expect(validateEmail('user@example.com')).toBeNull();
  });

  it('validateEmail отклоняет некорректный адрес', () => {
    expect(validateEmail('not-an-email')).toBe('Некорректный email');
  });

  it('validatePassword требует минимум 6 символов', () => {
    expect(validatePassword('123')).toBe('Пароль: минимум 6 символов');
    expect(validatePassword('secret1')).toBeNull();
  });

  it('validateUsername проверяет длину', () => {
    expect(validateUsername('ab')).toContain('от 3 до 50');
    expect(validateUsername('player1')).toBeNull();
  });

  it('validateLoginIdentifier для email и логина', () => {
    expect(validateLoginIdentifier('')).toBe('Введите email или логин');
    expect(validateLoginIdentifier('player1')).toBeNull();
    expect(validateLoginIdentifier('bad@')).toBe('Некорректный email');
  });

  it('normalizeRiotIdQuery убирает невидимые символы', () => {
    expect(normalizeRiotIdQuery('  Player#TAG  ')).toBe('Player#TAG');
  });

  it('formatRecentWinRate считает процент побед', () => {
    expect(formatRecentWinRate([])).toBeNull();
    expect(formatRecentWinRate([{ win: true }, { win: false }])).toBe('50.0%');
  });

  it('formatRankLabel для unranked и ranked', () => {
    expect(formatRankLabel(null, null, null)).toBe('Без ранга');
    expect(formatRankLabel('GOLD', 'II', 75)).toBe('Золото II · 75 LP');
  });

  it('formatGold использует ru-RU', () => {
    expect(formatGold(12000)).toMatch(/12/);
  });

  it('resolveDdragonVersion из версии патча', () => {
    expect(resolveDdragonVersion('14.6.1')).toBe('14.6.1');
    expect(resolveDdragonVersion('14.6')).toBe('14.6.1');
  });

  it('getProfileIconUrl и getItemIconUrl', () => {
    expect(getProfileIconUrl(1, '14.6.1')).toContain('profileicon/1.png');
    expect(getItemIconUrl(0)).toBe('');
    expect(getItemIconUrl(1001, '14.6.1')).toContain('item/1001.png');
  });
});
