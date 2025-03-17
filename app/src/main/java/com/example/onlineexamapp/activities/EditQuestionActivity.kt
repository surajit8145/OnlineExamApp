package com.example.onlineexamapp.activities

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineexamapp.R
import com.example.onlineexamapp.adapters.QuestionAdapter
import com.example.onlineexamapp.databinding.ActivityEditQuestionBinding
import com.example.onlineexamapp.models.Question
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditQuestionBinding
    private lateinit var db: FirebaseFirestore
    private var selectedExamId: String = ""
    private var selectedQuestionId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        checkUserRole()
        setupRecyclerView()
        setupListeners()
        loadExams()

        // Prevent UI from shrinking behind keyboard
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun checkUserRole() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: "student"
                if (role != "admin") {
                    Toast.makeText(this, "Access Denied!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewQuestions.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        binding.spinnerExam.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val examIds = binding.spinnerExam.tag as? List<String> ?: return
                if (position < examIds.size) {
                    selectedExamId = examIds[position]
                    clearEditForm()
                    loadQuestions()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnUpdateQuestion.setOnClickListener {
            updateQuestion()
        }

        // Disable update button until a question is selected
        binding.btnUpdateQuestion.isEnabled = false

        // Enable update button only when text is modified
        binding.etEditQuestion.doOnTextChanged { _, _, _, _ ->
            binding.btnUpdateQuestion.isEnabled = true
        }
    }

    private fun loadExams() {
        db.collection("exams").get().addOnSuccessListener { documents ->
            val examList = mutableListOf<String>()
            val examIdList = mutableListOf<String>()

            for (document in documents) {
                examList.add(document.getString("title") ?: "Unnamed Exam")
                examIdList.add(document.id)
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, examList)
            binding.spinnerExam.adapter = adapter
            binding.spinnerExam.tag = examIdList
        }
    }

    private fun loadQuestions() {
        if (selectedExamId.isEmpty()) return // Prevent Firestore crash

        binding.progressBar.visibility = View.VISIBLE
        db.collection("questions").whereEqualTo("examId", selectedExamId).get()
            .addOnSuccessListener { documents ->
                val questionList = mutableListOf<Question>()
                for (document in documents) {
                    val options = (document.get("options") as? List<*>)?.map { it.toString() } ?: emptyList()

                    questionList.add(
                        Question(
                            id = document.id,
                            examId = selectedExamId,
                            questionText = document.getString("question") ?: "",
                            options = options.toMutableList(),
                            correctAnswer = document.getString("correctAnswer") ?: ""
                        )
                    )
                }

                binding.recyclerViewQuestions.adapter = QuestionAdapter(
                    questionList,
                    isEditable = true
                ) { selectedQuestion ->
                    loadQuestionDetails(selectedQuestion.id)
                }

                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load questions!", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun loadQuestionDetails(questionId: String) {
        selectedQuestionId = questionId
        db.collection("questions").document(questionId).get()
            .addOnSuccessListener { document ->
                if (!isFinishing) {  // ✅ FIXED: Removed isInitialized check

                    binding.etEditQuestion.setText(document.getString("question"))

                    val options = (document.get("options") as? List<*>)?.map { it.toString() } ?: emptyList()
                    binding.etEditOption1.setText(options.getOrNull(0) ?: "")
                    binding.etEditOption2.setText(options.getOrNull(1) ?: "")
                    binding.etEditOption3.setText(options.getOrNull(2) ?: "")
                    binding.etEditOption4.setText(options.getOrNull(3) ?: "")
                    binding.etEditCorrectAnswer.setText(document.getString("correctAnswer"))

                    binding.layoutEditQuestion.visibility = View.VISIBLE
                    binding.btnUpdateQuestion.isEnabled = true
                }
            }
    }


    private fun updateQuestion() {
        if (selectedQuestionId.isEmpty()) {
            Toast.makeText(this, "No question selected!", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedData = mapOf(
            "question" to binding.etEditQuestion.text.toString(),
            "options" to listOf(
                binding.etEditOption1.text.toString(),
                binding.etEditOption2.text.toString(),
                binding.etEditOption3.text.toString(),
                binding.etEditOption4.text.toString()
            ),
            "correctAnswer" to binding.etEditCorrectAnswer.text.toString()
        )

        db.collection("questions").document(selectedQuestionId).update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(this, "Question Updated!", Toast.LENGTH_SHORT).show()
                binding.layoutEditQuestion.visibility = View.GONE
                loadQuestions()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update question!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearEditForm() {
        binding.etEditQuestion.text?.clear()
        binding.etEditOption1.text?.clear()
        binding.etEditOption2.text?.clear()
        binding.etEditOption3.text?.clear()
        binding.etEditOption4.text?.clear()
        binding.etEditCorrectAnswer.text?.clear()
        binding.layoutEditQuestion.visibility = View.GONE
    }
}
