package postgres

import (
	"context"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/triflow/backend/internal/domain"
)

type ProjectRepo struct{ pool *pgxpool.Pool }

func NewProjectRepo(p *pgxpool.Pool) *ProjectRepo { return &ProjectRepo{pool: p} }

func (r *ProjectRepo) Create(ctx context.Context, p *domain.Project) error {
	const q = `
		INSERT INTO projects (id, user_id, title, description, status)
		VALUES ($1, $2, $3, $4, $5)
		RETURNING created_at, updated_at`
	return mapErr(r.pool.QueryRow(ctx, q,
		p.ID, p.UserID, p.Title, p.Description, p.Status,
	).Scan(&p.CreatedAt, &p.UpdatedAt))
}

func (r *ProjectRepo) GetByID(ctx context.Context, userID, id uuid.UUID) (*domain.Project, error) {
	const q = `SELECT id, user_id, title, COALESCE(description,''), status,
	                   created_at, updated_at, completed_at
	           FROM projects WHERE user_id = $1 AND id = $2`
	p := &domain.Project{}
	if err := r.pool.QueryRow(ctx, q, userID, id).
		Scan(&p.ID, &p.UserID, &p.Title, &p.Description, &p.Status,
			&p.CreatedAt, &p.UpdatedAt, &p.CompletedAt); err != nil {
		return nil, mapErr(err)
	}
	return p, nil
}

func (r *ProjectRepo) Update(ctx context.Context, p *domain.Project) error {
	const q = `
		UPDATE projects
		SET title = $3, description = $4, status = $5,
		    completed_at = CASE WHEN $5 = 'completed' AND completed_at IS NULL THEN now()
		                        WHEN $5 <> 'completed' THEN NULL
		                        ELSE completed_at END,
		    updated_at = now()
		WHERE user_id = $1 AND id = $2
		RETURNING updated_at, completed_at`
	return mapErr(r.pool.QueryRow(ctx, q,
		p.UserID, p.ID, p.Title, p.Description, p.Status,
	).Scan(&p.UpdatedAt, &p.CompletedAt))
}

func (r *ProjectRepo) Delete(ctx context.Context, userID, id uuid.UUID) error {
	const q = `DELETE FROM projects WHERE user_id = $1 AND id = $2`
	ct, err := r.pool.Exec(ctx, q, userID, id)
	if err != nil {
		return mapErr(err)
	}
	if ct.RowsAffected() == 0 {
		return domain.ErrNotFound
	}
	return nil
}

func (r *ProjectRepo) List(ctx context.Context, userID uuid.UUID, status *string, page domain.Page) ([]domain.Project, int, error) {
	page = page.Normalize()

	var (
		args  = []any{userID}
		where = "user_id = $1"
	)
	if status != nil {
		args = append(args, *status)
		where += " AND status = $2"
	}

	var total int
	if err := r.pool.QueryRow(ctx, "SELECT COUNT(*) FROM projects WHERE "+where, args...).Scan(&total); err != nil {
		return nil, 0, mapErr(err)
	}

	args = append(args, page.Limit, page.Offset)
	q := `
		SELECT id, user_id, title, COALESCE(description,''), status, created_at, updated_at, completed_at
		FROM projects
		WHERE ` + where + `
		ORDER BY created_at DESC
		LIMIT $` + itoa(len(args)-1) + ` OFFSET $` + itoa(len(args))

	rows, err := r.pool.Query(ctx, q, args...)
	if err != nil {
		return nil, 0, mapErr(err)
	}
	defer rows.Close()

	var out []domain.Project
	for rows.Next() {
		var p domain.Project
		if err := rows.Scan(&p.ID, &p.UserID, &p.Title, &p.Description, &p.Status,
			&p.CreatedAt, &p.UpdatedAt, &p.CompletedAt); err != nil {
			return nil, 0, mapErr(err)
		}
		out = append(out, p)
	}
	return out, total, nil
}

// ProjectsWithoutNextAction restituisce progetti attivi senza alcuna task
// in stato 'next_action'. Tipico segnale di weekly review.
func (r *ProjectRepo) ProjectsWithoutNextAction(ctx context.Context, userID uuid.UUID) ([]domain.Project, error) {
	const q = `
		SELECT p.id, p.user_id, p.title, COALESCE(p.description,''), p.status,
		       p.created_at, p.updated_at, p.completed_at
		FROM projects p
		WHERE p.user_id = $1 AND p.status = 'active'
		  AND NOT EXISTS (
		    SELECT 1 FROM tasks t
		    WHERE t.project_id = p.id AND t.status = 'next_action'
		  )
		ORDER BY p.updated_at DESC`
	rows, err := r.pool.Query(ctx, q, userID)
	if err != nil {
		return nil, mapErr(err)
	}
	defer rows.Close()
	var out []domain.Project
	for rows.Next() {
		var p domain.Project
		if err := rows.Scan(&p.ID, &p.UserID, &p.Title, &p.Description, &p.Status,
			&p.CreatedAt, &p.UpdatedAt, &p.CompletedAt); err != nil {
			return nil, mapErr(err)
		}
		out = append(out, p)
	}
	return out, nil
}
