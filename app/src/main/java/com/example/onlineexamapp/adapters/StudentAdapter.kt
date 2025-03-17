package com.example.onlineexamapp.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineexamapp.activities.EditStudentActivity
import com.example.onlineexamapp.databinding.ItemStudentBinding
import com.example.onlineexamapp.models.Student

class StudentAdapter(
    private val students: List<Student>,
    private val onDeleteClick: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    inner class StudentViewHolder(private val binding: ItemStudentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(student: Student) {
            binding.tvName.text = student.name
            binding.tvEmail.text = student.email
            binding.tvPhone.text = student.phone

            // Edit Student on Click
            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, EditStudentActivity::class.java)
                intent.putExtra("studentId", student.id)
                intent.putExtra("studentName", student.name)
                intent.putExtra("studentEmail", student.email)
                intent.putExtra("studentPhone", student.phone)
                binding.root.context.startActivity(intent)
            }

            // Delete Student
            binding.btnDelete.setOnClickListener {
                onDeleteClick(student)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(students[position])
    }

    override fun getItemCount() = students.size
}
