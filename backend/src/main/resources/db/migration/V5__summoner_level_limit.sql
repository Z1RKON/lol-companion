ALTER TABLE summoners DROP CONSTRAINT IF EXISTS summoners_summoner_level_check;
ALTER TABLE summoners
    ADD CONSTRAINT summoners_summoner_level_check
        CHECK (summoner_level >= 1 AND summoner_level <= 9999);
