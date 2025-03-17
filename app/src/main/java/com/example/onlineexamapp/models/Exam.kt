package com.example.onlineexamapp.models

data class Exam(
    var id: String = "",
    var title: String = "",
    var subject: String = "",
    var date: String = "",     // Exam date (e.g., "2025-03-16")
    var time: String = "",     // Exam time (e.g., "10:00 AM")
    var duration: Int = 0,     // Duration in minutes
    var createdBy: String = ""
)
