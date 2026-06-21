ALTER TABLE participant_stats
    ADD COLUMN IF NOT EXISTS summoner_display_name VARCHAR(50);
