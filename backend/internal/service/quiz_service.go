package service

import (
	"github.com/triflow/backend/internal/domain"
	"github.com/triflow/backend/pkg/validator"
)

// QuizService implementa la logica di scoring del questionario.
//
// Approccio: per ognuna delle 4 risposte assegniamo dei punti ai 3 metodi
// in base a quanto la risposta "tira" verso quel metodo. Il totale viene
// normalizzato 0..100 (max teorico = 4 risposte × 10 punti = 40).
type QuizService struct{}

func NewQuizService() *QuizService { return &QuizService{} }

const (
	MethodGTD         = "gtd"
	MethodPomodoro    = "pomodoro"
	MethodSecondBrain = "second_brain"
)

// matrice dei punti per ciascuna risposta.
// Ogni valore aggiunge punti al metodo corrispondente. Max per domanda = 10.
var scoreMatrix = map[string]map[string]map[string]int{
	"main_problem": {
		"overwhelm":       {MethodGTD: 10, MethodPomodoro: 3, MethodSecondBrain: 2},
		"distraction":     {MethodGTD: 2, MethodPomodoro: 10, MethodSecondBrain: 2},
		"knowledge_loss":  {MethodGTD: 2, MethodPomodoro: 1, MethodSecondBrain: 10},
	},
	"work_style": {
		"structured": {MethodGTD: 10, MethodPomodoro: 7, MethodSecondBrain: 3},
		"flexible":   {MethodGTD: 4, MethodPomodoro: 8, MethodSecondBrain: 6},
		"creative":   {MethodGTD: 2, MethodPomodoro: 4, MethodSecondBrain: 10},
	},
	"setup_tolerance": {
		// chi tollera poco setup va col più semplice: pomodoro
		"low":    {MethodGTD: 2, MethodPomodoro: 10, MethodSecondBrain: 1},
		"medium": {MethodGTD: 6, MethodPomodoro: 7, MethodSecondBrain: 5},
		"high":   {MethodGTD: 10, MethodPomodoro: 4, MethodSecondBrain: 10},
	},
	"goal": {
		"ship_tasks":      {MethodGTD: 10, MethodPomodoro: 5, MethodSecondBrain: 2},
		"focus_time":      {MethodGTD: 3, MethodPomodoro: 10, MethodSecondBrain: 2},
		"build_knowledge": {MethodGTD: 2, MethodPomodoro: 1, MethodSecondBrain: 10},
	},
}

const maxScorePerMethod = 40 // 4 domande × 10

var methodNames = []string{MethodGTD, MethodPomodoro, MethodSecondBrain}

var methodExplanations = map[string]string{
	MethodGTD: "Getting Things Done ti dà una pipeline chiara: cattura tutto in inbox, " +
		"poi processa decidendo prossima azione, progetto o riferimento. " +
		"È la scelta giusta quando il problema è la quantità di cose da fare.",
	MethodPomodoro: "La tecnica del Pomodoro spezza il lavoro in blocchi da 25 minuti " +
		"di focus pieno alternati a pause brevi. Funziona quando la difficoltà non è " +
		"sapere COSA fare ma riuscire a concentrarsi su una cosa per volta.",
	MethodSecondBrain: "Il Second Brain (BASB) ti dà un sistema per catturare, organizzare " +
		"e collegare conoscenza in modo che torni utile quando serve. " +
		"Va bene quando le idee e i materiali si perdono o restano isolati.",
}

// Score calcola il punteggio per ciascun metodo data una risposta.
func (s *QuizService) Score(a domain.QuizAnswers) (*domain.QuizResult, error) {
	if err := validator.V().Struct(a); err != nil {
		return nil, domain.ErrInvalidInput
	}

	raw := map[string]int{}
	answers := map[string]string{
		"main_problem":    a.MainProblem,
		"work_style":      a.WorkStyle,
		"setup_tolerance": a.SetupTolerance,
		"goal":            a.Goal,
	}
	for question, answer := range answers {
		if pts, ok := scoreMatrix[question][answer]; ok {
			for m, p := range pts {
				raw[m] += p
			}
		}
	}

	scores := make([]domain.MethodScore, 0, len(methodNames))
	bestMethod := MethodGTD
	bestScore := -1
	for _, m := range methodNames {
		v := raw[m]
		normalized := (v * 100) / maxScorePerMethod
		if normalized > 100 {
			normalized = 100
		}
		scores = append(scores, domain.MethodScore{
			Method:      m,
			Score:       normalized,
			Explanation: methodExplanations[m],
		})
		if normalized > bestScore {
			bestScore = normalized
			bestMethod = m
		}
	}

	return &domain.QuizResult{
		RecommendedMethod: bestMethod,
		Reasoning:         methodExplanations[bestMethod],
		Scores:            scores,
	}, nil
}
