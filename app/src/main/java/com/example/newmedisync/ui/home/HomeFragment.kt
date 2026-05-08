package com.example.newmedisync.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.ViewModelProvider
import com.example.newmedisync.R
import com.example.newmedisync.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupLogout()
        binding.cvPrescription.setOnClickListener{
            findNavController().navigate(
                R.id.action_navigation_home_to_navigation_patients_prescription
            )
        }

        return binding.root
    }

    private fun setupLogout() {

        binding.btnLogout.setOnClickListener {

            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setCancelable(false)

                .setPositiveButton("OK") { dialog, _ ->

                    FirebaseAuth.getInstance().signOut()

                    findNavController().navigate(
                        R.id.navigation_login
                    )

                    dialog.dismiss()
                }

                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}