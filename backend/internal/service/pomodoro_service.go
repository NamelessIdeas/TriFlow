package service

import (
	"context"
	"errors"
	"time"

	"github.com/google/uuid"
	"github.com/triflow/backend/internal/domain"
)

// PomodoroService gestisce il ciclo di vita di un pomodoro:
//  1. Start: salva ActiveTimer su Redis con TTL = durata.
//  2. Pause/Resume: aggiorna ActiveTimer accumulando tempo trascorso.
//  3. Complete/Abort: persiste su PostgreSQL e svuota Redis.
//
// La verità sul "tempo passato" è ricavata dai timestamp salvati, non da un
// loop server-side. Il client può ricostruire lo stato in qualsiasi momento.
type PomodoroService struct {
	sessions domain.PomodoroSessionRepository
	state    domain.ActiveTimerStore
	users    domain.UserRepository
	now      func() time.Time
}

func NewPomodoroService(
	s domain.PomodoroSessionRepository,
	state domain.ActiveTimerStore,
	users domain.UserRepository,
) *PomodoroService {
	return &PomodoroService{sessions: s, state: state, users: users, now: time.Now}
}

// StartInput contiene i parametri di start. Se Kind è vuoto si usa "focus".
type StartInput struct {
	TaskID     *uuid.UUID
	Kind       string
	CycleIndex int
	// DurationSec opzionale: se 0, viene dedotto dalle preferenze utente.
	DurationSec int
}

// Start avvia un timer; se ne esiste uno attivo torna ErrTimerActive.
func (s *PomodoroService) Start(ctx context.Context, userID uuid.UUID, in StartInput) (*domain.ActiveTimer, error) {
	if existing, err := s.state.Get(ctx, userID); err == nil && existing != nil {
		return nil, domain.ErrTimerActive
	} else if err != nil && !errors.Is(err, domain.ErrTimerNotFound) {
		return nil, err
	}

	if in.Kind == "" {
		in.Kind = domain.PomodoroKindFocus
	}
	if in.CycleIndex <= 0 {
		in.CycleIndex = 1
	}

	if in.DurationSec <= 0 {
		prefs, err := s.users.GetPreferences(ctx, userID)
		if err != nil {
			return nil, err
		}
		switch in.Kind {
		case domain.PomodoroKindShortBreak:
			in.DurationSec = prefs.ShortBreakMin * 60
		case domain.PomodoroKindLongBreak:
			in.DurationSec = prefs.LongBreakMin * 60
		default:
			in.DurationSec = prefs.PomodoroDurationMin * 60
		}
	}

	now := s.now()
	timer := &domain.ActiveTimer{
		UserID:             userID,
		TaskID:             in.TaskID,
		Kind:               in.Kind,
		PlannedDurationSec: in.DurationSec,
		CycleIndex:         in.CycleIndex,
		StartedAt:          now,
	}
	// TTL = durata + un margine per non perdere lo stato prima del client
	ttl := time.Duration(in.DurationSec)*time.Second + 5*time.Minute
	if err := s.state.Set(ctx, timer, ttl); err != nil {
		return nil, err
	}
	return timer, nil
}

// Pause mette in pausa il timer attivo.
func (s *PomodoroService) Pause(ctx context.Context, userID uuid.UUID) (*domain.ActiveTimer, error) {
	t, err := s.state.Get(ctx, userID)
	if err != nil {
		return nil, err
	}
	if !t.IsRunning() {
		return t, nil
	}
	now := s.now()
	t.ElapsedBeforePauseSec += int(now.Sub(t.StartedAt).Seconds())
	t.PausedAt = &now
	if err := s.state.Set(ctx, t, residualTTL(t)); err != nil {
		return nil, err
	}
	return t, nil
}

// Resume riprende un timer in pausa.
func (s *PomodoroService) Resume(ctx context.Context, userID uuid.UUID) (*domain.ActiveTimer, error) {
	t, err := s.state.Get(ctx, userID)
	if err != nil {
		return nil, err
	}
	if t.IsRunning() {
		return t, nil
	}
	t.PausedAt = nil
	t.StartedAt = s.now()
	if err := s.state.Set(ctx, t, residualTTL(t)); err != nil {
		return nil, err
	}
	return t, nil
}

// Current restituisce lo stato del timer attivo (o ErrTimerNotFound).
func (s *PomodoroService) Current(ctx context.Context, userID uuid.UUID) (*domain.ActiveTimer, error) {
	return s.state.Get(ctx, userID)
}

// Complete persiste la sessione e svuota lo stato Redis.
// Status sarà 'completed' se viene chiamato dopo il termine, 'aborted' se prima.
func (s *PomodoroService) Complete(ctx context.Context, userID uuid.UUID) (*domain.PomodoroSession, error) {
	return s.finalize(ctx, userID, domain.PomodoroStatusCompleted)
}

func (s *PomodoroService) Abort(ctx context.Context, userID uuid.UUID) (*domain.PomodoroSession, error) {
	return s.finalize(ctx, userID, domain.PomodoroStatusAborted)
}

func (s *PomodoroService) finalize(ctx context.Context, userID uuid.UUID, status string) (*domain.PomodoroSession, error) {
	t, err := s.state.Get(ctx, userID)
	if err != nil {
		return nil, err
	}
	now := s.now()
	elapsed := t.ElapsedSec(now)
	if elapsed > t.PlannedDurationSec {
		elapsed = t.PlannedDurationSec
	}
	sess := &domain.PomodoroSession{
		ID:                 uuid.New(),
		UserID:             userID,
		TaskID:             t.TaskID,
		Kind:               t.Kind,
		PlannedDurationSec: t.PlannedDurationSec,
		ActualDurationSec:  elapsed,
		CycleIndex:         t.CycleIndex,
		StartedAt:          t.StartedAt.Add(-time.Duration(t.ElapsedBeforePauseSec) * time.Second),
		EndedAt:            now,
		Status:             status,
	}
	if err := s.sessions.Create(ctx, sess); err != nil {
		return nil, err
	}
	if err := s.state.Delete(ctx, userID); err != nil {
		return nil, err
	}
	return sess, nil
}

// ListSessions paginata, filtrata per range temporale (default: ultimi 7 gg).
func (s *PomodoroService) ListSessions(ctx context.Context, userID uuid.UUID, from, to time.Time, page domain.Page) ([]domain.PomodoroSession, int, error) {
	if from.IsZero() {
		from = s.now().Add(-7 * 24 * time.Hour)
	}
	if to.IsZero() {
		to = s.now()
	}
	return s.sessions.ListByUser(ctx, userID, from, to, page)
}

func (s *PomodoroService) SessionsForTask(ctx context.Context, userID, taskID uuid.UUID) ([]domain.PomodoroSession, error) {
	return s.sessions.ListByTask(ctx, userID, taskID)
}

func (s *PomodoroService) Stats(ctx context.Context, userID uuid.UUID, from, to time.Time) (*domain.PomodoroStats, error) {
	if from.IsZero() {
		from = s.now().AddDate(0, 0, -30)
	}
	if to.IsZero() {
		to = s.now()
	}
	return s.sessions.Stats(ctx, userID, from, to)
}

func residualTTL(t *domain.ActiveTimer) time.Duration {
	rem := t.PlannedDurationSec - t.ElapsedBeforePauseSec
	if rem < 60 {
		rem = 60
	}
	return time.Duration(rem)*time.Second + 5*time.Minute
}
