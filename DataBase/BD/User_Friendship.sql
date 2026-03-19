CREATE TABLE users (
    id          BIGINT PRIMARY KEY, 
    user_type   VARCHAR(30) NOT NULL,        -- PERSON / DUCK
    username    VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,

    -- Pentru PERSON 
    first_name  VARCHAR(100),
    last_name   VARCHAR(100),
    birth_date  DATE,
    occupation  VARCHAR(200),
    empathy     DOUBLE PRECISION,

    -- Pentru rațe
	duck_kind   VARCHAR(30),                 -- FLYING / SWIMMING
    speed       DOUBLE PRECISION,
    stamina     DOUBLE PRECISION
);

CREATE TABLE friendships (
    user_id_1 BIGINT NOT NULL,
    user_id_2 BIGINT NOT NULL,
    PRIMARY KEY (user_id_1, user_id_2),
    FOREIGN KEY (user_id_1) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id_2) REFERENCES users(id) ON DELETE CASCADE
);

SELECT * FROM users;
SELECT * FROM friendships;
