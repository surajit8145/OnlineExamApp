package com.example.onlineexamapp.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.example.onlineexamapp.models.ResultModel

class ResultRepository {
    private val db = FirebaseFirestore.getInstance()

    fun submitResult(
        examId: String,
        studentId: String,
        score: Double,  // 🔹 Change from Int to Double
        totalMarks: Double, // 🔹 Change from Int to Double
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val resultData = hashMapOf(
            "examId" to examId,
            "studentId" to studentId,
            "score" to score,  // ✅ Now a Double
            "totalMarks" to totalMarks  // ✅ Now a Double
        )

        db.collection("results")
            .add(resultData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun getAllResults(onComplete: (List<ResultModel>) -> Unit) {
        db.collection("results")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val results = querySnapshot.documents.mapNotNull { document ->
                    val id = document.id
                    val examId = document.getString("examId") ?: ""
                    val studentId = document.getString("studentId") ?: ""

                    // 🔹 Use .getDouble() instead of .toInt()
                    val score = document.getDouble("score") ?: 0.0
                    val totalMarks = document.getDouble("totalMarks") ?: 0.0

                    ResultModel(id, examId, studentId, score, totalMarks)
                }
                onComplete(results)
            }
            .addOnFailureListener { exception ->
                Log.e("ResultRepository", "Error fetching results", exception)
                onComplete(emptyList())
            }
    }

    fun getResultsByStudent(studentId: String, onComplete: (List<ResultModel>) -> Unit) {
        db.collection("results")
            .whereEqualTo("studentId", studentId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val results = querySnapshot.documents.mapNotNull { document ->
                    val id = document.id
                    val examId = document.getString("examId") ?: ""

                    // 🔹 Use .getDouble() instead of .toInt()
                    val score = document.getDouble("score") ?: 0.0
                    val totalMarks = document.getDouble("totalMarks") ?: 0.0

                    ResultModel(id, examId, studentId, score, totalMarks)
                }
                onComplete(results)
            }
            .addOnFailureListener { exception ->
                Log.e("ResultRepository", "Error fetching student results", exception)
                onComplete(emptyList())
            }
    }

}
