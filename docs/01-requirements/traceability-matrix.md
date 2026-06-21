# Матрица трассировки требований

| ID | Требование | Use Case | Компонент | Тест |
|----|------------|----------|-----------|------|
| FR-01 | Регистрация и вход | UC-003 | AuthController, AuthService | AuthServiceTest |
| FR-02 | Поиск призывателя | UC-001 | SummonerService, RiotApiClient | SummonerServiceTest |
| FR-03 | Профиль: ранг, LP, винрейт | UC-001 | SummonerDtoMapper | SummonerEntityTest |
| FR-04 | История матчей | UC-001, UC-005 | MatchService | SummonerControllerTest |
| FR-05 | Частые союзники | UC-001 | MatchService | — |
| FR-06 | Избранное по PUUID | UC-002 | FavoriteService | FavoriteServiceTest |
| FR-07 | Привязка Riot ID | UC-004 | UserRiotService | UserRiotServiceTest |
| NFR-01 | Cache-Aside TTL 10 мин | UC-001 | Summoner.isCacheFresh() | SummonerEntityTest |
| NFR-02 | Секреты в .env | — | application.yml | Ручная проверка |
| NFR-03 | JSON-ошибки без stack trace | — | GlobalExceptionHandler | GlobalExceptionHandlerTest |
| NFR-04 | TypeScript strict | — | mobile/src/types/api.ts | — |
| NFR-05 | Покрытие ≥ 40% | — | JaCoCo, Jest | build.gradle, package.json |
