package com.example.onlineexamapp.fragments

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.onlineexamapp.MainActivity
import com.example.onlineexamapp.R
import com.example.onlineexamapp.activities.AuthActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WelcomeFragment : Fragment() {

    private lateinit var btnSignIn: TextView
    private lateinit var btnSignUp: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSignIn = view.findViewById(R.id.btn_sign_in)
        btnSignUp = view.findViewById(R.id.btn_sign_up)
        val skipTextView = view.findViewById<TextView>(R.id.tv_skip)

        // 🔽 Add underline to "Skip"
        val underlinedText = SpannableString("Skip")
        underlinedText.setSpan(UnderlineSpan(), 0, underlinedText.length, 0)
        skipTextView.text = underlinedText

        // Handle system insets (status bar height)
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            skipTextView.setPadding(0, statusBarHeight, 25, 0)
            insets
        }

        btnSignIn.setOnClickListener {
            (activity as? AuthActivity)?.navigateTo(SignInFragment())
        }

        btnSignUp.setOnClickListener {
            (activity as? AuthActivity)?.navigateTo(SignUpFragment())
        }

        skipTextView.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

}
