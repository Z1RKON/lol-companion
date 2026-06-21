# REST API (Control + JWT)

Базовый URL: `http://localhost:8080/api`

Все ответы об ошибках — JSON (`ErrorResponseDto`), без stack trace.

## Публичные (без JWT)

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/auth/register` | Регистрация (`RegisterRequest`, `@Valid`) |
| POST | `/auth/login` | Авторизация → JWT (`AuthResponseDto`) |

## С JWT (`Authorization: Bearer <token>`)

### Аккаунт

| Метод | Путь | Описание |
|-------|------|----------|
| GET | `/auth/me` | Текущий пользователь |
| PUT | `/auth/me/riot-account` | Привязать Riot ID (`LinkRiotAccountRequest`) |
| DELETE | `/auth/me/riot-account` | Отвязать Riot ID |
| GET | `/auth/me/summoner` | Профиль привязанного аккаунта |

### Призыватели и матчи

| Метод | Путь | Описание |
|-------|------|----------|
| GET | `/summoner/search?name=&region=RU` | Поиск по Riot ID или legacy-нику |
| GET | `/summoner/by-name/{name}?region=RU` | Профиль по имени |
| GET | `/summoner/{puuid}` | Профиль по PUUID (из локальной БД) |
| GET | `/summoner/{puuid}/matches?count=20` | История матчей (1–100) |
| GET | `/summoner/{puuid}/teammates?matches=20&limit=20` | Частые союзники |
| GET | `/summoner/matches/{matchId}` | Детали матча |
| POST | `/summoner/{puuid}/refresh` | Принудительное обновление из Riot |

### Избранное

| Метод | Путь | Описание |
|-------|------|----------|
| GET | `/summoner/favorites` | Список избранного текущего пользователя |
| POST | `/summoner/favorites` | Добавить: `{ "puuid": "<riot-puuid>" }` |
| DELETE | `/summoner/favorites/{summonerId}` | Удалить (ID записи summoner в БД) |

**Итого: 16 эндпоинтов** (2 публичных + 14 защищённых).

## Примеры

```bash
# Регистрация
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","email":"u@mail.com","password":"secret12"}'

# Логин
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"secret12"}'

# Поиск (с токеном)
curl "http://localhost:8080/api/summoner/search?name=Player%23TAG&region=RU" \
  -H "Authorization: Bearer <token>"

# Добавить в избранное
curl -X POST http://localhost:8080/api/summoner/favorites \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"puuid":"abcdefghijklmnopqrstuvwxyz123456"}'
```

## Тесты и покрытие

```powershell
cd backend
.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification
# отчёт: build/reports/jacoco/test/html/index.html
```

```powershell
cd mobile
npm run test:coverage
```
