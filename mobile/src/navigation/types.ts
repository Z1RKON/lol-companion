import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import type { BottomTabScreenProps } from '@react-navigation/bottom-tabs';
import type { CompositeScreenProps } from '@react-navigation/native';
import type { LoLRegion } from '../types/api';

export type RootStackParamList = {
  Login: undefined;
  MainTabs: undefined;
  Profile: { puuid: string; region?: LoLRegion };
  MatchDetail: { matchId: string; focusPuuid?: string };
};

export type MainTabParamList = {
  SearchTab: undefined;
  FavouritesTab: undefined;
  AccountTab: undefined;
};

export type LoginScreenProps = NativeStackScreenProps<RootStackParamList, 'Login'>;
export type SearchScreenProps = CompositeScreenProps<
  BottomTabScreenProps<MainTabParamList, 'SearchTab'>,
  NativeStackScreenProps<RootStackParamList>
>;
export type FavouritesScreenProps = CompositeScreenProps<
  BottomTabScreenProps<MainTabParamList, 'FavouritesTab'>,
  NativeStackScreenProps<RootStackParamList>
>;
export type AccountScreenProps = CompositeScreenProps<
  BottomTabScreenProps<MainTabParamList, 'AccountTab'>,
  NativeStackScreenProps<RootStackParamList>
>;
export type ProfileScreenProps = NativeStackScreenProps<RootStackParamList, 'Profile'>;
export type MatchDetailScreenProps = NativeStackScreenProps<
  RootStackParamList,
  'MatchDetail'
>;

/* eslint-disable @typescript-eslint/no-namespace, @typescript-eslint/no-empty-object-type -- React Navigation */
declare global {
  namespace ReactNavigation {
    interface RootParamList extends RootStackParamList {}
  }
}
/* eslint-enable @typescript-eslint/no-namespace, @typescript-eslint/no-empty-object-type */
