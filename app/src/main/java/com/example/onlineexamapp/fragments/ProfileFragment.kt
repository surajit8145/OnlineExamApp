package com.example.onlineexamapp.fragments

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Spanned
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.onlineexamapp.R
import com.example.onlineexamapp.activities.AuthActivity
import com.example.onlineexamapp.activities.ProfileActivity
import com.example.onlineexamapp.viewmodel.ProfileViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var loggedInSection: View
    private lateinit var loggedOutSection: View
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var profilePicture: ImageView
    private lateinit var themeSwitch: Switch

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbar setup
        val toolbar = view.findViewById<Toolbar>(R.id.profileToolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Profile"
        }
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .addToBackStack(null)
                .commit()
        }

        // Bind views
        loggedInSection = view.findViewById(R.id.loggedInProfileSection)
        loggedOutSection = view.findViewById(R.id.loggedOutProfileSection)
        userName = view.findViewById(R.id.userName)
        userEmail = view.findViewById(R.id.userEmail)
        profilePicture = view.findViewById(R.id.profilePicture)
        themeSwitch = view.findViewById(R.id.themeSwitch)

        // Theme preferences
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedDarkMode = prefs.getBoolean("dark_mode", false)
        themeSwitch.isChecked = savedDarkMode
        AppCompatDelegate.setDefaultNightMode(
            if (savedDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Auth buttons
        loggedOutSection.setOnClickListener {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
        }

        view.findViewById<LinearLayout>(R.id.editProfileButton).setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        view.findViewById<TextView>(R.id.logoutButton).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        // ViewModel
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        viewModel.userProfile.observe(viewLifecycleOwner) { document ->
            if (document != null) {
                loggedInSection.visibility = View.VISIBLE
                loggedOutSection.visibility = View.GONE
                userName.text = document.getString("name") ?: "Unknown"
                userEmail.text = document.getString("email") ?: ""
                val imageUrl = document.getString("profile_picture")
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(requireContext()).load(imageUrl).into(profilePicture)
                }
            } else {
                loggedInSection.visibility = View.GONE
                loggedOutSection.visibility = View.VISIBLE
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.fetchUserProfile()

        view.findViewById<LinearLayout>(R.id.aboutSetting).setOnClickListener {
            showBottomSheet(
                "About Exam Pilot",
                "📘 Welcome to Exam Pilot – your digital partner in diploma exam preparation! 🎓\n\n" +
                        "🧑‍🎓 <b>For Students:</b>\n" +
                        "• Register & login securely 🔐\n" +
                        "• Verify email for safe access 📧\n" +
                        "• Edit your profile with photo 👤\n" +
                        "• Take exams: Upcoming 🕒, Running 🔄, Attended 📝, and Past 📅\n" +
                        "• View instant results with scores 📊 and feedback 💬\n\n" +
                        "🧑‍💼 <b>For Admins:</b>\n" +
                        "• Create & schedule exams 🛠️\n" +
                        "• Add or edit questions ❓\n" +
                        "• Track student performance 📈\n" +
                        "• Use the Admin Dashboard for full control 💼\n\n" +
                        "⚙️ Powered by:\n" +
                        "• Firebase Auth 🔒 & Firestore 🔄\n" +
                        "• Cloudinary for profile images ☁️\n" +
                        "• Built with Kotlin + Jetpack 💻\n" +
                        "• Modern UI with bottom navigation 🎨\n\n" +
                        "🚀 Coming Soon:\n" +
                        "• Timed exams with countdown ⏰\n" +
                        "• Performance analytics 📡\n" +
                        "• Notifications & alerts 🔔\n\n" +
                        "🧠 Stay smart. Stay ahead. Welcome to Exam Pilot! 💡"
            )
        }



        view.findViewById<LinearLayout>(R.id.termsSetting).setOnClickListener {
            showBottomSheet(
                "Terms & Conditions",
                "By using this app, you agree to abide by our rules and regulations. Unauthorized activities are prohibited.\n\n" +
                        "1. **User Responsibilities**: You must provide accurate and complete information when registering for the app. You are responsible for keeping your account details, including login credentials, confidential and safe. Any activity occurring under your account is your responsibility.\n\n" +
                        "2. **Content Usage**: All content provided within the app, including exam materials, questions, answers, results, and user data, is the intellectual property of the app’s platform or its partners. Unauthorized copying, distribution, or use of content is strictly prohibited and may result in account suspension or termination.\n\n" +
                        "3. **Prohibited Activities**: The following activities are prohibited while using the app:\n" +
                        "   - **Cheating**: Any attempt to manipulate exam results or bypass security measures.\n" +
                        "   - **Hacking**: Engaging in actions that compromise the security of the app or other users.\n" +
                        "   - **Spamming**: Sending unsolicited messages or advertisements within the app.\n\n" +
                        "4. **Privacy**: Your personal information, including your name, email, and exam data, is stored securely. We do not share or sell your information to third parties unless required by law. For more details, please refer to our Privacy Policy.\n\n" +
                        "5. **Changes to Terms**: We reserve the right to update or change these terms at any time. You will be notified of any major changes, and it is your responsibility to review these terms regularly. Continued use of the app signifies your acceptance of these changes.\n\n" +
                        "6. **Disclaimer**: The app is provided ‘as is,’ without any guarantees or warranties. We are not responsible for any loss, damage, or issues that arise from using the app. By agreeing to these terms, you acknowledge that the app may experience downtime, and we cannot guarantee uninterrupted service.\n\n" +
                        "7. **Governing Law**: These Terms & Conditions are governed by the laws of the country where the app is registered. Any disputes arising from the use of this app will be subject to the exclusive jurisdiction of the courts in that jurisdiction.\n\n" +
                        "8. **Contact Us**: If you have any questions or concerns about our Terms & Conditions, please reach out to our support team at support@examapp.com.\n\n" +
                        "By using this app, you agree to these Terms & Conditions. Thank you for choosing our app!"
            )
        }


        view.findViewById<LinearLayout>(R.id.privacySetting).setOnClickListener {
            showBottomSheet(
                "Privacy Policy",
                "We respect your privacy. Your data is stored securely and not shared with third parties.\n\n" +
                        "1. **Information We Collect**:\n" +
                        "   - **Personal Information**: We collect personal details such as your name, email address, profile picture, and phone number when you register or update your profile.\n" +
                        "   - **Exam Data**: We collect information related to the exams you take, including exam scores, completion times, and question responses.\n" +
                        "   - **Usage Data**: We track your interactions with the app, such as the pages you visit, the buttons you press, and other actions to improve app functionality.\n\n" +
                        "2. **How We Use Your Data**:\n" +
                        "   - **App Functionality**: Your data is used to provide core app features such as account management, exam scheduling, result tracking, and profile updates.\n" +
                        "   - **Personalization**: We may use your data to personalize content and improve your app experience, such as recommending exams based on past performance.\n" +
                        "   - **Communication**: We may send you notifications regarding exam updates, promotions, and account-related activities.\n\n" +
                        "3. **Data Sharing and Third Parties**:\n" +
                        "   - **No Third-Party Sharing**: We do not share your personal information with third parties except for the following:\n" +
                        "     - **Service Providers**: We may share your data with trusted third-party service providers that help us operate the app, such as Cloudinary for image storage and Firebase for authentication.\n" +
                        "     - **Legal Requirements**: We may share your data when required by law or to protect our rights or the rights of others.\n\n" +
                        "4. **Data Security**:\n" +
                        "   - **Encryption**: We use industry-standard encryption methods to store and transmit your personal data securely.\n" +
                        "   - **Access Control**: Access to your data is restricted to authorized personnel only, and we take steps to prevent unauthorized access.\n" +
                        "   - **Secure Servers**: We host your data on secure servers provided by trusted service providers to ensure its safety.\n\n" +
                        "5. **Your Rights**:\n" +
                        "   - **Data Access**: You have the right to request access to the personal data we hold about you.\n" +
                        "   - **Data Deletion**: You can request that we delete your personal data from our systems at any time.\n" +
                        "   - **Data Correction**: You can request updates or corrections to any inaccurate personal data.\n" +
                        "   - **Account Deactivation**: You may deactivate your account and stop using the app at any time.\n\n" +
                        "6. **Cookies and Tracking Technologies**:\n" +
                        "   - We use cookies and other tracking technologies to enhance your user experience. You can control cookie settings through your device settings.\n\n" +
                        "7. **Children's Privacy**:\n" +
                        "   - Our app is not intended for children under 13. We do not knowingly collect personal information from children. If we become aware that we have collected personal data from a child, we will take steps to remove it.\n\n" +
                        "8. **Changes to the Privacy Policy**:\n" +
                        "   - We reserve the right to update or modify this Privacy Policy at any time. Any changes will be posted on this page, and we will notify you of significant changes via email or in-app notifications.\n\n" +
                        "9. **Contact Us**:\n" +
                        "   - If you have any questions or concerns about our Privacy Policy, please contact us at support@examapp.com.\n\n" +
                        "By using this app, you consent to our Privacy Policy and agree to the collection and use of your personal data as described."
            )
        }


        view.findViewById<LinearLayout>(R.id.rateSetting).setOnClickListener {
            showRatingBottomSheet()
        }

    }

    private fun showBottomSheet(title: String, description: String) {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_info, null)

        val titleView = sheetView.findViewById<TextView>(R.id.infoTitle)
        val descView = sheetView.findViewById<TextView>(R.id.infoDescription)
        val iconView = sheetView.findViewById<ImageView>(R.id.sheetIcon)
        val closeBtn = sheetView.findViewById<ImageView>(R.id.closeButton)

        titleView.text = title
        descView.text = description.toHtmlFormatted()


        // Animate icon (e.g., pulse)
        val pulseAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse)
        iconView.startAnimation(pulseAnim)

        // Close button dismisses dialog
        closeBtn.setOnClickListener { bottomSheetDialog.dismiss() }

        bottomSheetDialog.setContentView(sheetView)

        // Optional: 75% height
        sheetView.post {
            val layoutParams = sheetView.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.75).toInt()
            sheetView.layoutParams = layoutParams
        }

        bottomSheetDialog.show()
    }
    private fun showRatingBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_rating, null)

        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val submitBtn = view.findViewById<Button>(R.id.submitRating)
        val ratingText = view.findViewById<TextView>(R.id.ratingText)
        val thankYouText = view.findViewById<TextView>(R.id.thankYouMessage) // Add this in XML, initially GONE
        val closeBtn = view.findViewById<ImageView>(R.id.closeButton) // Corrected this line

        // Set star color
        ratingBar.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.star_gold))

        // Close button dismisses dialog
        closeBtn.setOnClickListener { bottomSheetDialog.dismiss() }

        ratingBar.setOnRatingBarChangeListener { _, ratingValue, _ ->
            ratingText.text = when (ratingValue.toInt()) {
                1 -> "😞 Very Poor"
                2 -> "😐 Poor"
                3 -> "😌 Average"
                4 -> "😊 Good"
                5 -> "🤩 Excellent"
                else -> ""
            }
        }

        submitBtn.setOnClickListener {
            val rating = ratingBar.rating.toInt()
            if (rating == 0) {
                Toast.makeText(requireContext(), "Please select a rating.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val message = when (rating) {
                1 -> "We're sorry you had a bad experience 😢"
                2 -> "Thanks! We’ll try to do better!"
                3 -> "Thanks for your feedback!"
                4 -> "Great! We're happy you like it 😊"
                5 -> "Awesome! Thank you for your support! 🌟"
                else -> ""
            }

            // Show message in dialog
            thankYouText.text = message
            thankYouText.setBackgroundColor(
                when (rating) {
                    1 -> Color.parseColor("#FFCDD2")
                    2 -> Color.parseColor("#FFF9C4")
                    3 -> Color.parseColor("#FFE0B2")
                    4 -> Color.parseColor("#C8E6C9")
                    5 -> Color.parseColor("#B2EBF2")
                    else -> Color.LTGRAY
                }
            )
            thankYouText.visibility = View.VISIBLE

            // Optionally hide other views
            ratingBar.isEnabled = false

            submitBtn.visibility = View.GONE
            ratingText.visibility = View.GONE
        }

        bottomSheetDialog.setContentView(view)

        view.post {
            val layoutParams = view.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.60).toInt()
            view.layoutParams = layoutParams
        }

        bottomSheetDialog.show()
    }



    fun String.toHtmlFormatted(): Spanned {
        // Ensure the string can support both line breaks and bold text using HTML
        val formattedText = this.replace("\n", "<br/>")  // Handle line breaks
            .replace("**", "<b>")  // Convert **bold** to <b> tags
            .replace("<b>(.*?)<b>".toRegex(), "<b>$1</b>")  // Ensure bold is properly closed

        // Using HtmlCompat.fromHtml() to render the text as HTML
        return HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }


}
