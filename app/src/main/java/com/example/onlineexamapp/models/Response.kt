package com.example.onlineexamapp.models

data class Response(
    var id: String = "",
    val examId: String = "",
    val studentId: String = "",
    val questionId: String = "",
    val selectedAnswer: String = "",
    val correct: Boolean = false,
    var question: Question? = null // Include Question data
)

