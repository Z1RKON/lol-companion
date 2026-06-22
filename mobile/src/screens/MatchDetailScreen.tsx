import React, { useCallback, useEffect } from 'react';
import {
  ActivityIndicator,
  FlatList,
  Image,
  ListRenderItem,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { ChampionIcon } from '../components/ChampionIcon';
import { useChampionCatalog } from '../hooks/useChampionCatalog';
import { useMatchDetailStore } from '../store/useMatchDetailStore';
import { colors, elevation, spacing, typography } from '../theme/material';
import type { MatchParticipantDTO } from '../types/api';
import { getChampionNameRu } from '../utils/champions';
import { formatGold, getItemIconUrl } from '../utils/validation';
import type { MatchDetailScreenProps } from '../navigation/types';

function ItemSlots({
  items,
  patchVersion,
}: {
  items: number[];
  patchVersion?: string | null;
}): React.JSX.Element {
  const slots = items.length > 0 ? items : [0, 0, 0, 0, 0, 0];

  return (
    <View style={styles.itemsRow}>
      {slots.slice(0, 6).map((itemId, index) => {
        const iconUrl = getItemIconUrl(itemId, patchVersion);
        return (
          <View key={`${itemId}-${index}`} style={styles.itemSlot}>
            {iconUrl ? (
              <Image source={{ uri: iconUrl }} style={styles.itemIcon} resizeMode="contain" />
            ) : (
              <Text style={styles.itemText}>—</Text>
            )}
          </View>
        );
      })}
    </View>
  );
}

export function MatchDetailScreen({
  route,
}: MatchDetailScreenProps): React.JSX.Element {
  const { matchId, focusPuuid } = route.params;
  const { status, errorMessage, detail, loadMatchDetail, reset } =
    useMatchDetailStore();

  const loading = status === 'LOADING';
  useChampionCatalog(detail?.patchVersion);

  useEffect(() => {
    void loadMatchDetail(matchId);
    return () => {
      reset();
    };
  }, [matchId, loadMatchDetail, reset]);

  const renderParticipant: ListRenderItem<MatchParticipantDTO> = useCallback(
    ({ item }) => {
      const highlighted = focusPuuid != null && item.puuid === focusPuuid;
      return (
        <View
          style={[
            styles.participantCard,
            item.win ? styles.participantWin : styles.participantLoss,
            highlighted && styles.participantHighlight,
          ]}
        >
          <View style={styles.participantHeader}>
            <ChampionIcon
              championKey={item.championName}
              patchVersion={detail?.patchVersion}
              size={44}
            />
            <View style={styles.participantTitle}>
              <Text style={styles.summonerName}>{item.summonerName}</Text>
              <Text style={styles.championName}>
                {getChampionNameRu(item.championName)}
              </Text>
            </View>
            <Text style={styles.teamBadge}>{item.team}</Text>
          </View>

          <Text style={styles.kdaLine}>
            KDA {item.kills}/{item.deaths}/{item.assists} · {item.kda}
          </Text>
          <Text style={styles.statsLine}>
            CS {item.csScore} · Золото {formatGold(item.goldEarned)}
          </Text>

          <Text style={styles.itemsLabel}>Предметы</Text>
          <ItemSlots items={item.items} patchVersion={detail?.patchVersion} />
        </View>
      );
    },
    [focusPuuid, detail?.patchVersion],
  );

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color={colors.primary} />
        <Text style={styles.loadingText}>Загрузка матча…</Text>
      </View>
    );
  }

  if (status === 'ERROR' || !detail) {
    return (
      <View style={styles.centered}>
        <Text style={styles.error}>
          {errorMessage ?? 'Не удалось загрузить детали матча'}
        </Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.headerCard}>
        <Text style={styles.matchTitle}>{detail.gameMode}</Text>
        <Text style={styles.matchMeta}>
          {detail.gameDurationMinutes} мин · ID {detail.matchId}
        </Text>
      </View>

      <Text style={styles.sectionTitle}>Статистика игроков</Text>

      <FlatList
        data={detail.participants}
        keyExtractor={(item) => item.puuid}
        renderItem={renderParticipant}
        scrollEnabled={false}
        contentContainerStyle={styles.list}
      />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: spacing.md, paddingBottom: spacing.xl },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: colors.background,
    padding: spacing.lg,
  },
  loadingText: { marginTop: spacing.md, color: colors.onSurfaceVariant },
  headerCard: {
    backgroundColor: colors.card,
    borderRadius: 16,
    padding: spacing.lg,
    ...elevation.card,
  },
  matchTitle: { ...typography.title, color: colors.onSurface },
  matchMeta: { ...typography.body, color: colors.onSurfaceVariant, marginTop: spacing.xs },
  sectionTitle: {
    ...typography.title,
    color: colors.onSurface,
    marginTop: spacing.lg,
    marginBottom: spacing.sm,
  },
  list: { gap: spacing.sm },
  participantCard: {
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: spacing.md,
    marginBottom: spacing.sm,
    ...elevation.card,
  },
  participantWin: { borderLeftWidth: 4, borderLeftColor: colors.win },
  participantLoss: { borderLeftWidth: 4, borderLeftColor: colors.loss },
  participantHighlight: { backgroundColor: colors.primaryContainer },
  participantHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: spacing.sm,
  },
  participantTitle: { flex: 1 },
  summonerName: { fontSize: 16, fontWeight: '700', color: colors.onSurface },
  championName: { fontSize: 14, color: colors.onSurfaceVariant, marginTop: 2 },
  teamBadge: {
    fontSize: 11,
    fontWeight: '600',
    color: colors.onPrimaryContainer,
    backgroundColor: colors.primaryContainer,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 8,
    overflow: 'hidden',
  },
  kdaLine: { ...typography.label, color: colors.onSurface, marginTop: spacing.sm },
  statsLine: { ...typography.caption, color: colors.onSurfaceVariant, marginTop: 4 },
  itemsLabel: {
    ...typography.caption,
    color: colors.onSurfaceVariant,
    marginTop: spacing.sm,
    marginBottom: spacing.xs,
  },
  itemsRow: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.xs },
  itemSlot: {
    width: 44,
    height: 44,
    borderRadius: 8,
    backgroundColor: colors.surfaceVariant,
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  itemIcon: { width: 40, height: 40 },
  itemText: { fontSize: 10, color: colors.onSurfaceVariant, fontWeight: '600' },
  error: { color: colors.error, textAlign: 'center' },
});
