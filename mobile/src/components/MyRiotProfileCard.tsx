import React from 'react';
import {
  ActivityIndicator,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { colors, elevation, spacing, typography } from '../theme/material';
import type { UserDTO } from '../types/api';
import { hasLinkedRiotAccount } from '../utils/riotAccount';

type MyRiotProfileCardProps = {
  user: UserDTO | null;
  loading: boolean;
  onOpenProfile: () => void;
};

export function MyRiotProfileCard({
  user,
  loading,
  onOpenProfile,
}: MyRiotProfileCardProps): React.JSX.Element | null {
  if (!hasLinkedRiotAccount(user)) {
    return null;
  }

  return (
    <View style={styles.card}>
      <Text style={styles.riotId}>{user?.linkedRiotId}</Text>
      <Text style={styles.region}>Регион: {user?.linkedRiotRegion ?? '—'}</Text>
      <TouchableOpacity
        style={[styles.button, loading && styles.buttonDisabled]}
        onPress={onOpenProfile}
        disabled={loading}
        activeOpacity={0.85}
      >
        {loading ? (
          <ActivityIndicator color={colors.onPrimary} />
        ) : (
          <Text style={styles.buttonText}>Мой профиль</Text>
        )}
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.primaryContainer,
    borderRadius: 16,
    padding: spacing.md,
    marginBottom: spacing.lg,
    ...elevation.card,
  },
  riotId: {
    ...typography.title,
    color: colors.onPrimaryContainer,
    marginTop: spacing.xs,
  },
  region: {
    ...typography.caption,
    color: colors.onPrimaryContainer,
    marginTop: 2,
    marginBottom: spacing.md,
  },
  button: {
    backgroundColor: colors.primary,
    borderRadius: 12,
    paddingVertical: 12,
    alignItems: 'center',
    minHeight: 44,
    justifyContent: 'center',
  },
  buttonDisabled: { opacity: 0.65 },
  buttonText: { color: colors.onPrimary, fontWeight: '600', fontSize: 15 },
});
