# Структура проекта

```
lol-companion/
├── backend/                 # Spring Boot API
│   └── src/main/java/com/lolcompanion/
│       ├── controller/      # Control
│       ├── service/         # Mediator
│       ├── entity/          # Entity
│       ├── repository/      # Foundation
│       ├── security/
│       ├── advice/          # Presentation (errors)
│       └── dto/
├── mobile/                  # React Native (Expo)
│   └── src/
│       ├── screens/         # Presentation
│       ├── navigation/
│       ├── store/           # Zustand
│       ├── api/
│       └── utils/
├── docs/                    # Документация по этапам
└── README.md
```

## Ключевые классы Mediator

| Класс | Ответственность |
|-------|-----------------|
| SummonerService | Cache-Aside профиля, поиск по Riot ID / PUUID |
| MatchService | История матчей, immutable cache |
| FavoriteService | CRUD избранного по PUUID |
| RiotApiClient | Account, Summoner, League, Match API |
| AuthService | Регистрация, login, BCrypt |

## Mobile экраны

LoginScreen, SearchScreen, ProfileScreen, FavouritesScreen, AccountScreen, MatchDetailScreen.
