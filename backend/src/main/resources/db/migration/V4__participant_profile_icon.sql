ALTER TABLE participant_stats
    ADD COLUMN IF NOT EXISTS profile_icon_id INTEGER;
