# PROJECT CONTEXT — LoL Companion (курсовая ПИ)

---

## 1. Цель проекта

Мобильное приложение-компаньон для **League of Legends**:
- регистрация/вход и привязка Riot ID;
- поиск и просмотр профиля призывателя (Riot ID: Имя#Тег);
- история матчей, KDA, союзники;
- избранные игроки (идентификация по **PUUID**).

Данные из **Riot Games API**, кэш в **PostgreSQL**, чтобы не превышать rate limit.

---

## 2. Стек и архитектура

| Часть | Технологии |
|-------|------------|
| Backend | Java **17**, Spring Boot 3.3, JPA, Flyway, PostgreSQL, Spring Security, JWT (JJWT), RestTemplate |
| Mobile | React Native (Expo SDK 54), TypeScript, Zustand, React Navigation, Axios, AsyncStorage |
| Архитектура | **PCMEF** (строго) |

### PCMEF — направление зависимостей

```
Presentation  →  Control (@RestController)
Control       →  Mediator (@Service)
Mediator      →  Foundation (@Repository, RiotApiClient)
Entity        →  только бизнес-методы внутри классов, БЕЗ вызова Repository/API
```

**Запрещено:** Repository → Service, Controller → RiotApiClient, Entity → БД напрямую.

### Слои backend (`com.lolcompanion`)

| Слой | Пакет / классы |
|------|----------------|
| Presentation | `advice/GlobalExceptionHandler` |
| Control | `controller/AuthController`, `controller/SummonerController` |
| Mediator | `service/*Service`, `service/RiotApiClient` |
| Entity | `entity/*` |
| Foundation | `repository/*`, `config/*`, `resources/db/` |

---

## 3. Пять обязательных правил курса

| # | Правило | Где проверить |
|---|---------|---------------|
| 1 | Controller → только Service; Service → Repository | `docs/12-final-report/compliance/COMPLIANCE-AUDIT.md` |
| 2 | `@RestControllerAdvice`, JSON-ошибки без stack trace | `GlobalExceptionHandler` |
| 3 | TypeScript: без `any`, интерфейсы DTO | `mobile/src/types/api.ts` |
| 4 | Секреты: `riot.api.key: ${RIOT_API_KEY}`, не в коде | `application.yml`, `backend/.env` |
| 5 | Матчи — `FlatList`; ввод с очисткой и валидацией | `ProfileScreen`, `SearchInput` |

---

## 4. База данных (PostgreSQL)

**БД:** `lol_companion_db`  
**Миграция:** `backend/src/main/resources/db/migration/V1__initial_schema.sql`

| Таблица | Назначение |
|---------|------------|
| `users` | Локальные пользователи + привязка Riot ID |
| `summoners` | Кэш профилей LoL (`riot_puuid`, ранг, LP, W/L) |
| `matches` | Матчи (неизменяемый кэш) |
| `participant_stats` | KDA, чемпион, CS, win per match |
| `user_favorite_summoners` | M:N user ↔ summoner |
| `champion_stats` | Агрегат по чемпионам (опционально) |

**Индексы:** `users.username`, `summoners.riot_puuid`, `summoners.summoner_name`.

---

## 5. Riot API (Mediator)

**Клиент:** `RiotApiClient` + `RiotApiConfig` (RestTemplate, заголовок `X-Riot-Token`).

| Эндпоинт Riot | Метод клиента |
|---------------|---------------|
| Account-V1: Riot ID (gameName + tagLine) | `getAccountByRiotId` |
| `/lol/summoner/v4/summoners/by-name/{name}` (+ legacy) | `getSummonerByName` |
| `/lol/summoner/v4/summoners/by-puuid/{puuid}` | `getSummonerByPuuid` |
| `/lol/league/v4/entries/by-puuid/{puuid}` | `getLeagueEntriesByPuuid` |
| `/lol/match/v5/matches/by-puuid/{puuid}/ids` | `getMatchIds` |
| `/lol/match/v5/matches/{matchId}` | `getMatchDetails` |

**Конфиг:** `riot.api.cache-ttl-minutes`: **10** (Cache-Aside), регион по умолчанию RU.

### SummonerService — Cache-Aside

1. Поиск в БД по Riot ID / нику и региону.
2. Если `lastUpdated` свежее TTL → вернуть из PostgreSQL.
3. Иначе → Riot API → save → DTO.

### MatchService

1. ID матчей из Riot.
2. Есть в БД → читаем с участниками.
3. Нет → детали матча → парсинг → `Match` + `ParticipantStats` в одной транзакции.

---

## 6. REST API (Control + JWT)

**Base URL:** `http://localhost:8080/api`  
**Полный список:** `docs/09-api/REST-ENDPOINTS.md`, `docs/09-api/openapi.yaml`

### Публичные
- `POST /auth/register`
- `POST /auth/login`

### С Bearer JWT
**Auth:**
- `GET /auth/me`
- `PUT /auth/me/riot-account`
- `DELETE /auth/me/riot-account`
- `GET /auth/me/summoner`

**Summoner:**
- `GET /summoner/search?name=&region=RU`
- `GET /summoner/by-name/{name}?region=RU`
- `GET /summoner/{puuid}` — профиль из локальной БД
- `GET /summoner/{puuid}/matches?count=20`
- `GET /summoner/{puuid}/teammates?matches=20&limit=20`
- `GET /summoner/matches/{matchId}`
- `POST /summoner/{puuid}/refresh`
- `GET /summoner/favorites`
- `POST /summoner/favorites` — body: `{ "puuid": "..." }`
- `DELETE /summoner/favorites/{summonerId}`

**Итого:** 16 эндпоинтов (2 публичных + 14 за JWT).

---

## 7. Mobile

Путь: `mobile/`

| Компонент | Роль |
|-----------|------|
| `navigation/AppNavigator.tsx` | Стек: Login → MainTabs → Profile / MatchDetail |
| `screens/LoginScreen.tsx` | Вход и регистрация |
| `screens/SearchScreen.tsx` | Поиск по Riot ID, выбор региона |
| `screens/ProfileScreen.tsx` | Профиль по `puuid` (route param), матчи, союзники |
| `screens/FavouritesScreen.tsx` | Избранное, открытие профиля по PUUID |
| `screens/AccountScreen.tsx` | Привязка Riot ID, выход |
| `store/useAuthStore.ts` | JWT, пользователь |
| `store/useSummonerStore.ts` | Поиск, `loadSummonerByPuuid`, матчи |
| `api/client.ts` | Axios + interceptor `Authorization: Bearer` |
| `types/api.ts` | Строгие DTO |

**Навигация к профилю:** `Profile: { puuid: string; region?: LoLRegion }` — ник только для отображения (из API).

---

## 8. Тесты и покрытие

### Backend (JUnit 5 + Mockito + JaCoCo)

~64 unit-теста: сервисы, контроллеры, security, entity, exception handler.

```powershell
cd backend
.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification
# HTML: backend\build\reports\jacoco\test\html\index.html
```


### Mobile (Jest)

```powershell
cd mobile
npm run test:coverage
```


---

## 9. Запуск

### Backend
```powershell
cd C:\Projects\lol-companion\backend
.\start-with-env.ps1
```

### Mobile (эмулятор Android)
```powershell
cd C:\Projects\lol-companion\mobile
npm run android
```

Или ярлык **LoL Companion** / `C:\Projects\lol-companion\start-lol-companion.ps1` (backend + mobile).

### Переменные окружения

| Переменная | Назначение |
|------------|------------|
| `RIOT_API_KEY` | Ключ Riot (`backend/.env`) |
| `DB_PASSWORD` | Пароль PostgreSQL |
| `JWT_SECRET` | Секрет JWT (мин. 32 символа) |
| `EXPO_PUBLIC_API_URL` | `http://10.0.2.2:8080/api` для эмулятора 

---

## 10. Документация

| Файл | Содержание |
|------|------------|
| `docs/00-project-charter/glossary.md` | Бизнес-глоссарий (22 термина) |
| `docs/01-requirements/use-cases-detailed.md` | UC-001…UC-007 |
| `docs/01-requirements/technical-specification.md` | ТЗ (ГОСТ 34.602) |
| `docs/02-architecture/pcmef-layers.md` | PCMEF |
| `docs/09-api/REST-ENDPOINTS.md` | REST + JWT |
| `docs/10-deployment/admin-guide.md` | Руководство администратора |
| `docs/12-final-report/compliance/COMPLIANCE-AUDIT.md` | Аудит PCMEF |
| `docs/07-report/POYASNITELNAYA-ZAPISKA.md` | Пояснительная записка (локально) |

---

## 11. Быстрая навигация по коду

```
C:\Projects\lol-companion\
├── backend/          # Spring Boot API
├── mobile/           # Expo / React Native
├── docs/             # документация
├── start-lol-companion.ps1
└── LoL Companion.bat
```

---

