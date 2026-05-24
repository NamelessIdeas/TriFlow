package postgres

import (
	"context"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/triflow/backend/internal/domain"
)

type UserRepo struct{ pool *pgxpool.Pool }

func NewUserRepo(p *pgxpool.Pool) *UserRepo { return &UserRepo{pool: p} }

func (r *UserRepo) Create(ctx context.Context, u *domain.User) error {
	const q = `
		INSERT INTO users (id, email, password_hash, display_name)
		VALUES ($1, $2, $3, $4)
		RETURNING created_at, updated_at`
	return mapErr(r.pool.QueryRow(ctx, q, u.ID, u.Email, u.PasswordHash, u.DisplayName).
		Scan(&u.CreatedAt, &u.UpdatedAt))
}

func (r *UserRepo) GetByID(ctx context.Context, id uuid.UUID) (*domain.User, error) {
	const q = `SELECT id, email, password_hash, COALESCE(display_name,''), created_at, updated_at
	           FROM users WHERE id = $1`
	u := &domain.User{}
	if err := r.pool.QueryRow(ctx, q, id).
		Scan(&u.ID, &u.Email, &u.PasswordHash, &u.DisplayName, &u.CreatedAt, &u.UpdatedAt); err != nil {
		return nil, mapErr(err)
	}
	return u, nil
}

func (r *UserRepo) GetByEmail(ctx context.Context, email string) (*domain.User, error) {
	const q = `SELECT id, email, password_hash, COALESCE(display_name,''), created_at, updated_at
	           FROM users WHERE email = $1`
	u := &domain.User{}
	if err := r.pool.QueryRow(ctx, q, email).
		Scan(&u.ID, &u.Email, &u.PasswordHash, &u.DisplayName, &u.CreatedAt, &u.UpdatedAt); err != nil {
		return nil, mapErr(err)
	}
	return u, nil
}

func (r *UserRepo) UpdateProfile(ctx context.Context, id uuid.UUID, displayName string) (*domain.User, error) {
	const q = `
		UPDATE users SET display_name = $2, updated_at = now()
		WHERE id = $1
		RETURNING id, email, password_hash, COALESCE(display_name,''), created_at, updated_at`
	u := &domain.User{}
	if err := r.pool.QueryRow(ctx, q, id, displayName).
		Scan(&u.ID, &u.Email, &u.PasswordHash, &u.DisplayName, &u.CreatedAt, &u.UpdatedAt); err != nil {
		return nil, mapErr(err)
	}
	return u, nil
}

func (r *UserRepo) GetPreferences(ctx context.Context, userID uuid.UUID) (*domain.UserPreferences, error) {
	const q = `SELECT user_id, pomodoro_duration_min, short_break_min, long_break_min,
	                   pomodoros_until_long_break, timezone, updated_at
	           FROM user_preferences WHERE user_id = $1`
	p := &domain.UserPreferences{}
	if err := r.pool.QueryRow(ctx, q, userID).
		Scan(&p.UserID, &p.PomodoroDurationMin, &p.ShortBreakMin, &p.LongBreakMin,
			&p.PomodorosUntilLongBreak, &p.Timezone, &p.UpdatedAt); err != nil {
		return nil, mapErr(err)
	}
	return p, nil
}

func (r *UserRepo) UpsertPreferences(ctx context.Context, p *domain.UserPreferences) error {
	const q = `
		INSERT INTO user_preferences
		    (user_id, pomodoro_duration_min, short_break_min, long_break_min,
		     pomodoros_until_long_break, timezone, updated_at)
		VALUES ($1, $2, $3, $4, $5, $6, now())
		ON CONFLICT (user_id) DO UPDATE SET
		    pomodoro_duration_min      = EXCLUDED.pomodoro_duration_min,
		    short_break_min            = EXCLUDED.short_break_min,
		    long_break_min             = EXCLUDED.long_break_min,
		    pomodoros_until_long_break = EXCLUDED.pomodoros_until_long_break,
		    timezone                   = EXCLUDED.timezone,
		    updated_at                 = now()
		RETURNING updated_at`
	return mapErr(r.pool.QueryRow(ctx, q,
		p.UserID, p.PomodoroDurationMin, p.ShortBreakMin, p.LongBreakMin,
		p.PomodorosUntilLongBreak, p.Timezone,
	).Scan(&p.UpdatedAt))
}

// --- RefreshTokenRepo ---

type RefreshTokenRepo struct{ pool *pgxpool.Pool }

func NewRefreshTokenRepo(p *pgxpool.Pool) *RefreshTokenRepo { return &RefreshTokenRepo{pool: p} }

func (r *RefreshTokenRepo) Create(ctx context.Context, t *domain.RefreshToken) error {
	const q = `INSERT INTO refresh_tokens (jti, user_id, expires_at) VALUES ($1, $2, $3)`
	_, err := r.pool.Exec(ctx, q, t.JTI, t.UserID, t.ExpiresAt)
	return mapErr(err)
}

func (r *RefreshTokenRepo) Get(ctx context.Context, jti uuid.UUID) (*domain.RefreshToken, error) {
	const q = `SELECT jti, user_id, expires_at, revoked_at, created_at
	           FROM refresh_tokens WHERE jti = $1`
	t := &domain.RefreshToken{}
	if err := r.pool.QueryRow(ctx, q, jti).
		Scan(&t.JTI, &t.UserID, &t.ExpiresAt, &t.RevokedAt, &t.CreatedAt); err != nil {
		return nil, mapErr(err)
	}
	return t, nil
}

func (r *RefreshTokenRepo) Revoke(ctx context.Context, jti uuid.UUID) error {
	const q = `UPDATE refresh_tokens SET revoked_at = now() WHERE jti = $1 AND revoked_at IS NULL`
	_, err := r.pool.Exec(ctx, q, jti)
	return mapErr(err)
}

func (r *RefreshTokenRepo) RevokeAllForUser(ctx context.Context, userID uuid.UUID) error {
	const q = `UPDATE refresh_tokens SET revoked_at = now() WHERE user_id = $1 AND revoked_at IS NULL`
	_, err := r.pool.Exec(ctx, q, userID)
	return mapErr(err)
}
