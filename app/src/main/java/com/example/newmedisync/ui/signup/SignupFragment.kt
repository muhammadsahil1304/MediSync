package com.example.newmedisync.ui.signup

import android.os.Bundle
import android.util.Patterns
import com.google.firebase.auth.userProfileChangeRequest
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class SignupFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_signup, container, false)
        val progressBar =
            view.findViewById<ProgressBar>(R.id.progressBarSignup)

        auth = FirebaseAuth.getInstance()

        val etName = view.findViewById<EditText>(R.id.etFullName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = view.findViewById<EditText>(R.id.etConfirmPassword)

        val btnSignup = view.findViewById<Button>(R.id.btnSignUp)

        val tvLogin = view.findViewById<TextView>(R.id.tvLogin)

        tvLogin.setOnClickListener {
            findNavController().navigate(R.id.navigation_login)
        }

        btnSignup.setOnClickListener {

            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            when {

                name.isEmpty() -> {
                    etName.error = "Enter full name"
                }

                email.isEmpty() -> {
                    etEmail.error = "Enter email"
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etEmail.error = "Enter valid email"
                }

                phone.isEmpty() -> {
                    etPhone.error = "Enter phone number"
                }

                password.isEmpty() -> {
                    etPassword.error = "Enter password"
                }

                password.length < 6 -> {
                    etPassword.error = "Password must be at least 6 characters"
                }

                confirmPassword != password -> {
                    etConfirmPassword.error = "Passwords do not match"
                }

                else -> {

                    progressBar.visibility = View.VISIBLE
                    btnSignup.isEnabled = false

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->

                            progressBar.visibility = View.GONE
                            btnSignup.isEnabled = true

                            if (task.isSuccessful) {

                                val user = auth.currentUser

                                val profileUpdates = userProfileChangeRequest {
                                    displayName = name
                                }

                                user?.updateProfile(profileUpdates)

                                val snackbar = Snackbar.make(
                                    view,
                                    "Signup Successful",
                                    Snackbar.LENGTH_SHORT
                                )

                                snackbar.setBackgroundTint(
                                    requireContext().getColor(
                                        android.R.color.holo_green_dark
                                    )
                                )

                                snackbar.show()

                                findNavController().navigate(
                                    R.id.action_navigation_signup_to_navigation_home
                                )
                            } else {

                                val snackbar = Snackbar.make(
                                    view,
                                    task.exception?.message ?: "Signup Failed",
                                    Snackbar.LENGTH_LONG
                                )

                                snackbar.setBackgroundTint(
                                    requireContext().getColor(android.R.color.holo_red_dark)
                                )

                                snackbar.show()
                            }
                        }
                }
            }
        }

        return view
    }
}