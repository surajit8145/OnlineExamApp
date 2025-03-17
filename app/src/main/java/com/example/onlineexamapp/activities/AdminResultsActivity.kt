package com.example.onlineexamapp.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.databinding.ActivityAdminResultsBinding
import com.google.firebase.firestore.FirebaseFirestore

class AdminResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminResultsBinding
    private lateinit var db: FirebaseFirestore
    private val resultsList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        fetchResults()
    }

    private fun fetchResults() {
        db.collection("results").get()
            .addOnSuccessListener { documents ->
                resultsList.clear()
                for (document in documents) {
                    val userId = document.getString("userId") ?: "Unknown"
                    val examId = document.getString("examId") ?: "Unknown"
                    val score = document.getLong("score") ?: 0
                    val totalQuestions = document.getLong("totalQuestions") ?: 0

                    val resultText = "Student: $userId | Exam: $examId | Score: $score / $totalQuestions"
                    resultsList.add(resultText)
                }
                displayResults()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load results!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayResults() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resultsList)
        binding.lvResults.adapter = adapter
    }
}
