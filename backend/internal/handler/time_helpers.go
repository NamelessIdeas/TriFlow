package handler

import "time"

// orZero restituisce time.Time{} se il puntatore è nil.
func orZero(t *time.Time) time.Time {
	if t == nil {
		return time.Time{}
	}
	return *t
}
