package postgres

import "strconv"

// itoa è un alias compatto per fmt.Sprintf("%d", n) in costruzione query.
func itoa(n int) string { return strconv.Itoa(n) }
