package com.example.newmedisync.ui.patient

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.example.newmedisync.databinding.FragmentEditPatientProfileBinding
import com.example.newmedisync.firebase.PatientRepository
import com.example.newmedisync.model.PatientModel
import com.example.newmedisync.model.User
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class EditPatientProfileFragment : Fragment() {

    private var _binding: FragmentEditPatientProfileBinding? = null
    private val binding get() = _binding!!

    private val repository = PatientRepository()

    private val allergiesList = mutableListOf<String>()
    private val diseasesList = mutableListOf<String>()
    private val medicationsList = mutableListOf<String>()

    private var currentUser: User? = null
    private var currentPatientModel: PatientModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPatientProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        setupListeners()
        loadExistingPatientData()
    }

    private fun setupDropdowns() {
        val bloodAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.blood_group_array)
        )
        binding.etBloodGroup.setAdapter(bloodAdapter)

        val genderAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.gender_array)
        )
        binding.etGender.setAdapter(genderAdapter)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAddAllergy.setOnClickListener {
            showAddItemDialog("Add Allergy", "e.g. Penicillin, Dust") { allergy ->
                allergiesList.add(allergy)
                renderAllergiesChips()
            }
        }

        binding.btnAddDisease.setOnClickListener {
            showAddItemDialog("Add Medical Condition", "e.g. Hypertension, Diabetes") { disease ->
                diseasesList.add(disease)
                renderDiseasesChips()
            }
        }

        binding.btnAddMedication.setOnClickListener {
            showAddItemDialog("Add Medication", "e.g. Metformin 500mg") { med ->
                medicationsList.add(med)
                renderMedicationsChips()
            }
        }

        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun loadExistingPatientData() {
        binding.progressBarLoad.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val (user, patientModel) = repository.getPatientFullProfile()
                binding.progressBarLoad.visibility = View.GONE

                currentUser = user
                currentPatientModel = patientModel

                binding.etName.setText(user.name)
                binding.etPhone.setText(user.phone)

                if (patientModel != null) {
                    binding.etAge.setText(if (patientModel.age > 0) patientModel.age.toString() else "")
                    binding.etGender.setText(patientModel.gender, false)
                    binding.etBloodGroup.setText(patientModel.bloodGroup, false)
                    binding.etHeight.setText(if (patientModel.height > 0) patientModel.height.toString() else "")
                    binding.etWeight.setText(if (patientModel.weight > 0) patientModel.weight.toString() else "")

                    allergiesList.clear()
                    allergiesList.addAll(patientModel.allergies)
                    renderAllergiesChips()

                    diseasesList.clear()
                    diseasesList.addAll(patientModel.diseases)
                    renderDiseasesChips()

                    medicationsList.clear()
                    medicationsList.addAll(patientModel.medications)
                    renderMedicationsChips()

                    binding.etEmergencyName.setText(patientModel.emergencyName)
                    binding.etEmergencyPhone.setText(patientModel.emergencyPhone)
                }
            } catch (e: Exception) {
                binding.progressBarLoad.visibility = View.GONE
                Toast.makeText(requireContext(), "Error loading profile: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderAllergiesChips() {
        binding.cgAllergies.removeAllViews()
        allergiesList.forEachIndexed { index, allergy ->
            val chip = Chip(requireContext()).apply {
                text = allergy
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    allergiesList.removeAt(index)
                    renderAllergiesChips()
                }
            }
            binding.cgAllergies.addView(chip)
        }
    }

    private fun renderDiseasesChips() {
        binding.cgDiseases.removeAllViews()
        diseasesList.forEachIndexed { index, disease ->
            val chip = Chip(requireContext()).apply {
                text = disease
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    diseasesList.removeAt(index)
                    renderDiseasesChips()
                }
            }
            binding.cgDiseases.addView(chip)
        }
    }

    private fun renderMedicationsChips() {
        binding.cgMedications.removeAllViews()
        medicationsList.forEachIndexed { index, med ->
            val chip = Chip(requireContext()).apply {
                text = med
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    medicationsList.removeAt(index)
                    renderMedicationsChips()
                }
            }
            binding.cgMedications.addView(chip)
        }
    }

    private fun showAddItemDialog(title: String, hintText: String, onItemAdded: (String) -> Unit) {
        val editText = EditText(requireContext()).apply {
            hint = hintText
            setPadding(32, 24, 32, 24)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(editText)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->
                val itemStr = editText.text.toString().trim()
                if (itemStr.isNotBlank()) {
                    onItemAdded(itemStr)
                }
            }
            .show()
    }

    private fun saveChanges() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val ageStr = binding.etAge.text.toString().trim()
        val gender = binding.etGender.text.toString().trim()
        val bloodGroup = binding.etBloodGroup.text.toString().trim()
        val heightStr = binding.etHeight.text.toString().trim()
        val weightStr = binding.etWeight.text.toString().trim()
        val emergencyName = binding.etEmergencyName.text.toString().trim()
        val emergencyPhone = binding.etEmergencyPhone.text.toString().trim()

        // Validation
        if (name.isBlank()) {
            binding.etName.error = "Name cannot be empty"
            binding.etName.requestFocus()
            return
        }

        if (phone.isBlank() || phone.length < 10) {
            binding.etPhone.error = "Valid 10-digit phone number is required"
            binding.etPhone.requestFocus()
            return
        }

        val age = ageStr.toIntOrNull()
        if (age == null || age !in 1..120) {
            binding.etAge.error = "Enter a valid age (1 - 120)"
            binding.etAge.requestFocus()
            return
        }

        val height = heightStr.toDoubleOrNull()
        if (height == null || height !in 30.0..250.0) {
            binding.etHeight.error = "Enter valid height in cm (30 - 250)"
            binding.etHeight.requestFocus()
            return
        }

        val weight = weightStr.toDoubleOrNull()
        if (weight == null || weight !in 2.0..300.0) {
            binding.etWeight.error = "Enter valid weight in kg (2 - 300)"
            binding.etWeight.requestFocus()
            return
        }

        if (emergencyPhone.isNotBlank() && emergencyName.isBlank()) {
            binding.etEmergencyName.error = "Emergency contact name is required"
            binding.etEmergencyName.requestFocus()
            return
        }

        if (emergencyName.isNotBlank() && (emergencyPhone.isBlank() || emergencyPhone.length < 10)) {
            binding.etEmergencyPhone.error = "Valid emergency contact phone is required"
            binding.etEmergencyPhone.requestFocus()
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBarSave.visibility = View.VISIBLE
        binding.btnSaveChanges.isEnabled = false

        lifecycleScope.launch {
            try {
                val updatedUser = (currentUser ?: User(uid = uid)).copy(
                    name = name,
                    phone = phone,
                    role = "patient"
                )

                val updatedPatientModel = PatientModel(
                    uid = uid,
                    age = age,
                    gender = gender,
                    bloodGroup = bloodGroup,
                    height = height,
                    weight = weight,
                    allergies = allergiesList,
                    diseases = diseasesList,
                    medications = medicationsList,
                    emergencyName = emergencyName,
                    emergencyPhone = emergencyPhone,
                    profileImageUrl = currentPatientModel?.profileImageUrl ?: ""
                )

                repository.updateFullPatientProfile(updatedUser, updatedPatientModel)

                binding.progressBarSave.visibility = View.GONE
                Toast.makeText(requireContext(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()

            } catch (e: Exception) {
                binding.progressBarSave.visibility = View.GONE
                binding.btnSaveChanges.isEnabled = true
                Toast.makeText(requireContext(), "Failed to update profile: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
