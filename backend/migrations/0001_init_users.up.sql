CREATE EXTENSION IF NOT EXISTS "citext";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email         CITEXT      NOT NULL UNIQUE,
    password_hash TEXT        NOT NULL,
    display_name  TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_preferences (
    user_id                    UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    pomodoro_duration_min      INT  NOT NULL DEFAULT 25 CHECK (pomodoro_duration_min  BETWEEN 1 AND 180),
    short_break_min            INT  NOT NULL DEFAULT 5  CHECK (short_break_min        BETWEEN 1 AND 60),
    long_break_min             INT  NOT NULL DEFAULT 15 CHECK (long_break_min         BETWEEN 1 AND 120),
    pomodoros_until_long_break INT  NOT NULL DEFAULT 4  CHECK (pomodoros_until_long_break BETWEEN 2 AND 12),
    timezone                   TEXT NOT NULL DEFAULT 'UTC',
    updated_at                 TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE refresh_tokens (
    jti        UUID        PRIMARY KEY,
    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX refresh_tokens_user_id_idx ON refresh_tokens(user_id);
