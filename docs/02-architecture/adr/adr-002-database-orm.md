# ADR-002: PostgreSQL и Spring Data JPA

**Статус:** Принято

## Решение

- СУБД: PostgreSQL (`lol_companion_db`)
- ORM: Hibernate через Spring Data JPA
- Миграции: Flyway (`backend/src/main/resources/db/migration/`)

## Обоснование

Кэш профилей и матчей требует реляционной модели с внешними ключами. JPA реализует паттерн Data Mapper (Фаулер).
