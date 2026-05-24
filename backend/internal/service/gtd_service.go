package service

import (
	"context"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/triflow/backend/internal/domain"
)

type GTDService struct {
	tasks    domain.TaskRepository
	projects domain.ProjectRepository
	inbox    domain.InboxRepository
	contexts domain.ContextRepository
	now      func() time.Time
}

func NewGTDService(
	tasks domain.TaskRepository,
	projects domain.ProjectRepository,
	inbox domain.InboxRepository,
	contexts domain.ContextRepository,
) *GTDService {
	return &GTDService{tasks: tasks, projects: projects, inbox: inbox, contexts: contexts, now: time.Now}
}

// ----- Inbox -----

func (s *GTDService) CaptureInbox(ctx context.Context, userID uuid.UUID, rawText string) (*domain.InboxItem, error) {
	rawText = strings.TrimSpace(rawText)
	if rawText == "" {
		return nil, domain.ErrInvalidInput
	}
	i := &domain.InboxItem{ID: uuid.New(), UserID: userID, RawText: rawText}
	if err := s.inbox.Create(ctx, i); err != nil {
		return nil, err
	}
	return i, nil
}

func (s *GTDService) ListInbox(ctx context.Context, userID uuid.UUID, page domain.Page) ([]domain.InboxItem, int, error) {
	return s.inbox.ListUnprocessed(ctx, userID, page)
}

// ProcessInboxInput è il payload per trasformare un inbox item in task o project.
type ProcessInboxInput struct {
	// "task" | "project" | "discard"
	Action     string
	Title      string
	Notes      string
	Status     string
	ContextID  *uuid.UUID
	ProjectID  *uuid.UUID
	Energy     *string
	DueDate    *time.Time
	Priority   int
	Tags       []string
}

type ProcessInboxResult struct {
	Task    *domain.Task    `json:"task,omitempty"`
	Project *domain.Project `json:"project,omitempty"`
}

// ProcessInbox è l'endpoint che cattura il flusso GTD del "processing": decide
// cosa fare di un inbox item (azionarlo come task, promuoverlo a project, o scartarlo).
func (s *GTDService) ProcessInbox(ctx context.Context, userID, inboxID uuid.UUID, in ProcessInboxInput) (*ProcessInboxResult, error) {
	item, err := s.inbox.GetByID(ctx, userID, inboxID)
	if err != nil {
		return nil, err
	}
	if item.ProcessedAt != nil {
		return nil, domain.ErrConflict
	}

	title := strings.TrimSpace(in.Title)
	if title == "" {
		title = item.RawText
	}

	res := &ProcessInboxResult{}
	switch in.Action {
	case "task":
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
			Notes:     in.Notes,
			Status:    status,
			Energy:    in.Energy,
			Priority:  in.Priority,
			DueDate:   in.DueDate,
		}
		if err := s.tasks.Create(ctx, t); err != nil {
			return nil, err
		}
		if len(in.Tags) > 0 {
			if err := s.tasks.ReplaceTags(ctx, t.ID, in.Tags); err != nil {
				return nil, err
			}
			t.Tags = in.Tags
		}
		res.Task = t

	case "project":
		p := &domain.Project{
			ID:          uuid.New(),
			UserID:      userID,
			Title:       title,
			Description: in.Notes,
			Status:      domain.ProjectStatusActive,
		}
		if err := s.projects.Create(ctx, p); err != nil {
			return nil, err
		}
		res.Project = p

	case "discard":
		// nessuna entità creata, solo marca processato

	default:
		return nil, domain.ErrInvalidInput
	}

	if err := s.inbox.MarkProcessed(ctx, userID, inboxID); err != nil {
		return nil, err
	}
	return res, nil
}

// ----- Projects -----

func (s *GTDService) CreateProject(ctx context.Context, userID uuid.UUID, title, description, status string) (*domain.Project, error) {
	title = strings.TrimSpace(title)
	if title == "" {
		return nil, domain.ErrInvalidInput
	}
	if status == "" {
		status = domain.ProjectStatusActive
	}
	p := &domain.Project{
		ID: uuid.New(), UserID: userID, Title: title, Description: description, Status: status,
	}
	if err := s.projects.Create(ctx, p); err != nil {
		return nil, err
	}
	return p, nil
}

func (s *GTDService) GetProject(ctx context.Context, userID, id uuid.UUID) (*domain.Project, error) {
	return s.projects.GetByID(ctx, userID, id)
}

func (s *GTDService) UpdateProject(ctx context.Context, p *domain.Project) error {
	return s.projects.Update(ctx, p)
}

func (s *GTDService) DeleteProject(ctx context.Context, userID, id uuid.UUID) error {
	return s.projects.Delete(ctx, userID, id)
}

func (s *GTDService) ListProjects(ctx context.Context, userID uuid.UUID, status *string, page domain.Page) ([]domain.Project, int, error) {
	return s.projects.List(ctx, userID, status, page)
}

// ----- Contexts -----

func (s *GTDService) CreateContext(ctx context.Context, userID uuid.UUID, name string) (*domain.Context, error) {
	name = strings.TrimSpace(name)
	if name == "" {
		return nil, domain.ErrInvalidInput
	}
	c := &domain.Context{ID: uuid.New(), UserID: userID, Name: name}
	if err := s.contexts.Create(ctx, c); err != nil {
		return nil, err
	}
	return c, nil
}

func (s *GTDService) ListContexts(ctx context.Context, userID uuid.UUID) ([]domain.Context, error) {
	return s.contexts.List(ctx, userID)
}

func (s *GTDService) DeleteContext(ctx context.Context, userID, id uuid.UUID) error {
	return s.contexts.Delete(ctx, userID, id)
}

// ----- Tasks -----

type CreateTaskInput struct {
	Title            string
	Notes            string
	ProjectID        *uuid.UUID
	ContextID        *uuid.UUID
	Status           string
	Energy           *string
	EstimatedMinutes *int
	Priority         int
	DueDate          *time.Time
	DeferDate        *time.Time
	Tags             []string
}

func (s *GTDService) CreateTask(ctx context.Context, userID uuid.UUID, in CreateTaskInput) (*domain.Task, error) {
	if strings.TrimSpace(in.Title) == "" {
		return nil, domain.ErrInvalidInput
	}
	if in.Status == "" {
		in.Status = domain.TaskStatusNextAction
	}
	t := &domain.Task{
		ID:               uuid.New(),
		UserID:           userID,
		ProjectID:        in.ProjectID,
		ContextID:        in.ContextID,
		Title:            in.Title,
		Notes:            in.Notes,
		Status:           in.Status,
		Energy:           in.Energy,
		EstimatedMinutes: in.EstimatedMinutes,
		Priority:         in.Priority,
		DueDate:          in.DueDate,
		DeferDate:        in.DeferDate,
		Tags:             in.Tags,
	}
	if err := s.tasks.Create(ctx, t); err != nil {
		return nil, err
	}
	if len(in.Tags) > 0 {
		if err := s.tasks.ReplaceTags(ctx, t.ID, in.Tags); err != nil {
			return nil, err
		}
	}
	return t, nil
}

type UpdateTaskInput struct {
	Title            *string
	Notes            *string
	ProjectID        *uuid.UUID
	ContextID        *uuid.UUID
	Status           *string
	Energy           *string
	EstimatedMinutes *int
	Priority         *int
	DueDate          *time.Time
	DeferDate        *time.Time
	Tags             *[]string
}

func (s *GTDService) UpdateTask(ctx context.Context, userID, id uuid.UUID, in UpdateTaskInput) (*domain.Task, error) {
	t, err := s.tasks.GetByID(ctx, userID, id)
	if err != nil {
		return nil, err
	}
	if in.Title != nil {
		t.Title = *in.Title
	}
	if in.Notes != nil {
		t.Notes = *in.Notes
	}
	if in.ProjectID != nil {
		t.ProjectID = in.ProjectID
	}
	if in.ContextID != nil {
		t.ContextID = in.ContextID
	}
	if in.Status != nil {
		t.Status = *in.Status
	}
	if in.Energy != nil {
		t.Energy = in.Energy
	}
	if in.EstimatedMinutes != nil {
		t.EstimatedMinutes = in.EstimatedMinutes
	}
	if in.Priority != nil {
		t.Priority = *in.Priority
	}
	if in.DueDate != nil {
		t.DueDate = in.DueDate
	}
	if in.DeferDate != nil {
		t.DeferDate = in.DeferDate
	}
	if err := s.tasks.Update(ctx, t); err != nil {
		return nil, err
	}
	if in.Tags != nil {
		if err := s.tasks.ReplaceTags(ctx, t.ID, *in.Tags); err != nil {
			return nil, err
		}
		t.Tags = *in.Tags
	}
	return t, nil
}

func (s *GTDService) GetTask(ctx context.Context, userID, id uuid.UUID) (*domain.Task, error) {
	return s.tasks.GetByID(ctx, userID, id)
}

func (s *GTDService) DeleteTask(ctx context.Context, userID, id uuid.UUID) error {
	return s.tasks.Delete(ctx, userID, id)
}

func (s *GTDService) ListTasks(ctx context.Context, userID uuid.UUID, f domain.TaskFilter) ([]domain.Task, int, error) {
	return s.tasks.List(ctx, userID, f)
}

// ----- Weekly review -----

func (s *GTDService) WeeklyReview(ctx context.Context, userID uuid.UUID) (*domain.WeeklyReview, error) {
	inbox, _, err := s.inbox.ListUnprocessed(ctx, userID, domain.Page{Limit: 100})
	if err != nil {
		return nil, err
	}
	waiting, err := s.tasks.ListByStatus(ctx, userID, domain.TaskStatusWaiting)
	if err != nil {
		return nil, err
	}
	projects, err := s.projects.ProjectsWithoutNextAction(ctx, userID)
	if err != nil {
		return nil, err
	}
	return &domain.WeeklyReview{
		InboxToProcess:      inbox,
		WaitingTasks:        waiting,
		ProjectsWithoutNext: projects,
		GeneratedAt:         s.now(),
	}, nil
}
