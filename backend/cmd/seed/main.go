// Command seed popola il DB con dati demo (utente, contesti, progetto,
// task, sessioni pomodoro e note). Idempotente per email.
package main

import (
	"context"
	"fmt"
	"log/slog"
	"os"
	"time"

	"github.com/google/uuid"

	"github.com/triflow/backend/internal/config"
	"github.com/triflow/backend/internal/domain"
	pg "github.com/triflow/backend/internal/repository/postgres"
	"github.com/triflow/backend/pkg/hash"
)

const demoEmail = "demo@triflow.app"

func main() {
	cfg, err := config.Load()
	if err != nil {
		fmt.Fprintln(os.Stderr, "config:", err)
		os.Exit(1)
	}
	ctx := context.Background()
	pool, err := pg.NewPool(ctx, cfg.DSN(), cfg.DBMaxConns)
	if err != nil {
		fmt.Fprintln(os.Stderr, "db:", err)
		os.Exit(1)
	}
	defer pool.Close()

	users := pg.NewUserRepo(pool)
	contexts := pg.NewContextRepo(pool)
	projects := pg.NewProjectRepo(pool)
	tasks := pg.NewTaskRepo(pool)
	pomos := pg.NewPomodoroRepo(pool)
	notes := pg.NewNoteRepo(pool)

	// 1. utente demo (idempotente)
	u, err := users.GetByEmail(ctx, demoEmail)
	if err != nil {
		pwd, _ := hash.Password("demopass123")
		u = &domain.User{
			ID: uuid.New(), Email: demoEmail,
			PasswordHash: pwd, DisplayName: "Demo User",
		}
		if err := users.Create(ctx, u); err != nil {
			fail("create user", err)
		}
		_ = users.UpsertPreferences(ctx, &domain.UserPreferences{
			UserID:                  u.ID,
			PomodoroDurationMin:     25,
			ShortBreakMin:           5,
			LongBreakMin:            15,
			PomodorosUntilLongBreak: 4,
			Timezone:                "Europe/Rome",
		})
		slog.Info("created demo user", "id", u.ID, "email", u.Email)
	} else {
		slog.Info("demo user already exists", "id", u.ID)
	}

	// 2. contesti
	for _, name := range []string{"@casa", "@ufficio", "@telefono"} {
		_ = contexts.Create(ctx, &domain.Context{
			ID: uuid.New(), UserID: u.ID, Name: name,
		})
	}

	// 3. progetto
	proj := &domain.Project{
		ID: uuid.New(), UserID: u.ID,
		Title:       "Lanciare TriFlow MVP",
		Description: "Backend, mobile e onboarding entro fine quarter.",
		Status:      domain.ProjectStatusActive,
	}
	_ = projects.Create(ctx, proj)

	// 4. task
	estimated := 60
	due := time.Now().Add(48 * time.Hour)
	for i, title := range []string{
		"Scrivere README di TriFlow",
		"Disegnare schermata Today",
		"Configurare CI",
	} {
		t := &domain.Task{
			ID: uuid.New(), UserID: u.ID, ProjectID: &proj.ID,
			Title: title, Status: domain.TaskStatusNextAction,
			Priority:         5 - i,
			EstimatedMinutes: &estimated,
			DueDate:          &due,
		}
		if err := tasks.Create(ctx, t); err == nil {
			_ = tasks.ReplaceTags(ctx, t.ID, []string{"mvp", "demo"})
		}
	}

	// 5. una sessione pomodoro storica
	start := time.Now().Add(-90 * time.Minute)
	_ = pomos.Create(ctx, &domain.PomodoroSession{
		ID:                 uuid.New(),
		UserID:             u.ID,
		Kind:               domain.PomodoroKindFocus,
		PlannedDurationSec: 1500,
		ActualDurationSec:  1500,
		CycleIndex:         1,
		StartedAt:          start,
		EndedAt:            start.Add(25 * time.Minute),
		Status:             domain.PomodoroStatusCompleted,
	})

	// 6. una nota
	note := &domain.Note{
		ID: uuid.New(), UserID: u.ID,
		Title:        "Idea: integrare il quiz nell'onboarding",
		ContentMD:    "Il quiz dovrebbe partire alla prima apertura...",
		PARACategory: domain.PARAProject,
	}
	if err := notes.Create(ctx, note); err == nil {
		_ = notes.ReplaceTags(ctx, note.ID, []string{"onboarding", "idea"})
	}

	slog.Info("seed done", "email", demoEmail, "password", "demopass123")
}

func fail(msg string, err error) {
	fmt.Fprintln(os.Stderr, msg+":", err)
	os.Exit(1)
}
