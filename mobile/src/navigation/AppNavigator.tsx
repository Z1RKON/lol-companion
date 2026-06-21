import React, { useEffect } from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { NavigationContainer } from '@react-navigation/native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { useAuthStore } from '../store/useAuthStore';
import { useSummonerStore } from '../store/useSummonerStore';
import { colors, spacing } from '../theme/material';
import { LoginScreen } from '../screens/LoginScreen';
import { SearchScreen } from '../screens/SearchScreen';
import { ProfileScreen } from '../screens/ProfileScreen';
import { MatchDetailScreen } from '../screens/MatchDetailScreen';
import { FavouritesScreen } from '../screens/FavouritesScreen';
import { AccountScreen } from '../screens/AccountScreen';
import type { MainTabParamList, RootStackParamList } from './types';

const Stack = createNativeStackNavigator<RootStackParamList>();
const Tab = createBottomTabNavigator<MainTabParamList>();

function MainTabs(): React.JSX.Element {
  return (
    <Tab.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: colors.primary },
        headerTintColor: colors.onPrimary,
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.onSurfaceVariant,
      }}
    >
      <Tab.Screen
        name="SearchTab"
        component={SearchScreen}
        options={{
          title: 'Поиск',
          tabBarLabel: 'Поиск',
          tabBarIcon: ({ color }) => <Text style={{ color, fontSize: 18 }}>🔍</Text>,
        }}
      />
      <Tab.Screen
        name="FavouritesTab"
        component={FavouritesScreen}
        options={{
          title: 'Избранное',
          tabBarLabel: 'Избранное',
          tabBarIcon: ({ color }) => <Text style={{ color, fontSize: 18 }}>★</Text>,
        }}
      />
      <Tab.Screen
        name="AccountTab"
        component={AccountScreen}
        options={{
          title: 'Аккаунт',
          tabBarLabel: 'Аккаунт',
          tabBarIcon: ({ color }) => <Text style={{ color, fontSize: 18 }}>👤</Text>,
        }}
      />
    </Tab.Navigator>
  );
}

function BootstrapGate({ children }: { children: React.ReactNode }): React.JSX.Element {
  const isHydrating = useAuthStore((state) => state.isHydrating);

  if (isHydrating) {
    return (
      <View style={styles.bootstrap}>
        <ActivityIndicator size="large" color={colors.primary} />
        <Text style={styles.bootstrapText}>Загрузка…</Text>
      </View>
    );
  }

  return <>{children}</>;
}

export function AppNavigator(): React.JSX.Element {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const hydrateSession = useAuthStore((state) => state.hydrateSession);
  const hydrateRegion = useSummonerStore((state) => state.hydrateRegion);

  useEffect(() => {
    void (async () => {
      await hydrateRegion();
      await hydrateSession();
    })();
  }, [hydrateRegion, hydrateSession]);

  return (
    <SafeAreaProvider>
      <BootstrapGate>
        <NavigationContainer>
          <Stack.Navigator
            key={isAuthenticated ? 'authenticated' : 'guest'}
            screenOptions={{
              headerStyle: { backgroundColor: colors.primary },
              headerTintColor: colors.onPrimary,
              contentStyle: { backgroundColor: colors.background },
            }}
          >
            {!isAuthenticated ? (
              <Stack.Screen
                name="Login"
                component={LoginScreen}
                options={{ headerShown: false }}
              />
            ) : (
              <>
                <Stack.Screen
                  name="MainTabs"
                  component={MainTabs}
                  options={{ headerShown: false }}
                />
                <Stack.Screen
                  name="Profile"
                  component={ProfileScreen}
                  options={{ title: 'Профиль' }}
                />
                <Stack.Screen
                  name="MatchDetail"
                  component={MatchDetailScreen}
                  options={{ title: 'Детали матча' }}
                />
              </>
            )}
          </Stack.Navigator>
        </NavigationContainer>
      </BootstrapGate>
    </SafeAreaProvider>
  );
}

const styles = StyleSheet.create({
  bootstrap: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: colors.background,
  },
  bootstrapText: {
    marginTop: spacing.md,
    color: colors.onSurfaceVariant,
  },
});
