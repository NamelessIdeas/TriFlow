package config

import (
	"fmt"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/joho/godotenv"
)

type Config struct {
	AppEnv      string
	AppPort     string
	LogLevel    string

	DBHost     string
	DBPort     string
	DBUser     string
	DBPassword string
	DBName     string
	DBSSLMode  string
	DBMaxConns int

	RedisAddr     string
	RedisPassword string
	RedisDB       int

	JWTSecret     string
	JWTAccessTTL  time.Duration
	JWTRefreshTTL time.Duration

	RateLimitAuthRPM int

	CORSAllowedOrigins []string
}

func Load() (*Config, error) {
	_ = godotenv.Load() // .env è opzionale in produzione

	c := &Config{
		AppEnv:   getEnv("APP_ENV", "development"),
		AppPort:  getEnv("APP_PORT", "8080"),
		LogLevel: getEnv("APP_LOG_LEVEL", "info"),

		DBHost:     getEnv("DB_HOST", "localhost"),
		DBPort:     getEnv("DB_PORT", "5432"),
		DBUser:     getEnv("DB_USER", "triflow"),
		DBPassword: getEnv("DB_PASSWORD", "triflow"),
		DBName:     getEnv("DB_NAME", "triflow"),
		DBSSLMode:  getEnv("DB_SSLMODE", "disable"),
		DBMaxConns: getEnvInt("DB_MAX_CONNS", 10),

		RedisAddr:     getEnv("REDIS_ADDR", "localhost:6379"),
		RedisPassword: getEnv("REDIS_PASSWORD", ""),
		RedisDB:       getEnvInt("REDIS_DB", 0),

		JWTSecret:     getEnv("JWT_SECRET", ""),
		JWTAccessTTL:  getEnvDuration("JWT_ACCESS_TTL", 15*time.Minute),
		JWTRefreshTTL: getEnvDuration("JWT_REFRESH_TTL", 7*24*time.Hour),

		RateLimitAuthRPM: getEnvInt("RATELIMIT_AUTH_RPM", 10),

		CORSAllowedOrigins: splitCSV(getEnv("CORS_ALLOWED_ORIGINS", "*")),
	}

	if c.JWTSecret == "" {
		return nil, fmt.Errorf("JWT_SECRET is required")
	}
	if len(c.JWTSecret) < 16 {
		return nil, fmt.Errorf("JWT_SECRET must be at least 16 characters")
	}

	return c, nil
}

// DSN restituisce la connection string PostgreSQL nel formato pgx.
func (c *Config) DSN() string {
	return fmt.Sprintf(
		"postgres://%s:%s@%s:%s/%s?sslmode=%s",
		c.DBUser, c.DBPassword, c.DBHost, c.DBPort, c.DBName, c.DBSSLMode,
	)
}

func getEnv(key, def string) string {
	if v, ok := os.LookupEnv(key); ok && v != "" {
		return v
	}
	return def
}

func getEnvInt(key string, def int) int {
	if v, ok := os.LookupEnv(key); ok {
		if n, err := strconv.Atoi(v); err == nil {
			return n
		}
	}
	return def
}

func getEnvDuration(key string, def time.Duration) time.Duration {
	if v, ok := os.LookupEnv(key); ok {
		if d, err := time.ParseDuration(v); err == nil {
			return d
		}
	}
	return def
}

func splitCSV(s string) []string {
	if s == "" {
		return nil
	}
	parts := strings.Split(s, ",")
	out := make([]string, 0, len(parts))
	for _, p := range parts {
		p = strings.TrimSpace(p)
		if p != "" {
			out = append(out, p)
		}
	}
	return out
}
