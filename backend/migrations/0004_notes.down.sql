DROP TABLE IF EXISTS note_links;
DROP TABLE IF EXISTS note_tags;
DROP TRIGGER  IF EXISTS notes_tsv_update ON notes;
DROP FUNCTION IF EXISTS notes_tsv_trigger();
DROP TABLE IF EXISTS notes;
