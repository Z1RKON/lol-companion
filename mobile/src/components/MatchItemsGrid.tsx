import React from 'react';
import { Image, StyleSheet, Text, View } from 'react-native';
import { colors } from '../theme/material';
import { getItemIconUrl } from '../utils/validation';

type MatchItemsGridProps = {
  items?: number[];
  patchVersion?: string | null;
  slotSize?: number;
};

/** Сетка предметов 2×3 для истории матчей. */
export function MatchItemsGrid({
  items,
  patchVersion,
  slotSize = 32,
}: MatchItemsGridProps): React.JSX.Element {
  const slots =
    items != null && items.length > 0 ? items.slice(0, 6) : [0, 0, 0, 0, 0, 0];
  const topRow = slots.slice(0, 3);
  const bottomRow = slots.slice(3, 6);

  return (
    <View style={styles.grid}>
      {[topRow, bottomRow].map((row, rowIndex) => (
        <View key={`row-${rowIndex}`} style={styles.row}>
          {row.map((itemId, index) => {
            const iconUrl = getItemIconUrl(itemId, patchVersion);
            return (
              <View
                key={`${rowIndex}-${index}-${itemId}`}
                style={[
                  styles.slot,
                  { width: slotSize, height: slotSize },
                ]}
              >
                {iconUrl ? (
                  <Image
                    source={{ uri: iconUrl }}
                    style={{ width: slotSize - 4, height: slotSize - 4 }}
                    resizeMode="contain"
                  />
                ) : (
                  <Text style={styles.empty}>—</Text>
                )}
              </View>
            );
          })}
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  grid: {
    gap: 4,
    marginTop: 8,
  },
  row: {
    flexDirection: 'row',
    gap: 4,
  },
  slot: {
    borderRadius: 6,
    backgroundColor: colors.surfaceVariant,
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  empty: {
    fontSize: 10,
    color: colors.onSurfaceVariant,
  },
});
