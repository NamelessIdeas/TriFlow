package middleware

import (
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/triflow/backend/internal/domain"
	"github.com/triflow/backend/pkg/response"
)

// HandleError trasforma un errore di dominio in una risposta HTTP coerente.
// Gli handler chiamano questa funzione quando un service ritorna errore.
func HandleError(c *gin.Context, err error) {
	switch {
	case errors.Is(err, domain.ErrNotFound):
		response.Err(c, http.StatusNotFound, "not_found", err.Error(), nil)
	case errors.Is(err, domain.ErrAlreadyExists):
		response.Err(c, http.StatusConflict, "already_exists", err.Error(), nil)
	case errors.Is(err, domain.ErrConflict):
		response.Err(c, http.StatusConflict, "conflict", err.Error(), nil)
	case errors.Is(err, domain.ErrInvalidInput):
		response.Err(c, http.StatusBadRequest, "invalid_input", err.Error(), nil)
	case errors.Is(err, domain.ErrInvalidCreds):
		response.Err(c, http.StatusUnauthorized, "invalid_credentials", "invalid email or password", nil)
	case errors.Is(err, domain.ErrTokenInvalid):
		response.Err(c, http.StatusUnauthorized, "token_invalid", err.Error(), nil)
	case errors.Is(err, domain.ErrUnauthorized):
		response.Err(c, http.StatusUnauthorized, "unauthorized", err.Error(), nil)
	case errors.Is(err, domain.ErrForbidden):
		response.Err(c, http.StatusForbidden, "forbidden", err.Error(), nil)
	case errors.Is(err, domain.ErrTimerActive):
		response.Err(c, http.StatusConflict, "timer_active", err.Error(), nil)
	case errors.Is(err, domain.ErrTimerNotFound):
		response.Err(c, http.StatusNotFound, "timer_not_found", err.Error(), nil)
	case errors.Is(err, domain.ErrRateLimited):
		response.Err(c, http.StatusTooManyRequests, "rate_limited", err.Error(), nil)
	default:
		response.Err(c, http.StatusInternalServerError, "internal_error", "internal server error", nil)
	}
}
