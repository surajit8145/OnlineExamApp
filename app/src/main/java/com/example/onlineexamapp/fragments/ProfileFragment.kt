package com.example.onlineexamapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.onlineexamapp.R
import com.example.onlineexamapp.activities.LoginActivity
import com.example.onlineexamapp.activities.ProfileActivity
import com.example.onlineexamapp.activities.RegisterActivity
import com.example.onlineexamapp.databinding.FragmentProfileBinding
import com.example.onlineexamapp.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            // Not logged in
            showNotLoggedInUI()
        } else {
            // Logged in
            showLoggedInUI()
            observeUserProfile()
            profileViewModel.fetchUserProfile()
        }
    }

    private fun showNotLoggedInUI() {
        binding.loggedInLayout.visibility = View.GONE
        binding.notLoggedInLayout.visibility = View.VISIBLE

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(requireContext(), RegisterActivity::class.java))
        }
    }

    private fun showLoggedInUI() {
        binding.loggedInLayout.visibility = View.VISIBLE
        binding.notLoggedInLayout.visibility = View.GONE

        binding.btnUpdateProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            parentFragmentManager.popBackStack(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()

            Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeUserProfile() {
        profileViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        profileViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        profileViewModel.userProfile.observe(viewLifecycleOwner) { document ->
            if (document != null && document.exists()) {
                val name = document.getString("name") ?: ""
                val email = document.getString("email") ?: ""
                val phone = document.getString("phone") ?: ""
                val role = document.getString("role") ?: ""
                val profilePicture = document.getString("profile_picture") ?: ""

                binding.tvName.text = "Name - $name"
                binding.tvEmail.text = "Email - $email"
                binding.tvPhone.text = "Phone - $phone"
                binding.tvRole.text = "Role - ${role.replaceFirstChar { it.uppercase() }}"

                if (profilePicture.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(profilePicture)
                        .circleCrop()
                        .into(binding.profileImage)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
