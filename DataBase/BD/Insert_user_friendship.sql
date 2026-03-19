INSERT INTO users (user_type, username, email, password,
                   first_name, last_name, birth_date, occupation, empathy)
VALUES
('PERSON', 'pers1', 'pers1@gmail.com', 'pass1',
 'Nume1', 'Pren1', NULL, 'ocupatie1', 0.6),
('PERSON', 'pers2', 'pers2@gmail.com', 'pass2',
 'Nume2', 'Pren2', NULL, 'ocupatie2', 0.7),
('PERSON', 'pers3', 'pers3@gmail.com', 'pass3',
 'Nume3', 'Pren3', NULL, 'ocupatie3', 0.8);

INSERT INTO users (user_type, username, email, password,
                   duck_kind, speed, stamina)
VALUES
('DUCK', 'duck1', 'duck1@gmail.com', 'passd1',
 'SWIMMING', 3.0, 6.0),
('DUCK', 'duck2', 'duck2@gmail.com', 'passd2',
 'FLYING', 4.0, 7.0);


INSERT INTO friendships(user_id_1, user_id_2) VALUES
(1, 2),
(1, 3),
(2, 3),
(2, 4),
(3, 5);

ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
