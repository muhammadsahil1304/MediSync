package com.example.newmedisync.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.google.android.material.button.MaterialButton

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_welcome, container, false)

        val btnDoctorLogin = view.findViewById<MaterialButton>(R.id.btnDoctorLogin)
        val btnDoctorSignup = view.findViewById<MaterialButton>(R.id.btnDoctorSignup)
        val btnPatientLogin = view.findViewById<MaterialButton>(R.id.btnPatientLogin)
        val btnPatientSignup = view.findViewById<MaterialButton>(R.id.btnPatientSignup)

        btnDoctorLogin.setOnClickListener {
            val bundle = Bundle().apply { putString("role", "doctor") }
            findNavController().navigate(R.id.action_navigation_welcome_to_navigation_login, bundle)
        }

        btnDoctorSignup.setOnClickListener {
            val bundle = Bundle().apply { putString("role", "doctor") }
            findNavController().navigate(R.id.action_navigation_welcome_to_navigation_signup, bundle)
        }

        btnPatientLogin.setOnClickListener {
            val bundle = Bundle().apply { putString("role", "patient") }
            findNavController().navigate(R.id.action_navigation_welcome_to_navigation_login, bundle)
        }

        btnPatientSignup.setOnClickListener {
            val bundle = Bundle().apply { putString("role", "patient") }
            findNavController().navigate(R.id.action_navigation_welcome_to_navigation_signup, bundle)
        }

        return view
    }
}