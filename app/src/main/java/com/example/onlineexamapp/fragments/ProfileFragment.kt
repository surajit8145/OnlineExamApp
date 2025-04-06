package com.example.onlineexamapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.onlineexamapp.activities.LoginActivity
import com.example.onlineexamapp.activities.RegisterActivity
import com.example.onlineexamapp.activities.ProfileActivity
import com.example.onlineexamapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = auth.currentUser

        if (user == null) {
            // User is NOT logged in.  Show login/register options.
            showGuestMode()
        } else {
            // User IS logged in. Show the user's profile.
            showUserProfile()
        }
    }

    private fun showGuestMode() {
        binding.layoutProfile.visibility = View.GONE
        binding.layoutGuest.visibility = View.VISIBLE

        binding.btnLogin.setOnClickListener {
            // Start the LoginActivity.
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnRegister.setOnClickListener {
            // Start the RegisterActivity.
            val intent = Intent(activity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showUserProfile() {
        binding.layoutProfile.visibility = View.VISIBLE
        binding.layoutGuest.visibility = View.GONE

        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Get user data from the document.  Handle nulls.
                        val name = document.getString("name") ?: ""
                        val email = document.getString("email") ?: ""
                        val phone = document.getString("phone") ?: ""
                        val role = document.getString("role") ?: ""

                        // Update the UI with the user's information.
                        binding.tvName.text = "Name - $name"
                        binding.tvEmail.text = "Email - $email"
                        binding.tvPhone.text = "Phone - $phone"
                        binding.tvRole.text = "Role - ${role.replaceFirstChar { it.uppercase() }}"


                        binding.btnUpdateProfile.setOnClickListener {
                            startActivity(Intent(activity, ProfileActivity::class.java))
                        }


                        // Set up the logout button's click listener.
                        binding.btnLogout.setOnClickListener {
                            auth.signOut() // Sign the user out.
                            val intent = Intent(activity, LoginActivity::class.java)
                            startActivity(intent) // Go to LoginActivity.
                            activity?.finish() // Finish this activity.
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle errors when loading the profile.
                    Toast.makeText(activity, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding to prevent memory leaks.
    }
}
