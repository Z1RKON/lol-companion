# Инструкция по установке

## Требования

| Компонент | Версия |
|-----------|--------|
| JDK | 17+ |
| Node.js | 18+ |
| PostgreSQL | 14+ |
| Android Studio | для эмулятора |
| Riot API Key | developer.riotgames.com |

## 1. Клонирование

```bash
git clone https://github.com/Z1RKON/lol-companion.git
cd lol-companion
```

## 2. База данных

```sql
CREATE DATABASE lol_companion_db;
```

## 3. Backend

```powershell
cd backend
copy .env.example .env
# Заполнить: RIOT_API_KEY, DB_PASSWORD, JWT_SECRET
cd ..
.\start-lol-companion.ps1
```

Или: `cd backend && .\gradlew.bat bootRun`

API: `http://localhost:8080/api`

## 4. Mobile

```powershell
cd mobile
npm install
npm run android
```

Для эмулятора API: `http://10.0.2.2:8080/api` (настроено в `client.ts`).

## 5. Первый запуск

1. Зарегистрироваться в приложении
2. Войти
3. Найти призывателя по Riot ID (формат `Player#TAG`)

Подробнее: [RIOT-API-SETUP.md](../05-setup/RIOT-API-SETUP.md), [admin-guide.md](admin-guide.md)
