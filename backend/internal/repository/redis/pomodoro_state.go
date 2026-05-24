package redis

import (
	"context"
	"encoding/json"
	"errors"
	"time"

	"github.com/google/uuid"
	goredis "github.com/redis/go-redis/v9"

	"github.com/triflow/backend/internal/domain"
)

// ActiveTimerStore implementa domain.ActiveTimerStore con Redis.
//
// Chiave: pomodoro:active:{user_id} → JSON di ActiveTimer.
// Il TTL viene impostato pari al residuo del timer; quando scade la chiave
// sparisce da sola (il client può comunque chiamare /complete o /abort).
type ActiveTimerStore struct {
	rdb *goredis.Client
}

func NewActiveTimerStore(rdb *goredis.Client) *ActiveTimerStore { return &ActiveTimerStore{rdb: rdb} }

func key(userID uuid.UUID) string { return "pomodoro:active:" + userID.String() }

func (s *ActiveTimerStore) Set(ctx context.Context, t *domain.ActiveTimer, ttl time.Duration) error {
	b, err := json.Marshal(t)
	if err != nil {
		return err
	}
	if ttl <= 0 {
		ttl = time.Minute
	}
	return s.rdb.Set(ctx, key(t.UserID), b, ttl).Err()
}

func (s *ActiveTimerStore) Get(ctx context.Context, userID uuid.UUID) (*domain.ActiveTimer, error) {
	raw, err := s.rdb.Get(ctx, key(userID)).Bytes()
	if err != nil {
		if errors.Is(err, goredis.Nil) {
			return nil, domain.ErrTimerNotFound
		}
		return nil, err
	}
	t := &domain.ActiveTimer{}
	if err := json.Unmarshal(raw, t); err != nil {
		return nil, err
	}
	return t, nil
}

func (s *ActiveTimerStore) Delete(ctx context.Context, userID uuid.UUID) error {
	return s.rdb.Del(ctx, key(userID)).Err()
}
