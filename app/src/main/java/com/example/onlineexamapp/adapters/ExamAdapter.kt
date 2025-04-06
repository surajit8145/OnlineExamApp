package com.example.onlineexamapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineexamapp.R
import com.example.onlineexamapp.models.Exam

class ExamAdapter(
    private var examList: List<Exam>,
    private val onExamClick: (Exam) -> Unit
) : RecyclerView.Adapter<ExamAdapter.ExamViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exam, parent, false)
        return ExamViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        val exam = examList[position]
        holder.bind(exam)
        holder.itemView.setOnClickListener { onExamClick(exam) }
    }

    override fun getItemCount(): Int = examList.size

    fun updateData(newList: List<Exam>) {
        examList = newList
        notifyDataSetChanged()
    }

    class ExamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewExamTitle)
        private val subjectTextView: TextView = itemView.findViewById(R.id.textViewExamSubject)
        private val dateTimeTextView: TextView = itemView.findViewById(R.id.textViewExamDateTime)
        private val durationTextView: TextView = itemView.findViewById(R.id.textViewExamDuration)

        fun bind(exam: Exam) {
            titleTextView.text = exam.title
            subjectTextView.text = " ${exam.subject}"
            dateTimeTextView.text = " ${exam.date}  ${exam.time}"
            durationTextView.text = " ${exam.duration} mins"
        }
    }
}
