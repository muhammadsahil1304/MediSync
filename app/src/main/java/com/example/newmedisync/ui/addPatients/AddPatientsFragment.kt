package com.example.newmedisync.ui.addPatients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.newmedisync.databinding.FragmentAddPatientsBinding
import androidx.lifecycle.lifecycleScope
import com.example.newmedisync.room.AppDatabase
import com.example.newmedisync.room.PatientEntity
import kotlinx.coroutines.launch
class AddPatientsFragment : Fragment() {

    private var _binding: FragmentAddPatientsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPatientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBloodGroupSpinner()

        binding.btnSavePatient.setOnClickListener {
            validateAndSave()
        }
    }

    private fun setupBloodGroupSpinner() {

        val bloodGroups = listOf(
            "Select Blood Group",
            "A+",
            "A-",
            "B+",
            "B-",
            "AB+",
            "AB-",
            "O+",
            "O-"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            bloodGroups
        )

        binding.spBloodGroup.adapter = adapter
    }

    private fun validateAndSave() {

        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val age = binding.etAge.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val notes = binding.etMedicalNotes.text.toString().trim()

        val gender = when (binding.genderGroup.checkedRadioButtonId) {
            binding.rbMale.id -> "Male"
            binding.rbFemale.id -> "Female"
            else -> "Other"
        }

        val bloodGroup = binding.spBloodGroup.selectedItem.toString()

        if (name.isEmpty()) {
            toast("Enter patient name")
            return
        }

        lifecycleScope.launch {

            val patient = PatientEntity(
                name = name,
                phone = phone,
                age = age,
                gender = gender,
                bloodGroup = bloodGroup,
                address = address,
                medicalNotes = notes
            )

            AppDatabase
                .getDatabase(requireContext())
                .patientDao()
                .insertPatient(patient)

            toast("Patient Added Successfully")

        }
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}