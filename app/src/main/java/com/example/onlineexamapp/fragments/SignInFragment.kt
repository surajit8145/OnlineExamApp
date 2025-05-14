package com.example.onlineexamapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.onlineexamapp.activities.EmailVerificationActivity
import com.example.onlineexamapp.activities.ForgetPasswordActivity
import com.example.onlineexamapp.MainActivity
import com.example.onlineexamapp.R
import com.example.onlineexamapp.activities.AuthActivity
import com.example.onlineexamapp.databinding.FragmentSignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null // <-- updated type
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val sharedPreferences = requireContext().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)

        binding.tvResendVerification.visibility = View.GONE

        val savedEmail = sharedPreferences.getString("email", "")
        val rememberMe = sharedPreferences.getBoolean("rememberMe", false)

        binding.etEmail.setText(savedEmail)
        binding.checkboxRemember.isChecked = rememberMe

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val rememberMeChecked = binding.checkboxRemember.isChecked

            if (!isValidEmail(email)) {
                binding.etEmail.error = "Invalid email address"
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            binding.btnLogin.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            with(sharedPreferences.edit()) {
                putString("email", email)
                putBoolean("rememberMe", rememberMeChecked)
                apply()
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null && user.isEmailVerified) {
                        db.collection("users").document(user.uid).get()
                            .addOnSuccessListener { document ->
                                binding.progressBar.visibility = View.GONE
                                binding.btnLogin.isEnabled = true

                                if (document.exists()) {
                                    val role = document.getString("role")
                                    startActivity(Intent(requireContext(), MainActivity::class.java))
                                    requireActivity().finish()
                                } else {
                                    Toast.makeText(requireContext(), "User data not found!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                binding.progressBar.visibility = View.GONE
                                binding.btnLogin.isEnabled = true
                                Log.e("SignInFragment", "Failed to fetch user role: ", e)
                                Toast.makeText(requireContext(), "Failed to fetch user role!", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(requireContext(), "Please verify your email first!", Toast.LENGTH_LONG).show()
                        binding.tvResendVerification.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Log.e("SignInFragment", "Login failed: ", e)
                    Toast.makeText(requireContext(), "Login Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        binding.tvResendVerification.setOnClickListener {
            val currentUser = auth.currentUser
            currentUser?.sendEmailVerification()
                ?.addOnSuccessListener {
                    Toast.makeText(requireContext(), "Verification email sent again!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), EmailVerificationActivity::class.java)
                    intent.putExtra("email",  binding.etEmail.text.toString().trim())
                    startActivity(intent)
                    requireActivity().finish()
                }
                ?.addOnFailureListener { e ->
                    Log.e("SignInFragment", "Failed to send verification email: ", e)
                    Toast.makeText(requireContext(), "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                }
        }

        binding.tvRegister.setOnClickListener {
            (activity as? AuthActivity)?.navigateTo(SignUpFragment())
        }

        binding.btnBack.setOnClickListener {
            (activity as? AuthActivity)?.navigateTo(WelcomeFragment(), false)
        }


        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(requireContext(), ForgetPasswordActivity::class.java))
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
