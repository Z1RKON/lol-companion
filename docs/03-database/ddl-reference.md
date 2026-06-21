# DDL-скрипты

## Flyway-миграции

Путь: `backend/src/main/resources/db/migration/`

| Версия | Файл | Описание |
|--------|------|----------|
| V1 | V1__initial_schema.sql | Основная схема |
| V2–V7 | V2…V7 | display_name, items, profile_icon, level, emerald tier, linked riot |

## Полный DDL

`backend/src/main/resources/db/ddl.sql`

## Запуск

Миграции применяются автоматически при старте Spring Boot (`spring.flyway.enabled=true`).

```sql
-- Пример: создание БД
CREATE DATABASE lol_companion_db;
```
