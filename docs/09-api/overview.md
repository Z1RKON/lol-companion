# REST API

Полная спецификация: [REST-ENDPOINTS.md](REST-ENDPOINTS.md)

**Базовый URL:** `http://localhost:8080/api`

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

## OpenAPI / Swagger

Документация OpenAPI в проекте описана в REST-ENDPOINTS.md. Для добавления Swagger UI можно подключить `springdoc-openapi` (бонус траектории).

## Аутентификация

Публичные: register, login. Остальные — заголовок `Authorization: Bearer <token>`.
