package com.example.onlineexamapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.onlineexamapp.MainActivity
import com.example.onlineexamapp.R
import com.example.onlineexamapp.utils.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var tvEmail: TextView
    private lateinit var btnUpdate: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnBack: Button
    private lateinit var profileImageView: ImageView

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profileImageView.setImageURI(it) // Show preview
            uploadImageToCloudinary(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        tvEmail = findViewById(R.id.tvEmail)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnBack = findViewById(R.id.btnBack)
        profileImageView = findViewById(R.id.ivProfileImage)

        loadUserProfile()

        btnUpdate.setOnClickListener {
            updateUserProfile()
        }

        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }

        profileImageView.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun loadUserProfile() {
        val userId = FirebaseHelper.getUserId()
        if (userId == null) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    etName.setText(document.getString("name") ?: "")
                    tvEmail.text = document.getString("email") ?: ""
                    etPhone.setText(document.getString("phone") ?: "")
                    val profileImageUrl = document.getString("profile_picture") ?: ""
                    if (profileImageUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .circleCrop()
                            .into(profileImageView)
                    }
                } else {
                    Toast.makeText(this, "User profile not found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserProfile() {
        val userId = FirebaseHelper.getUserId() ?: return
        val newName = etName.text.toString().trim()
        val newPhone = etPhone.text.toString().trim()

        if (newName.isEmpty()) {
            etName.error = "Name cannot be empty"
            return
        }

        val updates = mutableMapOf<String, Any>(
            "name" to newName,
            "phone" to newPhone
        )

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToCloudinary(imageUri: Uri) {
        val inputStream = contentResolver.openInputStream(imageUri)
        val imageBytes = inputStream?.readBytes() ?: return

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "profile.jpg",
                RequestBody.create("image/*".toMediaTypeOrNull(), imageBytes))
            .addFormDataPart("upload_preset", "unsigned_android")
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/dmpclbuxl/image/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()
                val secureUrl = JSONObject(bodyString).optString("secure_url")
                if (secureUrl.isNotEmpty()) {
                    updateFirestoreWithImageUrl(secureUrl)
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "Failed to get image URL", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun updateFirestoreWithImageUrl(imageUrl: String) {
        val userId = FirebaseHelper.getUserId() ?: return
        val userRef = db.collection("users").document(userId)

        userRef.update("profile_picture", imageUrl)
            .addOnSuccessListener {
                runOnUiThread {
                    Toast.makeText(this, "Profile image updated!", Toast.LENGTH_SHORT).show()
                    // Update the ImageView immediately with the new URL
                    Glide.with(this)
                        .load(imageUrl)
                        .circleCrop()
                        .into(profileImageView)
                }
            }
            .addOnFailureListener {
                runOnUiThread {
                    Toast.makeText(this, "Failed to update profile image in Firestore", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
