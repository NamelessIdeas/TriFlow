package postgres

import (
	"context"
	"strings"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/triflow/backend/internal/domain"
)

type NoteRepo struct{ pool *pgxpool.Pool }

func NewNoteRepo(p *pgxpool.Pool) *NoteRepo { return &NoteRepo{pool: p} }

const noteColumns = `id, user_id, title, content_md, para_category, created_at, updated_at`

func scanNote(row pgx.Row, n *domain.Note) error {
	return row.Scan(&n.ID, &n.UserID, &n.Title, &n.ContentMD, &n.PARACategory,
		&n.CreatedAt, &n.UpdatedAt)
}

func (r *NoteRepo) Create(ctx context.Context, n *domain.Note) error {
	const q = `INSERT INTO notes (id, user_id, title, content_md, para_category)
	           VALUES ($1,$2,$3,$4,$5) RETURNING created_at, updated_at`
	return mapErr(r.pool.QueryRow(ctx, q,
		n.ID, n.UserID, n.Title, n.ContentMD, n.PARACategory,
	).Scan(&n.CreatedAt, &n.UpdatedAt))
}

func (r *NoteRepo) GetByID(ctx context.Context, userID, id uuid.UUID) (*domain.Note, error) {
	q := `SELECT ` + noteColumns + ` FROM notes WHERE user_id = $1 AND id = $2`
	n := &domain.Note{}
	if err := scanNote(r.pool.QueryRow(ctx, q, userID, id), n); err != nil {
		return nil, mapErr(err)
	}
	tags, err := r.loadTags(ctx, id)
	if err != nil {
		return nil, err
	}
	n.Tags = tags
	return n, nil
}

func (r *NoteRepo) Update(ctx context.Context, n *domain.Note) error {
	const q = `UPDATE notes SET title = $3, content_md = $4, para_category = $5, updated_at = now()
	           WHERE user_id = $1 AND id = $2
	           RETURNING updated_at`
	return mapErr(r.pool.QueryRow(ctx, q,
		n.UserID, n.ID, n.Title, n.ContentMD, n.PARACategory,
	).Scan(&n.UpdatedAt))
}

func (r *NoteRepo) Delete(ctx context.Context, userID, id uuid.UUID) error {
	ct, err := r.pool.Exec(ctx, `DELETE FROM notes WHERE user_id = $1 AND id = $2`, userID, id)
	if err != nil {
		return mapErr(err)
	}
	if ct.RowsAffected() == 0 {
		return domain.ErrNotFound
	}
	return nil
}

func (r *NoteRepo) List(ctx context.Context, userID uuid.UUID, f domain.NoteFilter) ([]domain.Note, int, error) {
	f.Page = f.Page.Normalize()
	var (
		args    = []any{userID}
		filters = []string{"n.user_id = $1"}
		join    = ""
		orderBy = "n.updated_at DESC"
	)
	if f.PARACategory != nil {
		args = append(args, *f.PARACategory)
		filters = append(filters, "n.para_category = $"+itoa(len(args)))
	}
	if f.Tag != nil {
		args = append(args, *f.Tag)
		join = " JOIN note_tags nt ON nt.note_id = n.id"
		filters = append(filters, "nt.tag = $"+itoa(len(args)))
	}
	if f.Search != nil && *f.Search != "" {
		args = append(args, *f.Search)
		filters = append(filters, "n.search_tsv @@ plainto_tsquery('simple', $"+itoa(len(args))+")")
		orderBy = "ts_rank(n.search_tsv, plainto_tsquery('simple', $" + itoa(len(args)) + ")) DESC, n.updated_at DESC"
	}

	where := strings.Join(filters, " AND ")
	var total int
	if err := r.pool.QueryRow(ctx,
		`SELECT COUNT(DISTINCT n.id) FROM notes n`+join+` WHERE `+where, args...,
	).Scan(&total); err != nil {
		return nil, 0, mapErr(err)
	}

	args = append(args, f.Page.Limit, f.Page.Offset)
	q := `
		SELECT DISTINCT ` + noteColumns + `
		FROM notes n` + join + `
		WHERE ` + where + `
		ORDER BY ` + orderBy + `
		LIMIT $` + itoa(len(args)-1) + ` OFFSET $` + itoa(len(args))
	rows, err := r.pool.Query(ctx, q, args...)
	if err != nil {
		return nil, 0, mapErr(err)
	}
	defer rows.Close()

	var out []domain.Note
	for rows.Next() {
		var n domain.Note
		if err := scanNote(rows, &n); err != nil {
			return nil, 0, mapErr(err)
		}
		out = append(out, n)
	}
	if err := r.attachTags(ctx, out); err != nil {
		return nil, 0, err
	}
	return out, total, nil
}

func (r *NoteRepo) ListRecent(ctx context.Context, userID uuid.UUID, limit int) ([]domain.Note, error) {
	if limit <= 0 {
		limit = 5
	}
	q := `SELECT ` + noteColumns + ` FROM notes WHERE user_id = $1
	      ORDER BY updated_at DESC LIMIT $2`
	rows, err := r.pool.Query(ctx, q, userID, limit)
	if err != nil {
		return nil, mapErr(err)
	}
	defer rows.Close()
	var out []domain.Note
	for rows.Next() {
		var n domain.Note
		if err := scanNote(rows, &n); err != nil {
			return nil, mapErr(err)
		}
		out = append(out, n)
	}
	if err := r.attachTags(ctx, out); err != nil {
		return nil, err
	}
	return out, nil
}

func (r *NoteRepo) ReplaceTags(ctx context.Context, noteID uuid.UUID, tags []string) error {
	tx, err := r.pool.Begin(ctx)
	if err != nil {
		return mapErr(err)
	}
	defer func() { _ = tx.Rollback(ctx) }()
	if _, err := tx.Exec(ctx, `DELETE FROM note_tags WHERE note_id = $1`, noteID); err != nil {
		return mapErr(err)
	}
	for _, tag := range tags {
		if tag == "" {
			continue
		}
		if _, err := tx.Exec(ctx,
			`INSERT INTO note_tags (note_id, tag) VALUES ($1, $2) ON CONFLICT DO NOTHING`,
			noteID, tag); err != nil {
			return mapErr(err)
		}
	}
	return mapErr(tx.Commit(ctx))
}

func (r *NoteRepo) AddLink(ctx context.Context, userID, source, target uuid.UUID) error {
	if err := r.ensureOwns(ctx, userID, source); err != nil {
		return err
	}
	if err := r.ensureOwns(ctx, userID, target); err != nil {
		return err
	}
	_, err := r.pool.Exec(ctx,
		`INSERT INTO note_links (source_note_id, target_note_id) VALUES ($1, $2)
		 ON CONFLICT DO NOTHING`, source, target)
	return mapErr(err)
}

func (r *NoteRepo) RemoveLink(ctx context.Context, userID, source, target uuid.UUID) error {
	if err := r.ensureOwns(ctx, userID, source); err != nil {
		return err
	}
	_, err := r.pool.Exec(ctx,
		`DELETE FROM note_links WHERE source_note_id = $1 AND target_note_id = $2`,
		source, target)
	return mapErr(err)
}

func (r *NoteRepo) OutgoingLinks(ctx context.Context, userID, id uuid.UUID) ([]domain.Note, error) {
	const q = `
		SELECT ` + noteColumns + `
		FROM notes n
		JOIN note_links l ON l.target_note_id = n.id
		WHERE l.source_note_id = $2 AND n.user_id = $1
		ORDER BY n.updated_at DESC`
	return r.queryNotes(ctx, q, userID, id)
}

func (r *NoteRepo) Backlinks(ctx context.Context, userID, id uuid.UUID) ([]domain.Note, error) {
	const q = `
		SELECT ` + noteColumns + `
		FROM notes n
		JOIN note_links l ON l.source_note_id = n.id
		WHERE l.target_note_id = $2 AND n.user_id = $1
		ORDER BY n.updated_at DESC`
	return r.queryNotes(ctx, q, userID, id)
}

func (r *NoteRepo) AddRef(ctx context.Context, userID, noteID uuid.UUID, refType string, refID uuid.UUID) error {
	if err := r.ensureOwns(ctx, userID, noteID); err != nil {
		return err
	}
	_, err := r.pool.Exec(ctx,
		`INSERT INTO note_refs (note_id, ref_type, ref_id) VALUES ($1, $2, $3)
		 ON CONFLICT DO NOTHING`, noteID, refType, refID)
	return mapErr(err)
}

func (r *NoteRepo) RemoveRef(ctx context.Context, userID, noteID uuid.UUID, refType string, refID uuid.UUID) error {
	if err := r.ensureOwns(ctx, userID, noteID); err != nil {
		return err
	}
	_, err := r.pool.Exec(ctx,
		`DELETE FROM note_refs WHERE note_id = $1 AND ref_type = $2 AND ref_id = $3`,
		noteID, refType, refID)
	return mapErr(err)
}

func (r *NoteRepo) NotesForRef(ctx context.Context, userID uuid.UUID, refType string, refID uuid.UUID) ([]domain.Note, error) {
	const q = `
		SELECT ` + noteColumns + `
		FROM notes n
		JOIN note_refs r ON r.note_id = n.id
		WHERE n.user_id = $1 AND r.ref_type = $2 AND r.ref_id = $3
		ORDER BY n.updated_at DESC`
	rows, err := r.pool.Query(ctx, q, userID, refType, refID)
	if err != nil {
		return nil, mapErr(err)
	}
	defer rows.Close()
	var out []domain.Note
	for rows.Next() {
		var n domain.Note
		if err := scanNote(rows, &n); err != nil {
			return nil, mapErr(err)
		}
		out = append(out, n)
	}
	if err := r.attachTags(ctx, out); err != nil {
		return nil, err
	}
	return out, nil
}

func (r *NoteRepo) queryNotes(ctx context.Context, q string, args ...any) ([]domain.Note, error) {
	rows, err := r.pool.Query(ctx, q, args...)
	if err != nil {
		return nil, mapErr(err)
	}
	defer rows.Close()
	var out []domain.Note
	for rows.Next() {
		var n domain.Note
		if err := scanNote(rows, &n); err != nil {
			return nil, mapErr(err)
		}
		out = append(out, n)
	}
	if err := r.attachTags(ctx, out); err != nil {
		return nil, err
	}
	return out, nil
}

func (r *NoteRepo) ensureOwns(ctx context.Context, userID, noteID uuid.UUID) error {
	var ok bool
	if err := r.pool.QueryRow(ctx,
		`SELECT EXISTS(SELECT 1 FROM notes WHERE id = $1 AND user_id = $2)`,
		noteID, userID).Scan(&ok); err != nil {
		return mapErr(err)
	}
	if !ok {
		return domain.ErrNotFound
	}
	return nil
}

func (r *NoteRepo) loadTags(ctx context.Context, noteID uuid.UUID) ([]string, error) {
	rows, err := r.pool.Query(ctx, `SELECT tag FROM note_tags WHERE note_id = $1 ORDER BY tag`, noteID)
	if err != nil {
		return nil, mapErr(err)
	}
	defer rows.Close()
	var tags []string
	for rows.Next() {
		var t string
		if err := rows.Scan(&t); err != nil {
			return nil, mapErr(err)
		}
		tags = append(tags, t)
	}
	return tags, nil
}

func (r *NoteRepo) attachTags(ctx context.Context, notes []domain.Note) error {
	if len(notes) == 0 {
		return nil
	}
	ids := make([]uuid.UUID, len(notes))
	idx := make(map[uuid.UUID]int, len(notes))
	for i, n := range notes {
		ids[i] = n.ID
		idx[n.ID] = i
	}
	rows, err := r.pool.Query(ctx,
		`SELECT note_id, tag FROM note_tags WHERE note_id = ANY($1) ORDER BY tag`, ids)
	if err != nil {
		return mapErr(err)
	}
	defer rows.Close()
	for rows.Next() {
		var id uuid.UUID
		var tag string
		if err := rows.Scan(&id, &tag); err != nil {
			return mapErr(err)
		}
		i := idx[id]
		notes[i].Tags = append(notes[i].Tags, tag)
	}
	return nil
}
