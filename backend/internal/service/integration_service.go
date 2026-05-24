package service

import (
	"context"
	"errors"
	"time"

	"github.com/google/uuid"
	"github.com/triflow/backend/internal/domain"
)

// IntegrationService implementa la "collaborazione tra metodi": è qui che
// GTD, Pomodoro e Second Brain si parlano tra loro.
type IntegrationService struct {
	tasks    domain.TaskRepository
	projects domain.ProjectRepository
	notes    domain.NoteRepository
	sessions domain.PomodoroSessionRepository
	timers   domain.ActiveTimerStore
	now      func() time.Time
}

func NewIntegrationService(
	tasks domain.TaskRepository,
	projects domain.ProjectRepository,
	notes domain.NoteRepository,
	sessions domain.PomodoroSessionRepository,
	timers domain.ActiveTimerStore,
) *IntegrationService {
	return &IntegrationService{
		tasks: tasks, projects: projects, notes: notes,
		sessions: sessions, timers: timers, now: time.Now,
	}
}

// PromoteNoteToTask crea una task GTD partendo da una nota e crea il ref
// bidirezionale nota↔task (la nota resta intatta).
type PromoteNoteInput struct {
	NoteID    uuid.UUID
	Title     string
	ProjectID *uuid.UUID
	ContextID *uuid.UUID
	Status    string
	DueDate   *time.Time
	Priority  int
}

func (s *IntegrationService) PromoteNoteToTask(ctx context.Context, userID uuid.UUID, in PromoteNoteInput) (*domain.Task, error) {
	n, err := s.notes.GetByID(ctx, userID, in.NoteID)
	if err != nil {
		return nil, err
	}
	title := in.Title
	if title == "" {
		title = n.Title
	}
	status := in.Status
	if status == "" {
		status = domain.TaskStatusNextAction
	}
	t := &domain.Task{
		ID:        uuid.New(),
		UserID:    userID,
		ProjectID: in.ProjectID,
		ContextID: in.ContextID,
		Title:     title,
		Notes:     n.ContentMD,
		Status:    status,
		Priority:  in.Priority,
		DueDate:   in.DueDate,
	}
	if err := s.tasks.Create(ctx, t); err != nil {
		return nil, err
	}
	if err := s.notes.AddRef(ctx, userID, n.ID, domain.NoteRefTask, t.ID); err != nil {
		return nil, err
	}
	return t, nil
}

// TaskContext aggrega una task con le sue sessioni Pomodoro e le note collegate.
type TaskContext struct {
	Task     *domain.Task              `json:"task"`
	Sessions []domain.PomodoroSession  `json:"pomodoro_sessions"`
	Notes    []domain.Note             `json:"linked_notes"`
}

func (s *IntegrationService) TaskFullContext(ctx context.Context, userID, taskID uuid.UUID) (*TaskContext, error) {
	t, err := s.tasks.GetByID(ctx, userID, taskID)
	if err != nil {
		return nil, err
	}
	sessions, err := s.sessions.ListByTask(ctx, userID, taskID)
	if err != nil {
		return nil, err
	}
	notes, err := s.notes.NotesForRef(ctx, userID, domain.NoteRefTask, taskID)
	if err != nil {
		return nil, err
	}
	return &TaskContext{Task: t, Sessions: sessions, Notes: notes}, nil
}

// Dashboard è la vista unificata richiesta dal frontend.
func (s *IntegrationService) Dashboard(ctx context.Context, userID uuid.UUID) (*domain.Dashboard, error) {
	now := s.now()

	today, err := s.tasks.TodayList(ctx, userID, now)
	if err != nil {
		return nil, err
	}

	var active *domain.ActiveTimer
	if t, err := s.timers.Get(ctx, userID); err == nil {
		active = t
	} else if !errors.Is(err, domain.ErrTimerNotFound) {
		return nil, err
	}

	recent, err := s.notes.ListRecent(ctx, userID, 5)
	if err != nil {
		return nil, err
	}

	dayStart := time.Date(now.Year(), now.Month(), now.Day(), 0, 0, 0, 0, now.Location())
	weekStart := dayStart.AddDate(0, 0, -6)
	stats, err := s.sessions.Stats(ctx, userID, weekStart, now)
	if err != nil {
		return nil, err
	}

	pomodorosToday := 0
	if v, ok := stats.ByDay[dayStart.Format("2006-01-02")]; ok {
		pomodorosToday = v
	}

	return &domain.Dashboard{
		TodayTasks:       today,
		ActiveTimer:      active,
		RecentNotes:      recent,
		PomodorosToday:   pomodorosToday,
		FocusSecondsWeek: stats.FocusSeconds,
		GeneratedAt:      now,
	}, nil
}
