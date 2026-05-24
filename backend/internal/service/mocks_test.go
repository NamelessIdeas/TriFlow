package service

import (
	"context"
	"sync"
	"time"

	"github.com/google/uuid"

	"github.com/triflow/backend/internal/domain"
)

// --- mock ActiveTimerStore (in-memory) ---

type mockTimerStore struct {
	mu     sync.Mutex
	timers map[uuid.UUID]*domain.ActiveTimer
}

func newMockTimerStore() *mockTimerStore {
	return &mockTimerStore{timers: map[uuid.UUID]*domain.ActiveTimer{}}
}

func (m *mockTimerStore) Set(_ context.Context, t *domain.ActiveTimer, _ time.Duration) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	cp := *t
	m.timers[t.UserID] = &cp
	return nil
}

func (m *mockTimerStore) Get(_ context.Context, userID uuid.UUID) (*domain.ActiveTimer, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	t, ok := m.timers[userID]
	if !ok {
		return nil, domain.ErrTimerNotFound
	}
	cp := *t
	return &cp, nil
}

func (m *mockTimerStore) Delete(_ context.Context, userID uuid.UUID) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	delete(m.timers, userID)
	return nil
}

// --- mock PomodoroSessionRepository (cattura le Create) ---

type mockSessionRepo struct {
	created []domain.PomodoroSession
}

func (m *mockSessionRepo) Create(_ context.Context, s *domain.PomodoroSession) error {
	m.created = append(m.created, *s)
	return nil
}
func (m *mockSessionRepo) ListByUser(_ context.Context, _ uuid.UUID, _, _ time.Time, _ domain.Page) ([]domain.PomodoroSession, int, error) {
	return m.created, len(m.created), nil
}
func (m *mockSessionRepo) ListByTask(_ context.Context, _ uuid.UUID, _ uuid.UUID) ([]domain.PomodoroSession, error) {
	return nil, nil
}
func (m *mockSessionRepo) Stats(_ context.Context, _ uuid.UUID, _, _ time.Time) (*domain.PomodoroStats, error) {
	return &domain.PomodoroStats{}, nil
}

// --- mock UserRepository (solo GetPreferences usato qui) ---

type mockUserRepo struct{ prefs domain.UserPreferences }

func (m *mockUserRepo) Create(context.Context, *domain.User) error                   { return nil }
func (m *mockUserRepo) GetByID(context.Context, uuid.UUID) (*domain.User, error)     { return nil, nil }
func (m *mockUserRepo) GetByEmail(context.Context, string) (*domain.User, error)     { return nil, nil }
func (m *mockUserRepo) UpdateProfile(context.Context, uuid.UUID, string) (*domain.User, error) {
	return nil, nil
}
func (m *mockUserRepo) GetPreferences(_ context.Context, _ uuid.UUID) (*domain.UserPreferences, error) {
	cp := m.prefs
	return &cp, nil
}
func (m *mockUserRepo) UpsertPreferences(context.Context, *domain.UserPreferences) error { return nil }
