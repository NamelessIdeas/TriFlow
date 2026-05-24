package domain

// QuizAnswers raccoglie le risposte del questionario.
// I valori sono enum stringa per essere leggibili da Android senza traduzione.
type QuizAnswers struct {
	// MainProblem: che problema principale stai cercando di risolvere?
	// "overwhelm" | "distraction" | "knowledge_loss"
	MainProblem string `json:"main_problem" validate:"required,oneof=overwhelm distraction knowledge_loss"`

	// WorkStyle: come ti piace lavorare?
	// "structured" | "flexible" | "creative"
	WorkStyle string `json:"work_style" validate:"required,oneof=structured flexible creative"`

	// SetupTolerance: quanto sei disposto a configurare un sistema?
	// "low" | "medium" | "high"
	SetupTolerance string `json:"setup_tolerance" validate:"required,oneof=low medium high"`

	// Goal: qual è il tuo obiettivo?
	// "ship_tasks" | "focus_time" | "build_knowledge"
	Goal string `json:"goal" validate:"required,oneof=ship_tasks focus_time build_knowledge"`
}

// MethodScore è il punteggio per uno dei tre metodi.
type MethodScore struct {
	Method      string `json:"method"`       // "gtd" | "pomodoro" | "second_brain"
	Score       int    `json:"score"`        // 0..100
	Explanation string `json:"explanation"`
}

// QuizResult è la risposta dell'endpoint /quiz.
type QuizResult struct {
	RecommendedMethod string        `json:"recommended_method"`
	Reasoning         string        `json:"reasoning"`
	Scores            []MethodScore `json:"scores"`
}
