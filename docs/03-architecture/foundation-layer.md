# Архитектура Foundation слоя (Сущности и репозитории)

## Обзор структуры БД и PCMEF паттерна

Проект следует **PCMEF** архитектуре (Presentation-Control-Mediator-Entity-Foundation):

```
┌─────────────────────────────────────────────────────────────┐
│ Presentation слой (React Native UI)                         │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│ Control слой (@RestController)                              │
│ - SummonerController, MatchController, UserController       │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│ Mediator слой (@Service, бизнес-логика)                    │
│ - SummonerService, MatchService, RiotApiService            │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│ Entity слой (@Entity, бизнес-правила данных)               │
│ - User, Summoner, Match, ParticipantStats и т.д.           │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│ Foundation слой (JpaRepository, JDBC, SQL)                  │
│ - UserRepository, SummonerRepository и т.д.                 │
└──────────────────────┬──────────────────────────────────────┘
                       │
                   PostgreSQL БД
```

---

## Entity диаграмма взаимосвязей

```
┌────────────┐                    ┌──────────────────────┐
│   User     │                    │     Summoner         │
│────────────│                    │──────────────────────│
│ id         │                    │ id                   │
│ username   │                    │ puuid (UNIQUE)       │
│ email      │◄──── M:M ──────┐   │ summoner_name        │
│ role       │                │   │ summoner_level       │
│ password   │                │   │ tier, rank, LP       │
│ created_at │         ┌──────┴───┤ winCount, lossCount  │
└────────────┘         │          │ last_updated         │
                       │          └──────────┬───────────┘
                ┌──────▼───────────────┐     │
                │UserFavoriteSummoner  │     │ 1:M
                │──────────────────────│     │
                │ id                   │     │
                │ user_id (FK) ────────┼─────┤
                │ summoner_id (FK) ────┼─────┤
                │ added_at             │     │
                └──────────────────────┘     │
                                             │
                                  ┌──────────▼──────────┐
                                  │  ParticipantStats   │
                                  │──────────────────── │
                                  │ id                  │
                                  │ match_id (FK) ──┐   │
                                  │ summoner_id (FK)│   │
                                  │ puuid           │   │
                                  │ champion_name   │   │
                                  │ kills, deaths   │   │
                                  │ assists, cs     │   │
                                  │ gold, damage    │   │
                                  │ win/loss        │   │
                                  │ items (1-7)     │   │
                                  └─────────────────┼───┘
                                                    │
                                       ┌────────────▼────────┐
                                       │     Match           │
                                       │────────────────────│
                                       │ id                  │
                                       │ match_id (UNIQUE)   │
                                       │ game_mode           │
                                       │ game_duration       │
                                       │ timestamps          │
                                       │ winning_team        │
                                       │ region              │
                                       └─────────────────────┘

┌─────────────────┐          ┌──────────────────────┐
│   Summoner      │          │   ChampionStats      │
│                 │ 1:M      │──────────────────────│
│                 │◄─────────┤ id                   │
│                 │          │ summoner_id (FK)     │
│                 │          │ champion_name        │
└─────────────────┘          │ total_games          │
                             │ total_wins           │
                             │ total_losses         │
                             │ avg_kda, avg_cs      │
                             │ last_played          │
                             │ created_at           │
                             └──────────────────────┘
```

---

## Описание сущностей

### 1. User (Локальный пользователь)
**Таблица:** `users`

| Поле | Тип | Примечание |
|------|-----|-----------|
| id | BIGSERIAL | Primary Key |
| username | VARCHAR(50) | Уникальный, индекс для быстрого поиска |
| password_hash | VARCHAR(255) | Захэширован (bcrypt) |
| email | VARCHAR(100) | Уникальный |
| role | ENUM | USER, ADMIN, MODERATOR |
| created_at | TIMESTAMP | Неизменяемо |
| updated_at | TIMESTAMP | Обновляется при каждой модификации |

**Бизнес-логика:**
- `addFavoriteSummoner()` — добавить игрока в избранное
- `removeFavoriteSummoner()` — удалить из избранного
- `isSummonerInFavorites()` — проверка наличия в избранном

---

### 2. Summoner (Игрок League of Legends)
**Таблица:** `summoners`

| Поле | Тип | Примечание |
|------|-----|-----------|
| id | BIGSERIAL | Primary Key |
| puuid | VARCHAR(78) | Riot Games ID, UNIQUE, индекс |
| summoner_name | VARCHAR(50) | Имя игрока, индекс |
| summoner_level | INTEGER | 1-500 |
| profile_icon_id | INTEGER | ID иконки профиля |
| tier | ENUM | IRON..CHALLENGER, может быть NULL (unranked) |
| rank | ENUM | I, II, III, IV |
| league_points | INTEGER | LP текущего ранга |
| win_count | INTEGER | Победы в текущем сезоне |
| loss_count | INTEGER | Поражения в текущем сезоне |
| region | VARCHAR(10) | EUW1, NA1, KR, CN1 и т.д. |
| last_updated | TIMESTAMP | Время кэширования (для валидации кэша) |
| created_at | TIMESTAMP | Неизменяемо |

**Бизнес-логика:**
- `getWinRate()` — вычисляет процент побед
- `updateRankStats()` — обновляет ранг и LP
- `addMatchResult()` — добавляет результат матча в статистику
- `isCacheFresh()` — проверяет, свежи ли данные (< 1 часа)
- `getFullRank()` — возвращает "PLATINUM I" вместо отдельных значений

---

### 3. Match (Матч League of Legends)
**Таблица:** `matches`

| Поле | Тип | Примечание |
|------|-----|-----------|
| id | BIGSERIAL | Primary Key |
| match_id | VARCHAR(50) | Riot Match ID, UNIQUE |
| game_mode | VARCHAR(30) | RANKED_SOLO_5x5, ARAM, etc |
| game_duration_minutes | INTEGER | Длительность в минутах |
| game_creation_timestamp | BIGINT | Unix timestamp создания |
| game_start_timestamp | BIGINT | Unix timestamp начала |
| game_end_timestamp | BIGINT | Unix timestamp конца |
| winning_team | ENUM | BLUE, RED |
| region | VARCHAR(10) | Регион (EUW1, NA1, KR) |
| patch_version | VARCHAR(20) | Версия патча (11.5, 12.1) |
| created_at | TIMESTAMP | Когда добавлено в БД |

**Бизнес-логика:**
- `getWinningTeamParticipants()` — список игроков выигрышной команды
- `getLosingTeamParticipants()` — список игроков проигрышной команды
- `getAverageGoldPerTeam()` — среднее золото на команду
- `getTotalDurationSeconds()` — длительность в секундах

---

### 4. ParticipantStats (Статистика игрока в матче)
**Таблица:** `participant_stats`

| Поле | Тип | Примечание |
|------|-----|-----------|
| id | BIGSERIAL | Primary Key |
| match_id | BIGINT | FK → matches |
| summoner_id | BIGINT | FK → summoners (SET NULL если удалён) |
| puuid | VARCHAR(78) | Riot PUUID, индекс |
| champion_name | VARCHAR(50) | Имя чемпиона, индекс |
| role | ENUM | TOP, JUNGLE, MIDDLE, BOTTOM, SUPPORT |
| kills | INTEGER | Количество убийств |
| deaths | INTEGER | Количество смертей |
| assists | INTEGER | Количество ассистов |
| cs_score | DECIMAL | Creep Score (убитые миньоны) |
| gold_earned | BIGINT | Заработанное золото |
| damage_dealt | BIGINT | Урон врагам |
| damage_taken | BIGINT | Урон от врагов |
| healing_done | BIGINT | Исцеление союзников |
| team | ENUM | BLUE, RED |
| win | BOOLEAN | Победа/Поражение |
| item_1..6, trinket | VARCHAR(50) | IDs предметов |
| created_at | TIMESTAMP | Когда добавлено |

**Бизнес-логика:**
- `calculateKDA()` — вычисляет KDA: (K+A) / D
- `getFormattedKDA()` — строка "5/2/8"
- `getGoldPerMinute()` — GPM (золото в минуту)
- `getDamagePerMinute()` — DPM (урон в минуту)
- `getCSPerMinute()` — средний CS в минуту
- `getKillParticipation()` — % участия в килах команды
- `getPerformanceType()` — оценка: LEGENDARY, EXCELLENT, GOOD, AVERAGE, POOR

---

### 5. UserFavoriteSummoner (Связь M:M)
**Таблица:** `user_favorite_summoners`

| Поле | Тип | Примечание |
|------|-----|-----------|
| id | BIGSERIAL | Primary Key |
| user_id | BIGINT | FK → users (ON DELETE CASCADE) |
| summoner_id | BIGINT | FK → summoners (ON DELETE CASCADE) |
| added_at | TIMESTAMP | Когда добавлено в избранное |

**UNIQUE:** `(user_id, summoner_id)` — один игрок раз в избранном

---

### 6. ChampionStats (Агрегированная статистика чемпиона)
**Таблица:** `champion_stats`

| Поле | Тип | Примечание |
|------|-----|-----------|
| id | BIGSERIAL | Primary Key |
| summoner_id | BIGINT | FK → summoners |
| champion_name | VARCHAR(50) | Имя чемпиона |
| total_games | INTEGER | Всего игр на этом чемпионе |
| total_wins | INTEGER | Побед |
| total_losses | INTEGER | Поражений |
| avg_kills | DECIMAL | Средние киллы |
| avg_deaths | DECIMAL | Средние смерти |
| avg_assists | DECIMAL | Средние ассисты |
| avg_cs | DECIMAL | Средний CS |
| last_played | TIMESTAMP | Последняя игра |
| created_at, updated_at | TIMESTAMP | Метаданные |

**UNIQUE:** `(summoner_id, champion_name)` — один чемпион на игрока

**Бизнес-логика:**
- `getWinRate()` — процент побед
- `getAverageKDA()` — средний KDA
- `addGameResult()` — добавить результат и пересчитать средние
- `getMasteryLevel()` — оценка мастерства: BEGINNER..MASTERED

---

## Индексы для оптимизации

### Критичные индексы:

| Таблица | Колонки | Назначение |
|---------|---------|-----------|
| users | username | Логин |
| users | email | Восстановление пароля |
| summoners | puuid | Поиск по Riot ID |
| summoners | summoner_name | Поиск по нику |
| summoners | tier, rank | Поиск по рейтингу |
| summoners | last_updated | Валидация кэша |
| matches | match_id | Дедупликация матчей |
| matches | game_creation_timestamp | Сортировка по времени |
| participant_stats | match_id | Получение всех участников матча |
| participant_stats | summoner_id | История матчей игрока |
| participant_stats | puuid | Поиск по Riot ID |
| participant_stats | champion_name | Статистика чемпиона |
| participant_stats | role | Статистика по ролям |

---

## Ограничения и проверки данных

### CHECK constraints:
- `summoner_level BETWEEN 1 AND 500`
- `wins >= 0, losses >= 0, kills >= 0` и т.д. (неотрицательные значения)
- `role IN ('TOP', 'JUNGLE', 'MIDDLE', 'BOTTOM', 'SUPPORT')`
- `tier IN ('IRON', ..., 'CHALLENGER', NULL)`
- `game_duration_minutes > 0`

### UNIQUE constraints:
- `users.username` — уникальное имя пользователя
- `users.email` — уникальная почта
- `summoners.puuid` — уникальный Riot ID
- `matches.match_id` — уникальный ID матча
- `user_favorite_summoners(user_id, summoner_id)` — не добавить дважды
- `champion_stats(summoner_id, champion_name)` — один чемпион на игрока

### FOREIGN KEY constraints:
- `ON DELETE CASCADE` для `user_favorite_summoners` → удалить избранные при удалении пользователя
- `ON DELETE SET NULL` для `participant_stats.summoner_id` → оставить статистику, но без ссылки на суммонера

---

## Примеры SQL запросов

### Получить профиль игрока с рейтингом:
```sql
SELECT * FROM summoners 
WHERE LOWER(summoner_name) = LOWER('Player#TAG') 
  AND region = 'KR'
LIMIT 1;
```

### Получить последние 10 матчей конкретного игрока:
```sql
SELECT m.*, ps.kills, ps.deaths, ps.assists, ps.champion_name, ps.win
FROM matches m
JOIN participant_stats ps ON m.id = ps.match_id
WHERE ps.summoner_id = :summonerId
ORDER BY m.game_creation_timestamp DESC
LIMIT 10;
```

### Получить win rate на конкретном чемпионе:
```sql
SELECT 
  champion_name,
  COUNT(*) as total_games,
  SUM(CASE WHEN win THEN 1 ELSE 0 END) as wins,
  ROUND(100.0 * SUM(CASE WHEN win THEN 1 ELSE 0 END) / COUNT(*), 2) as win_rate
FROM participant_stats
WHERE summoner_id = :summonerId 
  AND champion_name = :championName
GROUP BY champion_name;
```

### Получить топ игроков по рейтингу:
```sql
SELECT * FROM summoners
WHERE tier IS NOT NULL
ORDER BY 
  CASE tier 
    WHEN 'CHALLENGER' THEN 1
    WHEN 'GRANDMASTER' THEN 2
    ...
    WHEN 'IRON' THEN 9
  END,
  CASE rank
    WHEN 'I' THEN 1
    WHEN 'II' THEN 2
    WHEN 'III' THEN 3
    WHEN 'IV' THEN 4
  END,
  league_points DESC
LIMIT 50;
```

---

## Паттерны использования репозиториев

### В Service классе:

```java
// Поиск призывателя по PUUID
Summoner summoner = summonerRepository.findByPuuid(puuid)
    .orElseThrow(() -> new ResourceNotFoundException("Summoner not found"));

// Добавление в избранное
UserFavoriteSummoner favorite = new UserFavoriteSummoner();
favorite.setUser(user);
favorite.setSummoner(summoner);
favorite.setAddedAt(LocalDateTime.now());
userFavoriteSummonerRepository.save(favorite);

// Получение последних матчей
List<ParticipantStats> recentGames = participantStatsRepository
    .findRecentGamesBySummoner(summonerId, 20);

// Получение win rate на чемпионе
List<ParticipantStats> championGames = participantStatsRepository
    .findBySummonerAndChampionNameOrderByCreatedAtDesc(summoner, "Ahri");
long wins = championGames.stream().filter(ParticipantStats::isWin).count();
double winRate = (double) wins / championGames.size() * 100;
```

---

## Соблюдение архитектурных правил (PCMEF)

✅ **Правило 1:** Направление зависимостей
- Control → Service → Entity → Repository → БД
- ❌ Repository НИКОГДА не вызывает Service
- ❌ Entity не обращается в БД напрямую

✅ **Правило 2:** Бизнес-логика в Entity
- `ParticipantStats.calculateKDA()`
- `Summoner.getWinRate()`
- `Match.getWinningTeamParticipants()`
- Service вызывает эти методы, а не дублирует логику

✅ **Правило 3:** Типизация (@Entity, конкретные типы полей)
- ❌ Нет `Object` или `String` вместо конкретных типов
- ✅ Enum для Role, Team, Tier, Rank

---

## Миграции (Flyway)

Скрипт `ddl.sql` следует поместить в `src/main/resources/db/migration/V1__Initial_schema.sql` для автоматической миграции при запуске приложения.

Конфигурация в `application.yml`:
```yaml
spring:
  flyway:
    locations: classpath:db/migration
    baselineOnMigrate: true
```
