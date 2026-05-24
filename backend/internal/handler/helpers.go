package handler

import (
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"

	"github.com/triflow/backend/internal/domain"
)

// parseUUID prende un path/query param, restituisce errore se non valido.
func parseUUID(s string) (uuid.UUID, error) { return uuid.Parse(s) }

// queryPage estrae limit/offset dalla query string applicando i default.
func queryPage(c *gin.Context) domain.Page {
	lim, _ := strconv.Atoi(c.DefaultQuery("limit", "20"))
	off, _ := strconv.Atoi(c.DefaultQuery("offset", "0"))
	return domain.Page{Limit: lim, Offset: off}.Normalize()
}

// queryStrPtr restituisce un *string se il parametro è presente e non vuoto.
func queryStrPtr(c *gin.Context, key string) *string {
	if v := c.Query(key); v != "" {
		return &v
	}
	return nil
}

// queryUUIDPtr restituisce un *UUID se il parametro è presente e parsabile.
func queryUUIDPtr(c *gin.Context, key string) *uuid.UUID {
	if v := c.Query(key); v != "" {
		if id, err := uuid.Parse(v); err == nil {
			return &id
		}
	}
	return nil
}

// queryTimePtr legge un timestamp RFC3339 o una data YYYY-MM-DD.
func queryTimePtr(c *gin.Context, key string) *time.Time {
	v := c.Query(key)
	if v == "" {
		return nil
	}
	if t, err := time.Parse(time.RFC3339, v); err == nil {
		return &t
	}
	if t, err := time.Parse("2006-01-02", v); err == nil {
		return &t
	}
	return nil
}
