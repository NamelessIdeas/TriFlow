// Package hash wrappa bcrypt per non sparpagliare la dipendenza.
package hash

import "golang.org/x/crypto/bcrypt"

// Cost di default (10) è un buon compromesso server-side per il 2024+.
const Cost = bcrypt.DefaultCost

func Password(plain string) (string, error) {
	b, err := bcrypt.GenerateFromPassword([]byte(plain), Cost)
	if err != nil {
		return "", err
	}
	return string(b), nil
}

func Verify(hash, plain string) bool {
	return bcrypt.CompareHashAndPassword([]byte(hash), []byte(plain)) == nil
}
