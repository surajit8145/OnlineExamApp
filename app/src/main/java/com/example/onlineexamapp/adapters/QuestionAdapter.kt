package com.example.onlineexamapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineexamapp.databinding.ItemQuestionBinding
import com.example.onlineexamapp.models.Question

class QuestionAdapter(
    private val questionList: List<Question>,
    private val isEditable: Boolean,
    private val onQuestionClick: ((Question) -> Unit)? = null // Optional click listener
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    inner class QuestionViewHolder(private val binding: ItemQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(question: Question) {
            binding.tvQuestion.text = question.question
            binding.rbOption1.text = question.options[0]
            binding.rbOption2.text = question.options[1]
            binding.rbOption3.text = question.options[2]
            binding.rbOption4.text = question.options[3]

            // Enable click only if editable
            if (isEditable) {
                binding.root.setOnClickListener {
                    onQuestionClick?.invoke(question)
                }
            } else {
                binding.root.setOnClickListener(null) // Remove click listener
            }

            // Handle answer selection
            binding.radioGroupOptions.setOnCheckedChangeListener { _, checkedId ->
                question.selectedAnswer = when (checkedId) {
                    binding.rbOption1.id -> binding.rbOption1.text.toString()
                    binding.rbOption2.id -> binding.rbOption2.text.toString()
                    binding.rbOption3.id -> binding.rbOption3.text.toString()
                    binding.rbOption4.id -> binding.rbOption4.text.toString()
                    else -> null
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questionList[position]) // ✅ Fixed incorrect variable name
    }

    override fun getItemCount(): Int = questionList.size
}
