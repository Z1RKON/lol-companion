# ADR-003: JWT-аутентификация

**Статус:** Принято  
**Дата:** 2026-04

## Контекст

Mobile-клиент должен обращаться к stateless REST API без серверных сессий.

## Рассмотренные альтернативы

| Вариант | Плюсы | Минусы | Решение |
|---------|-------|--------|---------|
| **JWT (stateless)** | Масштабируемость, mobile-friendly | Отзыв токена сложнее | ✅ Выбрано |
| Session + cookie | Простой отзыв | Плохо для native mobile | Отклонено |
| OAuth2 + Riot | Единый вход Riot | Сложная интеграция, не в ТЗ | Отклонено |
| API Key per user | Простота | Нет стандарта для UI | Отклонено |

## Решение

- Stateless JWT (JJWT, HS256)
- Публичные: `POST /auth/register`, `POST /auth/login`
- Остальные эндпоинты: `Authorization: Bearer <token>`
- Пароли: BCrypt
- Клиент: AsyncStorage + Axios interceptor

## Безопасность

- `JWT_SECRET` только в `.env`
- CSRF не требуется (токен в заголовке, не в cookie)

## Последствия

- (+) Нет server-side session store
- (−) При компрометации секрета — ротация JWT_SECRET
