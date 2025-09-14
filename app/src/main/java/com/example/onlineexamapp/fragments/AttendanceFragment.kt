package com.example.onlineexamapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlineexamapp.databinding.FragmentAttendanceBinding
import com.example.onlineexamapp.models.Student
import com.example.onlineexamapp.models.SubjectAttendancePercent
import com.example.onlineexamapp.adapters.AttendancePercentAdapter
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var attendanceAdapter: AttendancePercentAdapter

    private val subjectAttendanceList = mutableListOf<SubjectAttendancePercent>()
    private val studentAttendanceList = mutableListOf<Pair<Student, Int>>()
    private var userRole: String = "student"
    private var userStudent: Student? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        checkUserRole()
    }

    private fun setupRecyclerView() {
        attendanceAdapter = AttendancePercentAdapter(subjectAttendanceList, studentAttendanceList, userRole)
        binding.recyclerViewAttendance.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = attendanceAdapter
        }
    }

    private fun checkUserRole() {
        val userId = auth.currentUser?.uid ?: return
        lifecycleScope.launch {
            try {
                val document = firestore.collection("users").document(userId).get().await()
                userStudent = document.toObject(Student::class.java)
                userRole = userStudent?.role ?: "student"

                if (userRole == "student") {
                    binding.teacherOptionsLayout.visibility = View.GONE
                    binding.barChart.visibility = View.VISIBLE
                    fetchStudentAttendance()
                    setupBarChart()
                } else {
                    binding.teacherOptionsLayout.visibility = View.VISIBLE
                    binding.barChart.visibility = View.GONE
                    setupSpinners()
                }
            } catch (e: Exception) {
                Log.e("AttendanceFragment", "Error checking user role", e)
            }
        }
    }

    private fun setupSpinners() {
        lifecycleScope.launch {
            try {
                val semesters = firestore.collection("subjects")
                    .get().await()
                    .documents
                    .mapNotNull { it.getString("semester") }
                    .distinct()

                val departments = firestore.collection("subjects")
                    .get().await()
                    .documents
                    .mapNotNull { it.getString("department") }
                    .distinct()

                binding.spinnerSemester.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    semesters
                )

                binding.spinnerDepartment.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    departments
                )

                binding.spinnerSemester.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        updateSubjectSpinner()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

                binding.spinnerDepartment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        updateSubjectSpinner()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

                binding.spinnerSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedSubject = binding.spinnerSubject.selectedItem?.toString() ?: return
                        fetchTeacherStudentAttendance(selectedSubject)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            } catch (e: Exception) {
                Log.e("AttendanceFragment", "Error setting up spinners", e)
            }
        }
    }

    private fun updateSubjectSpinner() {
        lifecycleScope.launch {
            try {
                val selectedSemester = binding.spinnerSemester.selectedItem?.toString() ?: return@launch
                val selectedDepartment = binding.spinnerDepartment.selectedItem?.toString() ?: return@launch

                val subjects = firestore.collection("subjects")
                    .whereEqualTo("semester", selectedSemester)
                    .whereEqualTo("department", selectedDepartment)
                    .get().await()
                    .documents
                    .mapNotNull { it.getString("subject_name") }

                binding.spinnerSubject.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    subjects
                )
            } catch (e: Exception) {
                Log.e("AttendanceFragment", "Error updating subject spinner", e)
            }
        }
    }

    private fun fetchStudentAttendance() {
        val userId = auth.currentUser?.uid ?: return
        val department = userStudent?.department ?: return
        val semester = userStudent?.semester ?: return
        subjectAttendanceList.clear()

        lifecycleScope.launch {
            try {
                val subjects = firestore.collection("subjects")
                    .whereEqualTo("semester", semester)
                    .whereEqualTo("department", department)
                    .get().await()
                    .documents.mapNotNull { it.getString("subject_name") }

                subjects.forEach { subjectName ->
                    val sessionId = "$semester-$department-$subjectName"

                    val attendanceDocs = firestore.collection("attendance_sessions")
                        .document(sessionId)
                        .collection("students")
                        .whereEqualTo("uid", userId)
                        .get().await()

                    val totalClasses = firestore.collection("attendance_sessions")
                        .document(sessionId)
                        .get().await()
                        .getLong("totalClasses")?.toInt() ?: 30

                    val attendedClasses = attendanceDocs.size()
                    val percentage = if (totalClasses > 0) (attendedClasses * 100) / totalClasses else 0

                    subjectAttendanceList.add(SubjectAttendancePercent(subjectName, percentage))
                }

                attendanceAdapter.updateData(subjectAttendanceList, emptyList())
                updateBarChart()
            } catch (e: Exception) {
                Log.e("AttendanceFragment", "Error fetching student attendance", e)
            }
        }
    }

    private fun fetchTeacherStudentAttendance(subjectName: String) {
        studentAttendanceList.clear()

        lifecycleScope.launch {
            try {
                val selectedSemester = binding.spinnerSemester.selectedItem?.toString() ?: return@launch
                val selectedDepartment = binding.spinnerDepartment.selectedItem?.toString() ?: return@launch

                val sessionId = "$selectedSemester-$selectedDepartment-$subjectName"

                val students = firestore.collection("users")
                    .whereEqualTo("role", "student")
                    .whereEqualTo("semester", selectedSemester)
                    .whereEqualTo("department", selectedDepartment)
                    .get().await()
                    .documents.mapNotNull { doc ->
                        val student = doc.toObject(Student::class.java)
                        student?.copy(id = doc.id)
                    }

                students.forEach { student ->
                    val uid = student.id
                    val attendanceDocs = firestore.collection("attendance_sessions")
                        .document(sessionId)
                        .collection("students")
                        .whereEqualTo("uid", uid)
                        .get().await()

                    val totalClasses = firestore.collection("attendance_sessions")
                        .document(sessionId)
                        .get().await()
                        .getLong("totalClasses")?.toInt() ?: 30

                    val attendedClasses = attendanceDocs.size()
                    val percentage = if (totalClasses > 0) (attendedClasses * 100) / totalClasses else 0

                    studentAttendanceList.add(Pair(student, percentage))
                }

                attendanceAdapter.updateData(emptyList(), studentAttendanceList)
            } catch (e: Exception) {
                Log.e("AttendanceFragment", "Error fetching teacher attendance", e)
            }
        }
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setFitBars(true)
            animateY(1000)
        }
    }

    private fun updateBarChart() {
        val entries = subjectAttendanceList.mapIndexed { index, subject ->
            BarEntry(index.toFloat(), subject.percentage.toFloat())
        }
        val dataSet = BarDataSet(entries, "Attendance Percentage")
        val barData = BarData(dataSet)
        barData.barWidth = 0.4f

        binding.barChart.apply {
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(subjectAttendanceList.map { it.subjectName })
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            axisRight.isEnabled = false
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
