package com.example.onlineexamapp.models

data class Response(
    val id: String = "", // Auto ID
    val examId: String = "",
    val studentId: String = "",
    val questionId: String = "",
    val selectedAnswer: String = "",
    val correct: Boolean = false
)
