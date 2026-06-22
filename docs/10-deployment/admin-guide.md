# Руководство администратора

**Система:** LoL Companion API (Spring Boot)  
**Аудитория:** администратор сервера / DevOps

## 1. Компоненты

| Компонент | Порт | Описание |
|-----------|------|----------|
| lol-companion-api | 8080 | Spring Boot REST API |
| PostgreSQL | 5432 | БД `lol_companion_db` |
| Riot API | HTTPS | Внешний источник данных |

## 2. Установка

См. [installation-guide.md](installation-guide.md).

## 3. Переменные окружения

| Переменная | Обязательно | Описание |
|------------|-------------|----------|
| `RIOT_API_KEY` | Да | Ключ developer.riotgames.com |
| `JWT_SECRET` | Да | ≥ 32 символов, уникальный для среды |
| `DB_PASSWORD` | Да | Пароль PostgreSQL |

Файл: `backend/.env` (не коммитить в git).

## 4. Конфигурация application.yml

| Параметр | Значение по умолчанию | Назначение |
|----------|----------------------|------------|
| `server.servlet.context-path` | `/api` | Базовый путь API |
| `riot.api.cache-ttl-minutes` | 10 | TTL кэша призывателей |
| `riot.api.regional-url` | ru.api.riotgames.com | Региональный кластер |
| `riot.api.platform-url` | europe.api.riotgames.com | Match-V5 |
| `app.jwt.expiration-ms` | 86400000 | Срок жизни JWT (24 ч) |

## 5. Миграции БД

Flyway применяет скрипты из `backend/src/main/resources/db/migration/` при старте.

```powershell
cd backend
.\gradlew.bat bootRun
# Проверка: таблицы users, summoners, matches, ...
```

Откат: восстановить snapshot БД; ручной откат Flyway не настроен (учебный проект).

## 6. Мониторинг и логи

| Ресурс | Путь / URL |
|--------|------------|
| Логи | `backend/logs/lol-companion-api.log` |
| Health | `GET http://localhost:8080/api/actuator/health` |
| Swagger UI | `http://localhost:8080/api/swagger-ui/index.html` |
| OpenAPI JSON | `http://localhost:8080/api/v3/api-docs` |

## 7. Резервное копирование

```bash
pg_dump -U postgres -d lol_companion_db -F c -f lol_companion_backup.dump
```

Рекомендуемая периодичность: ежедневно при production-развёртывании.

## 8. Обновление Riot API Key

Development Key действует ~24 часа. При 403 от Riot:

1. Получить новый ключ на developer.riotgames.com
2. Обновить `RIOT_API_KEY` в `.env`
3. Перезапустить backend

## 9. Устранение неполадок

| Симптом | Причина | Действие |
|---------|---------|----------|
| 429 Too Many Requests | Лимит Riot API | Увеличить TTL кэша; подождать |
| 401 на /summoner/* | Истёк JWT | Повторный login в mobile |
| Flyway validate error | Расхождение схемы | Проверить миграции V1–V7 |
| Connection refused :5432 | PostgreSQL не запущен | Запустить службу PostgreSQL |

## 10. Безопасность

- Не публиковать `.env` и `JWT_SECRET`.
- В production: HTTPS, firewall, Production Riot Key.
- CORS настроен для mobile-клиента в `CorsConfig`.

## Связанные документы

- [configuration.md](configuration.md)
- [../05-setup/RIOT-API-SETUP.md](../05-setup/RIOT-API-SETUP.md)
- [../09-api/openapi.yaml](../09-api/openapi.yaml)
