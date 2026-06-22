# REST API

Полная спецификация: [REST-ENDPOINTS.md](REST-ENDPOINTS.md)

**Базовый URL:** `http://localhost:8080/api`

## OpenAPI / Swagger

| Ресурс | URL |
|--------|-----|
| Swagger UI | http://localhost:8080/api/swagger-ui/index.html |
| OpenAPI JSON (runtime) | http://localhost:8080/api/v3/api-docs |
| OpenAPI YAML (репозиторий) | [openapi.yaml](openapi.yaml) |

Подключено: `springdoc-openapi-starter-webmvc-ui` 2.6.0.

## Соответствие траектории В (8+ эндпоинтов)

| Метод | Путь | Описание |
|-------|------|----------|
| POST | /auth/register | Регистрация |
| POST | /auth/login | Аутентификация (JWT) |
| GET | /auth/me | Текущий пользователь |
| GET | /summoner/search | Поиск по Riot ID |
| GET | /summoner/{puuid} | Профиль по PUUID |
| GET | /summoner/{puuid}/matches | История матчей |
| GET | /summoner/matches/{matchId} | Детали матча |
| POST | /summoner/favorites | Добавить в избранное |
| GET | /summoner/favorites | Список избранного |
| DELETE | /summoner/favorites/{id} | Удалить из избранного |
| PUT | /auth/me/riot-account | Привязка Riot ID |
| … | … | **Итого: 16 эндпоинтов** |

## Аутентификация

Публичные: register, login, swagger-ui, v3/api-docs. Остальные — заголовок `Authorization: Bearer <token>`.
