package com.example.newmedisync.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.google.android.material.textfield.TextInputEditText

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val btnSignIn = view.findViewById<Button>(R.id.btnLogin)

        val emailEditText =
            view.findViewById<TextInputEditText>(R.id.Et_email)

        val passwordEditText =
            view.findViewById<TextInputEditText>(R.id.Et_password)

        btnSignIn.setOnClickListener {

            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            findNavController().navigate(
                R.id.action_navigation_login_to_navigation_home
            )

            if (email.isEmpty() || password.isEmpty()) {

                if (email.isEmpty()) {
                    emailEditText.error = "Email is required"
                }

                if (password.isEmpty()) {
                    passwordEditText.error = "Password is required"
                }

            } else {

                findNavController().navigate(
                    R.id.action_navigation_login_to_navigation_home
                )
            }
        }

        return view
    }
}