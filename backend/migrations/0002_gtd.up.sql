CREATE TABLE contexts (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, name)
);

CREATE TABLE projects (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title        TEXT        NOT NULL,
    description  TEXT,
    status       TEXT        NOT NULL DEFAULT 'active'
                 CHECK (status IN ('active','someday','completed')),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ
);
CREATE INDEX projects_user_status_idx ON projects(user_id, status);

CREATE TABLE inbox_items (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    raw_text     TEXT        NOT NULL,
    processed_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX inbox_user_unprocessed_idx ON inbox_items(user_id) WHERE processed_at IS NULL;

CREATE TABLE tasks (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    project_id        UUID        REFERENCES projects(id) ON DELETE SET NULL,
    context_id        UUID        REFERENCES contexts(id) ON DELETE SET NULL,
    title             TEXT        NOT NULL,
    notes             TEXT,
    status            TEXT        NOT NULL DEFAULT 'inbox'
                      CHECK (status IN ('inbox','next_action','waiting','scheduled','done')),
    energy            TEXT        CHECK (energy IN ('low','medium','high')),
    estimated_minutes INT         CHECK (estimated_minutes IS NULL OR estimated_minutes > 0),
    priority          SMALLINT    NOT NULL DEFAULT 0,
    due_date          DATE,
    defer_date        DATE,
    completed_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX tasks_user_status_idx  ON tasks(user_id, status);
CREATE INDEX tasks_user_project_idx ON tasks(user_id, project_id);
CREATE INDEX tasks_user_due_idx     ON tasks(user_id, due_date) WHERE due_date IS NOT NULL;

CREATE TABLE task_tags (
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    tag     TEXT NOT NULL,
    PRIMARY KEY (task_id, tag)
);
CREATE INDEX task_tags_tag_idx ON task_tags(tag);
