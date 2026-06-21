import React from 'react';
import { ScrollView, StyleSheet, Text, TouchableOpacity } from 'react-native';
import { colors, spacing } from '../theme/material';
import { LOL_REGIONS, type LoLRegion } from '../types/api';

type RegionPickerProps = {
  selected: LoLRegion;
  onSelect: (region: LoLRegion) => void;
};

/** Горизонтальный выбор региона без FlatList внутри ScrollView (не блокирует клики). */
export function RegionPicker({
  selected,
  onSelect,
}: RegionPickerProps): React.JSX.Element {
  return (
    <ScrollView
      horizontal
      showsHorizontalScrollIndicator={false}
      nestedScrollEnabled
      keyboardShouldPersistTaps="handled"
      contentContainerStyle={styles.list}
    >
      {LOL_REGIONS.map((item) => {
        const active = item === selected;
        return (
          <TouchableOpacity
            key={item}
            style={[styles.chip, active && styles.chipActive]}
            onPress={() => onSelect(item)}
            activeOpacity={0.8}
          >
            <Text style={[styles.chipText, active && styles.chipTextActive]}>{item}</Text>
          </TouchableOpacity>
        );
      })}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  list: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: spacing.sm,
    gap: spacing.sm,
  },
  chip: {
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: 20,
    backgroundColor: colors.surfaceVariant,
  },
  chipActive: { backgroundColor: colors.primaryContainer },
  chipText: { color: colors.onSurfaceVariant, fontWeight: '500' },
  chipTextActive: { color: colors.onPrimaryContainer },
});
