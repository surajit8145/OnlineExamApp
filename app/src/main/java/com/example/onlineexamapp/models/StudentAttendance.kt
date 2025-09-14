package com.example.onlineexamapp.models

data class StudentAttendance(
    val uid: String = "",
    val email: String = "",
    val date: String = "",
    val docId: String = ""   // Firestore document ID (needed for delete/update)
)
