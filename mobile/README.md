# FoodRoute — система управления доставкой еды (вариант 10)

Распределённая система **FoodRoute** для автоматизации доставки еды из ресторанов: заказы, курьеры, маршрутизация, отслеживание.

Архитектура **PCMEF** (Presentation → Control → Mediator → Entity → Foundation):

| Компонент | Технология | Назначение |
|-----------|------------|------------|
| `backend/` | Java 17, Spring Boot 3, JPA, H2 | REST API, бизнес-логика |
| `web/` | React, Vite, TypeScript | Веб-панель диспетчера (ЛР №7) |
| корень | React Native, Expo | Мобильный клиент (курьер + клиент) |

## Быстрый старт

### 1. API-сервер (порт 8080)

**Node.js (рекомендуется для запуска сейчас):**

```bash
cd server
npm install
npm start
```

**Spring Boot (для сдачи лабораторных, нужен Java 17+ и Gradle):**

```bash
cd backend
gradle bootRun
```

API: `http://localhost:8080/api`  
H2 Console: `http://localhost:8080/h2-console` (JDBC: `jdbc:h2:mem:foodroute`, user: `sa`, password пустой)

### 2. Web-панель (порт 5173)

```bash
cd web
npm install
npm run dev
```

### 3. Мобильное приложение

```bash
npm install
npm start
```

Для телефона в `.env` укажите IP компьютера:

```
EXPO_PUBLIC_API_URL=http://192.168.x.x:8080/api
```

## Демо-аккаунты

| Роль | Email | Пароль |
|------|-------|--------|
| Клиент | client@foodroute.ru | client123 |
| Курьер | courier@foodroute.ru | courier123 |
| Диспетчер | dispatcher@foodroute.ru | dispatch123 |
| Управляющий | manager@foodroute.ru | manager123 |

## Основные функции

- **Клиент (mobile):** меню, корзина, оформление заказа, отслеживание на карте
- **Курьер (mobile):** список доставок, смена статусов, эмуляция GPS
- **Диспетчер (mobile + web):** мониторинг заказов, продвижение статусов, переназначение курьера
- **Backend:** автоподбор курьера, REST API `/api/orders`, `/api/couriers`, `/api/menu`

## Статусы заказа

`NEW` → `COOKING` → `AWAITING_DELIVERY` → `IN_PROGRESS` → `DELIVERED`

## Структура backend (PCMEF)

```
ru.sfedu.foodroute
├── control/     — REST-контроллеры, DTO
├── mediator/    — сервисы (OrderService, RoutePlanningService)
├── entity/      — JPA-сущности
└── foundation/  — репозитории
```
