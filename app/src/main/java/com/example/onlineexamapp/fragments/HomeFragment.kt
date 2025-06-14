package com.example.onlineexamapp.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onlineexamapp.R
import com.example.onlineexamapp.activities.AdminViewResultsActivity
import com.example.onlineexamapp.activities.AuthActivity
import com.example.onlineexamapp.activities.CreateAnnouncementActivity
import com.example.onlineexamapp.activities.CreateExamActivity
import com.example.onlineexamapp.activities.EditQuestionActivity
import com.example.onlineexamapp.activities.ManageExamsActivity
import com.example.onlineexamapp.activities.ManageStudentsActivity
import com.example.onlineexamapp.activities.TakeQuizActivity
import com.example.onlineexamapp.adapters.AnnouncementAdapter
import com.example.onlineexamapp.viewmodel.ProfileViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import com.example.onlineexamapp.databinding.FragmentHomeBinding
import com.example.onlineexamapp.models.Announcement
import com.google.firebase.firestore.FirebaseFirestore




class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupAnnouncementsRecyclerView()
        setTipOfTheDay()

        binding.studyMaterialsButton.setOnClickListener {
            openStudyMaterials()
        }

        binding.practiceQuizzesButton.setOnClickListener {
            showStartQuizDialog()
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            profileViewModel.fetchUserProfile()
            observeUserProfile() // This will now handle role-based quick actions visibility

            binding.loginRegisterPrompt.visibility = View.GONE
            binding.loggedInHeader.visibility = View.VISIBLE
            binding.guestHeader.visibility = View.GONE

            // Hide locked messages and enable buttons by default for logged-in users
            binding.studyMaterialsLockedMessage.visibility = View.GONE
            binding.practiceQuizzesLockedMessage.visibility = View.GONE
            binding.studyMaterialsButton.isClickable = true
            binding.studyMaterialsButton.isFocusable = true
            binding.practiceQuizzesButton.isClickable = true
            binding.practiceQuizzesButton.isFocusable = true

            // Set up common button listeners for student quick actions (will be made visible based on role)
            val navController = findNavController()
            binding.actionTakeExam.setOnClickListener {
                navController.navigate(R.id.action_homeFragment_to_examsFragment)
            }
            binding.actionViewResults.setOnClickListener {
                navController.navigate(R.id.action_homeFragment_to_resultsFragment)
            }
            binding.actionProfile.setOnClickListener {
                navController.navigate(R.id.action_homeFragment_to_profileFragment)
            }
            binding.actionHelp.setOnClickListener {
                // Assuming R.id.action_homeFragment_to_profileFragment is a placeholder for a help fragment
                // If you have a dedicated help fragment, update this
                navController.navigate(R.id.action_homeFragment_to_profileFragment)
            }
            binding.actionSettings.setOnClickListener {
                // Assuming R.id.action_homeFragment_to_profileFragment is a placeholder for a settings fragment
                // If you have a dedicated settings fragment, update this
                navController.navigate(R.id.action_homeFragment_to_profileFragment)
            }
            binding.actionExamHistory.setOnClickListener {
                navController.navigate(R.id.action_homeFragment_to_examsFragment)
            }

            // Admin Quick Actions Listeners (will only be visible for admin role)
            binding.actionManageStudent.setOnClickListener {
                startActivity(Intent(requireContext(), ManageStudentsActivity::class.java))
            }
            binding.actionCreateExam.setOnClickListener {
                startActivity(Intent(requireContext(), CreateExamActivity::class.java))
            }
            binding.actionManageExam.setOnClickListener {
                startActivity(Intent(requireContext(), ManageExamsActivity::class.java))
            }
            binding.actionCreateAnnouncement.setOnClickListener {
                startActivity(Intent(requireContext(), CreateAnnouncementActivity::class.java))
            }
            binding.actionAdminViewResults.setOnClickListener {
                startActivity(Intent(requireContext(), AdminViewResultsActivity::class.java))
            }
            binding.actionEditQuestion.setOnClickListener {
                startActivity(Intent(requireContext(), EditQuestionActivity::class.java))
            }


        } else {
            // Guest user state
            binding.loginRegisterPrompt.visibility = View.VISIBLE
            binding.loggedInHeader.visibility = View.GONE
            binding.guestHeader.visibility = View.VISIBLE

            // Hide both quick actions sections for guests
            binding.studentQuickActionsSection.visibility = View.GONE
            binding.adminQuickActionsSection.visibility = View.GONE

            binding.studyMaterialsLockedMessage.visibility = View.VISIBLE
            binding.practiceQuizzesLockedMessage.visibility = View.VISIBLE

            // Make buttons not clickable for guests
            binding.studyMaterialsButton.isClickable = false
            binding.studyMaterialsButton.isFocusable = false
            binding.practiceQuizzesButton.isClickable = false
            binding.practiceQuizzesButton.isFocusable = false
        }

        binding.buttonLogin.setOnClickListener {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
        }

        binding.buttonRegister.setOnClickListener {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
        }
    }

    private fun setupViewPager() {
        val bannerImages = listOf(R.drawable.banner1, R.drawable.banner2, R.drawable.banner3)
        val viewPager = binding.bannerCarousel

        viewPager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val imageView = ImageView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                return object : RecyclerView.ViewHolder(imageView) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                (holder.itemView as ImageView).setImageResource(bannerImages[position])
            }

            override fun getItemCount() = bannerImages.size
        }

        TabLayoutMediator(binding.bannerIndicator, binding.bannerCarousel) { _, _ -> }.attach()
    }

    private fun setupAnnouncementsRecyclerView() {
        val db = FirebaseFirestore.getInstance()

        db.collection("announcements")
            .get()
            .addOnSuccessListener { documents ->
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = sdf.parse(sdf.format(Date()))

                val announcementList = documents.mapNotNull { document ->
                    try {
                        val announcement = document.toObject(Announcement::class.java)
                        val startDate = parseDate(announcement.startDate, sdf)
                        val endDate = parseDate(announcement.endDate, sdf)

                        if (startDate != null && endDate != null &&
                            !today.before(startDate) && !today.after(endDate)) {
                            announcement
                        } else null
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Error parsing announcement", e)
                        null
                    }
                }

                val sortedList = announcementList.sortedWith(
                    compareByDescending<Announcement> { it.isImportant }
                        .thenByDescending { parseDate(it.startDate, sdf) ?: Date(0) }
                )

                if (!isAdded || _binding == null) return@addOnSuccessListener  // Check fragment is attached

                _binding?.let { binding ->
                    if (sortedList.isEmpty()) {
                        // Removed the Toast for "No valid announcements found" as it might be annoying on every load
                        // Consider showing a "No announcements" TextView instead
                    }

                    binding.announcementsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    binding.announcementsRecyclerView.adapter = AnnouncementAdapter(sortedList)
                }
            }
            .addOnFailureListener { exception ->
                if (!isAdded || _binding == null) return@addOnFailureListener
                Log.e("HomeFragment", "Error loading announcements", exception)
                Toast.makeText(requireContext(), "Failed to load announcements", Toast.LENGTH_SHORT).show()
            }
    }

    // Helper to parse dates safely
    private fun parseDate(dateString: String?, sdf: SimpleDateFormat): Date? {
        return try {
            dateString?.let { sdf.parse(it) }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error parsing date: $dateString", e)
            null
        }
    }

    private fun openStudyMaterials() {
        val studyMaterialLink = "https://drive.google.com/drive/folders/1zN01OPFaL3m_sQU5MrqhOb0_jpoIrG6c?usp=sharing"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(studyMaterialLink))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No app found to handle this action", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showStartQuizDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_start_quiz, null)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val rgDifficulty = dialogView.findViewById<RadioGroup>(R.id.rgDifficulty)
        val btnStart = dialogView.findViewById<Button>(R.id.btnStartQuiz)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnStart.setOnClickListener {
            val name = etName.text.toString().trim()
            val selectedId = rgDifficulty.checkedRadioButtonId

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show()
            } else if (selectedId == -1) {
                Toast.makeText(requireContext(), "Please select a difficulty level", Toast.LENGTH_SHORT).show()
            } else {
                val difficulty = when (selectedId) {
                    R.id.rbEasy -> "easy"
                    R.id.rbMedium -> "medium"
                    R.id.rbHard -> "hard"
                    else -> "easy"
                }

                val intent = Intent(requireContext(), TakeQuizActivity::class.java).apply {
                    putExtra("USER_NAME", name)
                    putExtra("DIFFICULTY_LEVEL", difficulty)
                }
                startActivity(intent)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun setTipOfTheDay() {
        val tips = resources.getStringArray(R.array.tips_of_the_day)
        binding.tipOfTheDayText.text = tips.random()
    }

    private fun observeUserProfile() {
        profileViewModel.userProfile.observe(viewLifecycleOwner) { document ->
            document?.takeIf { it.exists() }?.let {
                val name = it.getString("name") ?: ""
                val role = it.getString("role") ?: ""
                val profilePicture = it.getString("profile_picture") ?: ""

                binding.userName.text = name
                binding.userRole.text = "Role - ${role.replaceFirstChar { char -> char.uppercase() }}"

                if (profilePicture.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(profilePicture)
                        .circleCrop()
                        .into(binding.profileImage)
                }

                // Role-based visibility for quick actions
                if (role.equals("admin", ignoreCase = true)) {
                    binding.adminQuickActionsSection.visibility = View.VISIBLE
                    binding.studentQuickActionsSection.visibility = View.GONE
                } else if (role.equals("student", ignoreCase = true)) {
                    binding.adminQuickActionsSection.visibility = View.GONE
                    binding.studentQuickActionsSection.visibility = View.VISIBLE
                } else {
                    // Default to student quick actions or hide both if role is unknown/invalid
                    binding.adminQuickActionsSection.visibility = View.GONE
                    binding.studentQuickActionsSection.visibility = View.VISIBLE
                }
            }
        }

        profileViewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}