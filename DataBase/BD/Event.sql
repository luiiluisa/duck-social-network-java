CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY
);

CREATE TABLE event_subscribers (
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY(event_id, user_id)
);

