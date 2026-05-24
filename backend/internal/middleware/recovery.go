package middleware

import (
	"log/slog"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/triflow/backend/pkg/response"
)

// Recovery cattura panic e li trasforma in 500 con envelope coerente.
func Recovery(l *slog.Logger) gin.HandlerFunc {
	return func(c *gin.Context) {
		defer func() {
			if r := recover(); r != nil {
				l.Error("panic_recovered",
					slog.Any("error", r),
					slog.String("path", c.Request.URL.Path))
				response.Err(c, http.StatusInternalServerError,
					"internal_error", "an unexpected error occurred", nil)
			}
		}()
		c.Next()
	}
}
