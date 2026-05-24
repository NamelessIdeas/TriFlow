package postgres

import (
	"context"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/triflow/backend/internal/domain"
)

type TaskRepo struct{ pool *pgxpool.Pool }

func NewTaskRepo(p *pgxpool.Pool) *TaskRepo { return &TaskRepo{pool: p} }

const taskColumns = `id, user_id, project_id, context_id, title, COALESCE(notes,''),
                     status, energy, estimated_minutes, priority,
                     due_date, defer_date, completed_at, created_at, updated_at`

func scanTask(row pgx.Row, t *domain.Task) error {
	return row.Scan(&t.ID, &t.UserID, &t.ProjectID, &t.ContextID, &t.Title, &t.Notes,
		&t.Status, &t.Energy, &t.EstimatedMinutes, &t.Priority,
		&t.DueDate, &t.DeferDate, &t.CompletedAt, &t.CreatedAt, &t.UpdatedAt)
}

func (r *TaskRepo) Create(ctx context.Context, t *domain.Task) error {
	const q = `
		INSERT INTO tasks (id, user_id, project_id, context_id, title, notes, status,
		                   energy, estimated_minutes, priority, due_date, defer_date)
		VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12)
		RETURNING created_at, updated_at`
	return mapErr(r.pool.QueryRow(ctx, q,
		t.ID, t.UserID, t.ProjectID, t.ContextID, t.Title, t.Notes, t.Status,
		t.Energy, t.EstimatedMinutes, t.Priority, t.DueDate, t.DeferDate,
	).Scan(&t.CreatedAt, &t.UpdatedAt))
}

func (r *TaskRepo) GetByID(ctx context.Context, userID, id uuid.UUID) (*domain.Task, error) {
	q := `SELECT ` + taskColumns + ` FROM tasks WHERE user_id = $1 AND id = $2`
	t := &domain.Task{}
	if err := scanTask(r.pool.QueryRow(ctx, q, userID, id), t); err != nil {
		return nil, mapErr(err)
	}
	tags, err := r.loadTags(ctx, t.ID)
	if err != nil {
		return nil, err
	}
	t.Tags = tags
	return t, nil
}

func (r *TaskRepo) Update(ctx context.Context, t *domain.Task) error {
	const q = `
		UPDATE tasks
		SET project_id = $3, context_id = $4, title = $5, notes = $6,
		    status = $7,
		    energy = $8, estimated_minutes = $9, priority = $10,
		    due_date = $11, defer_date = $12,
		    completed_at = CASE WHEN $7 = 'done' AND completed_at IS NULL THEN now()
		                        WHEN $7 <> 'done' THEN NULL
		                        ELSE completed_at END,
		    updated_at = now()
		WHERE user_id = $1 AND id = $2
		RETURNING updated_at, completed_at`
	return mapErr(r.pool.QueryRow(ctx, q,
		t.UserID, t.ID, t.ProjectID, t.ContextID, t.Title, t.Notes, t.Status,
		t.Energy, t.EstimatedMinutes, t.Priority, t.DueDate, t.DeferDate,
	).Scan(&t.UpdatedAt, &t.CompletedAt))
}

func (r *TaskRepo) Delete(ctx context.Context, userID, id uuid.UUID) error {
	ct, err := r.pool.Exec(ctx, `DELETE FROM tasks WHERE user_id = $1 AND id = $2`, userID, id)
	if err != nil {
		return mapErr(err)
	}
	if ct.RowsAffected() == 0 {
		return domain.ErrNotFound
	}
	return nil
}

func (r *TaskRepo) List(ctx context.Context, userID uuid.UUID, f domain.TaskFilter) ([]domain.Task, int, error) {
	f.Page = f.Page.Normalize()

	var (
		args     = []any{userID}
		filters  = []string{"t.user_id = $1"}
		joinTags = ""
	)

	if f.Status != nil {
		args = append(args, *f.Status)
		filters = append(filters, "t.status = $"+itoa(len(args)))
	}
	if f.ProjectID != nil {
		args = append(args, *f.ProjectID)
		filters = append(filters, "t.project_id = $"+itoa(len(args)))
	}
	if f.ContextID != nil {
		args = append(args, *f.ContextID)
		filters = append(filters, "t.context_id = $"+itoa(len(args)))
	}
	if f.DueBefore != nil {
		args = append(args, *f.DueBefore)
		filters = append(filters, "t.due_date <= $"+itoa(len(args)))
	}
	if f.Tag != nil {
		args = append(args, *f.Tag)
		joinTags = " JOIN task_tags tt ON tt.task_id = t.id"
		filters = append(filters, "tt.tag = $"+itoa(len(args)))
	}

	where := strings.Join(filters, " AND ")

	var total int
	countQ := `SELECT COUNT(DISTINCT t.id) FROM tasks t` + joinTags + ` WHERE ` + where
	if err := r.pool.QueryRow(ctx, countQ, args...).Scan(&total); err != nil {
		return nil, 0, mapErr(err)
	}

	args = append(args, f.Page.Limit, f.Page.Offset)
	q := `
		SELECT DISTINCT ` + taskColumns + `
		FROM tasks t` + joinTags + `
		WHERE ` + where + `
		ORDER BY t.priority DESC, t.created_at DESC
		LIMIT $` + itoa(len(args)-1) + ` OFFSET $` + itoa(len(args))

	rows, err := r.pool.Query(ctx, q, args...)
	if err != nil {
		return nil, 0, mapErr(err)
	}
	defer rows.Close()

	var out []domain.Task
	for rows.Next() {
		var t domain.Task
		if err := scanTask(rows, &t); err != nil {
			return nil, 0, mapErr(err)
		}
		out = append(out, t)
	}

	if err := r.attachTags(ctx, out); err != nil {
		return nil, 0, err
	}
	return out, total, nil
}

func (r *TaskRepo) ListByStatus(ctx context.Context, userID uuid.UUID, status string) ([]domain.Task, error) {
	q := `SELECT ` + taskColumns + ` FROM tasks
	      WHERE user_id = $1 AND status = $2
	      ORDER BY priority DESC, created_at DESC`
	return r.queryTasks(ctx, q, userID, status)
}

func (r *TaskRepo) TodayList(ctx context.Context, userID uuid.UUID, today time.Time) ([]domain.Task, error) {
	q := `SELECT ` + taskColumns + ` FROM tasks
	      WHERE user_id = $1
	        AND status IN ('next_action','scheduled')
	        AND (due_date IS NULL OR due_date <= $2)
	        AND (defer_date IS NULL OR defer_date <= $2)
	      ORDER BY priority DESC, due_date NULLS LAST, created_at DESC
	      LIMIT 50`
	return r.queryTasks(ctx, q, userID, today)
}

func (r *TaskRepo) queryTasks(ctx context.Context, q string, args ...any) ([]domain.Task, error) {
	rows, err := r.pool.Query(ctx, q, args...)
	if err != nil {
		return nil, mapErr(err)
	}
	defer rows.Close()
	var out []domain.Task
	for rows.Next() {
		var t domain.Task
		if err := scanTask(rows, &t); err != nil {
			return nil, mapErr(err)
		}
		out = append(out, t)
	}
	if err := r.attachTags(ctx, out); err != nil {
		return nil, err
	}
	return out, nil
}

func (r *TaskRepo) ReplaceTags(ctx context.Context, taskID uuid.UUID, tags []string) error {
	tx, err := r.pool.Begin(ctx)
	if err != nil {
		return mapErr(err)
	}
	defer func() { _ = tx.Rollback(ctx) }()

	if _, err := tx.Exec(ctx, `DELETE FROM task_tags WHERE task_id = $1`, taskID); err != nil {
		return mapErr(err)
	}
	for _, tag := range tags {
		if tag == "" {
			continue
		}
		if _, err := tx.Exec(ctx,
			`INSERT INTO task_tags (task_id, tag) VALUES ($1, $2)
			 ON CONFLICT DO NOTHING`, taskID, tag); err != nil {
			return mapErr(err)
		}
	}
	return mapErr(tx.Commit(ctx))
}

func (r *TaskRepo) loadTags(ctx context.Context, taskID uuid.UUID) ([]string, error) {
	rows, err := r.pool.Query(ctx, `SELECT tag FROM task_tags WHERE task_id = $1 ORDER BY tag`, taskID)
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

// attachTags fa una sola query per popolare i tag di tutte le task della lista.
func (r *TaskRepo) attachTags(ctx context.Context, tasks []domain.Task) error {
	if len(tasks) == 0 {
		return nil
	}
	ids := make([]uuid.UUID, len(tasks))
	idx := make(map[uuid.UUID]int, len(tasks))
	for i, t := range tasks {
		ids[i] = t.ID
		idx[t.ID] = i
	}
	rows, err := r.pool.Query(ctx,
		`SELECT task_id, tag FROM task_tags WHERE task_id = ANY($1) ORDER BY tag`, ids)
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
		tasks[i].Tags = append(tasks[i].Tags, tag)
	}
	return nil
}
