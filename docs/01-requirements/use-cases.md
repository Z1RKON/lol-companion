# Спецификация прецедентов

Полные сценарии в формате ГОСТ: [use-cases.md](use-cases.md) (детализация UC-001, UC-002).

## UC-001. Поиск и просмотр профиля

| Поле | Значение |
|------|----------|
| Актор | Пользователь мобильного приложения |
| Предусловия | Приложение запущено, backend доступен, задан RIOT_API_KEY |
| Основной сценарий | Ввод Riot ID → GET `/summoner/search` → отображение профиля по `puuid` |
| Постусловия | Профиль закэширован в PostgreSQL |

**Расширения:** пустой ввод (400), не найден (404), rate limit (429).

## UC-002. Избранное

| Поле | Значение |
|------|----------|
| Предусловия | Пользователь аутентифицирован (JWT) |
| Основной сценарий | POST `/summoner/favorites` с `{ "puuid": "..." }` |
| Постусловия | Запись в `user_favorite_summoners` |

## UC-003. Регистрация и вход

POST `/auth/register`, POST `/auth/login` → JWT в AsyncStorage.

## UC-004. Привязка Riot ID

PUT `/auth/me/riot-account`, GET `/auth/me/summoner`.

## UC-005. Детали матча

GET `/summoner/matches/{matchId}` → экран MatchDetail.
