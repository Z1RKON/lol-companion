import { Alert, Platform } from 'react-native';

type ConfirmOptions = {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  destructive?: boolean;
};

/** Подтверждение действия (Alert на телефоне, window.confirm в браузере). */
export function confirmAction({
  title,
  message,
  confirmLabel = 'OK',
  cancelLabel = 'Отмена',
  destructive = false,
}: ConfirmOptions): Promise<boolean> {
  const fullMessage = `${title}\n\n${message}`;

  if (Platform.OS === 'web') {
    return Promise.resolve(window.confirm(fullMessage));
  }

  return new Promise((resolve) => {
    Alert.alert(title, message, [
      { text: cancelLabel, style: 'cancel', onPress: () => resolve(false) },
      {
        text: confirmLabel,
        style: destructive ? 'destructive' : 'default',
        onPress: () => resolve(true),
      },
    ]);
  });
}
