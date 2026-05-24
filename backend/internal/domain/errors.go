package domain

import "errors"

// Error types riusati in tutto il sistema. Il middleware/error mappa questi
// errori su status HTTP coerenti, così i layer alti restano agnostici da HTTP.
var (
	ErrNotFound       = errors.New("resource not found")
	ErrConflict       = errors.New("resource conflict")
	ErrInvalidInput   = errors.New("invalid input")
	ErrUnauthorized   = errors.New("unauthorized")
	ErrForbidden      = errors.New("forbidden")
	ErrAlreadyExists  = errors.New("already exists")
	ErrInvalidCreds   = errors.New("invalid credentials")
	ErrTokenInvalid   = errors.New("token invalid or expired")
	ErrTimerActive    = errors.New("a pomodoro timer is already active")
	ErrTimerNotFound  = errors.New("no active pomodoro timer")
	ErrRateLimited    = errors.New("too many requests")
)
