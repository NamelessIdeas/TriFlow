package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"

	"github.com/triflow/backend/internal/domain"
	mw "github.com/triflow/backend/internal/middleware"
	"github.com/triflow/backend/internal/service"
	"github.com/triflow/backend/pkg/response"
)

type PomodoroHandler struct{ svc *service.PomodoroService }

func NewPomodoroHandler(s *service.PomodoroService) *PomodoroHandler { return &PomodoroHandler{svc: s} }

type startReq struct {
	TaskID      *uuid.UUID `json:"task_id"`
	Kind        string     `json:"kind"         binding:"omitempty,oneof=focus short_break long_break"`
	CycleIndex  int        `json:"cycle_index"  binding:"omitempty,min=1"`
	DurationSec int        `json:"duration_sec" binding:"omitempty,min=1"`
}

func (h *PomodoroHandler) Start(c *gin.Context) {
	var req startReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	t, err := h.svc.Start(c.Request.Context(), mw.UserID(c), service.StartInput{
		TaskID: req.TaskID, Kind: req.Kind,
		CycleIndex: req.CycleIndex, DurationSec: req.DurationSec,
	})
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusCreated, t)
}

func (h *PomodoroHandler) Pause(c *gin.Context) {
	t, err := h.svc.Pause(c.Request.Context(), mw.UserID(c))
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, t)
}

func (h *PomodoroHandler) Resume(c *gin.Context) {
	t, err := h.svc.Resume(c.Request.Context(), mw.UserID(c))
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, t)
}

func (h *PomodoroHandler) Current(c *gin.Context) {
	t, err := h.svc.Current(c.Request.Context(), mw.UserID(c))
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, t)
}

func (h *PomodoroHandler) Complete(c *gin.Context) {
	s, err := h.svc.Complete(c.Request.Context(), mw.UserID(c))
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, s)
}

func (h *PomodoroHandler) Abort(c *gin.Context) {
	s, err := h.svc.Abort(c.Request.Context(), mw.UserID(c))
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, s)
}

func (h *PomodoroHandler) ListSessions(c *gin.Context) {
	page := queryPage(c)
	from := queryTimePtr(c, "from")
	to := queryTimePtr(c, "to")
	var fromT, toT = orZero(from), orZero(to)
	items, total, err := h.svc.ListSessions(c.Request.Context(), mw.UserID(c), fromT, toT, page)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OKWithMeta(c, http.StatusOK, items, domain.PageMeta{
		Limit: page.Limit, Offset: page.Offset, Total: total,
	})
}

func (h *PomodoroHandler) Stats(c *gin.Context) {
	from := queryTimePtr(c, "from")
	to := queryTimePtr(c, "to")
	s, err := h.svc.Stats(c.Request.Context(), mw.UserID(c), orZero(from), orZero(to))
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, s)
}
