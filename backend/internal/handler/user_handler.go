package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"github.com/triflow/backend/internal/domain"
	mw "github.com/triflow/backend/internal/middleware"
	"github.com/triflow/backend/internal/service"
	"github.com/triflow/backend/pkg/response"
)

type UserHandler struct{ svc *service.UserService }

func NewUserHandler(s *service.UserService) *UserHandler { return &UserHandler{svc: s} }

func (h *UserHandler) Me(c *gin.Context) {
	u, err := h.svc.Me(c.Request.Context(), mw.UserID(c))
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, u)
}

type updateProfileReq struct {
	DisplayName string `json:"display_name" binding:"required,min=1,max=80"`
}

func (h *UserHandler) UpdateMe(c *gin.Context) {
	var req updateProfileReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	u, err := h.svc.UpdateProfile(c.Request.Context(), mw.UserID(c), req.DisplayName)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, u)
}

func (h *UserHandler) GetPreferences(c *gin.Context) {
	p, err := h.svc.GetPreferences(c.Request.Context(), mw.UserID(c))
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, p)
}

type prefsReq struct {
	PomodoroDurationMin     int    `json:"pomodoro_duration_min"      binding:"required,min=1,max=180"`
	ShortBreakMin           int    `json:"short_break_min"            binding:"required,min=1,max=60"`
	LongBreakMin            int    `json:"long_break_min"             binding:"required,min=1,max=120"`
	PomodorosUntilLongBreak int    `json:"pomodoros_until_long_break" binding:"required,min=2,max=12"`
	Timezone                string `json:"timezone"                   binding:"required"`
}

func (h *UserHandler) UpdatePreferences(c *gin.Context) {
	var req prefsReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	p := &domain.UserPreferences{
		UserID:                  mw.UserID(c),
		PomodoroDurationMin:     req.PomodoroDurationMin,
		ShortBreakMin:           req.ShortBreakMin,
		LongBreakMin:            req.LongBreakMin,
		PomodorosUntilLongBreak: req.PomodorosUntilLongBreak,
		Timezone:                req.Timezone,
	}
	if err := h.svc.UpdatePreferences(c.Request.Context(), p); err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, p)
}
