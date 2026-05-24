package service

import (
	"testing"

	"github.com/triflow/backend/internal/domain"
)

func TestQuizScore_RecommendsGTD_WhenOverwhelmedAndStructured(t *testing.T) {
	s := NewQuizService()
	res, err := s.Score(domain.QuizAnswers{
		MainProblem:    "overwhelm",
		WorkStyle:      "structured",
		SetupTolerance: "high",
		Goal:           "ship_tasks",
	})
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if res.RecommendedMethod != MethodGTD {
		t.Fatalf("expected gtd, got %s (scores=%+v)", res.RecommendedMethod, res.Scores)
	}
}

func TestQuizScore_RecommendsPomodoro_WhenDistractionAndLowSetup(t *testing.T) {
	s := NewQuizService()
	res, err := s.Score(domain.QuizAnswers{
		MainProblem:    "distraction",
		WorkStyle:      "flexible",
		SetupTolerance: "low",
		Goal:           "focus_time",
	})
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if res.RecommendedMethod != MethodPomodoro {
		t.Fatalf("expected pomodoro, got %s (scores=%+v)", res.RecommendedMethod, res.Scores)
	}
}

func TestQuizScore_RecommendsSecondBrain_WhenKnowledgeFocused(t *testing.T) {
	s := NewQuizService()
	res, err := s.Score(domain.QuizAnswers{
		MainProblem:    "knowledge_loss",
		WorkStyle:      "creative",
		SetupTolerance: "high",
		Goal:           "build_knowledge",
	})
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if res.RecommendedMethod != MethodSecondBrain {
		t.Fatalf("expected second_brain, got %s (scores=%+v)", res.RecommendedMethod, res.Scores)
	}
}

func TestQuizScore_ScoresAreNormalized(t *testing.T) {
	s := NewQuizService()
	res, err := s.Score(domain.QuizAnswers{
		MainProblem:    "overwhelm",
		WorkStyle:      "structured",
		SetupTolerance: "medium",
		Goal:           "ship_tasks",
	})
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if len(res.Scores) != 3 {
		t.Fatalf("expected 3 scores, got %d", len(res.Scores))
	}
	for _, sc := range res.Scores {
		if sc.Score < 0 || sc.Score > 100 {
			t.Errorf("score %s out of range: %d", sc.Method, sc.Score)
		}
		if sc.Explanation == "" {
			t.Errorf("explanation missing for %s", sc.Method)
		}
	}
}

func TestQuizScore_InvalidInputRejected(t *testing.T) {
	s := NewQuizService()
	_, err := s.Score(domain.QuizAnswers{
		MainProblem:    "bogus",
		WorkStyle:      "structured",
		SetupTolerance: "low",
		Goal:           "ship_tasks",
	})
	if err == nil {
		t.Fatal("expected error for invalid input")
	}
}
