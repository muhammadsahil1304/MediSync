package com.example.newmedisync.ui.completepatient

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.example.newmedisync.databinding.FragmentCompletePatientProfileBinding
import com.example.newmedisync.firebase.PatientRepository
import com.example.newmedisync.model.PatientModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CompletePatientProfileFragment : Fragment() {

    private lateinit var binding: FragmentCompletePatientProfileBinding
    private val repository = PatientRepository()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCompletePatientProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveProfile.setOnClickListener {

            lifecycleScope.launch {

                try {

                    val age = binding.etAge.text.toString().trim()
                    val gender = binding.etGender.text.toString().trim()
                    val blood = binding.etBloodGroup.text.toString().trim()
                    val height = binding.etHeight.text.toString().trim()
                    val weight = binding.etWeight.text.toString().trim()

                    val allergies = binding.etAllergies.text.toString()
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    val diseases = binding.etDiseases.text.toString()
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    val medications = binding.etMedications.text.toString()
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    val emergencyName =
                        binding.etEmergencyName.text.toString().trim()

                    val emergencyPhone =
                        binding.etEmergencyPhone.text.toString().trim()

                    if (
                        age.isEmpty() ||
                        gender.isEmpty() ||
                        blood.isEmpty() ||
                        height.isEmpty() ||
                        weight.isEmpty()
                    ) {
                        Toast.makeText(
                            requireContext(),
                            "Please fill all required fields.",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@launch
                    }

                    /*
                    // Uncomment when Firebase Storage is enabled

                    val imageUrl = repository.uploadProfileImage(
                        selectedImageUri!!
                    )
                    */

                    val patient = PatientModel(

                        uid = FirebaseAuth.getInstance().currentUser!!.uid,

                        age = age.toInt(),

                        gender = gender,

                        bloodGroup = blood,

                        height = height.toDouble(),

                        weight = weight.toDouble(),

                        allergies = allergies,

                        diseases = diseases,

                        medications = medications,

                        emergencyName = emergencyName,

                        emergencyPhone = emergencyPhone,

                        profileImageUrl = ""

                        // profileImageUrl = imageUrl

                    )

                    repository.savePatientProfile(patient)

                    Toast.makeText(
                        requireContext(),
                        "Profile Saved Successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().navigate(
                        R.id.action_completePatientProfileFragment_to_patientHomeFragment
                    )

                } catch (e: Exception) {

                    Toast.makeText(
                        requireContext(),
                        e.localizedMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }

            }

        }
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


}