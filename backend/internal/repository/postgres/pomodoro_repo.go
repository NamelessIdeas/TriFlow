package postgres

import (
	"context"
	"time"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/triflow/backend/internal/domain"
)

type PomodoroRepo struct{ pool *pgxpool.Pool }

func NewPomodoroRepo(p *pgxpool.Pool) *PomodoroRepo { return &PomodoroRepo{pool: p} }

func (r *PomodoroRepo) Create(ctx context.Context, s *domain.PomodoroSession) error {
	const q = `
		INSERT INTO pomodoro_sessions
		    (id, user_id, task_id, kind, planned_duration_sec, actual_duration_sec,
		     cycle_index, started_at, ended_at, status)
		VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10)
		RETURNING created_at`
	return mapErr(r.pool.QueryRow(ctx, q,
		s.ID, s.UserID, s.TaskID, s.Kind, s.PlannedDurationSec, s.ActualDurationSec,
		s.CycleIndex, s.StartedAt, s.EndedAt, s.Status,
	).Scan(&s.CreatedAt))
}

func (r *PomodoroRepo) ListByUser(ctx context.Context, userID uuid.UUID, from, to time.Time, page domain.Page) ([]domain.PomodoroSession, int, error) {
	page = page.Normalize()
	var total int
	if err := r.pool.QueryRow(ctx,
		`SELECT COUNT(*) FROM pomodoro_sessions
		 WHERE user_id = $1 AND started_at >= $2 AND started_at < $3`,
		userID, from, to).Scan(&total); err != nil {
		return nil, 0, mapErr(err)
	}
	const q = `
		SELECT id, user_id, task_id, kind, planned_duration_sec, actual_duration_sec,
		       cycle_index, started_at, ended_at, status, created_at
		FROM pomodoro_sessions
		WHERE user_id = $1 AND started_at >= $2 AND started_at < $3
		ORDER BY started_at DESC
		LIMIT $4 OFFSET $5`
	rows, err := r.pool.Query(ctx, q, userID, from, to, page.Limit, page.Offset)
	if err != nil {
		return nil, 0, mapErr(err)
	}
	defer rows.Close()
	var out []domain.PomodoroSession
	for rows.Next() {
		var s domain.PomodoroSession
		if err := rows.Scan(&s.ID, &s.UserID, &s.TaskID, &s.Kind, &s.PlannedDurationSec,
			&s.ActualDurationSec, &s.CycleIndex, &s.StartedAt, &s.EndedAt, &s.Status, &s.CreatedAt); err != nil {
			return nil, 0, mapErr(err)
		}
		out = append(out, s)
	}
	return out, total, nil
}

func (r *PomodoroRepo) ListByTask(ctx context.Context, userID, taskID uuid.UUID) ([]domain.PomodoroSession, error) {
	const q = `
		SELECT id, user_id, task_id, kind, planned_duration_sec, actual_duration_sec,
		       cycle_index, started_at, ended_at, status, created_at
		FROM pomodoro_sessions
		WHERE user_id = $1 AND task_id = $2
		ORDER BY started_at DESC`
	rows, err := r.pool.Query(ctx, q, userID, taskID)
	if err != nil {
		return nil, mapErr(err)
	}
	defer rows.Close()
	var out []domain.PomodoroSession
	for rows.Next() {
		var s domain.PomodoroSession
		if err := rows.Scan(&s.ID, &s.UserID, &s.TaskID, &s.Kind, &s.PlannedDurationSec,
			&s.ActualDurationSec, &s.CycleIndex, &s.StartedAt, &s.EndedAt, &s.Status, &s.CreatedAt); err != nil {
			return nil, mapErr(err)
		}
		out = append(out, s)
	}
	return out, nil
}

// Stats calcola il riepilogo dei pomodori focus completati nel range [from, to).
func (r *PomodoroRepo) Stats(ctx context.Context, userID uuid.UUID, from, to time.Time) (*domain.PomodoroStats, error) {
	stats := &domain.PomodoroStats{
		ByDay:  map[string]int{},
		ByTask: map[string]int{},
	}

	// totali
	if err := r.pool.QueryRow(ctx,
		`SELECT COUNT(*), COALESCE(SUM(actual_duration_sec),0)
		 FROM pomodoro_sessions
		 WHERE user_id = $1 AND kind = 'focus' AND status = 'completed'
		   AND started_at >= $2 AND started_at < $3`,
		userID, from, to,
	).Scan(&stats.PomodorosCompleted, &stats.FocusSeconds); err != nil {
		return nil, mapErr(err)
	}

	// raggruppamento per giorno (data ISO YYYY-MM-DD nello UTC)
	dayRows, err := r.pool.Query(ctx, `
		SELECT to_char(date_trunc('day', started_at), 'YYYY-MM-DD') AS day, COUNT(*)
		FROM pomodoro_sessions
		WHERE user_id = $1 AND kind = 'focus' AND status = 'completed'
		  AND started_at >= $2 AND started_at < $3
		GROUP BY day
		ORDER BY day`, userID, from, to)
	if err != nil {
		return nil, mapErr(err)
	}
	for dayRows.Next() {
		var day string
		var n int
		if err := dayRows.Scan(&day, &n); err != nil {
			dayRows.Close()
			return nil, mapErr(err)
		}
		stats.ByDay[day] = n
	}
	dayRows.Close()

	// raggruppamento per task
	taskRows, err := r.pool.Query(ctx, `
		SELECT task_id::text, COALESCE(SUM(actual_duration_sec),0)
		FROM pomodoro_sessions
		WHERE user_id = $1 AND task_id IS NOT NULL
		  AND kind = 'focus' AND status = 'completed'
		  AND started_at >= $2 AND started_at < $3
		GROUP BY task_id`, userID, from, to)
	if err != nil {
		return nil, mapErr(err)
	}
	defer taskRows.Close()
	for taskRows.Next() {
		var tid string
		var sec int
		if err := taskRows.Scan(&tid, &sec); err != nil {
			return nil, mapErr(err)
		}
		stats.ByTask[tid] = sec
	}
	return stats, nil
}
