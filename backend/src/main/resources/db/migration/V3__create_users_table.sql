CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);

-- Insert initial users for development (BCrypt hashed passwords with cost factor 10)
-- admin / admin123 (BCrypt hash)
-- viewer / viewer123 (BCrypt hash)
INSERT INTO users (username, password_hash, role, enabled) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', true),
('viewer', '$2a$10$xn3LI/AjqicNYZBM4XqX3eLLF4FYRYrYdYZ5.5c5gUPHq4vBNBqJe', 'VIEWER', true);
