package com.example.newmedisync.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.example.newmedisync.databinding.FragmentMedicalHistoryBinding
import com.google.android.material.chip.Chip

class MedicalHistoryFragment : Fragment() {

    private var _binding: FragmentMedicalHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PatientViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicalHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[PatientViewModel::class.java]

        setupListeners()
        setupObservers()

        viewModel.loadPatient()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnEditHealthProfile.setOnClickListener {
            findNavController().navigate(R.id.action_medicalHistoryFragment_to_editPatientProfile)
        }

        binding.btnViewPrescriptions.setOnClickListener {
            findNavController().navigate(R.id.navigation_reports)
        }
    }

    private fun setupObservers() {
        viewModel.patient.observe(viewLifecycleOwner) { patient ->
            binding.tvBloodGroup.text = if (patient.bloodGroup.isNotBlank()) patient.bloodGroup else "N/A"
            binding.tvHeight.text = if (patient.height > 0) "${patient.height} cm" else "N/A"
            binding.tvWeight.text = if (patient.weight > 0) "${patient.weight} kg" else "N/A"

            // Allergies
            binding.cgAllergies.removeAllViews()
            if (patient.allergies.isNotEmpty()) {
                binding.cgAllergies.visibility = View.VISIBLE
                binding.tvNoAllergies.visibility = View.GONE
                patient.allergies.forEach { allergy ->
                    val chip = Chip(requireContext()).apply {
                        text = allergy
                        setChipBackgroundColorResource(R.color.chipRed)
                        setTextColor(android.graphics.Color.parseColor("#D04545"))
                    }
                    binding.cgAllergies.addView(chip)
                }
            } else {
                binding.cgAllergies.visibility = View.GONE
                binding.tvNoAllergies.visibility = View.VISIBLE
            }

            // Diseases
            binding.cgDiseases.removeAllViews()
            if (patient.diseases.isNotEmpty()) {
                binding.cgDiseases.visibility = View.VISIBLE
                binding.tvNoDiseases.visibility = View.GONE
                patient.diseases.forEach { disease ->
                    val chip = Chip(requireContext()).apply {
                        text = disease
                        setChipBackgroundColorResource(R.color.chipYellow)
                        setTextColor(android.graphics.Color.parseColor("#8A5A00"))
                    }
                    binding.cgDiseases.addView(chip)
                }
            } else {
                binding.cgDiseases.visibility = View.GONE
                binding.tvNoDiseases.visibility = View.VISIBLE
            }

            // Medications
            if (patient.medications.isNotEmpty()) {
                binding.tvCurrentMedications.text = patient.medications.joinToString("\n") { "• $it" }
            } else {
                binding.tvCurrentMedications.text = "No current medications recorded."
            }

            // Emergency Contact
            if (patient.emergencyName.isNotBlank() || patient.emergencyPhone.isNotBlank()) {
                binding.tvEmergencyContact.text = "${patient.emergencyName} (${patient.emergencyPhone})"
            } else {
                binding.tvEmergencyContact.text = "Not provided"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
