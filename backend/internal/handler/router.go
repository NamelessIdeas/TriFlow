package handler

import (
	"log/slog"
	"net/http"

	"github.com/gin-gonic/gin"

	"github.com/triflow/backend/internal/config"
	mw "github.com/triflow/backend/internal/middleware"
	rdb "github.com/triflow/backend/internal/repository/redis"
	"github.com/triflow/backend/pkg/jwt"
)

// Handlers raccoglie tutti gli handler già costruiti, da iniettare nel router.
type Handlers struct {
	Auth        *AuthHandler
	User        *UserHandler
	GTD         *GTDHandler
	Pomodoro    *PomodoroHandler
	Note        *NoteHandler
	Integration *IntegrationHandler
	Quiz        *QuizHandler
}

// Deps è il bundle di dipendenze condivise dei middleware.
type Deps struct {
	Config      *config.Config
	Logger      *slog.Logger
	JWT         *jwt.Manager
	RateLimiter *rdb.RateLimiter
}

// NewRouter costruisce un engine Gin con tutte le route registrate sotto /api/v1.
func NewRouter(d *Deps, h *Handlers) *gin.Engine {
	if d.Config.AppEnv == "production" {
		gin.SetMode(gin.ReleaseMode)
	}
	r := gin.New()

	r.Use(mw.Recovery(d.Logger))
	r.Use(mw.Logger(d.Logger))
	r.Use(mw.CORS(d.Config.CORSAllowedOrigins))

	r.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "ok"})
	})

	v1 := r.Group("/api/v1")

	// ---- Public ----
	auth := v1.Group("/auth")
	auth.Use(mw.RateLimit(d.RateLimiter, "auth", d.Config.RateLimitAuthRPM))
	{
		auth.POST("/register", h.Auth.Register)
		auth.POST("/login", h.Auth.Login)
		auth.POST("/refresh", h.Auth.Refresh)
		auth.POST("/logout", h.Auth.Logout)
	}

	v1.POST("/quiz/score", h.Quiz.Score)

	// ---- Authenticated ----
	api := v1.Group("")
	api.Use(mw.Auth(d.JWT))
	{
		// User
		api.GET("/users/me", h.User.Me)
		api.PUT("/users/me", h.User.UpdateMe)
		api.GET("/users/me/preferences", h.User.GetPreferences)
		api.PUT("/users/me/preferences", h.User.UpdatePreferences)

		// Inbox
		api.POST("/inbox", h.GTD.CaptureInbox)
		api.GET("/inbox", h.GTD.ListInbox)
		api.POST("/inbox/:id/process", h.GTD.ProcessInbox)

		// Projects
		api.POST("/projects", h.GTD.CreateProject)
		api.GET("/projects", h.GTD.ListProjects)
		api.GET("/projects/:id", h.GTD.GetProject)
		api.PUT("/projects/:id", h.GTD.UpdateProject)
		api.DELETE("/projects/:id", h.GTD.DeleteProject)

		// Contexts
		api.POST("/contexts", h.GTD.CreateContext)
		api.GET("/contexts", h.GTD.ListContexts)
		api.DELETE("/contexts/:id", h.GTD.DeleteContext)

		// Tasks
		api.POST("/tasks", h.GTD.CreateTask)
		api.GET("/tasks", h.GTD.ListTasks)
		api.GET("/tasks/:id", h.GTD.GetTask)
		api.PATCH("/tasks/:id", h.GTD.UpdateTask)
		api.DELETE("/tasks/:id", h.GTD.DeleteTask)
		api.GET("/tasks/:id/context", h.Integration.TaskFullContext)

		// Weekly review
		api.GET("/reviews/weekly", h.GTD.WeeklyReview)

		// Pomodoro
		api.POST("/pomodoros/start", h.Pomodoro.Start)
		api.POST("/pomodoros/pause", h.Pomodoro.Pause)
		api.POST("/pomodoros/resume", h.Pomodoro.Resume)
		api.GET("/pomodoros/current", h.Pomodoro.Current)
		api.POST("/pomodoros/complete", h.Pomodoro.Complete)
		api.POST("/pomodoros/abort", h.Pomodoro.Abort)
		api.GET("/pomodoros/sessions", h.Pomodoro.ListSessions)
		api.GET("/pomodoros/stats", h.Pomodoro.Stats)

		// Notes
		api.POST("/notes", h.Note.Create)
		api.GET("/notes", h.Note.List)
		api.GET("/notes/:id", h.Note.Get)
		api.PATCH("/notes/:id", h.Note.Update)
		api.DELETE("/notes/:id", h.Note.Delete)
		api.GET("/notes/:id/backlinks", h.Note.Backlinks)
		api.GET("/notes/:id/links", h.Note.Outgoing)
		api.POST("/notes/:id/links", h.Note.Link)
		api.DELETE("/notes/:id/links/:targetId", h.Note.Unlink)
		api.POST("/notes/:id/refs", h.Note.AttachRef)
		api.DELETE("/notes/:id/refs/:refType/:refId", h.Note.DetachRef)
		api.POST("/notes/:id/promote-to-task", h.Integration.PromoteNote)

		// Dashboard unificata
		api.GET("/dashboard", h.Integration.Dashboard)
	}

	return r
}
