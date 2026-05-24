package domain

// Page rappresenta i parametri di paginazione limit/offset.
type Page struct {
	Limit  int
	Offset int
}

// DefaultPage applica i default se assenti e limita i massimi.
func (p Page) Normalize() Page {
	if p.Limit <= 0 {
		p.Limit = 20
	}
	if p.Limit > 100 {
		p.Limit = 100
	}
	if p.Offset < 0 {
		p.Offset = 0
	}
	return p
}

// PageMeta è il blocco "meta" tornato nelle risposte paginate.
type PageMeta struct {
	Limit  int `json:"limit"`
	Offset int `json:"offset"`
	Total  int `json:"total"`
}
