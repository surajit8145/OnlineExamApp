package com.example.onlineexamapp.models

import com.google.firebase.firestore.PropertyName

data class Announcement(
    var id: String = "", // Add this field to store Firestore document ID
    val title: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val content: String = "",

    @get:PropertyName("important")
    @set:PropertyName("important")
    var isImportant: Boolean = false
)
