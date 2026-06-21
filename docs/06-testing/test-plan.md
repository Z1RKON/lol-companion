# План тестирования

## Модульное тестирование (backend)

**Инструменты:** JUnit 5, Mockito, MockMvc

| Класс | Область |
|-------|---------|
| AuthServiceTest, AuthControllerTest | UC-003 |
| SummonerServiceTest, SummonerControllerTest | UC-001 |
| FavoriteServiceTest | UC-002 |
| UserRiotServiceTest | UC-004 |
| JwtServiceTest, GlobalExceptionHandlerTest | Безопасность, ошибки |
| SummonerEntityTest, UserEntityTest | Entity |
| RiotIdNormalizerTest, GameModeLabelsTest, RiotRegionTest | Утилиты |

**Запуск:**

```powershell
cd backend
.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification
```

## Модульное тестирование (mobile)

**Инструменты:** Jest (jest-expo)

| Файл | Область |
|------|---------|
| validation.test.ts | Валидация Riot ID |
| riotAccount.test.ts | Парсинг аккаунта |
| specialSummoners.test.ts | Константы |

```powershell
cd mobile
npm run test:coverage
```

## Системное тестирование

Ручная проверка на Android-эмуляторе: регистрация, поиск, профиль, избранное, привязка Riot ID.

