package com.example.onlineexamapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineexamapp.adapters.ExamAdapter
import com.example.onlineexamapp.databinding.ActivityExamListBinding
import com.example.onlineexamapp.models.Exam
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ExamListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExamListBinding
    private lateinit var db: FirebaseFirestore
    private val auth = FirebaseAuth.getInstance()

    private lateinit var upcomingAdapter: ExamAdapter
    private lateinit var runningAdapter: ExamAdapter
    private lateinit var attendedAdapter: ExamAdapter
    private lateinit var pastAdapter: ExamAdapter

    private val upcomingList = mutableListOf<Exam>()
    private val runningList = mutableListOf<Exam>()
    private val attendedList = mutableListOf<Exam>()
    private val pastList = mutableListOf<Exam>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        setupRecyclerViews()
        fetchExams()
    }

    private fun setupRecyclerViews() {
        binding.rvUpcomingExams.layoutManager = LinearLayoutManager(this)
        binding.rvRunningExams.layoutManager = LinearLayoutManager(this)
        binding.rvAttendedExams.layoutManager = LinearLayoutManager(this)
        binding.rvPastExams.layoutManager = LinearLayoutManager(this)

        upcomingAdapter = ExamAdapter(upcomingList) { }
        runningAdapter = ExamAdapter(runningList) { selectedExam ->
            showExamInstructionDialog(selectedExam)
        }
        attendedAdapter = ExamAdapter(attendedList) { }

        pastAdapter = ExamAdapter(pastList) { }





        binding.rvUpcomingExams.adapter = upcomingAdapter
        binding.rvRunningExams.adapter = runningAdapter
        binding.rvAttendedExams.adapter = attendedAdapter
        binding.rvPastExams.adapter = pastAdapter
    }

    private fun showExamInstructionDialog(exam: Exam) {
        val message = """
            📝 Exam: ${exam.title}
            📚 Subject: ${exam.subject}
            📅 Date: ${exam.date}
            ⏰ Time: ${exam.time}
            ⏳ Duration: ${exam.duration} minutes

            Please read all questions carefully.
            Once you start, you can't go back.
            Good luck!
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Exam Instructions")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Start") { dialog, _ ->
                val intent = Intent(this, TakeExamActivity::class.java)
                intent.putExtra("examId", exam.id)
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun fetchExams() {
        val currentUser = auth.currentUser ?: return

        db.collection("results")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { resultSnapshots ->
                val attendedExamIds = resultSnapshots.mapNotNull { it.getString("examId") }

                db.collection("exams").get()
                    .addOnSuccessListener { documents ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                        val now = System.currentTimeMillis()

                        upcomingList.clear()
                        runningList.clear()
                        attendedList.clear()
                        pastList.clear()

                        for (document in documents) {
                            val exam = document.toObject(Exam::class.java).apply { id = document.id }

                            // Filter: skip if date/time is empty OR subject is "quiz"
                            if (exam.date.isEmpty() || exam.time.isEmpty() || exam.subject.equals("quiz", ignoreCase = true)) continue

                            val startTime = try {
                                dateFormat.parse("${exam.date} ${exam.time}")?.time ?: continue
                            } catch (e: Exception) {
                                continue
                            }
                            val endTime = startTime + (3 * 60 * 60 * 1000) // 3 hours in ms

                            when {
                                attendedExamIds.contains(exam.id) -> attendedList.add(exam)
                                now < startTime -> upcomingList.add(exam)
                                now in startTime..endTime -> runningList.add(exam)
                                now > endTime -> pastList.add(exam)
                            }
                        }

                        runOnUiThread {
                            upcomingAdapter.notifyDataSetChanged()
                            runningAdapter.notifyDataSetChanged()
                            attendedAdapter.notifyDataSetChanged()
                            pastAdapter.notifyDataSetChanged()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to load exams!", Toast.LENGTH_SHORT).show()
                        Log.e("ExamListActivity", "Error fetching exams", e)
                    }
            }
    }
}