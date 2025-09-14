package com.example.onlineexamapp.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.R
import com.example.onlineexamapp.databinding.ActivityTakeAttendanceBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.util.*
import android.view.View

class TakeAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTakeAttendanceBinding
    private val db = FirebaseFirestore.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    private val subjectList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakeAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup spinners
        ArrayAdapter.createFromResource(
            this,
            R.array.semesters,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSemester.adapter = it
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.departments,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerDepartment.adapter = it
        }


        binding.spinnerSemester.onItemSelectedListener = SimpleItemSelectedListener {
            loadSubjectsFromFirestore()
        }
        binding.spinnerDepartment.onItemSelectedListener = SimpleItemSelectedListener {
            loadSubjectsFromFirestore()
        }

        binding.btnStartAttendance.setOnClickListener {
            startQrGeneration()
        }
    }

    private fun loadSubjectsFromFirestore() {
        val semester = binding.spinnerSemester.selectedItem?.toString() ?: return
        val department = binding.spinnerDepartment.selectedItem?.toString() ?: return

        db.collection("subjects")
            .whereEqualTo("semester", semester)
            .whereEqualTo("department", department)
            .get()
            .addOnSuccessListener { result ->
                subjectList.clear()
                for (doc in result) {
                    val subjectName = doc.getString("subject_name")
                    if (!subjectName.isNullOrEmpty()) {
                        subjectList.add(subjectName)
                    }
                }

                if (subjectList.isEmpty()) {
                    subjectList.add("No subjects available")
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    subjectList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerSubject.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load subjects", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startQrGeneration() {
        val semester = binding.spinnerSemester.selectedItem.toString()
        val department = binding.spinnerDepartment.selectedItem.toString()
        val subject = binding.spinnerSubject.selectedItem.toString()

        if (subject == "No subjects available") {
            Toast.makeText(this, "No subjects found in database", Toast.LENGTH_SHORT).show()
            return
        }

        // Runnable to refresh QR every 30 sec
        runnable = object : Runnable {
            override fun run() {
                val uniqueCode = UUID.randomUUID().toString().substring(0, 8)
                val timestamp = System.currentTimeMillis()

                val qrData = "$semester|$department|$subject|$uniqueCode|$timestamp"

                generateQRCode(qrData)

                // Save current QR session to Firestore
                val sessionData = hashMapOf(
                    "semester" to semester,
                    "department" to department,
                    "subject" to subject,
                    "code" to uniqueCode,
                    "timestamp" to timestamp
                )

                db.collection("attendance_sessions")
                    .document("$semester-$department-$subject")
                    .set(sessionData)

                handler.postDelayed(this, 30_000) // refresh every 30s
            }
        }

        handler.post(runnable!!)
        Toast.makeText(this, "Attendance session started", Toast.LENGTH_SHORT).show()
    }

    private fun generateQRCode(data: String) {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 500, 500)
        val bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.RGB_565)

        for (x in 0 until 500) {
            for (y in 0 until 500) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }

        binding.ivQrCode.setImageBitmap(bitmap)
    }

    override fun onDestroy() {
        super.onDestroy()
        runnable?.let { handler.removeCallbacks(it) }
    }
}


class SimpleItemSelectedListener(
    private val onItemSelected: () -> Unit
) : AdapterView.OnItemSelectedListener {
    override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ) {
        onItemSelected()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}
