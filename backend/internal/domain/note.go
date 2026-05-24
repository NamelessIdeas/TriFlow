package domain

import (
	"context"
	"time"

	"github.com/google/uuid"
)

const (
	PARAProject  = "project"
	PARAArea     = "area"
	PARAResource = "resource"
	PARAArchive  = "archive"

	NoteRefTask    = "task"
	NoteRefProject = "project"
)

type Note struct {
	ID           uuid.UUID `json:"id"`
	UserID       uuid.UUID `json:"user_id"`
	Title        string    `json:"title"`
	ContentMD    string    `json:"content_md"`
	PARACategory string    `json:"para_category"`
	Tags         []string  `json:"tags"`
	CreatedAt    time.Time `json:"created_at"`
	UpdatedAt    time.Time `json:"updated_at"`
}

// NoteLink rappresenta un link bidirezionale tra note (memorizzato solo
// come direzione source→target; i backlink si ottengono invertendo la query).
type NoteLink struct {
	SourceNoteID uuid.UUID `json:"source_note_id"`
	TargetNoteID uuid.UUID `json:"target_note_id"`
	CreatedAt    time.Time `json:"created_at"`
}

// NoteRef collega una nota a un'entità GTD (task o project).
type NoteRef struct {
	ID        uuid.UUID `json:"id"`
	NoteID    uuid.UUID `json:"note_id"`
	RefType   string    `json:"ref_type"`
	RefID     uuid.UUID `json:"ref_id"`
	CreatedAt time.Time `json:"created_at"`
}

type NoteFilter struct {
	PARACategory *string
	Tag          *string
	Search       *string
	Page         Page
}

type NoteRepository interface {
	Create(ctx context.Context, n *Note) error
	GetByID(ctx context.Context, userID, id uuid.UUID) (*Note, error)
	Update(ctx context.Context, n *Note) error
	Delete(ctx context.Context, userID, id uuid.UUID) error
	List(ctx context.Context, userID uuid.UUID, f NoteFilter) ([]Note, int, error)
	ListRecent(ctx context.Context, userID uuid.UUID, limit int) ([]Note, error)
	ReplaceTags(ctx context.Context, noteID uuid.UUID, tags []string) error

	// Link nota↔nota
	AddLink(ctx context.Context, userID, source, target uuid.UUID) error
	RemoveLink(ctx context.Context, userID, source, target uuid.UUID) error
	OutgoingLinks(ctx context.Context, userID, id uuid.UUID) ([]Note, error)
	Backlinks(ctx context.Context, userID, id uuid.UUID) ([]Note, error)

	// Link nota↔GTD
	AddRef(ctx context.Context, userID, noteID uuid.UUID, refType string, refID uuid.UUID) error
	RemoveRef(ctx context.Context, userID, noteID uuid.UUID, refType string, refID uuid.UUID) error
	NotesForRef(ctx context.Context, userID uuid.UUID, refType string, refID uuid.UUID) ([]Note, error)
}
