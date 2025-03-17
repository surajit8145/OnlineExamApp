package com.example.onlineexamapp.models

data class ResultModel(
    val id: String = "",
    val examId: String = "",
    val studentId: String = "",
    val score: Double = 0.0,
    val totalMarks: Double = 0.0,
    var examTitle: String = "",  // ✅ Change to var
    var subject: String = "",    // ✅ Change to var
    var studentName: String = "" // ✅ Change to var
)
