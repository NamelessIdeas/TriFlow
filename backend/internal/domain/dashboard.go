package domain

import "time"

// Dashboard è l'aggregato unificato che integra i 3 metodi.
type Dashboard struct {
	TodayTasks       []Task           `json:"today_tasks"`
	ActiveTimer      *ActiveTimer     `json:"active_timer,omitempty"`
	RecentNotes      []Note           `json:"recent_notes"`
	PomodorosToday   int              `json:"pomodoros_today"`
	FocusSecondsWeek int              `json:"focus_seconds_week"`
	GeneratedAt      time.Time        `json:"generated_at"`
}
