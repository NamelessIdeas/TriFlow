package service

import (
	"context"
	"errors"
	"time"

	"github.com/google/uuid"

	"github.com/triflow/backend/internal/domain"
	"github.com/triflow/backend/pkg/hash"
	"github.com/triflow/backend/pkg/jwt"
)

// TokenBlacklist è la dipendenza Redis del servizio di auth (interfaccia per testabilità).
type TokenBlacklist interface {
	Revoke(ctx context.Context, jti uuid.UUID, ttl time.Duration) error
	IsRevoked(ctx context.Context, jti uuid.UUID) (bool, error)
}

type AuthService struct {
	users     domain.UserRepository
	refresh   domain.RefreshTokenRepository
	blacklist TokenBlacklist
	jwt       *jwt.Manager
	now       func() time.Time
}

func NewAuthService(
	users domain.UserRepository,
	refresh domain.RefreshTokenRepository,
	blacklist TokenBlacklist,
	jwtm *jwt.Manager,
) *AuthService {
	return &AuthService{users: users, refresh: refresh, blacklist: blacklist, jwt: jwtm, now: time.Now}
}

// TokenPair restituito al client a login/refresh.
type TokenPair struct {
	AccessToken  string    `json:"access_token"`
	RefreshToken string    `json:"refresh_token"`
	AccessExpiry time.Time `json:"access_expires_at"`
}

// Register crea un nuovo utente con preferenze di default.
func (s *AuthService) Register(ctx context.Context, email, password, displayName string) (*domain.User, error) {
	if len(password) < 8 {
		return nil, domain.ErrInvalidInput
	}

	pwHash, err := hash.Password(password)
	if err != nil {
		return nil, err
	}
	u := &domain.User{
		ID:           uuid.New(),
		Email:        email,
		PasswordHash: pwHash,
		DisplayName:  displayName,
	}
	if err := s.users.Create(ctx, u); err != nil {
		return nil, err
	}
	// preferenze di default
	prefs := &domain.UserPreferences{
		UserID:                  u.ID,
		PomodoroDurationMin:     25,
		ShortBreakMin:           5,
		LongBreakMin:            15,
		PomodorosUntilLongBreak: 4,
		Timezone:                "UTC",
	}
	if err := s.users.UpsertPreferences(ctx, prefs); err != nil {
		return nil, err
	}
	return u, nil
}

// Login verifica le credenziali ed emette una coppia di token.
func (s *AuthService) Login(ctx context.Context, email, password string) (*domain.User, *TokenPair, error) {
	u, err := s.users.GetByEmail(ctx, email)
	if err != nil {
		if errors.Is(err, domain.ErrNotFound) {
			return nil, nil, domain.ErrInvalidCreds
		}
		return nil, nil, err
	}
	if !hash.Verify(u.PasswordHash, password) {
		return nil, nil, domain.ErrInvalidCreds
	}
	pair, err := s.issueTokens(ctx, u.ID)
	if err != nil {
		return nil, nil, err
	}
	return u, pair, nil
}

// Refresh scambia un refresh token valido con una nuova coppia (rotation).
func (s *AuthService) Refresh(ctx context.Context, refreshToken string) (*TokenPair, error) {
	claims, err := s.jwt.Parse(refreshToken)
	if err != nil {
		return nil, domain.ErrTokenInvalid
	}
	if claims.Type != jwt.TypeRefresh {
		return nil, domain.ErrTokenInvalid
	}

	revoked, err := s.blacklist.IsRevoked(ctx, claims.JTI)
	if err != nil {
		return nil, err
	}
	if revoked {
		return nil, domain.ErrTokenInvalid
	}

	rt, err := s.refresh.Get(ctx, claims.JTI)
	if err != nil {
		return nil, domain.ErrTokenInvalid
	}
	if rt.RevokedAt != nil || rt.ExpiresAt.Before(s.now()) {
		return nil, domain.ErrTokenInvalid
	}

	// rotation: revoca quello vecchio e ne emette uno nuovo
	if err := s.refresh.Revoke(ctx, claims.JTI); err != nil {
		return nil, err
	}
	ttl := time.Until(rt.ExpiresAt)
	if ttl < time.Minute {
		ttl = time.Minute
	}
	_ = s.blacklist.Revoke(ctx, claims.JTI, ttl)

	return s.issueTokens(ctx, claims.UserID)
}

// Logout revoca il refresh token in uso.
func (s *AuthService) Logout(ctx context.Context, refreshToken string) error {
	claims, err := s.jwt.Parse(refreshToken)
	if err != nil || claims.Type != jwt.TypeRefresh {
		return nil // logout è idempotente
	}
	if err := s.refresh.Revoke(ctx, claims.JTI); err != nil && !errors.Is(err, domain.ErrNotFound) {
		return err
	}
	_ = s.blacklist.Revoke(ctx, claims.JTI, s.jwt.RefreshTTL())
	return nil
}

func (s *AuthService) issueTokens(ctx context.Context, userID uuid.UUID) (*TokenPair, error) {
	now := s.now()
	access, accessExp, err := s.jwt.IssueAccess(userID, now)
	if err != nil {
		return nil, err
	}
	refresh, jti, refExp, err := s.jwt.IssueRefresh(userID, now)
	if err != nil {
		return nil, err
	}
	if err := s.refresh.Create(ctx, &domain.RefreshToken{
		JTI: jti, UserID: userID, ExpiresAt: refExp,
	}); err != nil {
		return nil, err
	}
	return &TokenPair{
		AccessToken:  access,
		RefreshToken: refresh,
		AccessExpiry: accessExp,
	}, nil
}
