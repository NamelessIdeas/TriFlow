package postgres

import (
	"context"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/triflow/backend/internal/domain"
)

type InboxRepo struct{ pool *pgxpool.Pool }

func NewInboxRepo(p *pgxpool.Pool) *InboxRepo { return &InboxRepo{pool: p} }

func (r *InboxRepo) Create(ctx context.Context, i *domain.InboxItem) error {
	const q = `INSERT INTO inbox_items (id, user_id, raw_text) VALUES ($1, $2, $3) RETURNING created_at`
	return mapErr(r.pool.QueryRow(ctx, q, i.ID, i.UserID, i.RawText).Scan(&i.CreatedAt))
}

func (r *InboxRepo) GetByID(ctx context.Context, userID, id uuid.UUID) (*domain.InboxItem, error) {
	const q = `SELECT id, user_id, raw_text, processed_at, created_at
	           FROM inbox_items WHERE user_id = $1 AND id = $2`
	i := &domain.InboxItem{}
	if err := r.pool.QueryRow(ctx, q, userID, id).
		Scan(&i.ID, &i.UserID, &i.RawText, &i.ProcessedAt, &i.CreatedAt); err != nil {
		return nil, mapErr(err)
	}
	return i, nil
}

func (r *InboxRepo) MarkProcessed(ctx context.Context, userID, id uuid.UUID) error {
	const q = `UPDATE inbox_items SET processed_at = now()
	           WHERE user_id = $1 AND id = $2 AND processed_at IS NULL`
	ct, err := r.pool.Exec(ctx, q, userID, id)
	if err != nil {
		return mapErr(err)
	}
	if ct.RowsAffected() == 0 {
		return domain.ErrNotFound
	}
	return nil
}

func (r *InboxRepo) Delete(ctx context.Context, userID, id uuid.UUID) error {
	const q = `DELETE FROM inbox_items WHERE user_id = $1 AND id = $2`
	ct, err := r.pool.Exec(ctx, q, userID, id)
	if err != nil {
		return mapErr(err)
	}
	if ct.RowsAffected() == 0 {
		return domain.ErrNotFound
	}
	return nil
}

func (r *InboxRepo) ListUnprocessed(ctx context.Context, userID uuid.UUID, page domain.Page) ([]domain.InboxItem, int, error) {
	page = page.Normalize()
	var total int
	if err := r.pool.QueryRow(ctx,
		`SELECT COUNT(*) FROM inbox_items WHERE user_id = $1 AND processed_at IS NULL`,
		userID).Scan(&total); err != nil {
		return nil, 0, mapErr(err)
	}
	const q = `
		SELECT id, user_id, raw_text, processed_at, created_at
		FROM inbox_items
		WHERE user_id = $1 AND processed_at IS NULL
		ORDER BY created_at DESC
		LIMIT $2 OFFSET $3`
	rows, err := r.pool.Query(ctx, q, userID, page.Limit, page.Offset)
	if err != nil {
		return nil, 0, mapErr(err)
	}
	defer rows.Close()
	var out []domain.InboxItem
	for rows.Next() {
		var i domain.InboxItem
		if err := rows.Scan(&i.ID, &i.UserID, &i.RawText, &i.ProcessedAt, &i.CreatedAt); err != nil {
			return nil, 0, mapErr(err)
		}
		out = append(out, i)
	}
	return out, total, nil
}
