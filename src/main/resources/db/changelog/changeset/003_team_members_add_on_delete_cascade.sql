ALTER TABLE team_members
DROP CONSTRAINT IF EXISTS fk_event_id,
ADD CONSTRAINT fk_event_id FOREIGN KEY (event_id)
    REFERENCES events (id) ON DELETE CASCADE;