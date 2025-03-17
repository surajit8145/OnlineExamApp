package com.example.onlineexamapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineexamapp.databinding.ActivityExamListBinding
import com.example.onlineexamapp.models.Exam
import com.example.onlineexamapp.adapters.ExamAdapter
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ExamListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExamListBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var examAdapter: ExamAdapter
    private val examList = mutableListOf<Exam>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        setupRecyclerView()
        fetchExams()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewExams.layoutManager = LinearLayoutManager(this)

        examAdapter = ExamAdapter(examList) { selectedExam ->
            val intent = Intent(this, TakeExamActivity::class.java)
            intent.putExtra("examId", selectedExam.id)
            startActivity(intent)
        }

        binding.recyclerViewExams.adapter = examAdapter
    }

    private fun fetchExams() {
        db.collection("exams").get()
            .addOnSuccessListener { documents ->
                examList.clear()

                for (document in documents) {
                    val exam = document.toObject(Exam::class.java).apply {
                        id = document.id
                    }

                    // ✅ Check if date and time are missing
                    if (exam.date.isEmpty() || exam.time.isEmpty()) {
                        Log.e("ExamListActivity", "Skipping exam: Missing date or time")
                        continue
                    }

                    examList.add(exam)
                }

                // ✅ Sort exams by date (nearest first)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                examList.sortBy {
                    try {
                        dateFormat.parse("${it.date} ${it.time}")?.time ?: Long.MAX_VALUE
                    } catch (e: Exception) {
                        Log.e("ExamListActivity", "Invalid date format for exam: ${it.title}")
                        Long.MAX_VALUE
                    }
                }

                runOnUiThread {
                    examAdapter.notifyDataSetChanged()
                    binding.recyclerViewExams.visibility = if (examList.isEmpty()) View.GONE else View.VISIBLE
                    if (examList.isEmpty()) {
                        Toast.makeText(this, "No exams available", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                runOnUiThread {
                    Toast.makeText(this, "Failed to load exams!", Toast.LENGTH_SHORT).show()
                    Log.e("ExamListActivity", "Error fetching exams", e)
                }
            }
    }
}
