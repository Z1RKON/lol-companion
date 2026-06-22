# Архитектурный документ (Arc42)

**Проект:** LoL Companion  
**Траектория:** В — Мобильная разработка  
**Версия:** 1.0  
**Автор:** Орлов Владимир Алексеевич

## 1. Введение и цели

LoL Companion — мобильный компаньон для League of Legends. Пользователь ищет призывателей, просматривает статистику и ведёт избранное. Сервер интегрируется с Riot Games API и кэширует данные в PostgreSQL.

| Цель | Описание |
|------|----------|
| Разделение ответственности | PCMEF: P на mobile, C/M/E/F на backend |
| Тестируемость | Unit-тесты с моками, JaCoCo ≥ 40% |
| Интеграция | REST + JWT между клиентом и сервером |

## 2. Ограничения

| Ограничение | Значение |
|-------------|----------|
| Backend | Java 17, Spring Boot 3.3 |
| Mobile | React Native (Expo), TypeScript |
| БД | PostgreSQL |
| API | Riot Games API (Development Key) |

## 3. Контекст системы

![Диаграмма пакетов PCMEF](../images/fig-04-packages.png)

Рисунок 4 — Архитектура клиент-сервер

- **Mobile (Presentation):** экраны, Zustand, Axios
- **Backend (C/M/E/F):** REST, сервисы, JPA, RiotApiClient
- **PostgreSQL:** кэш профилей и матчей
- **Riot Games API:** внешний источник данных

## 4. Стратегии

### PCMEF (траектория В)

| Слой | Расположение | Ответственность |
|------|--------------|-----------------|
| Presentation | React Native | UI, навигация |
| Control | Spring @RestController | REST, валидация DTO |
| Mediator | @Service | Бизнес-логика, Cache-Aside |
| Entity | @Entity | JPA, бизнес-методы |
| Foundation | @Repository, RiotApiClient | БД, внешний API |

**Правило:** P → C → M → E → F (зависимости только вниз).

### Безопасность

- JWT (JJWT), BCrypt для паролей
- См. [adr/adr-003-auth-strategy.md](adr/adr-003-auth-strategy.md)

## 5. ADR

| № | Документ |
|---|----------|
| ADR-001 | [Выбор PCMEF](adr/adr-001-arch-pattern.md) |
| ADR-002 | [PostgreSQL + JPA](adr/adr-002-database-orm.md) |
| ADR-003 | [JWT](adr/adr-003-auth-strategy.md) |
| ADR-004 | [PUUID как внутренний ключ](adr/adr-004-puuid-navigation.md) |

## 6. Качество

| Атрибут | Цель | Проверка |
|---------|------|----------|
| Покрытие тестами | ≥ 40% | JaCoCo, Jest |
| Соответствие PCMEF | 100% | [COMPLIANCE-AUDIT](../12-final-report/compliance/COMPLIANCE-AUDIT.md) |

Слои backend: [pcmef-layers.md](pcmef-layers.md), [interfaces.md](interfaces.md)
