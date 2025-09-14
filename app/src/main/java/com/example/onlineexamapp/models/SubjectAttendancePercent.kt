package com.example.onlineexamapp.models

data class SubjectAttendancePercent(
    val subjectName: String = "",
    val percentage: Int = 0 // store as number, easier for chart
)
