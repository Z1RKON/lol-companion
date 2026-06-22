# Спецификация методов (Mediator / Control)

Формальные контракты ключевых методов бизнес-логики и REST-слоя.

## AuthService

| Метод | Сигнатура | Предусловия | Постусловия | Исключения |
|-------|-----------|-------------|-------------|------------|
| register | `AuthResponseDto register(RegisterRequest request)` | username, email уникальны; password ≥ 8 | User в БД; возврат JWT | `DuplicateUserException` |
| login | `AuthResponseDto login(LoginRequest request)` | username/email существует | Валидный JWT в ответе | `InvalidCredentialsException` |
| getUserById | `UserResponseDto getUserById(Long userId)` | userId > 0 | DTO пользователя | `IllegalArgumentException` |

## SummonerService

| Метод | Сигнатура | Предусловия | Постусловия | Исключения |
|-------|-----------|-------------|-------------|------------|
| getSummonerProfileByName | `SummonerResponseDto getSummonerProfileByName(String name, String regionCode)` | name не пустой; region валиден | DTO профиля (кэш или Riot) | `SummonerNotFoundException`, `RiotApiException` |
| getSummonerByPuuid | `Summoner getSummonerByPuuid(String puuid)` | puuid не null | Entity Summoner актуален | `SummonerNotFoundException` |
| refreshSummoner | `SummonerResponseDto refreshSummoner(String puuid)` | JWT авторизован | Данные обновлены из Riot | `RiotApiException` |

## FavoriteService

| Метод | Сигнатура | Предусловия | Постусловия | Исключения |
|-------|-----------|-------------|-------------|------------|
| addFavorite | `FavoriteSummonerDto addFavorite(Long userId, String puuid)` | user авторизован; puuid существует | Запись в user_favorite_summoners | `DuplicateFavoriteException` |
| removeFavorite | `void removeFavorite(Long userId, Long summonerId)` | запись принадлежит user | Запись удалена | `FavoriteNotFoundException` |
| listFavorites | `List<FavoriteSummonerDto> listFavorites(Long userId)` | user авторизован | Список DTO | — |

## JwtService

| Метод | Сигнатура | Контракт |
|-------|-----------|----------|
| generateToken | `String generateToken(UserPrincipal principal)` | HS256, subject = userId, exp из `jwt.expiration-ms` |
| validateToken | `boolean validateToken(String token)` | false при истечении или неверной подписи |
| extractUserId | `Long extractUserId(String token)` | Возврат claims.sub как Long |

## REST Control (примеры)

| Эндпоинт | Метод контроллера | HTTP | Тело ответа |
|----------|-------------------|------|-------------|
| POST /auth/login | `AuthController.login(@Valid LoginRequest)` | 200 | `AuthResponseDto` |
| GET /summoner/search | `SummonerController.search(name, region)` | 200 | `SummonerResponseDto` |
| POST /summoner/favorites | `SummonerController.addFavorite(@RequestBody FavoriteRequest)` | 201 | `FavoriteSummonerDto` |

Полный перечень эндпоинтов: [../09-api/REST-ENDPOINTS.md](../09-api/REST-ENDPOINTS.md), [../09-api/openapi.yaml](../09-api/openapi.yaml).
