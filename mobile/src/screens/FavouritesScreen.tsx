import React, { useCallback, useEffect } from 'react';
import {
  ActivityIndicator,
  FlatList,
  ListRenderItem,
  RefreshControl,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { useFavoritesStore } from '../store/useFavoritesStore';
import { colors, elevation, spacing, typography } from '../theme/material';
import type { FavoriteSummonerDTO } from '../types/api';
import type { FavouritesScreenProps } from '../navigation/types';

export function FavouritesScreen({
  navigation,
}: FavouritesScreenProps): React.JSX.Element {
  const { status, errorMessage, favorites, loadFavorites, removeFavorite } =
    useFavoritesStore();

  const loading = status === 'LOADING';

  useEffect(() => {
    void loadFavorites();
  }, [loadFavorites]);

  const handleOpenProfile = useCallback(
    (puuid: string) => {
      navigation.navigate('Profile', { puuid });
    },
    [navigation],
  );

  const renderItem: ListRenderItem<FavoriteSummonerDTO> = useCallback(
    ({ item }) => (
      <TouchableOpacity
        style={styles.card}
        onPress={() => handleOpenProfile(item.puuid)}
        activeOpacity={0.85}
      >
        <View style={styles.cardBody}>
          <Text style={styles.name}>{item.summonerName}</Text>
          <Text style={styles.meta}>
            Ур. {item.summonerLevel ?? '—'} · {item.tier ?? 'UNRANKED'}{' '}
            {item.rank ?? ''}
          </Text>
          {item.winRate ? (
            <Text style={styles.winRate}>Винрейт: {item.winRate}</Text>
          ) : null}
        </View>
        <TouchableOpacity
          style={styles.removeBtn}
          onPress={() => void removeFavorite(item.summonerId)}
          hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
        >
          <Text style={styles.removeText}>✕</Text>
        </TouchableOpacity>
      </TouchableOpacity>
    ),
    [handleOpenProfile, removeFavorite],
  );

  if (loading && favorites.length === 0) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color={colors.primary} />
        <Text style={styles.loadingText}>Загрузка избранного…</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Избранные игроки</Text>
      <Text style={styles.subtitle}>Быстрый доступ к аналитике</Text>

      {errorMessage ? <Text style={styles.error}>{errorMessage}</Text> : null}

      <FlatList
        data={favorites}
        keyExtractor={(item) => String(item.favoriteId)}
        renderItem={renderItem}
        contentContainerStyle={styles.list}
        refreshControl={
          <RefreshControl
            refreshing={loading}
            onRefresh={() => void loadFavorites()}
            colors={[colors.primary]}
          />
        }
        ListEmptyComponent={
          !loading ? (
            <View style={styles.emptyBox}>
              <Text style={styles.emptyTitle}>Список пуст</Text>
              <Text style={styles.emptyText}>
                Добавьте игроков через экран профиля
              </Text>
            </View>
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
  },
  loadingText: { marginTop: spacing.md, color: colors.onSurfaceVariant },
  title: { ...typography.headline, color: colors.onSurface },
  subtitle: {
    ...typography.body,
    color: colors.onSurfaceVariant,
    marginBottom: spacing.md,
  },
  error: {
    color: colors.error,
    backgroundColor: colors.errorContainer,
    padding: spacing.sm,
    borderRadius: 8,
    marginBottom: spacing.sm,
  },
  list: { paddingBottom: spacing.xl },
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.card,
    borderRadius: 16,
    padding: spacing.md,
    marginBottom: spacing.sm,
    ...elevation.card,
  },
  cardBody: { flex: 1 },
  name: { fontSize: 17, fontWeight: '700', color: colors.onSurface },
  meta: { fontSize: 14, color: colors.onSurfaceVariant, marginTop: 4 },
  winRate: { fontSize: 13, color: colors.success, marginTop: 4 },
  removeBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: colors.errorContainer,
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: spacing.sm,
  },
  removeText: { color: colors.error, fontSize: 16, fontWeight: '700' },
  emptyBox: {
    alignItems: 'center',
    marginTop: spacing.xl,
    padding: spacing.lg,
    backgroundColor: colors.surfaceVariant,
    borderRadius: 16,
  },
  emptyTitle: { ...typography.title, color: colors.onSurface },
  emptyText: {
    ...typography.body,
    color: colors.onSurfaceVariant,
    marginTop: spacing.sm,
    textAlign: 'center',
  },
});
