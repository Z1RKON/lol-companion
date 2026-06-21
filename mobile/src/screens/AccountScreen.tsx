import React, { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { MyRiotProfileCard } from '../components/MyRiotProfileCard';
import { RegionPicker } from '../components/RegionPicker';
import { useAuthStore } from '../store/useAuthStore';
import { useSummonerStore } from '../store/useSummonerStore';
import { colors, elevation, spacing, typography } from '../theme/material';
import { hasLinkedRiotAccount } from '../utils/riotAccount';
import { confirmAction } from '../utils/confirm';
import { normalizeRiotIdQuery } from '../utils/validation';
import type { AccountScreenProps } from '../navigation/types';
import type { LoLRegion } from '../types/api';

export function AccountScreen({ navigation }: AccountScreenProps): React.JSX.Element {
  const user = useAuthStore((state) => state.user);
  const authStatus = useAuthStore((state) => state.status);
  const authError = useAuthStore((state) => state.errorMessage);
  const logout = useAuthStore((state) => state.logout);
  const linkRiot = useAuthStore((state) => state.linkRiot);
  const unlinkRiot = useAuthStore((state) => state.unlinkRiot);

  const profileLoading = useSummonerStore((state) => state.status === 'LOADING');
  const selectedRegion = useSummonerStore((state) => state.selectedRegion);
  const setRegion = useSummonerStore((state) => state.setRegion);
  const loadMyLinkedProfile = useSummonerStore((state) => state.loadMyLinkedProfile);

  const [riotIdInput, setRiotIdInput] = useState(user?.linkedRiotId ?? '');
  const [localError, setLocalError] = useState<string | null>(null);

  const linked = hasLinkedRiotAccount(user);
  const authBusy = authStatus === 'LOADING';
  const canLink = riotIdInput.trim().length > 0 && !authBusy;

  useEffect(() => {
    if (user?.linkedRiotId) {
      setRiotIdInput(user.linkedRiotId);
    }
  }, [user?.linkedRiotId]);

  const openMyProfile = useCallback(async () => {
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
  }, [loadMyLinkedProfile, navigation]);

  const handleLinkRiot = useCallback(async () => {
    setLocalError(null);
    const riotId = normalizeRiotIdQuery(riotIdInput);
    if (!riotId.includes('#')) {
      setLocalError('Укажите Riot ID в формате Имя#Тег');
      return;
    }
    const ok = await linkRiot(riotId, selectedRegion);
    if (!ok) {
      return;
    }
    setLocalError(null);
  }, [linkRiot, riotIdInput, selectedRegion]);

  const handleUnlinkRiot = useCallback(async () => {
    const confirmed = await confirmAction({
      title: 'Отвязать Riot ID',
      message: 'Удалить привязку к Riot-аккаунту?',
      confirmLabel: 'Отвязать',
      destructive: true,
    });
    if (confirmed) {
      await unlinkRiot();
    }
  }, [unlinkRiot]);

  const handleLogout = useCallback(async () => {
    const confirmed = await confirmAction({
      title: 'Сменить аккаунт',
      message: 'Выйти из текущего аккаунта? После выхода можно войти под другим логином.',
      confirmLabel: 'Выйти',
      destructive: true,
    });
    if (confirmed) {
      await logout();
    }
  }, [logout]);

  const displayError = localError ?? authError;

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      keyboardShouldPersistTaps="always"
      nestedScrollEnabled
    >
      <View style={styles.card}>
        <View style={styles.avatar}>
          <Text style={styles.avatarLetter}>
            {(user?.username ?? '?').charAt(0).toUpperCase()}
          </Text>
        </View>
        <Text style={styles.username}>{user?.username ?? '—'}</Text>
        <Text style={styles.email}>{user?.email ?? '—'}</Text>
      </View>

      {linked ? (
        <>
          <MyRiotProfileCard
            user={user}
            loading={profileLoading || authBusy}
            onOpenProfile={() => void openMyProfile()}
          />
          <Pressable
            style={({ pressed }) => [
              styles.secondaryButton,
              pressed && styles.pressed,
              authBusy && styles.buttonDisabled,
            ]}
            onPress={() => void handleUnlinkRiot()}
            disabled={authBusy}
          >
            <Text style={styles.secondaryButtonText}>Отвязать Riot ID</Text>
          </Pressable>
        </>
      ) : (
        <>
          <Text style={styles.sectionLabel}>Регион</Text>
          <RegionPicker selected={selectedRegion} onSelect={(r) => void setRegion(r)} />

          <View style={styles.inputCard}>
            <TextInput
              style={styles.input}
              value={riotIdInput}
              onChangeText={(text) => {
                setRiotIdInput(text);
                setLocalError(null);
              }}
              placeholder="Имя#Тег"
              placeholderTextColor={colors.onSurfaceVariant}
              autoCapitalize="none"
              autoCorrect={false}
              editable={!authBusy}
            />
          </View>

          {!canLink && riotIdInput.trim().length === 0 ? (
            <Text style={styles.helperText}>Введите Riot ID, чтобы активировать кнопку</Text>
          ) : null}

          <Pressable
            style={({ pressed }) => [
              styles.primaryButton,
              pressed && styles.pressed,
              !canLink && styles.buttonDisabled,
            ]}
            onPress={() => void handleLinkRiot()}
            disabled={!canLink}
          >
            {authBusy ? (
              <ActivityIndicator color={colors.onPrimary} />
            ) : (
              <Text style={styles.primaryButtonText}>Привязать Riot ID</Text>
            )}
          </Pressable>
        </>
      )}

      {displayError ? <Text style={styles.error}>{displayError}</Text> : null}

      <Pressable
        style={({ pressed }) => [
          styles.logoutButton,
          pressed && styles.pressed,
          authBusy && styles.buttonDisabled,
        ]}
        onPress={() => void handleLogout()}
        disabled={authBusy}
      >
        <Text style={styles.logoutButtonText}>Выйти и войти в другой аккаунт</Text>
      </Pressable>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: spacing.md, paddingBottom: spacing.xl },
  card: {
    backgroundColor: colors.card,
    borderRadius: 16,
    padding: spacing.lg,
    alignItems: 'center',
    ...elevation.card,
  },
  avatar: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: colors.primaryContainer,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: spacing.md,
  },
  avatarLetter: {
    fontSize: 32,
    fontWeight: '700',
    color: colors.onPrimaryContainer,
  },
  username: { ...typography.title, color: colors.onSurface },
  email: { ...typography.body, color: colors.onSurfaceVariant, marginTop: spacing.xs },
  sectionTitle: {
    ...typography.title,
    color: colors.onSurface,
    marginTop: spacing.lg,
  },
  sectionHint: {
    ...typography.body,
    color: colors.onSurfaceVariant,
    marginTop: spacing.xs,
    marginBottom: spacing.md,
    lineHeight: 22,
  },
  sectionLabel: { ...typography.label, color: colors.onSurfaceVariant },
  inputCard: {
    backgroundColor: colors.card,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.outline,
    paddingHorizontal: spacing.md,
    marginTop: spacing.sm,
  },
  input: { paddingVertical: 12, fontSize: 16, color: colors.onSurface },
  helperText: {
    ...typography.caption,
    color: colors.onSurfaceVariant,
    marginTop: spacing.sm,
  },
  primaryButton: {
    backgroundColor: colors.primary,
    borderRadius: 12,
    paddingVertical: spacing.md,
    alignItems: 'center',
    marginTop: spacing.md,
    minHeight: 48,
    justifyContent: 'center',
  },
  primaryButtonText: { color: colors.onPrimary, fontWeight: '600', fontSize: 16 },
  secondaryButton: {
    borderWidth: 1,
    borderColor: colors.outline,
    borderRadius: 12,
    paddingVertical: spacing.md,
    alignItems: 'center',
    marginBottom: spacing.lg,
  },
  secondaryButtonText: { color: colors.onSurfaceVariant, fontWeight: '600' },
  logoutButton: {
    marginTop: spacing.lg,
    backgroundColor: colors.error,
    borderRadius: 12,
    paddingVertical: spacing.md,
    alignItems: 'center',
    minHeight: 48,
    justifyContent: 'center',
  },
  pressed: { opacity: 0.85 },
  buttonDisabled: { opacity: 0.55 },
  logoutButtonText: { color: colors.onError, fontSize: 16, fontWeight: '600' },
  error: {
    marginTop: spacing.md,
    color: colors.error,
    backgroundColor: colors.errorContainer,
    padding: spacing.sm,
    borderRadius: 8,
  },
});
