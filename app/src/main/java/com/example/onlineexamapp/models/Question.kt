package com.example.onlineexamapp.models

data class Question(
    var id: String = "",                // Unique question ID (Firestore Auto ID)
    var examId: String = "",            // Links the question to an exam
    var questionText: String = "",      // The actual question
    var options: MutableList<String> = mutableListOf(), // List of answer choices
    var correctAnswer: String = "",     // Correct answer
    var selectedAnswer: String? = null  // Student's selected answer (nullable)
)
