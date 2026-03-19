-- tabela de carduri 
CREATE TABLE card (
    id        BIGSERIAL PRIMARY KEY,
    nume_card VARCHAR(100) NOT NULL
);

-- tabela de legatura card:duck 
CREATE TABLE card_members (
    card_id BIGINT NOT NULL,
    duck_id BIGINT NOT NULL,
    PRIMARY KEY (card_id, duck_id),
    FOREIGN KEY (card_id) REFERENCES card(id) ON DELETE CASCADE,
    FOREIGN KEY (duck_id) REFERENCES users(id) ON DELETE CASCADE
);

SELECT * FROM card;
SELECT * FROM card_members;