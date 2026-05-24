// Command api è l'entrypoint del backend TriFlow.
package main

import (
	"context"
	"errors"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/triflow/backend/internal/config"
	"github.com/triflow/backend/internal/handler"
	pg "github.com/triflow/backend/internal/repository/postgres"
	rdb "github.com/triflow/backend/internal/repository/redis"
	"github.com/triflow/backend/internal/service"
	"github.com/triflow/backend/pkg/jwt"
)

func main() {
	cfg, err := config.Load()
	if err != nil {
		slog.Error("config load", slog.Any("error", err))
		os.Exit(1)
	}

	logger := newLogger(cfg.LogLevel)
	logger.Info("starting triflow api",
		slog.String("env", cfg.AppEnv), slog.String("port", cfg.AppPort))

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	// --- Connessioni infra ---
	pool, err := pg.NewPool(ctx, cfg.DSN(), cfg.DBMaxConns)
	if err != nil {
		logger.Error("db connect", slog.Any("error", err))
		os.Exit(1)
	}
	defer pool.Close()

	redisClient, err := rdb.NewClient(ctx, cfg.RedisAddr, cfg.RedisPassword, cfg.RedisDB)
	if err != nil {
		logger.Error("redis connect", slog.Any("error", err))
		os.Exit(1)
	}
	defer func() { _ = redisClient.Close() }()

	// --- Repository (postgres) ---
	userRepo := pg.NewUserRepo(pool)
	rtRepo := pg.NewRefreshTokenRepo(pool)
	projectRepo := pg.NewProjectRepo(pool)
	inboxRepo := pg.NewInboxRepo(pool)
	taskRepo := pg.NewTaskRepo(pool)
	contextRepo := pg.NewContextRepo(pool)
	pomodoroRepo := pg.NewPomodoroRepo(pool)
	noteRepo := pg.NewNoteRepo(pool)

	// --- Repository (redis) ---
	timerStore := rdb.NewActiveTimerStore(redisClient)
	blacklist := rdb.NewTokenBlacklist(redisClient)
	rateLimiter := rdb.NewRateLimiter(redisClient)

	// --- JWT ---
	jwtm := jwt.NewManager(cfg.JWTSecret, cfg.JWTAccessTTL, cfg.JWTRefreshTTL)

	// --- Services ---
	authSvc := service.NewAuthService(userRepo, rtRepo, blacklist, jwtm)
	userSvc := service.NewUserService(userRepo)
	gtdSvc := service.NewGTDService(taskRepo, projectRepo, inboxRepo, contextRepo)
	pomodoroSvc := service.NewPomodoroService(pomodoroRepo, timerStore, userRepo)
	noteSvc := service.NewNoteService(noteRepo)
	integrationSvc := service.NewIntegrationService(
		taskRepo, projectRepo, noteRepo, pomodoroRepo, timerStore,
	)
	quizSvc := service.NewQuizService()

	// --- Handlers ---
	handlers := &handler.Handlers{
		Auth:        handler.NewAuthHandler(authSvc),
		User:        handler.NewUserHandler(userSvc),
		GTD:         handler.NewGTDHandler(gtdSvc),
		Pomodoro:    handler.NewPomodoroHandler(pomodoroSvc),
		Note:        handler.NewNoteHandler(noteSvc),
		Integration: handler.NewIntegrationHandler(integrationSvc),
		Quiz:        handler.NewQuizHandler(quizSvc),
	}
	deps := &handler.Deps{
		Config:      cfg,
		Logger:      logger,
		JWT:         jwtm,
		RateLimiter: rateLimiter,
	}
	router := handler.NewRouter(deps, handlers)

	// --- HTTP server con graceful shutdown ---
	srv := &http.Server{
		Addr:              ":" + cfg.AppPort,
		Handler:           router,
		ReadHeaderTimeout: 10 * time.Second,
		ReadTimeout:       30 * time.Second,
		WriteTimeout:      30 * time.Second,
		IdleTimeout:       120 * time.Second,
	}

	go func() {
		logger.Info("http listening", slog.String("addr", srv.Addr))
		if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			logger.Error("http server", slog.Any("error", err))
			cancel()
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	select {
	case <-ctx.Done():
	case sig := <-quit:
		logger.Info("shutdown signal received", slog.String("signal", sig.String()))
	}

	shutdownCtx, shutdownCancel := context.WithTimeout(context.Background(), 15*time.Second)
	defer shutdownCancel()
	if err := srv.Shutdown(shutdownCtx); err != nil {
		logger.Error("shutdown", slog.Any("error", err))
	}
	logger.Info("bye")
}

func newLogger(level string) *slog.Logger {
	var lvl slog.Level
	switch level {
	case "debug":
		lvl = slog.LevelDebug
	case "warn":
		lvl = slog.LevelWarn
	case "error":
		lvl = slog.LevelError
	default:
		lvl = slog.LevelInfo
	}
	return slog.New(slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{Level: lvl}))
}
