// Package handler contiene gli HTTP handler Gin.
//
// Convenzioni:
//   - Binding del body con tag binding+validate.
//   - Errori di binding → 400 + middleware.HandleError(domain.ErrInvalidInput).
//   - Errori di service → middleware.HandleError(err).
//   - Risposte sempre via response.OK / OKWithMeta / Err.
package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"github.com/triflow/backend/internal/domain"
	mw "github.com/triflow/backend/internal/middleware"
	"github.com/triflow/backend/internal/service"
	"github.com/triflow/backend/pkg/response"
)

type AuthHandler struct{ svc *service.AuthService }

func NewAuthHandler(s *service.AuthService) *AuthHandler { return &AuthHandler{svc: s} }

type registerReq struct {
	Email       string `json:"email"        binding:"required,email"`
	Password    string `json:"password"     binding:"required,min=8"`
	DisplayName string `json:"display_name" binding:"required,min=1,max=80"`
}

func (h *AuthHandler) Register(c *gin.Context) {
	var req registerReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	u, err := h.svc.Register(c.Request.Context(), req.Email, req.Password, req.DisplayName)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusCreated, u)
}

type loginReq struct {
	Email    string `json:"email"    binding:"required,email"`
	Password string `json:"password" binding:"required"`
}

type loginResp struct {
	User   *domain.User       `json:"user"`
	Tokens *service.TokenPair `json:"tokens"`
}

func (h *AuthHandler) Login(c *gin.Context) {
	var req loginReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	u, pair, err := h.svc.Login(c.Request.Context(), req.Email, req.Password)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, loginResp{User: u, Tokens: pair})
}

type refreshReq struct {
	RefreshToken string `json:"refresh_token" binding:"required"`
}

func (h *AuthHandler) Refresh(c *gin.Context) {
	var req refreshReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	pair, err := h.svc.Refresh(c.Request.Context(), req.RefreshToken)
	if err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusOK, pair)
}

func (h *AuthHandler) Logout(c *gin.Context) {
	var req refreshReq
	if err := c.ShouldBindJSON(&req); err != nil {
		mw.HandleError(c, domain.ErrInvalidInput)
		return
	}
	if err := h.svc.Logout(c.Request.Context(), req.RefreshToken); err != nil {
		mw.HandleError(c, err)
		return
	}
	response.OK(c, http.StatusNoContent, nil)
}
