package middleware

import (
	"log/slog"
	"time"

	"github.com/gin-gonic/gin"
)

// Logger logga richiesta + status + durata in formato strutturato.
func Logger(l *slog.Logger) gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		c.Next()
		l.Info("http_request",
			slog.String("method", c.Request.Method),
			slog.String("path", c.Request.URL.Path),
			slog.Int("status", c.Writer.Status()),
			slog.Duration("latency", time.Since(start)),
			slog.String("ip", c.ClientIP()),
		)
	}
}
