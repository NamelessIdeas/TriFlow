package handler

import (
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"

	"github.com/triflow/backend/internal/domain"
	mw "github.com/triflow/backend/internal/middleware"
	"github.com/triflow/backend/internal/service"
	"github.com/triflow/backend/pkg/response"
)

type IntegrationHandler struct{ svc *service.IntegrationService }

func NewIntegrationHandler(s *service.IntegrationService) *IntegrationHandler {
	return &IntegrationHandler{svc: s}
}

func (h *IntegrationHandler) Dashboard(c *gin.Context) {
	d, err := h.svc.Dashboard(c.Request.Context(), mw.UserID(c))
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, d)
}

func (h *IntegrationHandler) TaskFullContext(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	ctx, err := h.svc.TaskFullContext(c.Request.Context(), mw.UserID(c), id)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, ctx)
}

type promoteReq struct {
	Title     string     `json:"title"`
	ProjectID *uuid.UUID `json:"project_id"`
	ContextID *uuid.UUID `json:"context_id"`
	Status    string     `json:"status"     binding:"omitempty,oneof=inbox next_action waiting scheduled done"`
	DueDate   *time.Time `json:"due_date"`
	Priority  int        `json:"priority"`
}

// PromoteNote: POST /notes/{id}/promote-to-task
func (h *IntegrationHandler) PromoteNote(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	var req promoteReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	t, err := h.svc.PromoteNoteToTask(c.Request.Context(), mw.UserID(c), service.PromoteNoteInput{
		NoteID: id, Title: req.Title, ProjectID: req.ProjectID, ContextID: req.ContextID,
		Status: req.Status, DueDate: req.DueDate, Priority: req.Priority,
	})
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusCreated, t)
}
