CREATE TABLE pomodoro_sessions (
    id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    task_id               UUID        REFERENCES tasks(id) ON DELETE SET NULL,
    kind                  TEXT        NOT NULL
                          CHECK (kind IN ('focus','short_break','long_break')),
    planned_duration_sec  INT         NOT NULL CHECK (planned_duration_sec > 0),
    actual_duration_sec   INT         NOT NULL CHECK (actual_duration_sec >= 0),
    cycle_index           INT         NOT NULL DEFAULT 1,
    started_at            TIMESTAMPTZ NOT NULL,
    ended_at              TIMESTAMPTZ NOT NULL,
    status                TEXT        NOT NULL
                          CHECK (status IN ('completed','aborted')),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX pomodoro_user_started_idx ON pomodoro_sessions(user_id, started_at DESC);
CREATE INDEX pomodoro_user_task_idx    ON pomodoro_sessions(user_id, task_id) WHERE task_id IS NOT NULL;
