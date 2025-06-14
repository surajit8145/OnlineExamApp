package com.example.onlineexamapp.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.R
import com.example.onlineexamapp.models.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreateAnnouncementActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var etEndDate: EditText
    private lateinit var etStartDate: EditText
    private lateinit var cbImportant: CheckBox
    private lateinit var btnSubmit: Button
    private lateinit var announcementContainer: LinearLayout

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_announcement)

        // Initialize views
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        etEndDate = findViewById(R.id.etDate)
        etStartDate = findViewById(R.id.etValidFrom)
        cbImportant = findViewById(R.id.cbImportant)
        btnSubmit = findViewById(R.id.btnSubmit)
        announcementContainer = findViewById(R.id.announcementContainer)

        // Set up date pickers
        etStartDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showDatePicker(this) }
        }

        etEndDate.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showDatePicker(this) }
        }

        btnSubmit.setOnClickListener {
            createAnnouncement()
        }

        fetchAnnouncements()
    }

    private fun showDatePicker(targetEditText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            targetEditText.setText(formattedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun fetchAnnouncements() {
        db.collection("announcements")
            .get()
            .addOnSuccessListener { documents ->
                announcementContainer.removeAllViews()
                for (document in documents) {
                    try {
                        val announcement = document.toObject(Announcement::class.java)
                        announcement.id = document.id

                        val textView = TextView(this).apply {
                            text = "📌 ${announcement.title}\n📝 ${announcement.content}"
                            setPadding(24, 16, 24, 16)
                            textSize = 16f
                            setTextColor(resources.getColor(R.color.text_primary, null))
                            setBackgroundResource(R.drawable.edittext_background)
                            setOnClickListener {
                                AlertDialog.Builder(this@CreateAnnouncementActivity)
                                    .setTitle("Delete Announcement")
                                    .setMessage("Are you sure you want to delete \"${announcement.title}\"?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        deleteAnnouncement(announcement.id)
                                    }
                                    .setNegativeButton("No", null)
                                    .show()
                            }
                        }

                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 0, 0, 12)
                        }

                        announcementContainer.addView(textView, layoutParams)

                    } catch (e: Exception) {
                        Log.e("CreateAnnouncementActivity", "Error parsing document: ${document.id}", e)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching announcements: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun createAnnouncement() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        val startDate = etStartDate.text.toString().trim()
        val endDate = etEndDate.text.toString().trim()
        val isImportant = cbImportant.isChecked

        if (title.isEmpty()) {
            etTitle.error = "Title required"
            etTitle.requestFocus()
            return
        }

        if (content.isEmpty()) {
            etContent.error = "Content required"
            etContent.requestFocus()
            return
        }

        if (startDate.isEmpty()) {
            etStartDate.error = "Start date required"
            etStartDate.requestFocus()
            return
        }

        if (endDate.isEmpty()) {
            etEndDate.error = "End date required"
            etEndDate.requestFocus()
            return
        }

        val announcement = Announcement(
            title = title,
            startDate = startDate,
            endDate = endDate,
            content = content,
            isImportant = isImportant
        )

        db.collection("announcements")
            .add(announcement)
            .addOnSuccessListener {
                Toast.makeText(this, "Announcement created", Toast.LENGTH_SHORT).show()
                fetchAnnouncements()
                clearFields()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun deleteAnnouncement(id: String) {
        db.collection("announcements")
            .document(id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Announcement deleted", Toast.LENGTH_SHORT).show()
                fetchAnnouncements()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun clearFields() {
        etTitle.text.clear()
        etContent.text.clear()
        etStartDate.text.clear()
        etEndDate.text.clear()
        cbImportant.isChecked = false
    }
}
