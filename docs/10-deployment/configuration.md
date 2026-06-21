# Настройка окружения

## backend/.env

| Переменная | Описание |
|------------|----------|
| RIOT_API_KEY | Ключ с developer.riotgames.com |
| DB_PASSWORD | Пароль PostgreSQL |
| JWT_SECRET | Случайная строка ≥ 32 символов |

## application.yml

- `riot.api.cache-ttl-minutes: 10`
- `riot.api.default-region: RU`
- `riot.api.key: ${RIOT_API_KEY}`

## mobile/.env (опционально)

- `EXPO_PUBLIC_API_URL` — URL backend (для физического устройства укажите IP ПК)

## Скрипты

| Скрипт | Назначение |
|--------|------------|
| start-lol-companion.ps1 | Запуск backend + mobile |
| backend/start-with-env.ps1 | Backend с загрузкой .env |
| mobile/scripts/run-android-native.ps1 | Сборка Android |

## Администрирование

- Логи backend: `backend/logs/`
- Миграции Flyway применяются при старте
- Development Riot Key обновлять ~раз в 24 часа
