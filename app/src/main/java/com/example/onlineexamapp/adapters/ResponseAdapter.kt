package com.example.onlineexamapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineexamapp.databinding.ItemResponseBinding
import com.example.onlineexamapp.models.Response

class ResponseAdapter(private val responseList: List<Response>) :
    RecyclerView.Adapter<ResponseAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemResponseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResponseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val response = responseList[position]
        holder.binding.tvQuestionId.text = "Question: ${response.questionId}"
        holder.binding.tvSelectedAnswer.text = "Answer: ${response.selectedAnswer}"
        holder.binding.tvCorrect.text = if (response.correct) "✔ Correct" else "✘ Incorrect"
    }

    override fun getItemCount() = responseList.size
}
