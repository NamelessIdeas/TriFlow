// Package validator espone un singleton di go-playground/validator.
// Lo usiamo nei service/handler per validare struct con tag `validate:"..."`.
package validator

import (
	"sync"

	v10 "github.com/go-playground/validator/v10"
)

var (
	once     sync.Once
	instance *v10.Validate
)

func V() *v10.Validate {
	once.Do(func() {
		instance = v10.New(v10.WithRequiredStructEnabled())
	})
	return instance
}
