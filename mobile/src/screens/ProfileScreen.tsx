import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  ListRenderItem,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { ChampionIcon } from '../components/ChampionIcon';
import { MatchItemsGrid } from '../components/MatchItemsGrid';
import { ProfileAvatar } from '../components/ProfileAvatar';
import { useChampionCatalog } from '../hooks/useChampionCatalog';
import { useFavoritesStore } from '../store/useFavoritesStore';
import { useSummonerStore } from '../store/useSummonerStore';
import { colors, elevation, spacing, typography } from '../theme/material';
import type { MatchDTO } from '../types/api';
import { getChampionNameRu } from '../utils/champions';
import { formatRecentWinRate, formatRankLabel } from '../utils/validation';
import type { ProfileScreenProps } from '../navigation/types';

const RECENT_MATCHES_COUNT = 10;
/** Показываем только союзников с числом совместных игр > 2 */
const MIN_GAMES_TOGETHER = 2;

export function ProfileScreen({ navigation, route }: ProfileScreenProps): React.JSX.Element {
  const profilePuuid = route.params.puuid;
  const {
    status,
    matchesStatus,
    errorMessage,
    matchesErrorMessage,
    offlineWarning,
    summoner,
    matches,
    loadMatches,
    loadTeammates,
    loadSummonerByPuuid,
    teammates,
    teammatesStatus,
    teammatesErrorMessage,
  } = useSummonerStore();
  const { addFavorite } = useFavoritesStore();

  useEffect(() => {
    void loadSummonerByPuuid(profilePuuid);
  }, [profilePuuid, loadSummonerByPuuid]);

  const profileLoading = status === 'LOADING';
  const matchesLoading = matchesStatus === 'LOADING';
  const teammatesLoading = teammatesStatus === 'LOADING';
  const [teammatesExpanded, setTeammatesExpanded] = useState(false);
  const frequentTeammates = useMemo(
    () => teammates.filter((t) => t.gamesTogether > MIN_GAMES_TOGETHER),
    [teammates],
  );
  const recentMatches = matches.slice(0, RECENT_MATCHES_COUNT);
  const recentWinRate = formatRecentWinRate(recentMatches);
  useChampionCatalog(recentMatches[0]?.patchVersion);

  useEffect(() => {
    if (summoner) {
      void loadMatches(RECENT_MATCHES_COUNT);
      void loadTeammates(20, 20);
      setTeammatesExpanded(false);
    }
  }, [summoner?.puuid, loadMatches, loadTeammates]);

  const handleOpenMatch = useCallback(
    (match: MatchDTO) => {
      navigation.navigate('MatchDetail', {
        matchId: match.matchId,
        focusPuuid: summoner?.puuid,
      });
    },
    [navigation, summoner?.puuid],
  );

  const handleOpenTeammate = useCallback(
    (puuid: string) => {
      void loadSummonerByPuuid(puuid);
      navigation.setParams({ puuid });
    },
    [loadSummonerByPuuid, navigation],
  );

  const renderMatch: ListRenderItem<MatchDTO> = useCallback(
    ({ item }) => (
      <TouchableOpacity
        style={[styles.matchCard, item.win ? styles.matchWin : styles.matchLoss]}
        onPress={() => handleOpenMatch(item)}
        activeOpacity={0.85}
      >
        <View style={styles.matchHeader}>
          <ChampionIcon
            championKey={item.championName}
            patchVersion={item.patchVersion}
            size={40}
          />
          <View style={styles.matchTitleBlock}>
            <Text style={styles.champion}>{getChampionNameRu(item.championName)}</Text>
            <Text style={styles.matchMeta}>
              {item.gameMode} · {item.gameDurationMinutes} мин
            </Text>
          </View>
          <Text style={[styles.resultBadge, item.win ? styles.winText : styles.lossText]}>
            {item.win ? 'Победа' : 'Поражение'}
          </Text>
        </View>
        <Text style={styles.kda}>
          {item.kills}/{item.deaths}/{item.assists} · KDA {item.kda} · CS {item.csScore}
        </Text>
        <MatchItemsGrid items={item.items} patchVersion={item.patchVersion} />
      </TouchableOpacity>
    ),
    [handleOpenMatch],
  );

  if (status === 'ERROR' && !summoner) {
    return (
      <View style={styles.centered}>
        <Text style={styles.error}>{errorMessage ?? 'Не удалось загрузить профиль'}</Text>
      </View>
    );
  }

  if (profileLoading || !summoner) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color={colors.primary} />
        <Text style={styles.loadingText}>Загрузка профиля…</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.profileCard}>
        <ProfileAvatar
          puuid={summoner.puuid}
          profileIconId={summoner.profileIconId}
          size={72}
        />
        <View style={styles.profileInfo}>
          <Text style={styles.name}>{summoner.summonerName}</Text>
          <Text style={styles.level}>Уровень {summoner.summonerLevel}</Text>
          <Text style={styles.rank}>
            {formatRankLabel(summoner.tier, summoner.rank, summoner.leaguePoints)}
          </Text>
          <Text style={styles.winRate}>
            {matchesLoading && recentWinRate == null
              ? 'Винрейт: загрузка…'
              : recentWinRate != null
                ? `Винрейт (${recentMatches.length} игр): ${recentWinRate}`
                : 'Винрейт: нет данных'}
          </Text>
          <Text style={styles.region}>Регион: {summoner.region}</Text>
        </View>
      </View>

      {offlineWarning ? (
        <Text style={styles.offlineWarning}>{offlineWarning}</Text>
      ) : null}

      <TouchableOpacity
        style={styles.favoriteButton}
        onPress={() => void addFavorite(summoner.puuid)}
        activeOpacity={0.85}
      >
        <Text style={styles.favoriteButtonText}>★ В избранное</Text>
      </TouchableOpacity>

      <FlatList
        data={recentMatches}
        keyExtractor={(item) => item.matchId}
        renderItem={renderMatch}
        contentContainerStyle={styles.list}
        ListHeaderComponent={
          <>
            <TouchableOpacity
              style={styles.teammatesToggle}
              onPress={() => setTeammatesExpanded((open) => !open)}
              activeOpacity={0.85}
              disabled={teammatesLoading}
            >
              <View style={styles.teammatesToggleText}>
                <Text style={[styles.sectionTitle, styles.teammatesSectionTitle]}>
                  Играли вместе
                </Text>
                <Text style={styles.sectionHint}>
                  {teammatesLoading
                    ? 'Загрузка…'
                    : frequentTeammates.length === 0
                      ? 'Нет игроков с 3+ играми в команде'
                      : `${frequentTeammates.length} игроков · больше 2 игр вместе`}
                </Text>
              </View>
              <Text style={styles.teammatesChevron}>
                {teammatesExpanded ? '▼' : '▶'}
              </Text>
            </TouchableOpacity>
            {teammatesLoading ? (
              <ActivityIndicator style={styles.teammatesLoader} color={colors.primary} />
            ) : null}
            {teammatesErrorMessage ? (
              <Text style={styles.error}>{teammatesErrorMessage}</Text>
            ) : null}
            {teammatesExpanded && !teammatesLoading
              ? frequentTeammates.map((teammate) => (
                  <TouchableOpacity
                    key={teammate.puuid}
                    style={styles.teammateRow}
                    onPress={() => handleOpenTeammate(teammate.puuid)}
                    activeOpacity={0.85}
                  >
                    <ProfileAvatar
                      puuid={teammate.puuid}
                      profileIconId={teammate.profileIconId}
                      size={44}
                    />
                    <View style={styles.teammateInfo}>
                      <Text style={styles.teammateName}>{teammate.summonerName}</Text>
                      <Text style={styles.teammateGames}>
                        {teammate.gamesTogether}{' '}
                        {teammate.gamesTogether === 1
                          ? 'игра вместе'
                          : teammate.gamesTogether < 5
                            ? 'игры вместе'
                            : 'игр вместе'}
                      </Text>
                    </View>
                  </TouchableOpacity>
                ))
              : null}

            <Text style={[styles.sectionTitle, styles.matchesSectionTitle]}>
              Последние {RECENT_MATCHES_COUNT} матчей
            </Text>
            {matchesLoading ? (
              <ActivityIndicator style={styles.matchesLoader} color={colors.primary} />
            ) : null}
            {matchesErrorMessage ? (
              <Text style={styles.error}>{matchesErrorMessage}</Text>
            ) : null}
          </>
        }
        ListEmptyComponent={
          !matchesLoading ? (
            <Text style={styles.empty}>Матчи не найдены</Text>
          ) : null
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background, padding: spacing.md },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: colors.background,
    padding: spacing.lg,
  },
  loadingText: { marginTop: spacing.md, color: colors.onSurfaceVariant },
  profileCard: {
    flexDirection: 'row',
    backgroundColor: colors.card,
    borderRadius: 16,
    padding: spacing.md,
    overflow: 'visible',
    ...elevation.card,
  },
  profileInfo: { flex: 1, marginLeft: spacing.md, justifyContent: 'center' },
  name: { ...typography.title, color: colors.onSurface },
  level: { ...typography.body, color: colors.onSurfaceVariant, marginTop: 2 },
  rank: { ...typography.label, color: colors.primary, marginTop: 4 },
  winRate: { ...typography.label, color: colors.success, marginTop: 4 },
  region: { ...typography.caption, color: colors.onSurfaceVariant, marginTop: 2 },
  offlineWarning: {
    marginTop: spacing.sm,
    color: colors.secondary,
    backgroundColor: colors.primaryContainer,
    padding: spacing.sm,
    borderRadius: 8,
    textAlign: 'center',
  },
  favoriteButton: {
    alignSelf: 'flex-start',
    marginTop: spacing.md,
    backgroundColor: colors.primaryContainer,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: 20,
  },
  favoriteButtonText: { color: colors.onPrimaryContainer, fontWeight: '600' },
  sectionTitle: {
    ...typography.title,
    color: colors.onSurface,
    marginTop: spacing.lg,
    marginBottom: spacing.sm,
  },
  sectionHint: {
    ...typography.caption,
    color: colors.onSurfaceVariant,
    marginTop: 2,
  },
  teammatesToggle: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: spacing.lg,
    marginBottom: spacing.sm,
    paddingVertical: spacing.xs,
  },
  teammatesSectionTitle: { marginTop: 0 },
  teammatesToggleText: { flex: 1 },
  teammatesChevron: {
    fontSize: 14,
    color: colors.onSurfaceVariant,
    marginLeft: spacing.sm,
    paddingHorizontal: spacing.xs,
  },
  matchesSectionTitle: { marginTop: spacing.md },
  teammatesLoader: { marginVertical: spacing.sm },
  teammateRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: spacing.sm,
    marginBottom: spacing.sm,
    ...elevation.card,
  },
  teammateInfo: { flex: 1, marginLeft: spacing.sm },
  teammateName: { fontSize: 15, fontWeight: '600', color: colors.onSurface },
  teammateGames: { fontSize: 13, color: colors.onSurfaceVariant, marginTop: 2 },
  matchesLoader: { marginVertical: spacing.md },
  error: { color: colors.error, marginBottom: spacing.sm },
  list: { paddingBottom: spacing.xl },
  matchCard: {
    borderRadius: 12,
    padding: spacing.md,
    marginBottom: spacing.sm,
    backgroundColor: colors.card,
    ...elevation.card,
  },
  matchWin: { borderLeftWidth: 4, borderLeftColor: colors.win },
  matchLoss: { borderLeftWidth: 4, borderLeftColor: colors.loss },
  matchHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
  },
  matchTitleBlock: { flex: 1 },
  champion: { fontSize: 16, fontWeight: '600', color: colors.onSurface },
  resultBadge: { fontSize: 12, fontWeight: '700' },
  winText: { color: colors.win },
  lossText: { color: colors.loss },
  matchMeta: { fontSize: 13, color: colors.onSurfaceVariant, marginTop: 2 },
  kda: { fontSize: 13, color: colors.onSurface, marginTop: spacing.sm },
  empty: { textAlign: 'center', color: colors.onSurfaceVariant, marginTop: spacing.lg },
});
