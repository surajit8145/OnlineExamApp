package com.example.onlineexamapp.repositories

import com.example.onlineexamapp.models.Response
import com.google.firebase.firestore.FirebaseFirestore

class ResponseRepository {

    private val db = FirebaseFirestore.getInstance()

    // ✅ 1. Save a response (submit response)
    fun submitResponse(
        responseId: String,
        examId: String,
        studentId: String,
        questionId: String,
        selectedAnswer: String,
        correct: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val response = Response(
            id = responseId,
            examId = examId,
            studentId = studentId,
            questionId = questionId,
            selectedAnswer = selectedAnswer,
            correct = correct
        )

        db.collection("responses")
            .document(responseId)
            .set(response)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    // ✅ 2. Get responses by exam and student (already you had it)
    fun getResponsesByExamAndStudent(examId: String, studentId: String, callback: (List<Response>) -> Unit) {
        db.collection("responses")
            .whereEqualTo("examId", examId)
            .whereEqualTo("studentId", studentId)
            .get()
            .addOnSuccessListener { result ->
                val responses = mutableListOf<Response>()
                for (document in result) {
                    val response = document.toObject(Response::class.java)
                    responses.add(response)
                }
                callback(responses)
            }
            .addOnFailureListener {
                callback(emptyList()) // If there's an error, return an empty list
            }
    }
}
