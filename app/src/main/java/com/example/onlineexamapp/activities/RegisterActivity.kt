package com.example.onlineexamapp.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineexamapp.MainActivity
import com.example.onlineexamapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.util.Patterns
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.FileOutputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val ADMIN_CODE = "02122003"

    private var profilePictureUri: String? = null
    private var resizedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                checkImageSizeAndAspectRatio(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Toggle admin code field visibility
        binding.radioAdmin.setOnCheckedChangeListener { _, isChecked ->
            binding.adminCodeLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Pick image when profile picture is clicked
        binding.profileImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Register button logic
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                registerUser()
            }
        }

        // Login Text view logic
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun checkImageSizeAndAspectRatio(uri: Uri) {
        try {
            val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // For Android 9 and above (API level 28 and higher), use ImageDecoder
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                // For older versions, use BitmapFactory
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }

            // Check aspect ratio (1:1)
            val width = originalBitmap.width
            val height = originalBitmap.height

            if (width != height) {
                Toast.makeText(this, "Image aspect ratio must be 1:1", Toast.LENGTH_SHORT).show()
                return
            }

            // Resize image to 150x150 and save to file
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 250, 250, true)

            // Save resized image to file
            val file = File(cacheDir, "resized_profile.jpg")
            val outputStream = FileOutputStream(file)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            // Set the image to ImageView and store URI
            resizedImageUri = Uri.fromFile(file)
            binding.profileImage.setImageBitmap(resizedBitmap)

            // Upload image to Cloudinary
            uploadImageToCloudinary(resizedBitmap) { url ->
                if (url != null) {
                    profilePictureUri = url
                    Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Using default image URL", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to resize or validate image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToCloudinary(bitmap: Bitmap, onComplete: (String?) -> Unit) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()
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
                    Toast.makeText(this@RegisterActivity, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    onComplete(null)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { bodyString ->
                    try {
                        val json = JSONObject(bodyString)
                        val imageUrl = json.getString("secure_url")
                        runOnUiThread {
                            onComplete(imageUrl)
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@RegisterActivity, "Image parse error", Toast.LENGTH_SHORT).show()
                            onComplete(null)
                        }
                    }
                }
            }
        })
    }

    // Validate input fields before registering
    private fun validateInputs(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val role = if (binding.radioAdmin.isChecked) "admin" else "student"

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return false
        }

        if (phone.length != 10 || !phone.all { it.isDigit() }) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
            return false
        }

        val passwordPattern = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")
        if (!password.matches(passwordPattern)) {
            Toast.makeText(
                this,
                "Password must be at least 6 characters and include letters and numbers",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (role == "admin") {
            val adminCode = binding.etAdminCode.text.toString().trim()
            if (adminCode != ADMIN_CODE) {
                Toast.makeText(this, "Invalid admin registration code!", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        return true
    }

    // Register user and save to Firestore
    private fun registerUser() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val role = if (binding.radioAdmin.isChecked) "admin" else "student"

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    saveUserToFirestore(user, name, phone, email, role)
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true
                Toast.makeText(this, "Registration Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Save user data to Firestore
    private fun saveUserToFirestore(
        user: FirebaseUser,
        name: String,
        phone: String,
        email: String,
        role: String
    ) {
        val userId = user.uid
        val userData = hashMapOf(
            "name" to name,
            "phone" to phone,
            "email" to email,
            "role" to role,
            "profile_picture" to profilePictureUri,
            "created_at" to System.currentTimeMillis(),
            "id" to "${user.uid}_$email"  // Storing the UID and email in Firestore as id=UID_email
        )

        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                saveUserRoleInPreferences(role)
                redirectToHomePage()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save user data!", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true
            }
    }

    // Save user role in shared preferences
    private fun saveUserRoleInPreferences(role: String) {
        val sharedPref = getSharedPreferences("UserPref", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("userRole", role)
            apply()
        }
    }

    // Redirect user to the home page
    private fun redirectToHomePage() {
        binding.progressBar.visibility = View.GONE
        binding.btnRegister.isEnabled = true
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
