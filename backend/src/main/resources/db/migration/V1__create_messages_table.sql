CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    content VARCHAR(255) NOT NULL
);

INSERT INTO messages (code, content) VALUES ('hello', 'Hello, World!');