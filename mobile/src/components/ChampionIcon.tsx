import React from 'react';
import { Image, StyleSheet, View, type ViewStyle } from 'react-native';
import { colors } from '../theme/material';
import { getChampionIconUrl } from '../utils/champions';

type ChampionIconProps = {
  championKey: string;
  patchVersion?: string | null;
  size?: number;
  style?: ViewStyle;
};

export function ChampionIcon({
  championKey,
  patchVersion,
  size = 44,
  style,
}: ChampionIconProps): React.JSX.Element {
  const radius = Math.max(6, Math.round(size * 0.2));

  return (
    <View
      style={[
        styles.wrap,
        { width: size, height: size, borderRadius: radius },
        style,
      ]}
    >
      <Image
        source={{ uri: getChampionIconUrl(championKey, patchVersion) }}
        style={{ width: size, height: size, borderRadius: radius }}
        resizeMode="cover"
      />
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    overflow: 'hidden',
    backgroundColor: colors.surfaceVariant,
  },
});
