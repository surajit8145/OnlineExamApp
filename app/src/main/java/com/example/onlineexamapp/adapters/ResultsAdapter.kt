package com.example.onlineexamapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineexamapp.R
import com.example.onlineexamapp.models.ResultModel

class ResultsAdapter(private val resultsList: List<ResultModel>) :
    RecyclerView.Adapter<ResultsAdapter.ResultsViewHolder>() {

    class ResultsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvExamTitle: TextView = itemView.findViewById(R.id.tvExamTitle)
        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        val tvScore: TextView = itemView.findViewById(R.id.tvScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_result, parent, false)
        return ResultsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultsViewHolder, position: Int) {
        val result = resultsList[position]
        holder.tvExamTitle.text = "Exam: ${result.examTitle}"
        holder.tvSubject.text = "Subject: ${result.subject}"
        holder.tvStudentName.text = "Student: ${result.studentName}"
        holder.tvScore.text = "Score: ${result.score} / ${result.totalMarks}"
    }

    override fun getItemCount(): Int {
        return resultsList.size
    }
}
