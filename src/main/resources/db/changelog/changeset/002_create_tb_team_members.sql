CREATE TABLE IF NOT EXISTS team_members (
    event_id BIGINT NOT NULL REFERENCES events(id),
    user_id BIGINT NOT NULL,
    role varchar(255) NOT NULL,
    PRIMARY KEY ("event_id", "user_id")
);