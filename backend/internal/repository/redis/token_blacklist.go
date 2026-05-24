package redis

import (
	"context"
	"errors"
	"time"

	"github.com/google/uuid"
	goredis "github.com/redis/go-redis/v9"
)

// TokenBlacklist mantiene i jti di refresh token revocati con TTL pari alla
// loro scadenza residua. Permette di bloccare velocemente un token senza
// interrogare il DB ad ogni refresh.
type TokenBlacklist struct {
	rdb *goredis.Client
}

func NewTokenBlacklist(rdb *goredis.Client) *TokenBlacklist { return &TokenBlacklist{rdb: rdb} }

func blKey(jti uuid.UUID) string { return "blk:rt:" + jti.String() }

func (b *TokenBlacklist) Revoke(ctx context.Context, jti uuid.UUID, ttl time.Duration) error {
	if ttl <= 0 {
		ttl = time.Minute
	}
	return b.rdb.Set(ctx, blKey(jti), "1", ttl).Err()
}

func (b *TokenBlacklist) IsRevoked(ctx context.Context, jti uuid.UUID) (bool, error) {
	_, err := b.rdb.Get(ctx, blKey(jti)).Result()
	if err == nil {
		return true, nil
	}
	if errors.Is(err, goredis.Nil) {
		return false, nil
	}
	return false, err
}
