// Package response definisce il wrapper JSON uniforme per tutte le risposte.
//
// Schema:
//
//	{ "success": true,  "data": <payload>, "meta": <opt> }
//	{ "success": false, "error": { "code": "...", "message": "..." } }
package response

import (
	"github.com/gin-gonic/gin"
)

type Envelope struct {
	Success bool        `json:"success"`
	Data    interface{} `json:"data,omitempty"`
	Meta    interface{} `json:"meta,omitempty"`
	Error   *APIError   `json:"error,omitempty"`
}

type APIError struct {
	Code    string                 `json:"code"`
	Message string                 `json:"message"`
	Details map[string]interface{} `json:"details,omitempty"`
}

func OK(c *gin.Context, status int, data interface{}) {
	c.JSON(status, Envelope{Success: true, Data: data})
}

func OKWithMeta(c *gin.Context, status int, data, meta interface{}) {
	c.JSON(status, Envelope{Success: true, Data: data, Meta: meta})
}

func Err(c *gin.Context, status int, code, message string, details map[string]interface{}) {
	c.AbortWithStatusJSON(status, Envelope{
		Success: false,
		Error:   &APIError{Code: code, Message: message, Details: details},
	})
}
