package com.example.newmedisync.ui.signup

import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.example.newmedisync.utils.SnackbarUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class SignupFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.FROYO)
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

                                val user = auth.currentUser ?: return@addOnCompleteListener

                                val profileUpdates = userProfileChangeRequest {
                                    displayName = name
                                }

                                user.updateProfile(profileUpdates)
                                    .addOnCompleteListener { profileTask ->

                                        if (profileTask.isSuccessful) {

                                            user.sendEmailVerification()
                                                .addOnSuccessListener {

                                                    Log.d("EMAIL", "Verification email sent successfully")

                                                    val bundle = Bundle().apply {
                                                        putString("email", user.email)
                                                    }

                                                    findNavController().navigate(
                                                        R.id.action_navigation_signup_to_verifyEmailFragment,
                                                        bundle
                                                    )
                                                }
                                                .addOnFailureListener { e ->

                                                    Log.e("EMAIL", "Verification failed", e)

                                                    Toast.makeText(
                                                        requireContext(),
                                                        e.localizedMessage ?: "Failed to send verification email",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }

                                        } else {

                                            Toast.makeText(
                                                requireContext(),
                                                "Failed to update profile",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }

                            } else {

                                Toast.makeText(
                                    requireContext(),
                                    task.exception?.localizedMessage ?: "Signup Failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
        }

        return view
    }
}