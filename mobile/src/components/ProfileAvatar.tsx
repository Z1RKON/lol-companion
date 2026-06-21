import React from 'react';
import { Image, StyleSheet, Text, View } from 'react-native';
import { hasGasMaskEasterEgg } from '../constants/specialSummoners';
import { colors } from '../theme/material';
import { getProfileIconUrl } from '../utils/validation';
import { GasMaskOverlay } from './GasMaskOverlay';

type ProfileAvatarProps = {
  puuid: string;
  profileIconId: number | null;
  size?: number;
};

export function ProfileAvatar({
  puuid,
  profileIconId,
  size = 72,
}: ProfileAvatarProps): React.JSX.Element {
  const radius = size / 2;
  const showGasMask = hasGasMaskEasterEgg(puuid);

  return (
    <View
      style={[
        styles.wrap,
        { width: size, height: size, borderRadius: radius },
        showGasMask ? styles.wrapWithMask : null,
      ]}
    >
      {profileIconId != null ? (
        <Image
          source={{ uri: getProfileIconUrl(profileIconId) }}
          style={{ width: size, height: size, borderRadius: radius }}
        />
      ) : (
        <View style={[styles.placeholder, { width: size, height: size, borderRadius: radius }]}>
          <Text style={styles.placeholderText}>?</Text>
        </View>
      )}
      {showGasMask ? <GasMaskOverlay size={size} /> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    overflow: 'hidden',
    backgroundColor: colors.surfaceVariant,
  },
  wrapWithMask: {
    overflow: 'visible',
  },
  placeholder: {
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.surfaceVariant,
  },
  placeholderText: {
    fontSize: 28,
    color: colors.onSurfaceVariant,
    fontWeight: '600',
  },
});
