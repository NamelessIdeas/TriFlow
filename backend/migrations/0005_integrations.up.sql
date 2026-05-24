CREATE TABLE note_refs (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    note_id    UUID        NOT NULL REFERENCES notes(id) ON DELETE CASCADE,
    ref_type   TEXT        NOT NULL CHECK (ref_type IN ('task','project')),
    ref_id     UUID        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (note_id, ref_type, ref_id)
);
CREATE INDEX note_refs_target_idx ON note_refs(ref_type, ref_id);
