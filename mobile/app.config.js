/** Expo SDK 54 — подхватывает EXPO_PUBLIC_* из .env */
module.exports = {
  expo: {
    name: 'LoL Companion',
    slug: 'lol-companion',
    version: '1.0.0',
    orientation: 'portrait',
    userInterfaceStyle: 'light',
    android: {
      package: 'com.anonymous.lolcompanion',
      usesCleartextTraffic: true,
    },
    ios: {
      bundleIdentifier: 'com.anonymous.lolcompanion',
    },
    extra: {
      apiUrl: process.env.EXPO_PUBLIC_API_URL,
    },
  },
};
