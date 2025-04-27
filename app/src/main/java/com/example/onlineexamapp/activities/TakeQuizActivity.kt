package com.example.onlineexamapp.activities

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.R
import com.example.onlineexamapp.databinding.ActivityTakeQuizBinding
import com.example.onlineexamapp.models.Question
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.*

class TakeQuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTakeQuizBinding

    private var userName: String = ""
    private var difficultyLevel: String = ""
    private var questionsList: MutableList<Question> = mutableListOf()
    private var currentQuestionIndex = 0
    private var timer: CountDownTimer? = null
    private var correctAnswersCount = 0
    private val db = FirebaseFirestore.getInstance()
    private var job: Job? = null
    private var selectedAnswer: String? = null
    private var isAnswerSelected = false // Track if an answer has been selected
    private var totalQuestions: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakeQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userName = intent.getStringExtra("USER_NAME") ?: ""
        difficultyLevel = intent.getStringExtra("DIFFICULTY_LEVEL") ?: ""

        setupQuestions()
    }

    private fun setupQuestions() {
        db.collection("exams")
            .whereEqualTo("subject", "quiz")
            .whereEqualTo("title", difficultyLevel)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    showError("No exam found for this difficulty level.")
                    return@addOnSuccessListener
                }

                val examDocument = result.documents.first()
                val examId = examDocument.id

                db.collection("questions")
                    .whereEqualTo("examId", examId)
                    .limit(20)
                    .get()
                    .addOnSuccessListener { questionResult ->
                        if (questionResult.isEmpty) {
                            showError("No questions found for this exam.")
                            return@addOnSuccessListener
                        }

                        questionsList = questionResult.toObjects<Question>().toMutableList()
                        totalQuestions = questionsList.size // get the total questions
                        if (questionsList.isNotEmpty()) {
                            questionsList.forEach { it.selectedAnswer = null }
                            showQuestion()
                        } else {
                            showError("No questions found in this exam.")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("TakeQuizActivity", "Error fetching questions: ${exception.message}")
                        showError("Error loading questions: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("TakeQuizActivity", "Error fetching exam: ${exception.message}")
                showError("Error loading exam: ${exception.message}")
            }
    }

    private fun setupTimer() {
        timer?.cancel() // Cancel any existing timer
        var totalTimeInSeconds = 30
        binding.tvTimer.text = formatTime(totalTimeInSeconds.toLong())

        timer = object : CountDownTimer(totalTimeInSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text = formatTime(millisUntilFinished / 1000)
            }

            override fun onFinish() {
                // Handle timeout: Show correct answer and go to next question
                if (!isAnswerSelected) { // Only show if no answer has been selected
                    showCorrectAnswer()
                }
            }
        }.start()
    }

    private fun formatTime(seconds: Long): String {

        return String.format("%02d",seconds)
    }

    private fun resetOptionsForNewQuestion() {
        binding.rbOption1.isEnabled = true
        binding.rbOption2.isEnabled = true
        binding.rbOption3.isEnabled = true
        binding.rbOption4.isEnabled = true
        binding.rbOption1.setTextColor(getColor(R.color.defaultOptionColor))
        binding.rbOption2.setTextColor(getColor(R.color.defaultOptionColor))
        binding.rbOption3.setTextColor(getColor(R.color.defaultOptionColor))
        binding.rbOption4.setTextColor(getColor(R.color.defaultOptionColor))
        binding.radioGroup.clearCheck()
        binding.radioGroup.isEnabled = true
        selectedAnswer = null
        isAnswerSelected = false // Reset the flag
        binding.btnNext.isEnabled = false // Disable next button
    }

    private fun showQuestion() {
        resetOptionsForNewQuestion()

        if (currentQuestionIndex >= 0 && currentQuestionIndex < questionsList.size) {
            val question = questionsList[currentQuestionIndex]
            // set the question number and total
            binding.tvQuestionNumber.text = "Q ${currentQuestionIndex + 1} / $totalQuestions"
            binding.tvQuestion.text = question.question
            binding.rbOption1.text = question.options.getOrNull(0) ?: ""
            binding.rbOption2.text = question.options.getOrNull(1) ?: ""
            binding.rbOption3.text = question.options.getOrNull(2) ?: ""
            binding.rbOption4.text = question.options.getOrNull(3) ?: ""

            when (question.selectedAnswer) {
                binding.rbOption1.text -> binding.rbOption1.isChecked = true
                binding.rbOption2.text -> binding.rbOption2.isChecked = true
                binding.rbOption3.text -> binding.rbOption3.isChecked = true
                binding.rbOption4.text -> binding.rbOption4.isChecked = true
            }

            // Detach any existing listener
            binding.rbOption1.setOnClickListener(null)
            binding.rbOption2.setOnClickListener(null)
            binding.rbOption3.setOnClickListener(null)
            binding.rbOption4.setOnClickListener(null)

            // Set new listener
            binding.rbOption1.setOnClickListener {
                handleOptionSelection(question, binding.rbOption1.text.toString())
            }
            binding.rbOption2.setOnClickListener {
                handleOptionSelection(question, binding.rbOption2.text.toString())
            }
            binding.rbOption3.setOnClickListener {
                handleOptionSelection(question, binding.rbOption3.text.toString())
            }
            binding.rbOption4.setOnClickListener {
                handleOptionSelection(question, binding.rbOption4.text.toString())
            }
            setupTimer() // Start the timer for the question
        } else {
            //If all questions are finished.
            finishQuiz()
        }
        binding.btnNext.setOnClickListener {
            goToNextQuestion()
        }
    }

    private fun handleOptionSelection(question: Question, selectedOptionText: String) {
        if (timer != null) {
            timer?.cancel()
        }
        saveSelectedAnswer(selectedOptionText)
        showAnswer()
        isAnswerSelected = true
        binding.btnNext.isEnabled = true // Enable the next button after an answer
    }

    private fun saveSelectedAnswer(selectedOptionText: String) {
        questionsList[currentQuestionIndex].selectedAnswer = selectedOptionText
        selectedAnswer = selectedOptionText
    }

    private fun showAnswer() {
        val question = questionsList[currentQuestionIndex]
        val correctAnswer = question.correctAnswer

        val selectedRadioButton: RadioButton? =
            when (selectedAnswer) {
                binding.rbOption1.text -> binding.rbOption1
                binding.rbOption2.text -> binding.rbOption2
                binding.rbOption3.text -> binding.rbOption3
                binding.rbOption4.text -> binding.rbOption4
                else -> null
            }

        if (selectedRadioButton != null) {
            if (selectedRadioButton.text.toString() == correctAnswer) {
                selectedRadioButton.setTextColor(getColor(R.color.green))
                correctAnswersCount++
            } else {
                selectedRadioButton.setTextColor(getColor(R.color.red))

                val correctRadioButtonView = when (correctAnswer) {
                    binding.rbOption1.text -> binding.rbOption1
                    binding.rbOption2.text -> binding.rbOption2
                    binding.rbOption3.text -> binding.rbOption3
                    binding.rbOption4.text -> binding.rbOption4
                    else -> null
                }
                correctRadioButtonView?.setTextColor(getColor(R.color.green))
            }
        } else {
            val correctRadioButtonView = when (correctAnswer) {
                binding.rbOption1.text -> binding.rbOption1
                binding.rbOption2.text -> binding.rbOption2
                binding.rbOption3.text -> binding.rbOption3
                binding.rbOption4.text -> binding.rbOption4
                else -> null
            }
            correctRadioButtonView?.setTextColor(getColor(R.color.green))
        }

        binding.rbOption1.isEnabled = false
        binding.rbOption2.isEnabled = false
        binding.rbOption3.isEnabled = false
        binding.rbOption4.isEnabled = false
        binding.radioGroup.isEnabled = false
    }

    private fun showCorrectAnswer() {
        val question = questionsList[currentQuestionIndex]
        val correctAnswer = question.correctAnswer

        val correctRadioButtonView = when (correctAnswer) {
            binding.rbOption1.text -> binding.rbOption1
            binding.rbOption2.text -> binding.rbOption2
            binding.rbOption3.text -> binding.rbOption3
            binding.rbOption4.text -> binding.rbOption4
            else -> null
        }
        correctRadioButtonView?.setTextColor(getColor(R.color.green))
        binding.rbOption1.isEnabled = false
        binding.rbOption2.isEnabled = false
        binding.rbOption3.isEnabled = false
        binding.rbOption4.isEnabled = false
        binding.radioGroup.isEnabled = false
        binding.btnNext.isEnabled = true // Enable next button
    }

    private fun goToNextQuestion() {
        if (job?.isActive == true) {
            job?.cancel()
        }
        currentQuestionIndex++
        if (currentQuestionIndex < questionsList.size) {
            showQuestion()
        } else {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        timer?.cancel()
        var answeredCorrectly = 0;
        for (q in questionsList) {
            if (q.selectedAnswer != null && q.selectedAnswer.equals(q.correctAnswer)) {
                answeredCorrectly++
            }
        }
        val message = resources.getString(
            R.string.quiz_result_message,
            userName,
            answeredCorrectly,
            questionsList.size
        )
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        if (job?.isActive == true) {
            job?.cancel()
        }
    }
}
