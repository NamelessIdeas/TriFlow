CREATE TABLE notes (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title         TEXT        NOT NULL,
    content_md    TEXT        NOT NULL DEFAULT '',
    para_category TEXT        NOT NULL DEFAULT 'resource'
                  CHECK (para_category IN ('project','area','resource','archive')),
    search_tsv    TSVECTOR,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX notes_user_idx      ON notes(user_id);
CREATE INDEX notes_user_para_idx ON notes(user_id, para_category);
CREATE INDEX notes_search_idx    ON notes USING GIN(search_tsv);

CREATE OR REPLACE FUNCTION notes_tsv_trigger() RETURNS trigger AS $$
BEGIN
  NEW.search_tsv :=
    setweight(to_tsvector('simple', coalesce(NEW.title, '')),     'A') ||
    setweight(to_tsvector('simple', coalesce(NEW.content_md, '')), 'B');
  RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER notes_tsv_update
BEFORE INSERT OR UPDATE OF title, content_md ON notes
FOR EACH ROW EXECUTE FUNCTION notes_tsv_trigger();

CREATE TABLE note_tags (
    note_id UUID NOT NULL REFERENCES notes(id) ON DELETE CASCADE,
    tag     TEXT NOT NULL,
    PRIMARY KEY (note_id, tag)
);
CREATE INDEX note_tags_tag_idx ON note_tags(tag);

CREATE TABLE note_links (
    source_note_id UUID NOT NULL REFERENCES notes(id) ON DELETE CASCADE,
    target_note_id UUID NOT NULL REFERENCES notes(id) ON DELETE CASCADE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (source_note_id, target_note_id),
    CHECK (source_note_id <> target_note_id)
);
CREATE INDEX note_links_target_idx ON note_links(target_note_id);
