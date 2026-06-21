# ADR-004: PUUID для внутренней логики

**Статус:** Принято

## Контекст

Riot ID (Имя#Тег) может меняться; PUUID постоянен.

## Решение

- **Поиск в UI:** Riot ID
- **Избранное, навигация, матчи:** PUUID
- API: `POST /summoner/favorites` принимает `{ "puuid": "..." }`
- Mobile: `navigation.navigate('Profile', { puuid, region })`
