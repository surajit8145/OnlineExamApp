package com.example.onlineexamapp.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.R
import com.example.onlineexamapp.models.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreateAnnouncementActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var etEndDate: EditText // Renamed from etDate
    private lateinit var etStartDate: EditText // Renamed from etValidFrom
    private lateinit var cbImportant: CheckBox
    private lateinit var btnSubmit: Button
    private lateinit var announcementListView: ListView
    private lateinit var announcementsAdapter: ArrayAdapter<String>

    private val db = FirebaseFirestore.getInstance()
    private val announcementsList = mutableListOf<String>()
    // Map formatted string (Title - Content) to ID
    private val announcementIdMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_announcement)

        // Initialize views
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        etEndDate = findViewById(R.id.etDate) // R.id.etDate is now used for the end date
        etStartDate = findViewById(R.id.etValidFrom) // R.id.etValidFrom is now used for the start date
        cbImportant = findViewById(R.id.cbImportant)
        btnSubmit = findViewById(R.id.btnSubmit)
        announcementListView = findViewById(R.id.announcementListView)

        // Using a simple list item layout that supports single line text
        // If content is long, consider a custom layout or a different adapter
        announcementsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, announcementsList)
        announcementListView.adapter = announcementsAdapter

        fetchAnnouncements()

        // Set non-editable text fields for date selection
        etStartDate.apply { // Start Date
            isFocusable = false
            isClickable = true
            setOnClickListener { showDatePicker(this) }
        }

        etEndDate.apply { // End Date
            isFocusable = false
            isClickable = true
            setOnClickListener { showDatePicker(this) }
        }

        btnSubmit.setOnClickListener {
            createAnnouncement()
        }

        announcementListView.setOnItemClickListener { _, _, position, _ ->
            val selectedItemText = announcementsList[position]
            // Find the ID using the full formatted string
            val announcementId = announcementIdMap[selectedItemText]

            if (announcementId != null) {
                android.app.AlertDialog.Builder(this)
                    .setTitle("Delete Announcement")
                    // Display the title in the confirmation message
                    .setMessage("Are you sure you want to delete the announcement with title \"${announcementIdMap.entries.find { it.value == announcementId }?.key?.split(" - Content: ")?.get(0)}\"?")
                    .setPositiveButton("Yes") { _, _ ->
                        deleteAnnouncement(announcementId)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
    }

    private fun showDatePicker(targetEditText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Month is 0-indexed, so add 1
            val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            targetEditText.setText(formattedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun fetchAnnouncements() {
        db.collection("announcements")
            .get()
            .addOnSuccessListener { documents ->
                announcementsList.clear()
                announcementIdMap.clear()
                for (document in documents) {
                    try {
                        val announcement = document.toObject(Announcement::class.java)
                        announcement.id = document.id

                        // Format the string to display both title and content
                        val formattedItem = "Title: ${announcement.title} - Content: ${announcement.content}"
                        announcementsList.add(formattedItem)
                        // Map the formatted string to the document ID
                        announcementIdMap[formattedItem] = announcement.id

                    } catch (e: Exception) {
                        Log.e("CreateAnnouncementActivity", "Error parsing announcement document: ${document.id}", e)
                    }
                }
                announcementsAdapter.notifyDataSetChanged()
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
