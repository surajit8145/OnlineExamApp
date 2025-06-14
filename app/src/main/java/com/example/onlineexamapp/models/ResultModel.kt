package com.example.onlineexamapp.models

// This data class represents the result of a student's exam attempt
data class ResultModel(
    val id: String = "",            // Unique ID for the result entry (e.g., "result168....")
    val examId: String = "",        // Reference to the exam this result is for
    val userId: String = "",        // ID of the student who took the exam

    val score: Double = 0.0,        // The number of correct answers (raw score)
    val totalMarks: Double = 0.0,   // Total number of questions (used to calculate percentage if needed)
    val percentage: Double = 0.0,   // Hare store percentage value

    var examTitle: String = "",     // The title of the exam (e.g., "Cloud Computing Basics")
    var subject: String = "",       // The subject of the exam (e.g., "Cloud Computing")
    var studentName: String = "" ,   // Display name of the student for easier identification
    val examDate: String // 🆕 added field

)
