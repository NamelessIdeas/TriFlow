package domain

import (
	"context"
	"time"

	"github.com/google/uuid"
)

const (
	PomodoroKindFocus      = "focus"
	PomodoroKindShortBreak = "short_break"
	PomodoroKindLongBreak  = "long_break"

	PomodoroStatusCompleted = "completed"
	PomodoroStatusAborted   = "aborted"
)

// PomodoroSession è una sessione COMPLETATA persistita su PostgreSQL.
type PomodoroSession struct {
	ID                 uuid.UUID  `json:"id"`
	UserID             uuid.UUID  `json:"user_id"`
	TaskID             *uuid.UUID `json:"task_id,omitempty"`
	Kind               string     `json:"kind"`
	PlannedDurationSec int        `json:"planned_duration_sec"`
	ActualDurationSec  int        `json:"actual_duration_sec"`
	CycleIndex         int        `json:"cycle_index"`
	StartedAt          time.Time  `json:"started_at"`
	EndedAt            time.Time  `json:"ended_at"`
	Status             string     `json:"status"`
	CreatedAt          time.Time  `json:"created_at"`
}

// ActiveTimer è lo stato volatile in Redis del timer in corso.
type ActiveTimer struct {
	UserID             uuid.UUID  `json:"user_id"`
	TaskID             *uuid.UUID `json:"task_id,omitempty"`
	Kind               string     `json:"kind"`
	PlannedDurationSec int        `json:"planned_duration_sec"`
	CycleIndex         int        `json:"cycle_index"`
	StartedAt          time.Time  `json:"started_at"`
	// PausedAt è nil se il timer è running, valorizzato se è in pausa.
	PausedAt *time.Time `json:"paused_at,omitempty"`
	// ElapsedBeforePauseSec accumula il tempo trascorso prima della pausa.
	ElapsedBeforePauseSec int `json:"elapsed_before_pause_sec"`
}

// IsRunning indica se il timer è attivo (non in pausa).
func (t ActiveTimer) IsRunning() bool { return t.PausedAt == nil }

// ElapsedSec calcola i secondi totali di lavoro effettivo alla data 'now'.
func (t ActiveTimer) ElapsedSec(now time.Time) int {
	if t.PausedAt != nil {
		return t.ElapsedBeforePauseSec
	}
	return t.ElapsedBeforePauseSec + int(now.Sub(t.StartedAt).Seconds())
}

// RemainingSec calcola i secondi residui rispetto alla durata pianificata.
func (t ActiveTimer) RemainingSec(now time.Time) int {
	rem := t.PlannedDurationSec - t.ElapsedSec(now)
	if rem < 0 {
		return 0
	}
	return rem
}

// PomodoroStats è il riepilogo per un dato range temporale.
type PomodoroStats struct {
	PomodorosCompleted int            `json:"pomodoros_completed"`
	FocusSeconds       int            `json:"focus_seconds"`
	ByDay              map[string]int `json:"by_day"`
	ByTask             map[string]int `json:"by_task"`
}

type PomodoroSessionRepository interface {
	Create(ctx context.Context, s *PomodoroSession) error
	ListByUser(ctx context.Context, userID uuid.UUID, from, to time.Time, page Page) ([]PomodoroSession, int, error)
	ListByTask(ctx context.Context, userID, taskID uuid.UUID) ([]PomodoroSession, error)
	Stats(ctx context.Context, userID uuid.UUID, from, to time.Time) (*PomodoroStats, error)
}

// ActiveTimerStore gestisce il timer attivo in Redis con TTL.
type ActiveTimerStore interface {
	Set(ctx context.Context, t *ActiveTimer, ttl time.Duration) error
	Get(ctx context.Context, userID uuid.UUID) (*ActiveTimer, error)
	Delete(ctx context.Context, userID uuid.UUID) error
}
