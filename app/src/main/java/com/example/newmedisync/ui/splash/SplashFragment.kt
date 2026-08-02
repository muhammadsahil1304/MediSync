package com.example.newmedisync.ui.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.example.newmedisync.databinding.FragmentSplashBinding
import com.example.newmedisync.firebase.PatientRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Simple animation
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in)
        binding.ivLogo.startAnimation(fadeIn)

        // 3-second delay
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSession()
        }, 3000)
    }

    private fun checkUserSession() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null && currentUser.isEmailVerified) {
            // User is logged in, navigate based on role
            navigateToHome(currentUser.uid)
        } else {
            // No user logged in, go to Welcome Screen
            findNavController().navigate(R.id.action_navigation_splash_to_navigation_welcome)
        }
    }

    private fun navigateToHome(uid: String) {
        val firestore = FirebaseFirestore.getInstance()
        val patientRepository = PatientRepository()

        lifecycleScope.launch {
            try {
                val document = firestore.collection("users").document(uid).get().await()
                val role = document.getString("role")

                when (role) {
                    "admin" -> {
                        findNavController().navigate(R.id.action_navigation_splash_to_navigation_adminHome)
                    }
                    "doctor" -> {
                        findNavController().navigate(R.id.action_navigation_splash_to_navigation_home)
                    }
                    "patient" -> {
                        val exists = patientRepository.patientExists()
                        if (exists) {
                            findNavController().navigate(R.id.action_navigation_splash_to_navigation_patientHome)
                        } else {
                            findNavController().navigate(R.id.action_navigation_splash_to_navigation_completePatientProfile)
                        }
                    }
                    else -> {
                        findNavController().navigate(R.id.action_navigation_splash_to_navigation_welcome)
                    }
                }
            } catch (e: Exception) {
                findNavController().navigate(R.id.action_navigation_splash_to_navigation_welcome)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}