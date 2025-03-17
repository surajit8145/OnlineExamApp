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

        // Initialize View Binding
        binding = ActivityTakeExamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get examId from intent
        examId = intent.getStringExtra("examId")

        // Initialize RecyclerView
        binding.rvQuestions.layoutManager = LinearLayoutManager(this)
        questionAdapter = QuestionAdapter(questionList, isEditable = false)
        binding.rvQuestions.adapter = questionAdapter

        examId?.let {
            loadExamDuration(it)  // Fetch exam duration
            loadQuestions(it)  // Load exam questions
        } ?: run {
            Log.e("TakeExamActivity", "No exam ID provided")
        }

        // Submit Exam Button Click
        binding.btnSubmitExam.setOnClickListener {
            submitExam()
        }
    }

    private fun loadExamDuration(examId: String) {
        db.collection("exams").document(examId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val durationMinutes = document.getLong("duration") ?: 0
                    examDurationMillis = TimeUnit.MINUTES.toMillis(durationMinutes) // Convert to milliseconds
                    startCountdownTimer()
                } else {
                    Log.e("TakeExamActivity", "Exam document not found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("TakeExamActivity", "Error fetching exam duration", e)
            }
    }

    private fun startCountdownTimer() {
        countDownTimer = object : CountDownTimer(examDurationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

                binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds) // Update UI timer
            }

            override fun onFinish() {
                binding.tvTimer.text = "00:00"
                Toast.makeText(this@TakeExamActivity, "Time's up! Submitting exam...", Toast.LENGTH_SHORT).show()
                submitExam() // Auto-submit when time runs out
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

                    // Ensure options list is properly retrieved
                    val options = document.get("options") as? List<*> ?: emptyList<Any>()
                    val optionsList = options.filterIsInstance<String>()

                    val correctAnswer = document.getString("correctAnswer") ?: ""

                    questionList.add(Question(id, examId, questionText, optionsList.toMutableList(), correctAnswer))
                }
                questionAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("TakeExamActivity", "Error loading questions", e)
            }
    }

    private fun submitExam() {
        if (userId == null || examId == null) {
            Toast.makeText(this, "User or exam ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val responseRepository = ResponseRepository()
        var correctAnswers = 0
        val totalQuestions = questionList.size

        for (question in questionList) {
            val responseId = "$userId-${question.id}"  // Unique ID for response
            val correct = question.selectedAnswer == question.correctAnswer

            if (correct) correctAnswers++  // ✅ Count correct answers

            responseRepository.submitResponse(
                responseId,
                examId!!,
                userId!!,
                question.id,
                question.selectedAnswer ?: "",
                correct,
                onSuccess = {
                    Log.d("TakeExamActivity", "Response saved successfully")
                },
                onFailure = { e ->
                    Log.e("TakeExamActivity", "Error saving response", e)
                }
            )
        }

        // ✅ Calculate score percentage
        val score = (correctAnswers.toDouble() / totalQuestions) * 100

        // ✅ Save result to Firestore
        saveResultToFirestore(score)
    }

    private fun saveResultToFirestore(score: Double) {
        val resultData = hashMapOf(
            "examId" to examId,
            "userId" to userId,
            "score" to score
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("results") // 🔹 Store results in "results" collection
            .add(resultData)
            .addOnSuccessListener {
                isExamSubmitted = true
                Toast.makeText(this, "Exam submitted successfully!", Toast.LENGTH_SHORT).show()
                finish() // Close activity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving result", Toast.LENGTH_SHORT).show()
                Log.e("TakeExamActivity", "Error saving result", e)
            }
    }

    // 🔥 Prevent Back Button Until Exam is Submitted
    override fun onBackPressed() {
        if (!isExamSubmitted) {
            AlertDialog.Builder(this)
                .setTitle("Exit Exam")
                .setMessage("You cannot leave until you submit the exam.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        } else {
            super.onBackPressed()
        }
    }

    // 🔥 Detect App Minimize or Home Button Press
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
                .setPositiveButton("OK") { _, _ -> }
                .setCancelable(false)
                .show()
        }
    }

    private fun isAppInBackground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = activityManager.runningAppProcesses
        return runningTasks.any { it.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel() // Stop timer when activity is destroyed
    }
}
