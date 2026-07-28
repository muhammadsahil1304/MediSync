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
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_login, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
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

                                val user = auth.currentUser

                                if (user != null && user.isEmailVerified) {
                                    SnackbarUtils.showTopSnackbar(
                                        view,
                                        "Login Successful",
                                        true
                                    )

                                    navigateBasedOnRole(user.uid)

                                } else {

                                    auth.signOut()

                                    SnackbarUtils.showTopSnackbar(
                                        view,
                                        "Please verify your email first.",
                                        false
                                    )

                                    val bundle = Bundle().apply {
                                        putString("email", user?.email)
                                    }

                                    findNavController().navigate(
                                        R.id.navigateLogin_to_verify,
                                        bundle
                                    )
                                }

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

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null && user.isEmailVerified) {

            navigateBasedOnRole(user.uid)
        }
    }
    private fun navigateBasedOnRole(uid: String) {

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                val role = document.getString("role")

                if (role == "admin") {

                    findNavController().navigate(
                        R.id.action_navigation_login_to_adminHomeFragment
                    )

                } else {

                    findNavController().navigate(
                        R.id.action_navigation_login_to_navigation_home
                    )
                }
            }
            .addOnFailureListener {

                SnackbarUtils.showTopSnackbar(
                    requireView(),
                    "Failed to fetch user role",
                    false
                )
            }
    }
}