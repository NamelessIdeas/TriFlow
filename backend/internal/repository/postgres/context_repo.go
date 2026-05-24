package postgres

import (
	"context"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/triflow/backend/internal/domain"
)

type ContextRepo struct{ pool *pgxpool.Pool }

func NewContextRepo(p *pgxpool.Pool) *ContextRepo { return &ContextRepo{pool: p} }

func (r *ContextRepo) Create(ctx context.Context, c *domain.Context) error {
	const q = `INSERT INTO contexts (id, user_id, name) VALUES ($1, $2, $3) RETURNING created_at`
	return mapErr(r.pool.QueryRow(ctx, q, c.ID, c.UserID, c.Name).Scan(&c.CreatedAt))
}

func (r *ContextRepo) List(ctx context.Context, userID uuid.UUID) ([]domain.Context, error) {
	const q = `SELECT id, user_id, name, created_at FROM contexts WHERE user_id = $1 ORDER BY name`
	rows, err := r.pool.Query(ctx, q, userID)
	if err != nil {
		return nil, mapErr(err)
	}
	defer rows.Close()
	var out []domain.Context
	for rows.Next() {
		var c domain.Context
		if err := rows.Scan(&c.ID, &c.UserID, &c.Name, &c.CreatedAt); err != nil {
			return nil, mapErr(err)
		}
		out = append(out, c)
	}
	return out, nil
}

func (r *ContextRepo) Delete(ctx context.Context, userID, id uuid.UUID) error {
	const q = `DELETE FROM contexts WHERE user_id = $1 AND id = $2`
	ct, err := r.pool.Exec(ctx, q, userID, id)
	if err != nil {
		return mapErr(err)
	}
	if ct.RowsAffected() == 0 {
		return domain.ErrNotFound
	}
	return nil
}
