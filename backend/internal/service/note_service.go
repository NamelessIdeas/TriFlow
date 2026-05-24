package service

import (
	"context"
	"strings"

	"github.com/google/uuid"
	"github.com/triflow/backend/internal/domain"
)

type NoteService struct {
	notes domain.NoteRepository
}

func NewNoteService(n domain.NoteRepository) *NoteService { return &NoteService{notes: n} }

type CreateNoteInput struct {
	Title        string
	ContentMD    string
	PARACategory string
	Tags         []string
}

func (s *NoteService) Create(ctx context.Context, userID uuid.UUID, in CreateNoteInput) (*domain.Note, error) {
	if strings.TrimSpace(in.Title) == "" {
		return nil, domain.ErrInvalidInput
	}
	if in.PARACategory == "" {
		in.PARACategory = domain.PARAResource
	}
	n := &domain.Note{
		ID: uuid.New(), UserID: userID,
		Title: in.Title, ContentMD: in.ContentMD,
		PARACategory: in.PARACategory,
		Tags:         in.Tags,
	}
	if err := s.notes.Create(ctx, n); err != nil {
		return nil, err
	}
	if len(in.Tags) > 0 {
		if err := s.notes.ReplaceTags(ctx, n.ID, in.Tags); err != nil {
			return nil, err
		}
	}
	return n, nil
}

type UpdateNoteInput struct {
	Title        *string
	ContentMD    *string
	PARACategory *string
	Tags         *[]string
}

func (s *NoteService) Update(ctx context.Context, userID, id uuid.UUID, in UpdateNoteInput) (*domain.Note, error) {
	n, err := s.notes.GetByID(ctx, userID, id)
	if err != nil {
		return nil, err
	}
	if in.Title != nil {
		n.Title = *in.Title
	}
	if in.ContentMD != nil {
		n.ContentMD = *in.ContentMD
	}
	if in.PARACategory != nil {
		n.PARACategory = *in.PARACategory
	}
	if err := s.notes.Update(ctx, n); err != nil {
		return nil, err
	}
	if in.Tags != nil {
		if err := s.notes.ReplaceTags(ctx, n.ID, *in.Tags); err != nil {
			return nil, err
		}
		n.Tags = *in.Tags
	}
	return n, nil
}

func (s *NoteService) Get(ctx context.Context, userID, id uuid.UUID) (*domain.Note, error) {
	return s.notes.GetByID(ctx, userID, id)
}

func (s *NoteService) Delete(ctx context.Context, userID, id uuid.UUID) error {
	return s.notes.Delete(ctx, userID, id)
}

func (s *NoteService) List(ctx context.Context, userID uuid.UUID, f domain.NoteFilter) ([]domain.Note, int, error) {
	return s.notes.List(ctx, userID, f)
}

// Backlinks restituisce le note che linkano la nota target.
func (s *NoteService) Backlinks(ctx context.Context, userID, id uuid.UUID) ([]domain.Note, error) {
	if _, err := s.notes.GetByID(ctx, userID, id); err != nil {
		return nil, err
	}
	return s.notes.Backlinks(ctx, userID, id)
}

// Outgoing restituisce le note linkate dalla nota source.
func (s *NoteService) Outgoing(ctx context.Context, userID, id uuid.UUID) ([]domain.Note, error) {
	if _, err := s.notes.GetByID(ctx, userID, id); err != nil {
		return nil, err
	}
	return s.notes.OutgoingLinks(ctx, userID, id)
}

func (s *NoteService) Link(ctx context.Context, userID, source, target uuid.UUID) error {
	if source == target {
		return domain.ErrInvalidInput
	}
	return s.notes.AddLink(ctx, userID, source, target)
}

func (s *NoteService) Unlink(ctx context.Context, userID, source, target uuid.UUID) error {
	return s.notes.RemoveLink(ctx, userID, source, target)
}

func (s *NoteService) AttachRef(ctx context.Context, userID, noteID uuid.UUID, refType string, refID uuid.UUID) error {
	if refType != domain.NoteRefTask && refType != domain.NoteRefProject {
		return domain.ErrInvalidInput
	}
	return s.notes.AddRef(ctx, userID, noteID, refType, refID)
}

func (s *NoteService) DetachRef(ctx context.Context, userID, noteID uuid.UUID, refType string, refID uuid.UUID) error {
	return s.notes.RemoveRef(ctx, userID, noteID, refType, refID)
}

func (s *NoteService) NotesForRef(ctx context.Context, userID uuid.UUID, refType string, refID uuid.UUID) ([]domain.Note, error) {
	return s.notes.NotesForRef(ctx, userID, refType, refID)
}
