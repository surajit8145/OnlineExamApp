package com.example.onlineexamapp.activities

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineexamapp.adapters.EditQuestionAdapter
import com.example.onlineexamapp.databinding.ActivityEditQuestionBinding
import com.example.onlineexamapp.models.Question
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditQuestionBinding
    private lateinit var db: FirebaseFirestore
    private var selectedExamId: String = ""

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
                    loadQuestions()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
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
        if (selectedExamId.isEmpty()) return

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
                            question = document.getString("question") ?: "",
                            options = options.toMutableList(),
                            correctAnswer = document.getString("correctAnswer") ?: ""
                        )
                    )
                }

                val adapter = EditQuestionAdapter(
                    questionList,
                    { updatedQuestion -> updateQuestionInFirestore(updatedQuestion) },
                    { questionToDelete -> deleteQuestionFromFirestore(questionToDelete) }  // Pass delete callback
                )

                binding.recyclerViewQuestions.adapter = adapter
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load questions!", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }
    private fun deleteQuestionFromFirestore(question: Question) {
        db.collection("questions").document(question.id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Question deleted successfully", Toast.LENGTH_SHORT).show()
                loadQuestions() // Refresh the list after deletion
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete question", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateQuestionInFirestore(question: Question) {
        val updatedData = mapOf(
            "question" to question.question,
            "options" to question.options,
            "correctAnswer" to question.correctAnswer
        )

        db.collection("questions").document(question.id).update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(this, "Question updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update question", Toast.LENGTH_SHORT).show()
            }
    }
}
