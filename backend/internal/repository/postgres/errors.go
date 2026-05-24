package postgres

import (
	"errors"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/triflow/backend/internal/domain"
)

// mapErr traduce errori pgx in errori di dominio.
func mapErr(err error) error {
	if err == nil {
		return nil
	}
	if errors.Is(err, pgx.ErrNoRows) {
		return domain.ErrNotFound
	}
	var pgErr *pgconn.PgError
	if errors.As(err, &pgErr) {
		switch pgErr.Code {
		case "23505": // unique_violation
			return domain.ErrAlreadyExists
		case "23503": // foreign_key_violation
			return domain.ErrInvalidInput
		case "23514": // check_violation
			return domain.ErrInvalidInput
		}
	}
	return err
}
