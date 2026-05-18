package com.example.newmedisync.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.newmedisync.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.FirebaseAuth

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentNotificationsBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val user =
            FirebaseAuth.getInstance().currentUser

        val name =
            user?.displayName ?: "Doctor"

        val email =
            user?.email ?: "No Email"

        binding.tvDoctorName.text =
            "Dr. $name"

        binding.tvDoctorEmail.text =
            email

        binding.tvDoctorAge.text =
            "22 Years"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}