package com.example.onlineexamapp.repositories

import android.util.Log
import com.example.onlineexamapp.models.Response
import com.google.firebase.firestore.FirebaseFirestore

class ResponseRepository {
    private val db = FirebaseFirestore.getInstance()

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
        val responseData = hashMapOf(
            "id" to responseId,
            "examId" to examId,
            "studentId" to studentId,
            "questionId" to questionId,
            "selectedAnswer" to selectedAnswer,
            "correct" to correct
        )

        db.collection("responses")
            .document(responseId)
            .set(responseData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    // ✅ Fetch responses by examId
    fun getResponsesByExam(examId: String, onComplete: (List<Response>) -> Unit) {
        db.collection("responses")
            .whereEqualTo("examId", examId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val responses = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Response::class.java)?.copy(id = document.id)
                }
                onComplete(responses)
            }
            .addOnFailureListener { exception ->
                Log.e("ResponseRepository", "Error fetching responses", exception)
                onComplete(emptyList()) // Return empty list on failure
            }
    }
}
