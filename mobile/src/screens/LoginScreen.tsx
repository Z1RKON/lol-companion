import React, { useState } from 'react';
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { resolveApiBaseUrl } from '../config/api';
import { useAuthStore } from '../store/useAuthStore';
import { colors, spacing, typography } from '../theme/material';
import {
  validateEmail,
  validateLoginIdentifier,
  validatePassword,
  validateUsername,
} from '../utils/validation';

type AuthMode = 'login' | 'register';

export function LoginScreen(): React.JSX.Element {
  const [mode, setMode] = useState<AuthMode>('login');
  const [identifier, setIdentifier] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const { status, errorMessage, login, register } = useAuthStore();
  const loading = status === 'LOADING';

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};
    const passwordError = validatePassword(password);

    if (mode === 'login') {
      const identifierError = validateLoginIdentifier(identifier);
      if (identifierError) {
        errors.identifier = identifierError;
      }
    } else {
      const usernameError = validateUsername(username);
      if (usernameError) {
        errors.username = usernameError;
      }
      const emailError = validateEmail(email);
      if (emailError) {
        errors.email = emailError;
      }
    }

    if (passwordError) {
      errors.password = passwordError;
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (): Promise<void> => {
    if (!validateForm()) {
      return;
    }

    const success =
      mode === 'login'
        ? await login(identifier.trim(), password)
        : await register(username.trim(), email.trim(), password);

    if (!success) {
      return;
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.flex}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <ScrollView
        contentContainerStyle={styles.container}
        keyboardShouldPersistTaps="handled"
      >
        <View style={styles.hero}>
          <Text style={styles.appName}>LoL Companion</Text>
          <Text style={styles.subtitle}>
            {mode === 'login' ? 'Вход в аккаунт' : 'Регистрация'}
          </Text>
        </View>

        <View style={styles.card}>
          {mode === 'login' ? (
            <>
              <Text style={styles.label}>Email или логин</Text>
              <TextInput
                style={[styles.input, fieldErrors.identifier ? styles.inputError : undefined]}
                value={identifier}
                onChangeText={setIdentifier}
                keyboardType="email-address"
                autoCapitalize="none"
                autoCorrect={false}
                editable={!loading}
              />
              {fieldErrors.identifier ? (
                <Text style={styles.fieldError}>{fieldErrors.identifier}</Text>
              ) : null}
            </>
          ) : (
            <>
              <Text style={styles.label}>Имя пользователя</Text>
              <TextInput
                style={[styles.input, fieldErrors.username ? styles.inputError : undefined]}
                value={username}
                onChangeText={setUsername}
                autoCapitalize="none"
                autoCorrect={false}
                editable={!loading}
              />
              {fieldErrors.username ? (
                <Text style={styles.fieldError}>{fieldErrors.username}</Text>
              ) : null}

              <Text style={styles.label}>Email</Text>
              <TextInput
                style={[styles.input, fieldErrors.email ? styles.inputError : undefined]}
                value={email}
                onChangeText={setEmail}
                keyboardType="email-address"
                autoCapitalize="none"
                autoCorrect={false}
                editable={!loading}
              />
              {fieldErrors.email ? (
                <Text style={styles.fieldError}>{fieldErrors.email}</Text>
              ) : null}
            </>
          )}

          <Text style={styles.label}>Пароль</Text>
          <TextInput
            style={[styles.input, fieldErrors.password ? styles.inputError : undefined]}
            value={password}
            onChangeText={setPassword}
            secureTextEntry
            editable={!loading}
          />
          {fieldErrors.password ? (
            <Text style={styles.fieldError}>{fieldErrors.password}</Text>
          ) : null}

          {__DEV__ ? (
            <Text style={styles.devApiHint}>API: {resolveApiBaseUrl()}</Text>
          ) : null}

          {errorMessage ? <Text style={styles.apiError}>{errorMessage}</Text> : null}

          <TouchableOpacity
            style={[styles.primaryButton, loading && styles.buttonDisabled]}
            onPress={() => void handleSubmit()}
            disabled={loading}
            activeOpacity={0.85}
          >
            {loading ? (
              <ActivityIndicator color={colors.onPrimary} />
            ) : (
              <Text style={styles.primaryButtonText}>
                {mode === 'login' ? 'Войти' : 'Зарегистрироваться'}
              </Text>
            )}
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.linkButton}
            onPress={() => {
              setMode(mode === 'login' ? 'register' : 'login');
              setFieldErrors({});
            }}
            disabled={loading}
          >
            <Text style={styles.linkText}>
              {mode === 'login'
                ? 'Нет аккаунта? Зарегистрироваться'
                : 'Уже есть аккаунт? Войти'}
            </Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: colors.background },
  container: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: spacing.lg,
  },
  hero: { marginBottom: spacing.lg, alignItems: 'center' },
  appName: { ...typography.headline, color: colors.primary },
  subtitle: { ...typography.body, color: colors.onSurfaceVariant, marginTop: spacing.sm },
  card: {
    backgroundColor: colors.card,
    borderRadius: 16,
    padding: spacing.lg,
    gap: spacing.sm,
  },
  label: { ...typography.label, color: colors.onSurfaceVariant, marginTop: spacing.sm },
  input: {
    borderWidth: 1,
    borderColor: colors.outline,
    borderRadius: 12,
    paddingHorizontal: spacing.md,
    paddingVertical: 12,
    fontSize: 16,
    backgroundColor: colors.surface,
  },
  inputError: { borderColor: colors.error },
  fieldError: { color: colors.error, fontSize: 12 },
  devApiHint: {
    ...typography.caption,
    color: colors.onSurfaceVariant,
    marginBottom: spacing.sm,
  },
  apiError: {
    color: colors.error,
    backgroundColor: colors.errorContainer,
    padding: spacing.sm,
    borderRadius: 8,
    marginTop: spacing.sm,
  },
  primaryButton: {
    backgroundColor: colors.primary,
    borderRadius: 24,
    paddingVertical: 14,
    alignItems: 'center',
    marginTop: spacing.md,
  },
  buttonDisabled: { opacity: 0.7 },
  primaryButtonText: { color: colors.onPrimary, fontWeight: '600', fontSize: 16 },
  linkButton: { alignItems: 'center', marginTop: spacing.md },
  linkText: { color: colors.primary, fontSize: 14 },
});
