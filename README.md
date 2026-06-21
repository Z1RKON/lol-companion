# LoL Companion

Мобильное приложение-компаньон для League of Legends: поиск призывателей, профиль, матчи, избранное.

## Стек

- **Backend:** Java 17, Spring Boot 3.3, PostgreSQL, JWT, Riot Games API
- **Mobile:** React Native (Expo), TypeScript, Zustand

## Быстрый старт

1. PostgreSQL: создать БД `lol_companion_db`
2. `backend/.env` — скопировать из `backend/.env.example`, задать `RIOT_API_KEY`, `JWT_SECRET`
3. Backend: `.\start-lol-companion.ps1` или `backend\start-with-env.ps1`
4. Mobile: `cd mobile && npm install && npm run android`

Документация: `docs/PROJECT-CONTEXT.md`
