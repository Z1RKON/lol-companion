import React, { useEffect } from 'react';
import { StatusBar } from 'react-native';
import { AppNavigator } from './src/navigation/AppNavigator';
import { colors } from './src/theme/material';
import { ensureChampionCatalog } from './src/utils/champions';
import { initDdragonAssets } from './src/utils/validation';

export default function App(): React.JSX.Element {
  useEffect(() => {
    void Promise.all([ensureChampionCatalog(), initDdragonAssets()]);
  }, []);

  return (
    <>
      <StatusBar barStyle="light-content" backgroundColor={colors.primary} />
      <AppNavigator />
    </>
  );
}
