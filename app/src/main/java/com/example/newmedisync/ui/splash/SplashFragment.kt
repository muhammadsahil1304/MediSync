//package com.example.newmedisync.ui.splash
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import com.example.newmedisync.R
//
//
//class SplashFragment : Fragment() {
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_splash, container, false)
//
//        val btnSignIn = view.findViewById<View>(R.id.btnSignIn)
//        val btnSignUp = view.findViewById<View>(R.id.btnJoinPractice)
//
//
//        btnSignIn.setOnClickListener {
//            findNavController().navigate(
//                R.id.action_navigation_splash_to_navigation_login
//            )
//        }
//        btnSignUp.setOnClickListener {
//            findNavController().navigate(
//                R.id.action_navigation_splash_to_navigation_signup
//            )
//        }
//
//        return view
//    }
//
//
//}
package com.example.newmedisync.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.google.android.material.button.MaterialButton

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_splash, container, false)

        val btnDoctorLogin =
            view.findViewById<MaterialButton>(R.id.btnDoctorLogin)

        val btnDoctorSignup =
            view.findViewById<MaterialButton>(R.id.btnDoctorSignup)

        val btnPatientLogin =
            view.findViewById<MaterialButton>(R.id.btnPatientLogin)

        val btnPatientSignup =
            view.findViewById<MaterialButton>(R.id.btnPatientSignup)


        // Doctor Login
        btnDoctorLogin.setOnClickListener {

            val bundle = Bundle().apply {
                putString("role", "doctor")
            }

            findNavController().navigate(
                R.id.action_navigation_splash_to_navigation_login,
                bundle
            )
        }

        // Doctor Signup
        btnDoctorSignup.setOnClickListener {

            val bundle = Bundle().apply {
                putString("role", "doctor")
            }

            findNavController().navigate(
                R.id.action_navigation_splash_to_navigation_signup,
                bundle
            )
        }

        // Patient Login
        btnPatientLogin.setOnClickListener {

            val bundle = Bundle().apply {
                putString("role", "patient")
            }

            findNavController().navigate(
                R.id.action_navigation_splash_to_navigation_login,
                bundle
            )
        }

        // Patient Signup
        btnPatientSignup.setOnClickListener {

            val bundle = Bundle().apply {
                putString("role", "patient")
            }

            findNavController().navigate(
                R.id.action_navigation_splash_to_navigation_signup,
                bundle
            )
        }

        return view
    }
}