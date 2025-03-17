package com.example.onlineexamapp.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddQuestionActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var spinnerExam: Spinner
    private lateinit var etQuestion: EditText
    private lateinit var etOption1: EditText
    private lateinit var etOption2: EditText
    private lateinit var etOption3: EditText
    private lateinit var etOption4: EditText
    private lateinit var spinnerCorrectAnswer: Spinner
    private lateinit var btnSaveQuestion: Button
    private lateinit var progressBar: ProgressBar

    private var selectedExamId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_question)

        db = FirebaseFirestore.getInstance()

        // Initialize Views
        spinnerExam = findViewById(R.id.spinnerExam)
        etQuestion = findViewById(R.id.etQuestion)
        etOption1 = findViewById(R.id.etOption1)
        etOption2 = findViewById(R.id.etOption2)
        etOption3 = findViewById(R.id.etOption3)
        etOption4 = findViewById(R.id.etOption4)
        spinnerCorrectAnswer = findViewById(R.id.spinnerCorrectAnswer)
        btnSaveQuestion = findViewById(R.id.btnSaveQuestion)
        progressBar = findViewById(R.id.progressBar)

        loadExams() // Load exams into the spinner
        setupCorrectAnswerSpinner() // Set correct answer choices

        btnSaveQuestion.setOnClickListener {
            saveQuestion()
        }
    }

    private fun loadExams() {
        progressBar.visibility = View.VISIBLE

        db.collection("exams").get()
            .addOnSuccessListener { documents ->
                val examList = mutableListOf<String>()
                val examIdList = mutableListOf<String>()

                for (document in documents) {
                    val examName = document.getString("title") ?: "Unnamed Exam"
                    val examId = document.id
                    examList.add(examName)
                    examIdList.add(examId)
                }

                if (examList.isEmpty()) {
                    examList.add("No Exams Available")
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, examList)
                spinnerExam.adapter = adapter

                spinnerExam.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        selectedExamId = if (examIdList.isNotEmpty() && position < examIdList.size) examIdList[position] else ""
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedExamId = ""
                    }
                }

                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to load exams", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupCorrectAnswerSpinner() {
        val options = listOf("Select Correct Answer", "A", "B", "C", "D")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        spinnerCorrectAnswer.adapter = adapter
    }

    private fun saveQuestion() {
        val questionText = etQuestion.text.toString().trim()
        val option1 = etOption1.text.toString().trim()
        val option2 = etOption2.text.toString().trim()
        val option3 = etOption3.text.toString().trim()
        val option4 = etOption4.text.toString().trim()
        val correctAnswerIndex = spinnerCorrectAnswer.selectedItemPosition

        if (selectedExamId.isEmpty() || selectedExamId == "No Exams Available") {
            Toast.makeText(this, "Please select a valid exam!", Toast.LENGTH_SHORT).show()
            return
        }

        if (questionText.isEmpty() || option1.isEmpty() || option2.isEmpty() ||
            option3.isEmpty() || option4.isEmpty()
        ) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (correctAnswerIndex == 0) {
            Toast.makeText(this, "Please select the correct answer", Toast.LENGTH_SHORT).show()
            return
        }

        val correctAnswer = when (correctAnswerIndex) {
            1 -> option1 // A
            2 -> option2 // B
            3 -> option3 // C
            4 -> option4 // D
            else -> ""
        }

        progressBar.visibility = View.VISIBLE

        // Generate a unique question ID using timestamp
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val questionId = "Q$timeStamp-$selectedExamId"

        val questionData = hashMapOf(
            "id" to questionId,
            "examId" to selectedExamId,
            "question" to questionText,
            "options" to listOf(option1, option2, option3, option4),
            "correctAnswer" to correctAnswer
        )

        // Save question
        db.collection("questions").document(questionId)
            .set(questionData)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Question added successfully!", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        etQuestion.text.clear()
        etOption1.text.clear()
        etOption2.text.clear()
        etOption3.text.clear()
        etOption4.text.clear()
        spinnerCorrectAnswer.setSelection(0)
    }
}
