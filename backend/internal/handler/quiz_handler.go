package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"github.com/triflow/backend/internal/domain"
	mw "github.com/triflow/backend/internal/middleware"
	"github.com/triflow/backend/internal/service"
	"github.com/triflow/backend/pkg/response"
)

type QuizHandler struct{ svc *service.QuizService }

func NewQuizHandler(s *service.QuizService) *QuizHandler { return &QuizHandler{svc: s} }

// Score è un endpoint pubblico (non richiede auth): è un quiz introduttivo.
func (h *QuizHandler) Score(c *gin.Context) {
	var req domain.QuizAnswers
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	res, err := h.svc.Score(req)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, res)
}
