import React from 'react';
import {
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
  type TextInputProps,
} from 'react-native';
import { colors, elevation, spacing } from '../theme/material';

type SearchInputProps = {
  value: string;
  onChangeText: (text: string) => void;
  onSubmit?: () => void;
  placeholder?: string;
  editable?: boolean;
  autoCapitalize?: TextInputProps['autoCapitalize'];
  autoCorrect?: TextInputProps['autoCorrect'];
  returnKeyType?: TextInputProps['returnKeyType'];
};

export function SearchInput({
  value,
  onChangeText,
  onSubmit,
  placeholder = 'Имя#Тег',
  editable = true,
  autoCapitalize = 'none',
  autoCorrect = false,
  returnKeyType = 'search',
}: SearchInputProps): React.JSX.Element {
  return (
    <View style={styles.searchCard}>
      <TextInput
        style={styles.input}
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={colors.onSurfaceVariant}
        autoCapitalize={autoCapitalize}
        autoCorrect={autoCorrect}
        returnKeyType={returnKeyType}
        onSubmitEditing={onSubmit}
        editable={editable}
      />
      {value.length > 0 ? (
        <TouchableOpacity
          onPress={() => onChangeText('')}
          style={styles.clearBtn}
          accessibilityLabel="Очистить"
        >
          <Text style={styles.clearText}>✕</Text>
        </TouchableOpacity>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  searchCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.card,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.outline,
    paddingHorizontal: spacing.md,
    ...elevation.card,
  },
  input: {
    flex: 1,
    paddingVertical: 14,
    fontSize: 16,
    color: colors.onSurface,
  },
  clearBtn: { padding: spacing.sm },
  clearText: { fontSize: 18, color: colors.onSurfaceVariant },
});
