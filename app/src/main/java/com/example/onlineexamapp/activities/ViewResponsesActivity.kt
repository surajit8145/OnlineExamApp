package com.example.onlineexamapp.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineexamapp.adapters.ResponseAdapter
import com.example.onlineexamapp.databinding.ActivityViewResponsesBinding
import com.example.onlineexamapp.models.Response
import com.example.onlineexamapp.models.Question
import com.google.firebase.firestore.FirebaseFirestore

data class FullResponse(
    val questionText: String,
    val selectedAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean
)

class ViewResponsesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewResponsesBinding
    private lateinit var responseAdapter: ResponseAdapter
    private val fullResponseList = mutableListOf<FullResponse>()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewResponsesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val examId = intent.getStringExtra("examId") ?: ""
        val studentId = intent.getStringExtra("studentId") ?: ""

        if (examId.isNotEmpty() && studentId.isNotEmpty()) {
            setupRecyclerView()
            loadResponsesWithQuestions(examId, studentId)
        } else {
            Toast.makeText(this, "Invalid exam or student ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        binding.rvResponses.layoutManager = LinearLayoutManager(this)
        responseAdapter = ResponseAdapter(fullResponseList)
        binding.rvResponses.adapter = responseAdapter
    }

    private fun loadResponsesWithQuestions(examId: String, studentId: String) {
        db.collection("responses")
            .whereEqualTo("examId", examId)
            .whereEqualTo("studentId", studentId)
            .get()
            .addOnSuccessListener { responseSnapshot ->
                val responses = responseSnapshot.toObjects(Response::class.java)

                if (responses.isEmpty()) {
                    Toast.makeText(this, "No responses found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Fetch each related Question
                for (response in responses) {
                    db.collection("questions").document(response.questionId)
                        .get()
                        .addOnSuccessListener { questionSnapshot ->
                            val question = questionSnapshot.toObject(Question::class.java)

                            if (question != null) {
                                val fullResponse = FullResponse(
                                    questionText = question.question,
                                    selectedAnswer = response.selectedAnswer,
                                    correctAnswer = question.correctAnswer,
                                    isCorrect = response.correct
                                )
                                fullResponseList.add(fullResponse)
                                responseAdapter.notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to load question", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load responses", Toast.LENGTH_SHORT).show()
            }
    }
}
