package com.example.onlineexamapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.onlineexamapp.R
import com.example.onlineexamapp.activities.*

class AdminPanelFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var progressBar: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_admin_panel, container, false)
        progressBar = rootView.findViewById(R.id.progressBar)

        setupUI()
        return rootView
    }

    private fun setupUI() {
        // Admin only fragment
        showAdminUI()
        setButtonClickListeners()
    }

    private fun showAdminUI() {
        toggleButtons(
            true,
            R.id.btnManageStudents,
            R.id.btnCreateExam,
            R.id.btnManageExams,
            R.id.btnAdminViewResults,
            R.id.btnAddQuestion,
            R.id.btnEditQuestion,
            R.id.btnCreateAnnouncement,
            R.id.btnCreateSubject,
            R.id.btnTakeAttendance,
            R.id.btnManageAttendance   // ✅ Added Manage Attendance button
        )
    }

    private fun toggleButtons(visible: Boolean, vararg buttonIds: Int) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        buttonIds.forEach {
            rootView.findViewById<Button>(it)?.visibility = visibility
        }
    }

    private fun setButtonClickListeners() {
        mapOf(
            R.id.btnManageStudents to ManageStudentsActivity::class.java,
            R.id.btnCreateExam to CreateExamActivity::class.java,
            R.id.btnManageExams to ManageExamsActivity::class.java,
            R.id.btnAdminViewResults to AdminViewResultsActivity::class.java,
            R.id.btnAddQuestion to AddQuestionActivity::class.java,
            R.id.btnEditQuestion to EditQuestionActivity::class.java,
            R.id.btnCreateAnnouncement to CreateAnnouncementActivity::class.java,
            R.id.btnCreateSubject to CreateSubjectActivity::class.java,
            R.id.btnTakeAttendance to TakeAttendanceActivity::class.java,
            R.id.btnManageAttendance to ManageAttendanceActivity::class.java   // ✅ Linked
        ).forEach { (id, activity) ->
            rootView.findViewById<Button>(id)?.setOnClickListener {
                startActivity(Intent(requireContext(), activity))
            }
        }
    }
}
