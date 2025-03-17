package com.example.onlineexamapp.activities

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineexamapp.R
import com.example.onlineexamapp.adapters.ResultsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.onlineexamapp.models.ResultModel

class ViewResultsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var rvResults: RecyclerView
    private lateinit var tvNoResults: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_results)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        rvResults = findViewById(R.id.rvResults)
        tvNoResults = findViewById(R.id.tvNoResults)

        rvResults.layoutManager = LinearLayoutManager(this)

        fetchResults()
    }
    private fun fetchExamDetails(examId: String, callback: (String, String) -> Unit) {
        db.collection("exams").document(examId)
            .get()
            .addOnSuccessListener { document ->
                val examTitle = document.getString("title") ?: "Unknown Title"
                val subject = document.getString("subject") ?: "Unknown Subject"
                callback(examTitle, subject)
            }
            .addOnFailureListener {
                callback("Unknown Title", "Unknown Subject")
            }
    }
    private fun fetchStudentName(studentId: String, callback: (String) -> Unit) {
        db.collection("users").document(studentId)
            .get()
            .addOnSuccessListener { document ->
                val studentName = document.getString("name") ?: "Unknown Student"
                callback(studentName)
            }
            .addOnFailureListener {
                callback("Unknown Student")
            }
    }


    private fun fetchResults() {
        val studentId = auth.currentUser?.uid ?: return

        db.collection("results")
            .whereEqualTo("userId", studentId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    tvNoResults.text = "No results available."
                    tvNoResults.visibility = TextView.VISIBLE
                } else {
                    val resultsList = mutableListOf<ResultModel>()

                    for (document in result) {
                        val id = document.id
                        val examId = document.getString("examId") ?: "Unknown Exam"
                        val score = document.getDouble("score") ?: 0.0
                        val totalMarks = document.getDouble("totalMarks") ?: 0.0

                        fetchExamDetails(examId) { examTitle, subject ->
                            fetchStudentName(studentId) { studentName ->
                                resultsList.add(ResultModel(id, examId, studentId, score, totalMarks, examTitle, subject, studentName))

                                // Update RecyclerView once all data is fetched
                                if (resultsList.size == result.size()) {
                                    rvResults.adapter = ResultsAdapter(resultsList)
                                }
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching results", Toast.LENGTH_SHORT).show()
            }
    }

}
