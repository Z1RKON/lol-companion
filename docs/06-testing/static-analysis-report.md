# Отчёт статического анализа

**Дата:** 21.06.2026  
**Проект:** LoL Companion

## Backend — Checkstyle 10.12.5

| Параметр | Значение |
|----------|----------|
| Команда | `cd backend && .\gradlew.bat checkstyleMain` |
| Конфигурация | `backend/config/checkstyle/checkstyle.xml` |
| Правила | UnusedImports, NeedBraces, EmptyBlock, LeftCurly, RightCurly |
| HTML-отчёт | `backend/build/reports/checkstyle/main.html` |

**Результат:** `BUILD SUCCESSFUL` — нарушений, блокирующих сборку, не выявлено.

## Mobile — ESLint 9 + typescript-eslint

| Параметр | Значение |
|----------|----------|
| Команда | `cd mobile && npm run lint` |
| Конфигурация | `mobile/eslint.config.mjs` |
| Область | `mobile/src/**/*.ts`, `mobile/src/**/*.tsx` |
| Правила | recommended + `@typescript-eslint/no-explicit-any: error` |

**Результат:** 0 errors, 0 warnings (`npm run lint`, июнь 2026).

## TypeScript

- `strict: true` в `mobile/tsconfig.json`
- Типы API: `mobile/src/types/api.ts`

## Рекомендации

- Подключить Checkstyle к CI при расширении проекта
- Добавить `lint` в pre-commit hook

См. также: [test-plan.md](test-plan.md), [coverage-report.md](coverage-report.md).
