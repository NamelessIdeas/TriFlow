package domain

import (
	"context"
	"time"

	"github.com/google/uuid"
)

type User struct {
	ID           uuid.UUID `json:"id"`
	Email        string    `json:"email"`
	DisplayName  string    `json:"display_name"`
	PasswordHash string    `json:"-"`
	CreatedAt    time.Time `json:"created_at"`
	UpdatedAt    time.Time `json:"updated_at"`
}

type UserPreferences struct {
	UserID                  uuid.UUID `json:"user_id"`
	PomodoroDurationMin     int       `json:"pomodoro_duration_min"`
	ShortBreakMin           int       `json:"short_break_min"`
	LongBreakMin            int       `json:"long_break_min"`
	PomodorosUntilLongBreak int       `json:"pomodoros_until_long_break"`
	Timezone                string    `json:"timezone"`
	UpdatedAt               time.Time `json:"updated_at"`
}

type RefreshToken struct {
	JTI       uuid.UUID
	UserID    uuid.UUID
	ExpiresAt time.Time
	RevokedAt *time.Time
	CreatedAt time.Time
}

type UserRepository interface {
	Create(ctx context.Context, u *User) error
	GetByID(ctx context.Context, id uuid.UUID) (*User, error)
	GetByEmail(ctx context.Context, email string) (*User, error)
	UpdateProfile(ctx context.Context, id uuid.UUID, displayName string) (*User, error)

	GetPreferences(ctx context.Context, userID uuid.UUID) (*UserPreferences, error)
	UpsertPreferences(ctx context.Context, p *UserPreferences) error
}

type RefreshTokenRepository interface {
	Create(ctx context.Context, t *RefreshToken) error
	Get(ctx context.Context, jti uuid.UUID) (*RefreshToken, error)
	Revoke(ctx context.Context, jti uuid.UUID) error
	RevokeAllForUser(ctx context.Context, userID uuid.UUID) error
}
