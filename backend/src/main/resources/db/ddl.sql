-- ============================================================================
-- DDL: League of Legends Companion (PostgreSQL 14+)
-- Слой Foundation (PCMEF). Ручной скрипт; для Flyway см. db/migration/
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ============================================================================
-- users — локальные пользователи приложения
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    email           VARCHAR(100) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER'
                        CHECK (role IN ('USER', 'ADMIN', 'MODERATOR')),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email    UNIQUE (email)
);

-- Быстрый поиск по логину (требование курсового)
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

-- ============================================================================
-- summoners — кэш профилей LoL (Riot API)
-- ============================================================================
CREATE TABLE IF NOT EXISTS summoners (
    id               BIGSERIAL PRIMARY KEY,
    riot_puuid       VARCHAR(78)  NOT NULL,
    summoner_name    VARCHAR(50)  NOT NULL,
    summoner_level   INTEGER      NOT NULL DEFAULT 1
                        CHECK (summoner_level >= 1 AND summoner_level <= 500),
    profile_icon_id  INTEGER,
    tier             VARCHAR(20)
                        CHECK (tier IN (
                            'IRON', 'BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'EMERALD',
                            'DIAMOND', 'MASTER', 'GRANDMASTER', 'CHALLENGER'
                        )),
    rank             VARCHAR(5) CHECK (rank IN ('I', 'II', 'III', 'IV')),
    league_points    INTEGER      NOT NULL DEFAULT 0 CHECK (league_points >= 0),
    win_count        INTEGER      NOT NULL DEFAULT 0 CHECK (win_count >= 0),
    loss_count       INTEGER      NOT NULL DEFAULT 0 CHECK (loss_count >= 0),
    region           VARCHAR(10)  NOT NULL DEFAULT 'EUW1',
    last_updated     TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_summoners_riot_puuid UNIQUE (riot_puuid)
);

-- Быстрый поиск по PUUID и нику (требование курсового)
CREATE INDEX IF NOT EXISTS idx_summoners_riot_puuid ON summoners (riot_puuid);
CREATE INDEX IF NOT EXISTS idx_summoners_summoner_name ON summoners (summoner_name);
CREATE INDEX IF NOT EXISTS idx_summoners_summoner_name_lower ON summoners (LOWER(summoner_name));
CREATE INDEX IF NOT EXISTS idx_summoners_last_updated ON summoners (last_updated DESC);

-- ============================================================================
-- user_favorite_summoners — избранные призыватели
-- ============================================================================
CREATE TABLE IF NOT EXISTS user_favorite_summoners (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    summoner_id  BIGINT    NOT NULL REFERENCES summoners (id) ON DELETE CASCADE,
    added_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_user_summoner UNIQUE (user_id, summoner_id)
);

CREATE INDEX IF NOT EXISTS idx_user_fav_summoners_user_id ON user_favorite_summoners (user_id);
CREATE INDEX IF NOT EXISTS idx_user_fav_summoners_summoner_id ON user_favorite_summoners (summoner_id);

-- ============================================================================
-- matches — матчи LoL
-- ============================================================================
CREATE TABLE IF NOT EXISTS matches (
    id                       BIGSERIAL PRIMARY KEY,
    match_id                 VARCHAR(50)  NOT NULL,
    game_mode                VARCHAR(30)  NOT NULL,
    game_duration_seconds    INTEGER      NOT NULL CHECK (game_duration_seconds > 0),
    game_creation_timestamp  BIGINT       NOT NULL,
    game_start_timestamp     BIGINT,
    game_end_timestamp       BIGINT,
    winning_team             VARCHAR(10)  NOT NULL CHECK (winning_team IN ('BLUE', 'RED')),
    region                   VARCHAR(10)  NOT NULL DEFAULT 'EUW1',
    patch_version            VARCHAR(20),
    created_at               TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_matches_match_id UNIQUE (match_id)
);

CREATE INDEX IF NOT EXISTS idx_matches_match_id ON matches (match_id);
CREATE INDEX IF NOT EXISTS idx_matches_game_creation ON matches (game_creation_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_matches_game_mode ON matches (game_mode);

-- ============================================================================
-- participant_stats — статистика игрока в матче
-- ============================================================================
CREATE TABLE IF NOT EXISTS participant_stats (
    id                   BIGSERIAL PRIMARY KEY,
    match_id             BIGINT       NOT NULL REFERENCES matches (id) ON DELETE CASCADE,
    summoner_id          BIGINT       REFERENCES summoners (id) ON DELETE SET NULL,
    riot_puuid           VARCHAR(78)  NOT NULL,
    champion_name        VARCHAR(50)  NOT NULL,
    role                 VARCHAR(20)
                        CHECK (role IN ('TOP', 'JUNGLE', 'MIDDLE', 'ADC', 'SUPPORT', 'UNKNOWN')),
    kills                INTEGER      NOT NULL DEFAULT 0 CHECK (kills >= 0),
    deaths               INTEGER      NOT NULL DEFAULT 0 CHECK (deaths >= 0),
    assists              INTEGER      NOT NULL DEFAULT 0 CHECK (assists >= 0),
    cs_score             NUMERIC(8, 2) NOT NULL DEFAULT 0 CHECK (cs_score >= 0),
    gold_earned          BIGINT       NOT NULL DEFAULT 0 CHECK (gold_earned >= 0),
    damage_dealt         BIGINT       NOT NULL DEFAULT 0,
    damage_to_champions  BIGINT       NOT NULL DEFAULT 0,
    team                 VARCHAR(10)  NOT NULL CHECK (team IN ('BLUE', 'RED')),
    win                  BOOLEAN      NOT NULL,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_participant_match_puuid UNIQUE (match_id, riot_puuid)
);

CREATE INDEX IF NOT EXISTS idx_participant_stats_match_id ON participant_stats (match_id);
CREATE INDEX IF NOT EXISTS idx_participant_stats_summoner_id ON participant_stats (summoner_id);
CREATE INDEX IF NOT EXISTS idx_participant_stats_riot_puuid ON participant_stats (riot_puuid);
CREATE INDEX IF NOT EXISTS idx_participant_stats_champion_name ON participant_stats (champion_name);

-- ============================================================================
-- champion_stats — агрегат по чемпиону (опциональная аналитика)
-- ============================================================================
CREATE TABLE IF NOT EXISTS champion_stats (
    id              BIGSERIAL PRIMARY KEY,
    summoner_id     BIGINT       NOT NULL REFERENCES summoners (id) ON DELETE CASCADE,
    champion_name   VARCHAR(50)  NOT NULL,
    total_games     INTEGER      NOT NULL DEFAULT 0 CHECK (total_games >= 0),
    total_wins      INTEGER      NOT NULL DEFAULT 0 CHECK (total_wins >= 0),
    total_losses    INTEGER      NOT NULL DEFAULT 0 CHECK (total_losses >= 0),
    avg_kills       NUMERIC(5, 2) DEFAULT 0,
    avg_deaths      NUMERIC(5, 2) DEFAULT 0,
    avg_assists     NUMERIC(5, 2) DEFAULT 0,
    avg_cs          NUMERIC(6, 2) DEFAULT 0,
    last_played     TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_summoner_champion UNIQUE (summoner_id, champion_name)
);

CREATE INDEX IF NOT EXISTS idx_champion_stats_summoner_id ON champion_stats (summoner_id);
CREATE INDEX IF NOT EXISTS idx_champion_stats_champion_name ON champion_stats (champion_name);

COMMENT ON TABLE users IS 'Локальные пользователи мобильного компаньона';
COMMENT ON TABLE summoners IS 'Кэш призывателей LoL из Riot API';
COMMENT ON COLUMN summoners.riot_puuid IS 'Постоянный идентификатор Riot (PUUID)';
COMMENT ON TABLE matches IS 'Сыгранные матчи';
COMMENT ON TABLE participant_stats IS 'KDA и результат участника в одном матче';
