# Аудит соответствия PCMEF

Чеклист проверки архитектуры LoL Companion по методическим указаниям.

## Правила слоёв

| # | Правило | Статус | Проверка |
|---|---------|--------|----------|
| 1 | Controller → только Service | ✅ | Нет прямых вызовов Repository из Controller |
| 2 | Service → Repository, RiotApiClient | ✅ | `SummonerService`, `AuthService` |
| 3 | Entity без зависимостей от Spring Web | ✅ | `entity/*.java` |
| 4 | DTO отделены от Entity | ✅ | `dto/api/`, `dto/request/` |
| 5 | JWT фильтр в Foundation/Security | ✅ | `JwtAuthenticationFilter` |

## Траектория В (мобильная)

| Критерий | Требование | Факт |
|----------|------------|------|
| Экраны | ≥ 5 | 6 (Login, Search, Profile, Match, Favorites, Account) |
| Backend | Spring Boot | 3.3, Java 17 |
| JWT | Да | JJWT + BCrypt |
| REST | ≥ 8 эндпоинтов | 16 |
| OpenAPI | Да | `springdoc-openapi`, `docs/09-api/openapi.yaml` |
| Покрытие backend | ≥ 40% | ~61% JaCoCo |
| Покрытие mobile | ≥ 40% | ~43% Jest |
| Офлайн-кэш | Да | AsyncStorage |

## Документация

| Этап | Папка | Статус |
|------|-------|--------|
| 0–12 | `docs/00`–`docs/12` | ✅ |
| ТЗ | `docs/01-requirements/technical-specification.md` | ✅ |
| Руководство администратора | `docs/10-deployment/admin-guide.md` | ✅ |

Дата аудита: июнь 2026.
