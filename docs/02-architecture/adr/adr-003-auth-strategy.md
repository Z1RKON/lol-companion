# ADR-003: JWT-аутентификация

**Статус:** Принято

## Решение

- Stateless JWT (JJWT)
- Публичные: `POST /auth/register`, `POST /auth/login`
- Остальные эндпоинты: `Authorization: Bearer <token>`
- Пароли: BCrypt
- Клиент: AsyncStorage + Axios interceptor

## Безопасность

- `JWT_SECRET` только в `.env`
- CSRF не требуется (токен в заголовке, не в cookie)
