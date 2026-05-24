// Package jwt produce e verifica access + refresh token.
//
// Convenzioni:
//   - Access token: claim "uid" (subject), "typ"="access", scadenza breve (~15m).
//   - Refresh token: claim "jti" (uuid), "uid", "typ"="refresh", scadenza lunga (~7d).
//     Il jti è il PK in refresh_tokens (DB) e la chiave della blacklist Redis.
package jwt

import (
	"errors"
	"time"

	jwtv5 "github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

const (
	TypeAccess  = "access"
	TypeRefresh = "refresh"
)

type Manager struct {
	secret     []byte
	accessTTL  time.Duration
	refreshTTL time.Duration
}

func NewManager(secret string, accessTTL, refreshTTL time.Duration) *Manager {
	return &Manager{
		secret:     []byte(secret),
		accessTTL:  accessTTL,
		refreshTTL: refreshTTL,
	}
}

type Claims struct {
	UserID uuid.UUID `json:"uid"`
	Type   string    `json:"typ"`
	JTI    uuid.UUID `json:"jti,omitempty"`
	jwtv5.RegisteredClaims
}

// IssueAccess emette un access token con scadenza breve.
func (m *Manager) IssueAccess(userID uuid.UUID, now time.Time) (string, time.Time, error) {
	exp := now.Add(m.accessTTL)
	c := Claims{
		UserID: userID,
		Type:   TypeAccess,
		RegisteredClaims: jwtv5.RegisteredClaims{
			IssuedAt:  jwtv5.NewNumericDate(now),
			ExpiresAt: jwtv5.NewNumericDate(exp),
			Subject:   userID.String(),
		},
	}
	t, err := jwtv5.NewWithClaims(jwtv5.SigningMethodHS256, c).SignedString(m.secret)
	return t, exp, err
}

// IssueRefresh emette un refresh token con un nuovo jti.
func (m *Manager) IssueRefresh(userID uuid.UUID, now time.Time) (token string, jti uuid.UUID, exp time.Time, err error) {
	jti = uuid.New()
	exp = now.Add(m.refreshTTL)
	c := Claims{
		UserID: userID,
		Type:   TypeRefresh,
		JTI:    jti,
		RegisteredClaims: jwtv5.RegisteredClaims{
			IssuedAt:  jwtv5.NewNumericDate(now),
			ExpiresAt: jwtv5.NewNumericDate(exp),
			Subject:   userID.String(),
			ID:        jti.String(),
		},
	}
	token, err = jwtv5.NewWithClaims(jwtv5.SigningMethodHS256, c).SignedString(m.secret)
	return
}

// Parse verifica firma e scadenza, NON applica la blacklist (compito del service).
func (m *Manager) Parse(token string) (*Claims, error) {
	parsed, err := jwtv5.ParseWithClaims(token, &Claims{}, func(t *jwtv5.Token) (interface{}, error) {
		if _, ok := t.Method.(*jwtv5.SigningMethodHMAC); !ok {
			return nil, errors.New("unexpected signing method")
		}
		return m.secret, nil
	})
	if err != nil {
		return nil, err
	}
	c, ok := parsed.Claims.(*Claims)
	if !ok || !parsed.Valid {
		return nil, errors.New("invalid token")
	}
	return c, nil
}

func (m *Manager) AccessTTL() time.Duration  { return m.accessTTL }
func (m *Manager) RefreshTTL() time.Duration { return m.refreshTTL }
