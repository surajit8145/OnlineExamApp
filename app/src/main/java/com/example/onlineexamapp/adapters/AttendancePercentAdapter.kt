package com.example.onlineexamapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineexamapp.R
import com.example.onlineexamapp.models.Student
import com.example.onlineexamapp.models.SubjectAttendancePercent

class AttendancePercentAdapter(
    private var subjectAttendanceList: List<SubjectAttendancePercent>,
    private var studentAttendanceList: List<Pair<Student, Int>>,
    private val userRole: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_EMPTY = 1
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subjectOrName: TextView = itemView.findViewById(R.id.text_subject_name)
        val attendancePercent: TextView = itemView.findViewById(R.id.text_attendance_percent)
    }

    class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emptyText: TextView = itemView.findViewById(R.id.text_empty_state)
    }

    override fun getItemViewType(position: Int): Int {
        return if (userRole == "student" && subjectAttendanceList.isEmpty() ||
            userRole != "student" && studentAttendanceList.isEmpty()) {
            VIEW_TYPE_EMPTY
        } else {
            VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_empty_state, parent, false)
                EmptyViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_attendance_percent, parent, false)
                ItemViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                if (userRole == "student") {
                    val subjectAttendance = subjectAttendanceList[position]
                    holder.subjectOrName.text = subjectAttendance.subjectName
                    holder.attendancePercent.text = "${subjectAttendance.percentage}%"
                } else {
                    val (student, percentage) = studentAttendanceList[position]
                    val studentName = student.name?.takeIf { it.isNotBlank() } ?: "Unknown"
                    val studentEmail = student.email ?: "No Email"
                    holder.subjectOrName.text = "$studentName\n($studentEmail)"
                    holder.attendancePercent.text = "$percentage%"
                }
            }
            is EmptyViewHolder -> {
                holder.emptyText.text = if (userRole == "student") {
                    "No subjects available"
                } else {
                    "No students found"
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (userRole == "student") {
            if (subjectAttendanceList.isEmpty()) 1 else subjectAttendanceList.size
        } else {
            if (studentAttendanceList.isEmpty()) 1 else studentAttendanceList.size
        }
    }

    fun updateData(
        newSubjectAttendanceList: List<SubjectAttendancePercent>,
        newStudentAttendanceList: List<Pair<Student, Int>>
    ) {
        subjectAttendanceList = newSubjectAttendanceList
        studentAttendanceList = newStudentAttendanceList
        notifyDataSetChanged()
    }
}
