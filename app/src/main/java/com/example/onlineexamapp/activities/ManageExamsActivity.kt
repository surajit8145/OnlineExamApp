package com.example.onlineexamapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineexamapp.R
import com.example.onlineexamapp.adapters.ExamAdapter
import com.example.onlineexamapp.models.Exam
import com.google.firebase.firestore.FirebaseFirestore

class ManageExamsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var btnAddExam: Button
    private lateinit var recyclerViewExams: RecyclerView
    private lateinit var examAdapter: ExamAdapter
    private val examList = mutableListOf<Exam>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_exams)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize UI Components
        btnAddExam = findViewById(R.id.btnAddExam)
        recyclerViewExams = findViewById(R.id.recyclerViewExams)

        // Setup RecyclerView
        setupRecyclerView()

        // Load Exams from Firestore
        loadExams()

        // Add Exam Button Click
        btnAddExam.setOnClickListener {
            startActivity(Intent(this, CreateExamActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        recyclerViewExams.layoutManager = LinearLayoutManager(this)
        examAdapter = ExamAdapter(examList) { selectedExam ->
            val intent = Intent(this, EditExamActivity::class.java)
            intent.putExtra("examId", selectedExam.id)
            startActivity(intent)
        }
        recyclerViewExams.adapter = examAdapter
    }

    private fun loadExams() {
        db.collection("exams")
            .orderBy("date") // Sorting exams by date
            .get()
            .addOnSuccessListener { documents ->
                examList.clear() // Clear previous data

                if (documents.isEmpty) {
                    Toast.makeText(this, "No exams found!", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in documents) {
                        val examData = document.data

                        val exam = Exam(
                            id = document.id,
                            title = examData["title"] as? String ?: "Untitled",
                            subject = examData["subject"] as? String ?: "Unknown",
                            date = examData["date"] as? String ?: "Not Set",
                            duration = (examData["duration"] as? Number)?.toInt() ?: 0,
                            createdBy = examData["createdBy"] as? String ?: "Unknown"
                        )
                        examList.add(exam)
                    }
                    examAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load exams: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
