# Документация курсового проекта LoL Companion

**Траектория:** В — Мобильная разработка (React Native + Spring Boot)  
**Методические указания:** МУ КП «Программная инженерия», СКФУ, 2026

## Навигация по этапам

| Этап | Папка | Содержание |
|------|-------|------------|
| 0. Инициация | [00-project-charter/](00-project-charter/) | Паспорт, IDEF0, SWOT, стейкхолдеры, глоссарий |
| 1. Требования | [01-requirements/](01-requirements/) | Use Case, domain model, трассировка |
| 2. Архитектура | [02-architecture/](02-architecture/) | PCMEF, Arc42, ADR |
| 3. База данных | [03-database/](03-database/) | ER, DDL, ORM |
| 4. Детальное проектирование | [04-detailed-design/](04-detailed-design/) | Sequence, классы |
| 5. Реализация | [05-implementation/](05-implementation/) | Структура кода, слои |
| 6. Тестирование | [06-testing/](06-testing/) | План, JaCoCo, Jest |
| 7. Рефакторинг | [07-refactoring/](07-refactoring/) | Cache-Aside, Data Mapper |
| 8. Интерфейс | [08-ui/](08-ui/) | Экраны мобильного приложения |
| 9. API | [09-api/](09-api/) | REST, эндпоинты |
| 10. Развёртывание | [10-deployment/](10-deployment/) | Установка, настройка |
| 11. Руководство | [11-user-guide/](11-user-guide/) | Инструкция пользователя |
| 12. Отчёт | [12-final-report/](12-final-report/) | Пояснительная записка |

## Изображения

Все диаграммы и скриншоты: [images/](images/)

## Быстрые ссылки

- [PROJECT-CONTEXT.md](PROJECT-CONTEXT.md) — контекст для разработки
- [RIOT-API-SETUP.md](05-setup/RIOT-API-SETUP.md) — настройка Riot API
- [Корневой README](../README.md) — установка и запуск

## Устаревшая структура (архив)

Ранние черновики сохранены для справки; для сдачи используйте папки `00`–`12` выше:

| Старая папка | Актуальный аналог |
|--------------|-------------------|
| `01-business-model/` | `00-project-charter/` |
| `02-requirements/` | `01-requirements/` |
| `03-architecture/` | `02-architecture/` |
| `04-api/` | `09-api/` |
| `05-setup/` | `10-deployment/` + `05-setup/` |
| `06-checklist/` | чек-листы соответствия |
