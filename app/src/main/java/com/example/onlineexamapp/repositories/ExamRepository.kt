package com.example.onlineexamapp.repositories

import com.example.onlineexamapp.models.Exam
import com.google.firebase.firestore.FirebaseFirestore

object ExamRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getExams(callback: (List<Exam>) -> Unit) {
        db.collection("exams").get()
            .addOnSuccessListener { result ->
                val exams = result.map { it.toObject(Exam::class.java) }
                callback(exams)
            }
    }
}
