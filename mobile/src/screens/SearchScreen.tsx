import React, { useState } from 'react';
import {
  ActivityIndicator,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
} from 'react-native';
import { MyRiotProfileCard } from '../components/MyRiotProfileCard';
import { RegionPicker } from '../components/RegionPicker';
import { SearchInput } from '../components/SearchInput';
import { useAuthStore } from '../store/useAuthStore';
import { useSummonerStore } from '../store/useSummonerStore';
import { colors, spacing, typography } from '../theme/material';
import type { SearchScreenProps } from '../navigation/types';
import type { LoLRegion } from '../types/api';

export function SearchScreen({ navigation }: SearchScreenProps): React.JSX.Element {
  const [query, setQuery] = useState('');
  const user = useAuthStore((state) => state.user);
  const {
    status,
    errorMessage,
    offlineWarning,
    selectedRegion,
    setRegion,
    searchSummoner,
    loadMyLinkedProfile,
  } = useSummonerStore();

  const loading = status === 'LOADING';

  const handleSearch = async (): Promise<void> => {
    await searchSummoner(query);
    const { status: nextStatus, summoner } = useSummonerStore.getState();
    if (nextStatus === 'SUCCESS' && summoner) {
      navigation.navigate('Profile', {
        puuid: summoner.puuid,
        region: summoner.region as LoLRegion,
      });
    }
  };

  const handleOpenMyProfile = async (): Promise<void> => {
    const ok = await loadMyLinkedProfile();
    if (ok) {
      const { summoner } = useSummonerStore.getState();
      if (summoner) {
        navigation.navigate('Profile', {
          puuid: summoner.puuid,
          region: summoner.region as LoLRegion,
        });
      }
    }
  };

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      keyboardShouldPersistTaps="always"
      nestedScrollEnabled
    >
      <MyRiotProfileCard
        user={user}
        loading={loading}
        onOpenProfile={() => void handleOpenMyProfile()}
      />

      <Text style={styles.title}>Поиск призывателя</Text>
      <Text style={styles.subtitle}>
        Введите Riot ID в формате Имя#Тег или откройте свой профиль выше. Привязку
        Riot ID можно настроить во вкладке «Аккаунт».
      </Text>

      <Text style={styles.sectionLabel}>Регион</Text>
      <RegionPicker
        selected={selectedRegion}
        onSelect={(region) => void setRegion(region)}
      />

      <SearchInput
        value={query}
        onChangeText={setQuery}
        onSubmit={() => void handleSearch()}
        editable={!loading}
      />

      <TouchableOpacity
        style={[styles.primaryButton, loading && styles.buttonDisabled]}
        onPress={() => void handleSearch()}
        disabled={loading || !query.trim()}
        activeOpacity={0.85}
      >
        {loading ? (
          <ActivityIndicator color={colors.onPrimary} />
        ) : (
          <Text style={styles.primaryButtonText}>Найти</Text>
        )}
      </TouchableOpacity>

      {offlineWarning ? (
        <Text style={styles.offlineWarning}>{offlineWarning}</Text>
      ) : null}
      {errorMessage ? <Text style={styles.error}>{errorMessage}</Text> : null}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: spacing.lg, paddingBottom: spacing.xl },
  title: { ...typography.headline, color: colors.onSurface },
  subtitle: {
    ...typography.body,
    color: colors.onSurfaceVariant,
    marginTop: spacing.xs,
    marginBottom: spacing.lg,
  },
  sectionLabel: { ...typography.label, color: colors.onSurfaceVariant },
  regionList: { gap: spacing.sm, paddingVertical: spacing.sm },
  regionChip: {
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: 20,
    backgroundColor: colors.surfaceVariant,
    marginRight: spacing.sm,
  },
  regionChipActive: { backgroundColor: colors.primaryContainer },
  regionChipText: { color: colors.onSurfaceVariant, fontWeight: '500' },
  regionChipTextActive: { color: colors.onPrimaryContainer },
  primaryButton: {
    backgroundColor: colors.primary,
    borderRadius: 24,
    paddingVertical: 14,
    alignItems: 'center',
    marginTop: spacing.lg,
  },
  buttonDisabled: { opacity: 0.6 },
  primaryButtonText: { color: colors.onPrimary, fontWeight: '600', fontSize: 16 },
  offlineWarning: {
    marginTop: spacing.md,
    color: colors.secondary,
    backgroundColor: colors.primaryContainer,
    padding: spacing.sm,
    borderRadius: 8,
    textAlign: 'center',
  },
  error: {
    marginTop: spacing.md,
    color: colors.error,
    backgroundColor: colors.errorContainer,
    padding: spacing.sm,
    borderRadius: 8,
  },
});
