// Package redis contiene gli store basati su Redis.
package redis

import (
	"context"
	"time"

	goredis "github.com/redis/go-redis/v9"
)

// NewClient costruisce un client Redis con ping iniziale.
func NewClient(ctx context.Context, addr, password string, db int) (*goredis.Client, error) {
	c := goredis.NewClient(&goredis.Options{
		Addr:        addr,
		Password:    password,
		DB:          db,
		DialTimeout: 5 * time.Second,
	})
	pingCtx, cancel := context.WithTimeout(ctx, 5*time.Second)
	defer cancel()
	if err := c.Ping(pingCtx).Err(); err != nil {
		_ = c.Close()
		return nil, err
	}
	return c, nil
}
