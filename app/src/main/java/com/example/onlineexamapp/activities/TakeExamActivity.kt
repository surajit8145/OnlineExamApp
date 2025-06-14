package com.example.onlineexamapp.activities

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineexamapp.databinding.ActivityTakeExamBinding
import com.example.onlineexamapp.models.Question
import com.example.onlineexamapp.adapters.QuestionAdapter
import com.example.onlineexamapp.repositories.ResponseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class TakeExamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTakeExamBinding
    private lateinit var questionAdapter: QuestionAdapter
    private val questionList = mutableListOf<Question>()
    private val db = FirebaseFirestore.getInstance()
    private var examId: String? = null
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private var examDurationMillis: Long = 0
    private var countDownTimer: CountDownTimer? = null
    private var isExamSubmitted = false
    private var isMinimized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakeExamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        examId = intent.getStringExtra("examId")

        binding.rvQuestions.layoutManager = LinearLayoutManager(this)
        questionAdapter = QuestionAdapter(questionList, isEditable = true)
        binding.rvQuestions.adapter = questionAdapter

        examId?.let {
            loadExamDuration(it)
            loadQuestions(it)
        } ?: run {
            Log.e("TakeExamActivity", "No exam ID provided")
        }

        binding.btnSubmitExam.setOnClickListener {
            confirmSubmitExam()
        }
    }

    private fun loadExamDuration(examId: String) {
        db.collection("exams").document(examId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val durationMinutes = document.getLong("duration") ?: 0
                    examDurationMillis = TimeUnit.MINUTES.toMillis(durationMinutes)
                    startCountdownTimer()
                }
            }
            .addOnFailureListener {
                Log.e("TakeExamActivity", "Error fetching exam duration", it)
            }
    }

    private fun startCountdownTimer() {
        countDownTimer = object : CountDownTimer(examDurationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.tvTimer.text = "00:00"
                Toast.makeText(this@TakeExamActivity, "Time's up! Submitting exam...", Toast.LENGTH_SHORT).show()
                submitExam()
            }
        }.start()
    }

    private fun loadQuestions(examId: String) {
        db.collection("questions")
            .whereEqualTo("examId", examId)
            .get()
            .addOnSuccessListener { documents ->
                questionList.clear()
                for (document in documents) {
                    val id = document.id
                    val questionText = document.getString("question") ?: ""
                    val options = document.get("options") as? List<*> ?: emptyList<Any>()
                    val optionsList = options.filterIsInstance<String>()
                    val correctAnswer = document.getString("correctAnswer") ?: ""

                    questionList.add(
                        Question(id, examId, questionText, optionsList.toMutableList(), correctAnswer)
                    )
                }
                questionAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.e("TakeExamActivity", "Error loading questions", it)
            }
    }

    private fun confirmSubmitExam() {
        if (isExamSubmitted) return

        val unanswered = questionList.count { it.selectedAnswer == null }
        if (unanswered > 0) {
            AlertDialog.Builder(this)
                .setTitle("Unanswered Questions")
                .setMessage("You have $unanswered unanswered questions. Do you still want to submit?")
                .setPositiveButton("Yes") { _, _ -> submitExam() }
                .setNegativeButton("No", null)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Submit Exam")
                .setMessage("Are you sure you want to submit the exam?")
                .setPositiveButton("Submit") { _, _ -> submitExam() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun submitExam() {
        if (isExamSubmitted) return

        if (userId == null || examId == null) {
            Toast.makeText(this, "User or exam ID missing", Toast.LENGTH_SHORT).show()
            return
        }

        val responseRepository = ResponseRepository()
        var correctAnswers = 0
        val totalQuestions = questionList.size
        var responsesSaved = 0

        for (question in questionList) {
            val responseId = "$userId-${question.id}"
            val correct = question.selectedAnswer == question.correctAnswer
            if (correct) correctAnswers++

            responseRepository.submitResponse(
                responseId,
                examId!!,
                userId!!,
                question.id,
                question.selectedAnswer ?: "",
                correct,
                onSuccess = {
                    responsesSaved++
                    if (responsesSaved == totalQuestions) {
                        val score = correctAnswers.toDouble()
                        val totalMarks = totalQuestions.toDouble()
                        saveResultToFirestore(score, totalMarks)
                    }
                },
                onFailure = {
                    Toast.makeText(this, "Failed to save some responses", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun saveResultToFirestore(score: Double, totalMarks: Double) {
        val resultId = "result" + System.currentTimeMillis()

        // First, get student name from "users" collection using userId
        db.collection("users").document(userId!!).get()
            .addOnSuccessListener { userDoc ->
                val studentName = userDoc.getString("name") ?: "Unknown"

                // Now get exam title and subject
                db.collection("exams").document(examId!!).get()
                    .addOnSuccessListener { document ->
                        val examTitle = document.getString("title") ?: ""
                        val subject = document.getString("subject") ?: ""

                        val resultData = hashMapOf(
                            "id" to resultId,
                            "examId" to examId,
                            "userId" to userId,
                            "score" to score,
                            "totalMarks" to totalMarks,
                            "percentage" to String.format("%.2f", (score / totalMarks) * 100).toDouble(),
                            "examTitle" to examTitle,
                            "subject" to subject,
                            "studentName" to studentName
                        )

                        db.collection("results")
                            .document(resultId)
                            .set(resultData)
                            .addOnSuccessListener {
                                isExamSubmitted = true
                                AlertDialog.Builder(this)
                                    .setTitle("Exam Submitted")
                                    .setMessage("You scored: $score / $totalMarks")
                                    .setPositiveButton("OK") { _, _ -> finish() }
                                    .setCancelable(false)
                                    .show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save result", Toast.LENGTH_SHORT).show()
                                Log.e("TakeExamActivity", "Error saving result", it)
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to load exam info", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load user info", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onBackPressed() {
        if (!isExamSubmitted) {
            AlertDialog.Builder(this)
                .setTitle("Exit Exam")
                .setMessage("You cannot leave until you submit the exam.")
                .setPositiveButton("OK", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isExamSubmitted) {
            isMinimized = true
            showMinimizeWarning()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isExamSubmitted && isAppInBackground()) {
            isMinimized = true
            showMinimizeWarning()
        }
    }

    private fun showMinimizeWarning() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Warning!")
                .setMessage("You cannot minimize or leave the exam page. Please return to the exam.")
                .setPositiveButton("OK", null)
                .setCancelable(false)
                .show()
        }
    }

    private fun isAppInBackground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = activityManager.runningAppProcesses
        return runningTasks.any {
            it.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
