package middleware

import (
	"net/http"

	"github.com/gin-gonic/gin"
	rdb "github.com/triflow/backend/internal/repository/redis"
	"github.com/triflow/backend/pkg/response"
)

// RateLimit applica un fixed-window counter al minuto identificando il client
// via IP (sufficiente per endpoint di auth pre-login).
func RateLimit(rl *rdb.RateLimiter, namespace string, perMinute int) gin.HandlerFunc {
	return func(c *gin.Context) {
		ok, _, err := rl.Allow(c.Request.Context(), namespace, c.ClientIP(), perMinute)
		if err != nil {
			// In caso di errore Redis non blocchiamo il traffico
			c.Next()
			return
		}
		if !ok {
			response.Err(c, http.StatusTooManyRequests, "rate_limited",
				"too many requests, slow down", nil)
			return
		}
		c.Next()
	}
}
