# Инструкция по интеграции Riot Games API и настройке кэширования

## 📋 Что было реализовано

### Шаг 1: Конфигурация Riot API ✅
- **RiotApiProperties** — загрузка параметров из `application.yml`
- **RiotApiConfig** — создание RestTemplate с автоматическим добавлением X-Riot-Token
- **application.yml** — конфигурация с переменными окружения

### Шаг 2: Клиент Riot API ✅
- **RiotApiClient** — HTTP интеграция с эндпоинтами:
  - `/lol/summoner/v4/summoners/by-name/{summonerName}` — информация о профиле
  - `/lol/league/v4/entries/by-summoner/{summonerId}` — ранг, LP, W/L
  - `/lol/match/v5/matches/by-puuid/{puuid}/ids` — список ID матчей
  - `/lol/match/v5/matches/{matchId}` — детали матча
- **Обработка ошибок:**
  - 404 → `RiotApiException`
  - 429 → `RiotRateLimitException` (с информацией о времени ожидания)
  - 5xx → `RiotApiException`

### Шаг 3: Кэширование с TTL (SummonerService) ✅
- **Cache-Aside паттерн** — ищем в БД, если кэш свежий (< 10 мин) → возвращаем
- **Автоматическое обновление** — если кэш устарел → запрос к Riot API
- **Экономия запросов** — только актуальные данные обновляются из API

### Шаг 4: Парсинг матчей (MatchService) ✅
- **Парсинг RiotMatchDto** → Entity (Match + ParticipantStats)
- **Транзакционность** — вся операция парсинга в одной транзакции
- **Кэширование матчей** — старые матчи берутся из БД (не меняются)

### Шаг 5: Глобальная обработка ошибок ✅
- **GlobalExceptionHandler** — перехватывает все исключения
- **Красивые JSON ответы** — с кодами ошибок и понятными сообщениями
- **Логирование** — внутренние детали логируются на сервере

---

## 🚀 Запуск приложения

### Предварительные требования

1. **PostgreSQL 14+** (установлен и запущен)
2. **Java 17+**
3. **Gradle** (в комплекте — `gradlew.bat`, Maven не нужен)
4. **Riot Games API Key** (получить на https://developer.riotgames.com/)

### Шаг 1: Клонирование и подготовка

```bash
cd C:\Projects\lol-companion\backend
```

### Шаг 2: Создание БД

```bash
psql -U postgres -c "CREATE DATABASE lol_companion_db;"
```

### Шаг 3: Запуск Flyway миграций (автоматический)

Поместите `ddl.sql` в: `src/main/resources/db/migration/V1__Initial_schema.sql`

Spring Boot автоматически запустит Flyway при старте.

### Шаг 4: Переменные окружения (без ключа в коде)

**Рекомендуется (Windows):** файл `backend/.env` (уже в `.gitignore`):

```env
RIOT_API_KEY=RGAPI-ваш-ключ
DB_PASSWORD=postgres
```

Скопируйте шаблон: `copy .env.example .env` и вставьте ключ.

```bash
# Linux/Mac
export RIOT_API_KEY="RGAPI-ваш-ключ"
export DB_PASSWORD="postgres"

# Windows (PowerShell, одна сессия)
$env:RIOT_API_KEY="RGAPI-ваш-ключ"
$env:DB_PASSWORD="postgres"
```

> **Безопасность:** не коммитьте `.env` и не вставляйте ключ в `application.yml` или чаты. Если ключ утёк — отзовите его на developer.riotgames.com и выпустите новый.

**Где получить Riot API Key:**
1. Перейти на https://developer.riotgames.com/
2. Войти в аккаунт
3. Создать новое приложение
4. Скопировать API Key

### Шаг 5: Запуск приложения

**Windows (с подгрузкой `.env`):**

```powershell
cd backend
.\start-with-env.ps1
```

**Или вручную:**

```bash
.\gradlew.bat bootRun
```

или

```bash
.\gradlew.bat clean build
java -jar target/lol-companion-api.jar
```

**Приложение запустится на:** `http://localhost:8080/api`

---

## 📡 Примеры API запросов

### Получить информацию о призывателе

```bash
curl -X GET "http://localhost:8080/api/summoner/search?name=Player%23TAG&region=RU" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json"
```

**Ответ (200 OK):**
```json
{
  "id": 1,
  "puuid": "...",
  "summonerName": "Player#TAG",
  "summonerLevel": 183,
  "tier": "DIAMOND",
  "rank": "I",
  "leaguePoints": 87,
  "winRate": "52.3%",
  "region": "EUW1"
}
```

### Получить историю матчей

```bash
curl -X GET "http://localhost:8080/api/summoner/PUUID-12345/matches?count=20" \
  -H "Content-Type: application/json"
```

**Ответ (200 OK):**
```json
[
  {
    "matchId": "EU1_5234141520",
    "gameMode": "RANKED_SOLO_5x5",
    "gameDuration": 30,
    "championName": "Ahri",
    "kills": 5,
    "deaths": 2,
    "assists": 8,
    "kda": "6.5",
    "csScore": 245,
    "goldEarned": 15234,
    "win": true
  }
]
```

### Обновить информацию о призувателе

```bash
curl -X POST "http://localhost:8080/api/summoner/PUUID-12345/refresh" \
  -H "Content-Type: application/json"
```

---

## ⚠️ Обработка ошибок

### Ошибка 404 (Призыватель не найден)

```bash
curl -X GET "http://localhost:8080/api/summoner/xyzabc12345"
```

**Ответ (404):**
```json
{
  "timestamp": "2026-06-01T14:30:45",
  "status": 404,
  "error": "Not Found",
  "message": "Призыватель 'xyzabc12345' не найден в регионе EUW1",
  "code": "SUMMONER_NOT_FOUND"
}
```

### Ошибка 429 (Rate Limit)

**Ответ (429):**
```json
{
  "timestamp": "2026-06-01T14:30:45",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Сервер перегружен запросами к Riot Games. Пожалуйста, попробуйте позже.",
  "code": "RATE_LIMIT_EXCEEDED",
  "retryAfterSeconds": 120
}
```

**Заголовок:** `Retry-After: 120`

---

## 📊 Логирование

### Включение DEBUG логов

Отредактируйте `application.yml`:

```yaml
logging:
  level:
    com.lolcompanion: DEBUG
    com.lolcompanion.service.RiotApiClient: DEBUG
```

### Пример логов

```
INFO  2026-06-01 14:30:45 - Запрос информации о призувателе: Summoner#TAG
DEBUG 2026-06-01 14:30:45 - Запрос профиля из Riot API: Summoner#TAG
DEBUG 2026-06-01 14:30:45 - Профиль успешно получен: RiotSummonerDto(...)
DEBUG 2026-06-01 14:30:46 - Информация о призувателе успешно обновлена: Summoner#TAG
```

---

## 🔍 Архитектура (PCMEF)

```
Request: GET /api/summoner/Summoner#TAG
    ↓
@RestController SummonerController
    ├─ Валидирует входные параметры
    └─ Вызывает summonerService.getSummonerByName("Summoner#TAG")
    
@Service SummonerService (Mediator)
    ├─ Поиск в SummonerRepository
    ├─ Проверка TTL кэша (< 10 минут?)
    ├─ Если устарел → riotApiClient.getSummonerByName()
    │  ├─ HTTP GET к Riot API
    │  ├─ Парсинг RiotSummonerDto
    │  └─ Сохранение в summonerRepository.save()
    └─ Возвращает Summoner Entity

Entity Summoner (бизнес-правила)
    ├─ isCacheFresh() — проверка свежести кэша
    ├─ getWinRate() — вычисление процента побед
    └─ updateRankStats() — обновление рейтинга

@Repository SummonerRepository
    └─ CRUD операции с БД PostgreSQL

Response: 200 OK + SummonerDto JSON
```

---

## 🛠️ Troubleshooting

### Ошибка: "Cannot resolve symbol RIOT_API_KEY"

**Решение:** Убедитесь, что переменная окружения установлена:

```bash
# Проверка
echo $RIOT_API_KEY  # Linux/Mac
echo %RIOT_API_KEY%  # Windows CMD
```

### Ошибка: "Connection refused" (PostgreSQL)

**Решение:** Проверьте, что PostgreSQL запущена:

```bash
# Linux/Mac
sudo systemctl status postgresql

# Windows
net start PostgreSQL14
```

### Ошибка: "401 Unauthorized" (Riot API)

**Решение:** Проверьте API Key:
1. Убедитесь, что ключ скопирован правильно
2. Проверьте, что ключ не истёк (регенерируется ежедневно в dev)
3. Используйте разный ключ для production окружения

### Ошибка: "429 Too Many Requests"

**Решение:** Rate limit Riot API. Нужно ждать указанное время:

```javascript
// На фронтенде
if (error.response.status === 429) {
  const retryAfter = error.response.data.retryAfterSeconds;
  setTimeout(() => {
    // Повторить запрос
  }, retryAfter * 1000);
}
```

---

## ✅ Проверка работоспособности

### Тест 1: Базовая интеграция

```bash
# Должен вернуться 404 (или 200, если такой ник существует)
curl -i "http://localhost:8080/api/summoner/Summoner#TAG"
```

### Тест 2: Обработка ошибок

```bash
# Должен вернуться 404 с красивым JSON
curl -i "http://localhost:8080/api/summoner/nonexistent_summoner_xyz"
```

### Тест 3: Кэширование

```bash
# Первый запрос (может быть медленнее)
time curl "http://localhost:8080/api/summoner/Summoner#TAG" > /dev/null

# Второй запрос (должен быть быстрее, кэш)
time curl "http://localhost:8080/api/summoner/Summoner#TAG" > /dev/null
```

---

## 📚 Дополнительная документация

- **Mediator слой:** [docs/04-architecture/mediator-layer.md](../docs/04-architecture/mediator-layer.md)
- **Обработка ошибок:** [docs/04-architecture/error-handling.md](../docs/04-architecture/error-handling.md)
- **Foundation слой:** [docs/03-architecture/foundation-layer.md](../docs/03-architecture/foundation-layer.md)
- **Use Cases:** [docs/02-requirements/use-cases.md](../docs/02-requirements/use-cases.md)

---

## 🎯 Соблюдение архитектурных правил

| Правило | Статус | Реализация |
|---------|--------|-----------|
| #1: Направление зависимостей | ✅ | Control → Mediator → Entity → Foundation |
| #2: Обработка ошибок | ✅ | GlobalExceptionHandler + красивый JSON |
| #3: Типизация на фронтенде | ✅ | RiotSummonerDto, RiotMatchDto, SummonerDto |
| #4: Безопасность секретов | ✅ | `riot.api-key: ${RIOT_API_KEY}` |
| #5: Чистота UI | ✅ | Service готовит DTO для FlatList |

---

## 🚀 Следующие шаги

1. **Реализация Control слоя (Контроллеры)**
   - SummonerController, MatchController, UserController
   - Валидация входных данных
   - Маппинг Entity → DTO

2. **Реализация фронтенда (React Native)**
   - TypeScript интерфейсы (точные копии DTO)
   - FlatList для списков матчей
   - AsyncStorage для кэширования локально

3. **Автентификация**
   - Spring Security + JWT токены
   - Регистрация и вход пользователей

4. **Расширенное кэширование**
   - Redis для распределённого кэша
   - Exponential backoff для Rate Limit
   - Circuit breaker паттерн

---

## 📞 Support

При возникновении проблем:
1. Проверьте логи: `tail -f logs/lol-companion-api.log`
2. Убедитесь в переменных окружения: `echo $RIOT_API_KEY`
3. Проверьте подключение к БД: `psql -U postgres lol_companion_db`
