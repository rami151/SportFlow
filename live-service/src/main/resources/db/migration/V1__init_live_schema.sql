CREATE TABLE IF NOT EXISTS live_events (
    id UUID PRIMARY KEY,
    competition_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    athlete_a VARCHAR(255) NOT NULL,
    athlete_b VARCHAR(255) NOT NULL,
    score_a INT NOT NULL,
    score_b INT NOT NULL,
    event_timestamp BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_live_events_competition_id ON live_events(competition_id);
