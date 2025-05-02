package com.example.onlineexamapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineexamapp.R
import com.example.onlineexamapp.activities.FullResponse

class ResponseAdapter(private val responseList: List<FullResponse>) :
    RecyclerView.Adapter<ResponseAdapter.ResponseViewHolder>() {

    class ResponseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvQuestionText: TextView = itemView.findViewById(R.id.tvQuestionText)
        val tvSelectedAnswer: TextView = itemView.findViewById(R.id.tvSelectedAnswer)
        val tvCorrectAnswer: TextView = itemView.findViewById(R.id.tvCorrectAnswer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResponseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_response, parent, false)
        return ResponseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResponseViewHolder, position: Int) {
        val response = responseList[position]
        holder.tvQuestionText.text = "Q: ${response.questionText}"
        holder.tvSelectedAnswer.text = "Your Answer: ${response.selectedAnswer}"
        holder.tvCorrectAnswer.text = "Correct Answer: ${response.correctAnswer}"

        if (response.isCorrect) {
            holder.tvSelectedAnswer.setTextColor(holder.itemView.context.getColor(R.color.green))
        } else {
            holder.tvSelectedAnswer.setTextColor(holder.itemView.context.getColor(R.color.red))
        }
    }

    override fun getItemCount(): Int {
        return responseList.size
    }
}
