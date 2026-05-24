// Package middleware contiene i middleware Gin.
package middleware

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"

	"github.com/triflow/backend/pkg/jwt"
	"github.com/triflow/backend/pkg/response"
)

// chiave usata per propagare userID nel contesto Gin.
const ctxUserIDKey = "user_id"

// Auth richiede un access token valido nell'header Authorization: Bearer <jwt>.
func Auth(jwtm *jwt.Manager) gin.HandlerFunc {
	return func(c *gin.Context) {
		h := c.GetHeader("Authorization")
		if !strings.HasPrefix(h, "Bearer ") {
			response.Err(c, http.StatusUnauthorized, "unauthorized", "missing bearer token", nil)
			return
		}
		token := strings.TrimSpace(strings.TrimPrefix(h, "Bearer "))
		claims, err := jwtm.Parse(token)
		if err != nil || claims.Type != jwt.TypeAccess {
			response.Err(c, http.StatusUnauthorized, "unauthorized", "invalid or expired token", nil)
			return
		}
		c.Set(ctxUserIDKey, claims.UserID)
		c.Next()
	}
}

// UserID estrae l'utente autenticato dal contesto. Da usare negli handler.
func UserID(c *gin.Context) uuid.UUID {
	v, _ := c.Get(ctxUserIDKey)
	id, _ := v.(uuid.UUID)
	return id
}
