-- Riot добавил ранг Emerald; обновляем CHECK-ограничение tier в summoners.

ALTER TABLE summoners DROP CONSTRAINT IF EXISTS summoners_tier_check;

ALTER TABLE summoners
    ADD CONSTRAINT summoners_tier_check
        CHECK (tier IN (
            'IRON', 'BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'EMERALD',
            'DIAMOND', 'MASTER', 'GRANDMASTER', 'CHALLENGER'
        ));
