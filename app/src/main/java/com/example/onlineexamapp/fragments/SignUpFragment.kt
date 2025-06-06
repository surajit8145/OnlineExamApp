package com.example.onlineexamapp.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.onlineexamapp.R
import com.example.onlineexamapp.activities.EmailVerificationActivity
import com.example.onlineexamapp.databinding.FragmentSignUpBinding // Corrected import
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.graphics.ImageDecoder
import android.os.Looper
import com.example.onlineexamapp.activities.AuthActivity

class SignUpFragment : Fragment(R.layout.fragment_sign_up) { // Changed to Fragment and layout

    private lateinit var binding: FragmentSignUpBinding // Changed to FragmentSignUpBinding
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { // Use onViewCreated
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignUpBinding.bind(view) // Initialize binding here

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
        binding.btnBack.setOnClickListener {
            (activity as? AuthActivity)?.navigateTo(WelcomeFragment(), false)
        }
        // Login Text view logic.  Adjusted for fragments.
        binding.tvLogin.setOnClickListener {
            (activity as? AuthActivity)?.navigateTo(SignInFragment())
        }
    }

    //  Define an interface for fragment-to-activity communication
    interface FragmentNavigation {
        fun onNavigateToLogin()
    }


    private fun checkImageSizeAndAspectRatio(uri: Uri) {
        try {
            val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri) // Use requireContext()
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)  // Use requireContext()
            }

            val width = originalBitmap.width
            val height = originalBitmap.height

            if (width != height) {
                Toast.makeText(requireContext(), "Image aspect ratio must be 1:1", Toast.LENGTH_SHORT).show()
                return
            }

            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 250, 250, true)

            val file = File(requireContext().cacheDir, "resized_profile.jpg") // Use requireContext()
            val outputStream = FileOutputStream(file)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            resizedImageUri = Uri.fromFile(file)
            binding.profileImage.setImageBitmap(resizedBitmap)

            uploadImageToCloudinary(resizedBitmap) { url ->
                if (url != null) {
                    profilePictureUri = url
                    Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Using default image URL", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to resize or validate image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToCloudinary(bitmap: Bitmap, onComplete: (String?) -> Unit) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", "profile.jpg",
                RequestBody.create("image/*".toMediaTypeOrNull(), imageBytes)
            )
            .addFormDataPart("upload_preset", "unsigned_android")
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/dmpclbuxl/image/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread { // Use activity?.runOnUiThread
                    Toast.makeText(requireContext(), "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    onComplete(null)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { bodyString ->
                    try {
                        val json = JSONObject(bodyString)
                        val imageUrl = json.getString("secure_url")
                        activity?.runOnUiThread {  // Use activity?.runOnUiThread
                            onComplete(imageUrl)
                        }
                    } catch (e: Exception) {
                        activity?.runOnUiThread { // Use activity?.runOnUiThread
                            Toast.makeText(requireContext(), "Image parse error", Toast.LENGTH_SHORT).show()
                            onComplete(null)
                        }
                    }
                }
            }
        })
    }

    private fun validateInputs(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val role = if (binding.radioAdmin.isChecked) "admin" else "student"

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email format"
            return false
        }

        if (phone.length != 10 || !phone.all { it.isDigit() }) {
            binding.etPhone.error = "Invalid phone number"
            return false
        }

        val passwordPattern = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")
        if (!password.matches(passwordPattern)) {
            binding.tvPasswordHint.setTextColor(requireContext().getColor(R.color.error)) // define in colors.xml
            binding.tvPasswordHint.text = "Password must be at least 6 characters and include letters and numbers"
            return false
        } else {
            binding.tvPasswordHint.setTextColor(requireContext().getColor(R.color.success))
            binding.tvPasswordHint.text = "Strong password"
        }

        if (role == "admin") {
            val adminCode = binding.etAdminCode.text.toString().trim()
            if (adminCode != ADMIN_CODE) {
                binding.etAdminCode.error = "Invalid admin registration code"
                return false
            }
        }

        return true
    }

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
                user?.sendEmailVerification()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveUserToFirestore(user, name, phone, email, role)
                        // Navigate to verification page.  Use the interface.

                        val intent = Intent(requireContext(), EmailVerificationActivity::class.java)
                        intent.putExtra("email", binding.etEmail.text.toString().trim())
                        startActivity(intent)
                        startActivity(intent)

// Delay finish to allow user time before activity closes
                        android.os.Handler(Looper.getMainLooper()).postDelayed({
                            requireActivity().finish()
                        }, 15_000L) // 15 seconds = 15,000 milliseconds


                    } else {
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true;
                        Toast.makeText(requireContext(), "Failed to send verification email", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true
                Toast.makeText(requireContext(), "Registration Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

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
            "profile_picture" to (profilePictureUri ?: "https://res.cloudinary.com/dmpclbuxl/image/upload/v1746367153/Default_pfp_lxfo3i.png"),
            "created_at" to System.currentTimeMillis(),
            "id" to "${user.uid}_$email"
        )

        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show()

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save user data!", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true
            }
    }


}
