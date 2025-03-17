package com.example.onlineexamapp.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineexamapp.R
import com.example.onlineexamapp.adapters.ResultsAdapter
import com.example.onlineexamapp.models.ResultModel
import com.google.firebase.firestore.FirebaseFirestore

class AdminViewResultsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvResults: RecyclerView
    private lateinit var tvNoResults: TextView
    private lateinit var spinnerExams: Spinner
    private var selectedExamId: String? = null
    private val examList = mutableListOf<Pair<String, String>>() // List of (examId, title)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_view_results)

        db = FirebaseFirestore.getInstance()
        rvResults = findViewById(R.id.rvResults)
        tvNoResults = findViewById(R.id.tvNoResults)
        spinnerExams = findViewById(R.id.spinnerExams)

        rvResults.layoutManager = LinearLayoutManager(this)

        fetchExamList()

        spinnerExams.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    selectedExamId = examList[position - 1].first
                    fetchResultsForExam(selectedExamId!!)
                } else {
                    rvResults.adapter = null
                    tvNoResults.visibility = View.VISIBLE
                    tvNoResults.text = "Select an exam to view results."
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchExamList() {
        db.collection("exams").get().addOnSuccessListener { exams ->
            val examTitles = mutableListOf("Select Exam") // Default option
            for (exam in exams) {
                val examId = exam.id
                val title = exam.getString("title") ?: "Unknown Title"
                examList.add(Pair(examId, title))
                examTitles.add(title)
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, examTitles)
            spinnerExams.adapter = adapter
        }
    }

    private fun fetchResultsForExam(examId: String) {
        db.collection("results").whereEqualTo("examId", examId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    tvNoResults.text = "No results available for this exam."
                    tvNoResults.visibility = View.VISIBLE
                    rvResults.adapter = null
                } else {
                    val resultsList = mutableListOf<ResultModel>()
                    val studentMap = mutableMapOf<String, String>()  // Map<studentId, studentName>

                    for (document in result) {
                        val id = document.id
                        val studentId = document.getString("userId") ?: "Unknown"
                        val score = document.getDouble("score") ?: 0.0
                        val totalMarks = document.getDouble("totalMarks") ?: 0.0
                        resultsList.add(ResultModel(id, examId, studentId, score, totalMarks))
                    }

                    // Fetch exam details (title, subject) using examId
                    db.collection("exams").document(examId).get().addOnSuccessListener { examDoc ->
                        val examTitle = examDoc.getString("title") ?: "Unknown Exam"
                        val subject = examDoc.getString("subject") ?: "Unknown Subject"

                        // Fetch student names
                        db.collection("users").get().addOnSuccessListener { users ->
                            for (user in users) {
                                val studentId = user.id
                                val name = user.getString("name") ?: "Unknown Student"
                                studentMap[studentId] = name
                            }

                            resultsList.forEach { result ->
                                result.studentName = studentMap[result.studentId] ?: "Unknown Student"
                                result.examTitle = examTitle  // ✅ Add exam title
                                result.subject = subject      // ✅ Add subject name
                            }

                            rvResults.adapter = ResultsAdapter(resultsList)
                            tvNoResults.visibility = View.GONE
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching results", Toast.LENGTH_SHORT).show()
            }
    }

}
