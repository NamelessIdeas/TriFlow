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

type NoteHandler struct{ svc *service.NoteService }

func NewNoteHandler(s *service.NoteService) *NoteHandler { return &NoteHandler{svc: s} }

type noteCreateReq struct {
	Title        string   `json:"title"         binding:"required,min=1,max=200"`
	ContentMD    string   `json:"content_md"`
	PARACategory string   `json:"para_category" binding:"omitempty,oneof=project area resource archive"`
	Tags         []string `json:"tags"`
}

func (h *NoteHandler) Create(c *gin.Context) {
	var req noteCreateReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	n, err := h.svc.Create(c.Request.Context(), mw.UserID(c), service.CreateNoteInput{
		Title: req.Title, ContentMD: req.ContentMD,
		PARACategory: req.PARACategory, Tags: req.Tags,
	})
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusCreated, n)
}

type notePatchReq struct {
	Title        *string   `json:"title"         binding:"omitempty,min=1,max=200"`
	ContentMD    *string   `json:"content_md"`
	PARACategory *string   `json:"para_category" binding:"omitempty,oneof=project area resource archive"`
	Tags         *[]string `json:"tags"`
}

func (h *NoteHandler) Update(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	var req notePatchReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	n, err := h.svc.Update(c.Request.Context(), mw.UserID(c), id, service.UpdateNoteInput{
		Title: req.Title, ContentMD: req.ContentMD,
		PARACategory: req.PARACategory, Tags: req.Tags,
	})
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, n)
}

func (h *NoteHandler) Get(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	n, err := h.svc.Get(c.Request.Context(), mw.UserID(c), id)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, n)
}

func (h *NoteHandler) Delete(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	if err := h.svc.Delete(c.Request.Context(), mw.UserID(c), id); err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusNoContent, nil)
}

func (h *NoteHandler) List(c *gin.Context) {
	page := queryPage(c)
	f := domain.NoteFilter{
		PARACategory: queryStrPtr(c, "para_category"),
		Tag:          queryStrPtr(c, "tag"),
		Search:       queryStrPtr(c, "q"),
		Page:         page,
	}
	items, total, err := h.svc.List(c.Request.Context(), mw.UserID(c), f)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OKWithMeta(c, http.StatusOK, items, domain.PageMeta{
		Limit: page.Limit, Offset: page.Offset, Total: total,
	})
}

func (h *NoteHandler) Backlinks(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	items, err := h.svc.Backlinks(c.Request.Context(), mw.UserID(c), id)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, items)
}

func (h *NoteHandler) Outgoing(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	items, err := h.svc.Outgoing(c.Request.Context(), mw.UserID(c), id)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, items)
}

type linkReq struct {
	TargetNoteID uuid.UUID `json:"target_note_id" binding:"required"`
}

func (h *NoteHandler) Link(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	var req linkReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	if err := h.svc.Link(c.Request.Context(), mw.UserID(c), id, req.TargetNoteID); err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusNoContent, nil)
}

func (h *NoteHandler) Unlink(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	target, err := parseUUID(c.Param("targetId"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	if err := h.svc.Unlink(c.Request.Context(), mw.UserID(c), id, target); err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusNoContent, nil)
}

type refReq struct {
	RefType string    `json:"ref_type" binding:"required,oneof=task project"`
	RefID   uuid.UUID `json:"ref_id"   binding:"required"`
}

func (h *NoteHandler) AttachRef(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	var req refReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	if err := h.svc.AttachRef(c.Request.Context(), mw.UserID(c), id, req.RefType, req.RefID); err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusNoContent, nil)
}

func (h *NoteHandler) DetachRef(c *gin.Context) {
	id, err := parseUUID(c.Param("id"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	refType := c.Param("refType")
	refID, err := parseUUID(c.Param("refId"))
	if err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	if err := h.svc.DetachRef(c.Request.Context(), mw.UserID(c), id, refType, refID); err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusNoContent, nil)
}
