package middleware

import (
	"time"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
)

func CORS(allowed []string) gin.HandlerFunc {
	cfg := cors.Config{
		AllowMethods:     []string{"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Authorization", "Content-Type", "Accept"},
		ExposeHeaders:    []string{"Content-Length"},
		AllowCredentials: true,
		MaxAge:           12 * time.Hour,
	}
	if len(allowed) == 1 && allowed[0] == "*" {
		cfg.AllowAllOrigins = true
		cfg.AllowCredentials = false
	} else {
		cfg.AllowOrigins = allowed
	}
	return cors.New(cfg)
}
