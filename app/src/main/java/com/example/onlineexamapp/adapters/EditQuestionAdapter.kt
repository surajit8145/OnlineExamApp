package com.example.onlineexamapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineexamapp.databinding.ItemEditQuestionBinding
import com.example.onlineexamapp.models.Question
class EditQuestionAdapter(
    private val questionList: List<Question>,
    private val onSaveClick: (Question) -> Unit,  // Callback for saving
    private val onDeleteClick: (Question) -> Unit // Callback for deleting
) : RecyclerView.Adapter<EditQuestionAdapter.QuestionViewHolder>() {

    private var expandedPosition = -1  // For expanding the question edit form

    // View Holder for each question
    inner class QuestionViewHolder(val binding: ItemEditQuestionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(question: Question, position: Int) {
            // View mode
            // Question numbering (starting from 1)
            val questionNumber = position + 1 // Add 1 to make it start from 1, not 0
            binding.tvQuestionNumber.text = "Q$questionNumber" // Assuming you have a TextView to show the number

            binding.tvQuestionTitle.text = "Q $questionNumber: ${question.question}"
            binding.tvOption1.text = "1. ${question.options[0]}"
            binding.tvOption2.text = "2. ${question.options[1]}"
            binding.tvOption3.text = "3. ${question.options[2]}"
            binding.tvOption4.text = "4. ${question.options[3]}"
            binding.tvCorrectAnswer.text = "✔ Correct: ${question.correctAnswer}"

            // Edit mode visibility
            val isExpanded = position == expandedPosition
            binding.editLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE

            // Prefill edit fields if expanded
            if (isExpanded) {
                binding.etQuestion.setText(question.question)
                binding.etOption1.setText(question.options[0])
                binding.etOption2.setText(question.options[1])
                binding.etOption3.setText(question.options[2])
                binding.etOption4.setText(question.options[3])

                // Set up the spinner for correct answer
                val correctAnswerPosition = question.options.indexOf(question.correctAnswer)
                val spinnerAdapter = ArrayAdapter(
                    binding.root.context,
                    android.R.layout.simple_spinner_item,
                    question.options
                )
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerCorrectAnswer.adapter = spinnerAdapter

                // Set the correct answer to the selected item in the spinner
                binding.spinnerCorrectAnswer.setSelection(correctAnswerPosition)
            }

            // Toggle expand/collapse on click
            binding.itemLayout.setOnClickListener {
                expandedPosition = if (isExpanded) -1 else position
                notifyDataSetChanged()
            }

            // Save button logic
            binding.btnSave.setOnClickListener {
                val updatedQuestionText = binding.etQuestion.text.toString().trim()
                val updatedOptions = listOf(
                    binding.etOption1.text.toString().trim(),
                    binding.etOption2.text.toString().trim(),
                    binding.etOption3.text.toString().trim(),
                    binding.etOption4.text.toString().trim()
                )
                val updatedCorrectAnswer = binding.spinnerCorrectAnswer.selectedItem.toString().trim() // Get selected answer from spinner

                // Validation
                if (updatedQuestionText.isEmpty() ||
                    updatedOptions.any { it.isEmpty() } ||
                    updatedCorrectAnswer.isEmpty()
                ) {
                    Toast.makeText(binding.root.context, "All fields are required", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Update question object
                question.question = updatedQuestionText
                question.options = updatedOptions.toMutableList()
                question.correctAnswer = updatedCorrectAnswer

                onSaveClick(question)

                // Collapse the item after saving
                expandedPosition = -1
                notifyDataSetChanged()
            }

            // Delete button logic
            binding.btnDelete.setOnClickListener {
                onDeleteClick(question)  // Trigger delete callback
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemEditQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questionList[position], position)
    }

    override fun getItemCount(): Int {
        return questionList.size
    }
}
