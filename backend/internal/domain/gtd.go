package domain

import (
	"context"
	"time"

	"github.com/google/uuid"
)

// Status delle task GTD.
const (
	TaskStatusInbox      = "inbox"
	TaskStatusNextAction = "next_action"
	TaskStatusWaiting    = "waiting"
	TaskStatusScheduled  = "scheduled"
	TaskStatusDone       = "done"
)

const (
	ProjectStatusActive    = "active"
	ProjectStatusSomeday   = "someday"
	ProjectStatusCompleted = "completed"
)

const (
	EnergyLow    = "low"
	EnergyMedium = "medium"
	EnergyHigh   = "high"
)

type Context struct {
	ID        uuid.UUID `json:"id"`
	UserID    uuid.UUID `json:"user_id"`
	Name      string    `json:"name"`
	CreatedAt time.Time `json:"created_at"`
}

type Project struct {
	ID          uuid.UUID  `json:"id"`
	UserID      uuid.UUID  `json:"user_id"`
	Title       string     `json:"title"`
	Description string     `json:"description"`
	Status      string     `json:"status"`
	CreatedAt   time.Time  `json:"created_at"`
	UpdatedAt   time.Time  `json:"updated_at"`
	CompletedAt *time.Time `json:"completed_at,omitempty"`
}

type InboxItem struct {
	ID          uuid.UUID  `json:"id"`
	UserID      uuid.UUID  `json:"user_id"`
	RawText     string     `json:"raw_text"`
	ProcessedAt *time.Time `json:"processed_at,omitempty"`
	CreatedAt   time.Time  `json:"created_at"`
}

type Task struct {
	ID               uuid.UUID  `json:"id"`
	UserID           uuid.UUID  `json:"user_id"`
	ProjectID        *uuid.UUID `json:"project_id,omitempty"`
	ContextID        *uuid.UUID `json:"context_id,omitempty"`
	Title            string     `json:"title"`
	Notes            string     `json:"notes"`
	Status           string     `json:"status"`
	Energy           *string    `json:"energy,omitempty"`
	EstimatedMinutes *int       `json:"estimated_minutes,omitempty"`
	Priority         int        `json:"priority"`
	DueDate          *time.Time `json:"due_date,omitempty"`
	DeferDate        *time.Time `json:"defer_date,omitempty"`
	CompletedAt      *time.Time `json:"completed_at,omitempty"`
	Tags             []string   `json:"tags"`
	CreatedAt        time.Time  `json:"created_at"`
	UpdatedAt        time.Time  `json:"updated_at"`
}

// TaskFilter raccoglie i filtri opzionali della list.
type TaskFilter struct {
	Status     *string
	ProjectID  *uuid.UUID
	ContextID  *uuid.UUID
	DueBefore  *time.Time
	Tag        *string
	Page       Page
}

// WeeklyReview è il riepilogo restituito dall'endpoint di review.
type WeeklyReview struct {
	InboxToProcess         []InboxItem `json:"inbox_to_process"`
	WaitingTasks           []Task      `json:"waiting_tasks"`
	ProjectsWithoutNext    []Project   `json:"projects_without_next_action"`
	GeneratedAt            time.Time   `json:"generated_at"`
}

type ContextRepository interface {
	Create(ctx context.Context, c *Context) error
	List(ctx context.Context, userID uuid.UUID) ([]Context, error)
	Delete(ctx context.Context, userID, id uuid.UUID) error
}

type ProjectRepository interface {
	Create(ctx context.Context, p *Project) error
	GetByID(ctx context.Context, userID, id uuid.UUID) (*Project, error)
	Update(ctx context.Context, p *Project) error
	Delete(ctx context.Context, userID, id uuid.UUID) error
	List(ctx context.Context, userID uuid.UUID, status *string, page Page) ([]Project, int, error)
	ProjectsWithoutNextAction(ctx context.Context, userID uuid.UUID) ([]Project, error)
}

type InboxRepository interface {
	Create(ctx context.Context, i *InboxItem) error
	GetByID(ctx context.Context, userID, id uuid.UUID) (*InboxItem, error)
	MarkProcessed(ctx context.Context, userID, id uuid.UUID) error
	Delete(ctx context.Context, userID, id uuid.UUID) error
	ListUnprocessed(ctx context.Context, userID uuid.UUID, page Page) ([]InboxItem, int, error)
}

type TaskRepository interface {
	Create(ctx context.Context, t *Task) error
	GetByID(ctx context.Context, userID, id uuid.UUID) (*Task, error)
	Update(ctx context.Context, t *Task) error
	Delete(ctx context.Context, userID, id uuid.UUID) error
	List(ctx context.Context, userID uuid.UUID, f TaskFilter) ([]Task, int, error)
	ListByStatus(ctx context.Context, userID uuid.UUID, status string) ([]Task, error)
	TodayList(ctx context.Context, userID uuid.UUID, today time.Time) ([]Task, error)

	ReplaceTags(ctx context.Context, taskID uuid.UUID, tags []string) error
}
