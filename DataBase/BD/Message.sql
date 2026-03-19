CREATE TABLE message (
    id          BIGSERIAL PRIMARY KEY,
    sender_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content     TEXT   NOT NULL,
    sent_at     TIMESTAMP NOT NULL
);


SELECT * FROM message;