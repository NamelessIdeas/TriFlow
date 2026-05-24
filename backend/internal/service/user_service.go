package service

import (
	"context"

	"github.com/google/uuid"
	"github.com/triflow/backend/internal/domain"
)

type UserService struct{ users domain.UserRepository }

func NewUserService(u domain.UserRepository) *UserService { return &UserService{users: u} }

func (s *UserService) Me(ctx context.Context, userID uuid.UUID) (*domain.User, error) {
	return s.users.GetByID(ctx, userID)
}

func (s *UserService) UpdateProfile(ctx context.Context, userID uuid.UUID, displayName string) (*domain.User, error) {
	return s.users.UpdateProfile(ctx, userID, displayName)
}

func (s *UserService) GetPreferences(ctx context.Context, userID uuid.UUID) (*domain.UserPreferences, error) {
	return s.users.GetPreferences(ctx, userID)
}

func (s *UserService) UpdatePreferences(ctx context.Context, p *domain.UserPreferences) error {
	if p.PomodoroDurationMin < 1 || p.PomodoroDurationMin > 180 {
		return domain.ErrInvalidInput
	}
	if p.ShortBreakMin < 1 || p.ShortBreakMin > 60 {
		return domain.ErrInvalidInput
	}
	if p.LongBreakMin < 1 || p.LongBreakMin > 120 {
		return domain.ErrInvalidInput
	}
	if p.PomodorosUntilLongBreak < 2 || p.PomodorosUntilLongBreak > 12 {
		return domain.ErrInvalidInput
	}
	if p.Timezone == "" {
		p.Timezone = "UTC"
	}
	return s.users.UpsertPreferences(ctx, p)
}
