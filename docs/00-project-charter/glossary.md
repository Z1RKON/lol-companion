# Бизнес-глоссарий (этап 0)

Термины предметной области LoL Companion (≥ 20 терминов по методичке).

| Термин | Определение |
|--------|-------------|
| Призыватель (Summoner) | Игрок League of Legends; объект поиска и отображения статистики |
| Riot ID | Отображаемое имя в формате `Имя#Тег`; используется для поиска в UI |
| PUUID | Постоянный уникальный идентификатор игрока в Riot API; ключ навигации и избранного |
| Матч (Match) | Завершённая игровая сессия с уникальным `matchId` |
| Участник (ParticipantStats) | Статистика призывателя в конкретном матче (KDA, чемпион, победа) |
| Избранное | Список призывателей, сохранённых пользователем приложения |
| Cache-Aside | Стратегия кэширования: сначала БД, при устаревании — запрос к Riot API |
| Тир (Tier) | Ранговая лига: IRON, BRONZE, …, CHALLENGER |
| Ранг (Rank) | Подуровень внутри тира: IV, III, II, I |
| LP (League Points) | Очки лиги в ранговой очереди |
| Винрейт (Win Rate) | Доля побед: wins / (wins + losses) × 100% |
| Регион (Region) | Игровой кластер: RU, EUW, NA и др. |
| Riot Games API | Внешний REST API для данных LoL (Account-V1, League-V4, Match-V5) |
| JWT | JSON Web Token для stateless-аутентификации между mobile и backend |
| Пользователь (User) | Учётная запись в приложении LoL Companion |
| PCMEF | Presentation–Control–Mediator–Entity–Foundation — слоёная архитектура |
| Flyway | Инструмент версионирования схемы PostgreSQL |
| Data Mapper | Паттерн ORM: отделение доменной модели от схемы БД (Hibernate) |
| Identity Map | Кэш сущностей первого уровня Hibernate в рамках сессии |
| BUC | Business Use Case — бизнес-вариант использования на уровне процессов |
| Стейкхолдер | Заинтересованная сторона проекта (игрок, преподаватель, Riot) |
| Rate limit | Ограничение частоты запросов к Riot API (HTTP 429) |

Расширенный глоссарий (системные термины): [../01-requirements/glossary-extended.md](../01-requirements/glossary-extended.md).
