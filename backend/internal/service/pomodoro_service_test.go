package service

import (
	"context"
	"errors"
	"testing"
	"time"

	"github.com/google/uuid"

	"github.com/triflow/backend/internal/domain"
)

func newTestPomodoro(now time.Time) (*PomodoroService, *mockTimerStore, *mockSessionRepo) {
	store := newMockTimerStore()
	sess := &mockSessionRepo{}
	users := &mockUserRepo{prefs: domain.UserPreferences{
		PomodoroDurationMin: 25, ShortBreakMin: 5, LongBreakMin: 15, PomodorosUntilLongBreak: 4,
	}}
	svc := NewPomodoroService(sess, store, users)
	svc.now = func() time.Time { return now }
	return svc, store, sess
}

func TestPomodoro_StartUsesPrefsDuration(t *testing.T) {
	now := time.Date(2026, 1, 1, 10, 0, 0, 0, time.UTC)
	svc, _, _ := newTestPomodoro(now)
	uid := uuid.New()
	timer, err := svc.Start(context.Background(), uid, StartInput{})
	if err != nil {
		t.Fatalf("start: %v", err)
	}
	if timer.PlannedDurationSec != 25*60 {
		t.Errorf("expected 1500s, got %d", timer.PlannedDurationSec)
	}
	if timer.Kind != domain.PomodoroKindFocus {
		t.Errorf("expected focus, got %s", timer.Kind)
	}
}

func TestPomodoro_StartRefusesIfActive(t *testing.T) {
	now := time.Date(2026, 1, 1, 10, 0, 0, 0, time.UTC)
	svc, _, _ := newTestPomodoro(now)
	uid := uuid.New()
	if _, err := svc.Start(context.Background(), uid, StartInput{}); err != nil {
		t.Fatalf("first start: %v", err)
	}
	_, err := svc.Start(context.Background(), uid, StartInput{})
	if !errors.Is(err, domain.ErrTimerActive) {
		t.Fatalf("expected ErrTimerActive, got %v", err)
	}
}

func TestPomodoro_PauseAccumulatesElapsed(t *testing.T) {
	now := time.Date(2026, 1, 1, 10, 0, 0, 0, time.UTC)
	svc, _, _ := newTestPomodoro(now)
	uid := uuid.New()
	if _, err := svc.Start(context.Background(), uid, StartInput{}); err != nil {
		t.Fatal(err)
	}
	svc.now = func() time.Time { return now.Add(7 * time.Minute) }
	timer, err := svc.Pause(context.Background(), uid)
	if err != nil {
		t.Fatalf("pause: %v", err)
	}
	if timer.IsRunning() {
		t.Error("expected paused")
	}
	if timer.ElapsedBeforePauseSec != 7*60 {
		t.Errorf("expected 420s elapsed, got %d", timer.ElapsedBeforePauseSec)
	}
}

func TestPomodoro_ResumeResetsClock(t *testing.T) {
	now := time.Date(2026, 1, 1, 10, 0, 0, 0, time.UTC)
	svc, _, _ := newTestPomodoro(now)
	uid := uuid.New()
	_, _ = svc.Start(context.Background(), uid, StartInput{})

	svc.now = func() time.Time { return now.Add(10 * time.Minute) }
	_, _ = svc.Pause(context.Background(), uid)

	resumeAt := now.Add(15 * time.Minute)
	svc.now = func() time.Time { return resumeAt }
	timer, err := svc.Resume(context.Background(), uid)
	if err != nil {
		t.Fatalf("resume: %v", err)
	}
	if !timer.IsRunning() {
		t.Error("expected running")
	}
	if !timer.StartedAt.Equal(resumeAt) {
		t.Errorf("expected StartedAt=%v, got %v", resumeAt, timer.StartedAt)
	}
	if timer.ElapsedBeforePauseSec != 600 {
		t.Errorf("expected 600 accumulated, got %d", timer.ElapsedBeforePauseSec)
	}
}

func TestPomodoro_CompletePersistsAndClears(t *testing.T) {
	now := time.Date(2026, 1, 1, 10, 0, 0, 0, time.UTC)
	svc, store, sessions := newTestPomodoro(now)
	uid := uuid.New()
	_, _ = svc.Start(context.Background(), uid, StartInput{})

	svc.now = func() time.Time { return now.Add(25 * time.Minute) }
	sess, err := svc.Complete(context.Background(), uid)
	if err != nil {
		t.Fatalf("complete: %v", err)
	}
	if sess.Status != domain.PomodoroStatusCompleted {
		t.Errorf("expected completed, got %s", sess.Status)
	}
	if sess.ActualDurationSec != 1500 {
		t.Errorf("expected 1500s actual, got %d", sess.ActualDurationSec)
	}
	if len(sessions.created) != 1 {
		t.Errorf("expected 1 session persisted, got %d", len(sessions.created))
	}
	if _, err := store.Get(context.Background(), uid); !errors.Is(err, domain.ErrTimerNotFound) {
		t.Errorf("expected timer cleared, got err=%v", err)
	}
}

func TestPomodoro_AbortClampsElapsed(t *testing.T) {
	now := time.Date(2026, 1, 1, 10, 0, 0, 0, time.UTC)
	svc, _, _ := newTestPomodoro(now)
	uid := uuid.New()
	_, _ = svc.Start(context.Background(), uid, StartInput{DurationSec: 60})

	// abort dopo soli 10s
	svc.now = func() time.Time { return now.Add(10 * time.Second) }
	sess, err := svc.Abort(context.Background(), uid)
	if err != nil {
		t.Fatalf("abort: %v", err)
	}
	if sess.Status != domain.PomodoroStatusAborted {
		t.Errorf("expected aborted, got %s", sess.Status)
	}
	if sess.ActualDurationSec != 10 {
		t.Errorf("expected 10s actual, got %d", sess.ActualDurationSec)
	}
}
