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

type GTDHandler struct{ svc *service.GTDService }

func NewGTDHandler(s *service.GTDService) *GTDHandler { return &GTDHandler{svc: s} }

// ----- Inbox -----

type captureReq struct {
	RawText string `json:"raw_text" binding:"required,min=1,max=5000"`
}

func (h *GTDHandler) CaptureInbox(c *gin.Context) {
	var req captureReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	i, err := h.svc.CaptureInbox(c.Request.Context(), mw.UserID(c), req.RawText)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusCreated, i)
}

func (h *GTDHandler) ListInbox(c *gin.Context) {
	page := queryPage(c)
	items, total, err := h.svc.ListInbox(c.Request.Context(), mw.UserID(c), page)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OKWithMeta(c, http.StatusOK, items, domain.PageMeta{
		Limit: page.Limit, Offset: page.Offset, Total: total,
	})
}

type processInboxReq struct {
	Action    string     `json:"action"     binding:"required,oneof=task project discard"`
	Title     string     `json:"title"`
	Notes     string     `json:"notes"`
	Status    string     `json:"status"     binding:"omitempty,oneof=inbox next_action waiting scheduled done"`
	ContextID *uuid.UUID `json:"context_id"`
	ProjectID *uuid.UUID `json:"project_id"`
	Energy    *string    `json:"energy"     binding:"omitempty,oneof=low medium high"`
	DueDate   *time.Time `json:"due_date"`
	Priority  int        `json:"priority"`
	Tags      []string   `json:"tags"`
}

func (h *GTDHandler) ProcessInbox(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	var req processInboxReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	res, err := h.svc.ProcessInbox(c.Request.Context(), mw.UserID(c), id, service.ProcessInboxInput{
		Action: req.Action, Title: req.Title, Notes: req.Notes, Status: req.Status,
		ContextID: req.ContextID, ProjectID: req.ProjectID, Energy: req.Energy,
		DueDate: req.DueDate, Priority: req.Priority, Tags: req.Tags,
	})
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, res)
}

// ----- Projects -----

type projectReq struct {
	Title       string `json:"title"       binding:"required,min=1,max=200"`
	Description string `json:"description"`
	Status      string `json:"status"      binding:"omitempty,oneof=active someday completed"`
}

func (h *GTDHandler) CreateProject(c *gin.Context) {
	var req projectReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	p, err := h.svc.CreateProject(c.Request.Context(), mw.UserID(c), req.Title, req.Description, req.Status)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusCreated, p)
}

func (h *GTDHandler) GetProject(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	p, err := h.svc.GetProject(c.Request.Context(), mw.UserID(c), id)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, p)
}

func (h *GTDHandler) UpdateProject(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	var req projectReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	p, err := h.svc.GetProject(c.Request.Context(), mw.UserID(c), id)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	p.Title = req.Title
	p.Description = req.Description
	if req.Status != "" {
		p.Status = req.Status
	}
	if err := h.svc.UpdateProject(c.Request.Context(), p); err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, p)
}

func (h *GTDHandler) DeleteProject(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	if err := h.svc.DeleteProject(c.Request.Context(), mw.UserID(c), id); err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusNoContent, nil)
}

func (h *GTDHandler) ListProjects(c *gin.Context) {
	page := queryPage(c)
	status := queryStrPtr(c, "status")
	items, total, err := h.svc.ListProjects(c.Request.Context(), mw.UserID(c), status, page)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OKWithMeta(c, http.StatusOK, items, domain.PageMeta{
		Limit: page.Limit, Offset: page.Offset, Total: total,
	})
}

// ----- Contexts -----

type contextReq struct {
	Name string `json:"name" binding:"required,min=1,max=50"`
}

func (h *GTDHandler) CreateContext(c *gin.Context) {
	var req contextReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	ct, err := h.svc.CreateContext(c.Request.Context(), mw.UserID(c), req.Name)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusCreated, ct)
}

func (h *GTDHandler) ListContexts(c *gin.Context) {
	items, err := h.svc.ListContexts(c.Request.Context(), mw.UserID(c))
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, items)
}

func (h *GTDHandler) DeleteContext(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	if err := h.svc.DeleteContext(c.Request.Context(), mw.UserID(c), id); err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusNoContent, nil)
}

// ----- Tasks -----

type taskReq struct {
	Title            string     `json:"title"             binding:"required,min=1,max=200"`
	Notes            string     `json:"notes"`
	ProjectID        *uuid.UUID `json:"project_id"`
	ContextID        *uuid.UUID `json:"context_id"`
	Status           string     `json:"status"            binding:"omitempty,oneof=inbox next_action waiting scheduled done"`
	Energy           *string    `json:"energy"            binding:"omitempty,oneof=low medium high"`
	EstimatedMinutes *int       `json:"estimated_minutes" binding:"omitempty,min=1"`
	Priority         int        `json:"priority"`
	DueDate          *time.Time `json:"due_date"`
	DeferDate        *time.Time `json:"defer_date"`
	Tags             []string   `json:"tags"`
}

func (h *GTDHandler) CreateTask(c *gin.Context) {
	var req taskReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	t, err := h.svc.CreateTask(c.Request.Context(), mw.UserID(c), service.CreateTaskInput{
		Title: req.Title, Notes: req.Notes,
		ProjectID: req.ProjectID, ContextID: req.ContextID,
		Status: req.Status, Energy: req.Energy,
		EstimatedMinutes: req.EstimatedMinutes, Priority: req.Priority,
		DueDate: req.DueDate, DeferDate: req.DeferDate, Tags: req.Tags,
	})
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusCreated, t)
}

type taskPatchReq struct {
	Title            *string    `json:"title"             binding:"omitempty,min=1,max=200"`
	Notes            *string    `json:"notes"`
	ProjectID        *uuid.UUID `json:"project_id"`
	ContextID        *uuid.UUID `json:"context_id"`
	Status           *string    `json:"status"            binding:"omitempty,oneof=inbox next_action waiting scheduled done"`
	Energy           *string    `json:"energy"            binding:"omitempty,oneof=low medium high"`
	EstimatedMinutes *int       `json:"estimated_minutes" binding:"omitempty,min=1"`
	Priority         *int       `json:"priority"`
	DueDate          *time.Time `json:"due_date"`
	DeferDate        *time.Time `json:"defer_date"`
	Tags             *[]string  `json:"tags"`
}

func (h *GTDHandler) UpdateTask(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	var req taskPatchReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	t, err := h.svc.UpdateTask(c.Request.Context(), mw.UserID(c), id, service.UpdateTaskInput{
		Title: req.Title, Notes: req.Notes,
		ProjectID: req.ProjectID, ContextID: req.ContextID,
		Status: req.Status, Energy: req.Energy,
		EstimatedMinutes: req.EstimatedMinutes, Priority: req.Priority,
		DueDate: req.DueDate, DeferDate: req.DeferDate, Tags: req.Tags,
	})
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, t)
}

func (h *GTDHandler) GetTask(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	t, err := h.svc.GetTask(c.Request.Context(), mw.UserID(c), id)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, t)
}

func (h *GTDHandler) DeleteTask(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	if err := h.svc.DeleteTask(c.Request.Context(), mw.UserID(c), id); err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusNoContent, nil)
}

func (h *GTDHandler) ListTasks(c *gin.Context) {
	page := queryPage(c)
	filter := domain.TaskFilter{
		Status:    queryStrPtr(c, "status"),
		ProjectID: queryUUIDPtr(c, "project_id"),
		ContextID: queryUUIDPtr(c, "context_id"),
		DueBefore: queryTimePtr(c, "due_before"),
		Tag:       queryStrPtr(c, "tag"),
		Page:      page,
	}
	items, total, err := h.svc.ListTasks(c.Request.Context(), mw.UserID(c), filter)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OKWithMeta(c, http.StatusOK, items, domain.PageMeta{
		Limit: page.Limit, Offset: page.Offset, Total: total,
	})
}

// ----- Weekly review -----

func (h *GTDHandler) WeeklyReview(c *gin.Context) {
	r, err := h.svc.WeeklyReview(c.Request.Context(), mw.UserID(c))
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, r)
}
