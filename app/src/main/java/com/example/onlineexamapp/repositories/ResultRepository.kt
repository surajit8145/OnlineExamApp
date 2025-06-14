package com.example.onlineexamapp.repositories

import android.util.Log
import com.example.onlineexamapp.models.ResultModel
import com.google.firebase.firestore.FirebaseFirestore

class ResultRepository {
    private val db = FirebaseFirestore.getInstance()

    fun submitResult(
        examId: String,
        studentId: String,
        score: Double,
        totalMarks: Double,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val resultData = hashMapOf(
            "examId" to examId,
            "studentId" to studentId,
            "score" to score,
            "totalMarks" to totalMarks
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
                    val score = document.getDouble("score") ?: 0.0
                    val totalMarks = document.getDouble("totalMarks") ?: 0.0

                    ResultModel(
                        id = id,
                        examId = examId,
                        userId = studentId,
                        score = score,
                        totalMarks = totalMarks,
                        percentage = if (totalMarks > 0) (score / totalMarks) * 100 else 0.0,
                        examTitle = "",
                        subject = "",
                        studentName = "",
                        examDate = ""
                    )
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
                    val score = document.getDouble("score") ?: 0.0
                    val totalMarks = document.getDouble("totalMarks") ?: 0.0

                    ResultModel(
                        id = id,
                        examId = examId,
                        userId = studentId,
                        score = score,
                        totalMarks = totalMarks,
                        percentage = if (totalMarks > 0) (score / totalMarks) * 100 else 0.0,
                        examTitle = "",
                        subject = "",
                        studentName = "",
                        examDate = ""
                    )
                }
                onComplete(results)
            }
            .addOnFailureListener { exception ->
                Log.e("ResultRepository", "Error fetching student results", exception)
                onComplete(emptyList())
            }
    }
}
