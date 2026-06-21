-- Привязка Riot ID к локальному аккаунту пользователя

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS linked_riot_puuid VARCHAR(78),
    ADD COLUMN IF NOT EXISTS linked_riot_id   VARCHAR(50),
    ADD COLUMN IF NOT EXISTS linked_riot_region VARCHAR(10);

CREATE INDEX IF NOT EXISTS idx_users_linked_riot_puuid ON users (linked_riot_puuid);
