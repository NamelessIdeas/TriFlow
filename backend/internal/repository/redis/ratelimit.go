package redis

import (
	"context"
	"strconv"
	"time"

	goredis "github.com/redis/go-redis/v9"
)

// RateLimiter implementa un fixed-window counter al minuto per identificatore
// (es. IP o user_id). Semplice, sufficiente per /auth/*.
type RateLimiter struct {
	rdb *goredis.Client
}

func NewRateLimiter(rdb *goredis.Client) *RateLimiter { return &RateLimiter{rdb: rdb} }

// Allow incrementa il contatore della finestra corrente; restituisce true se è
// sotto il limite. La chiave scade allo scadere della finestra.
func (r *RateLimiter) Allow(ctx context.Context, namespace, identifier string, limitPerMinute int) (allowed bool, remaining int, err error) {
	if limitPerMinute <= 0 {
		return true, 0, nil
	}
	window := time.Now().UTC().Truncate(time.Minute).Unix()
	k := "rl:" + namespace + ":" + identifier + ":" + strconv.FormatInt(window, 10)

	n, err := r.rdb.Incr(ctx, k).Result()
	if err != nil {
		return false, 0, err
	}
	if n == 1 {
		// prima Incr della finestra: applica il TTL
		_ = r.rdb.Expire(ctx, k, time.Minute).Err()
	}
	if n > int64(limitPerMinute) {
		return false, 0, nil
	}
	return true, limitPerMinute - int(n), nil
}
