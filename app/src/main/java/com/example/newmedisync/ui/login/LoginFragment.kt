package com.example.newmedisync.ui.login

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.example.newmedisync.utils.SnackbarUtils
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_login, container, false)

        auth = FirebaseAuth.getInstance()

        val btnLogin = view.findViewById<Button>(R.id.btnLogin)

        val emailEditText =
            view.findViewById<TextInputEditText>(R.id.Et_email)

        val passwordEditText =
            view.findViewById<TextInputEditText>(R.id.Et_password)

        val tvCreateAccount =
            view.findViewById<TextView>(R.id.tvCreateAccount)

        tvCreateAccount.setOnClickListener {

            findNavController().navigate(R.id.navigation_signup)
        }

        val progressBar =
            view.findViewById<ProgressBar>(R.id.progressBarLogin)

        btnLogin.setOnClickListener {

            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            when {

                email.isEmpty() -> {
                    emailEditText.error = "Enter email"
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailEditText.error = "Enter valid email"
                }

                password.isEmpty() -> {
                    passwordEditText.error = "Enter password"
                }

                else -> {

                    progressBar.visibility = View.VISIBLE
                    btnLogin.isEnabled = false

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->

                            progressBar.visibility = View.GONE
                            btnLogin.isEnabled = true

                            if (task.isSuccessful) {

                                SnackbarUtils.showTopSnackbar(
                                    view,
                                    "Login Successful",
                                    true
                                )

                                findNavController().navigate(
                                    R.id.action_navigation_login_to_navigation_home
                                )

                            } else {

                                SnackbarUtils.showTopSnackbar(
                                    view,
                                    task.exception?.message ?: "Login Failed",
                                    false
                                )
                            }
                        }
                }
            }
        }

        return view
    }
    override fun onStart() {
        super.onStart()

        if (FirebaseAuth.getInstance().currentUser != null) {

            findNavController().navigate(
                R.id.action_navigation_login_to_navigation_home
            )
        }
    }
}