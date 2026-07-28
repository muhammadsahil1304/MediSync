package com.example.newmedisync.ui.docVerify

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.example.newmedisync.firebase.DoctorVerificationRepository
import com.example.newmedisync.model.DoctorVerification
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class DoctorVerificationFragment : Fragment() {

    private lateinit var imgProfile: ImageView
    private lateinit var imgCamera: ImageView
    private val repository = DoctorVerificationRepository()


    private lateinit var etSpecialization: AutoCompleteTextView
    private var selectedLicenseUri: Uri? = null
    private var selectedDegreeUri: Uri? = null
    private lateinit var tvLicenseFile: TextView
    private lateinit var tvDegreeFile: TextView
    private lateinit var btnUploadLicense: Button
    private lateinit var btnUploadDegree: Button
    private lateinit var btnSubmitVerification: Button
    private var selectedProfileImageUri: Uri? = null

    private lateinit var tvSkip: TextView
    private lateinit var etQualification: TextInputEditText
    private lateinit var etExperience: TextInputEditText
    private lateinit var etRegistrationNumber: TextInputEditText
    private lateinit var etClinicName: TextInputEditText
    private lateinit var etConsultationFee: TextInputEditText
    private lateinit var etClinicAddress: TextInputEditText
    private lateinit var etBio: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            R.layout.fragment_doctor_verification,
            container,
            false
        )

        // Profile
        imgProfile = view.findViewById(R.id.imgProfile)
        imgCamera = view.findViewById(R.id.imgCamera)
        tvLicenseFile = view.findViewById(R.id.tvLicenseFile)
        tvDegreeFile = view.findViewById(R.id.tvDegreeFile)
        // Fields
        etSpecialization = view.findViewById(R.id.etSpecialization)
        val specializationAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            resources.getStringArray(R.array.doctor_specializations)
        )

        etSpecialization.setAdapter(specializationAdapter)
        etQualification = view.findViewById(R.id.etQualification)
        etExperience = view.findViewById(R.id.etExperience)
        etRegistrationNumber = view.findViewById(R.id.etRegistrationNumber)
        etClinicName = view.findViewById(R.id.etClinicName)
        etConsultationFee = view.findViewById(R.id.etConsultationFee)
        etClinicAddress = view.findViewById(R.id.etClinicAddress)
        etBio = view.findViewById(R.id.etBio)
        // Buttons
        btnUploadLicense = view.findViewById(R.id.btnUploadLicense)
        btnUploadDegree = view.findViewById(R.id.btnUploadDegree)
        btnSubmitVerification =
            view.findViewById(R.id.btnSubmitVerification)

        tvSkip = view.findViewById(R.id.tvSkip)


        imgProfile.setOnClickListener {

            imagePickerLauncher.launch("image/*")

        }

        imgCamera.setOnClickListener {

            imagePickerLauncher.launch("image/*")

        }
        btnUploadLicense.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Document upload will be available in a future update.",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnUploadDegree.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Document upload will be available in a future update.",
                Toast.LENGTH_SHORT
            ).show()
        }


        btnSubmitVerification.setOnClickListener {

            lifecycleScope.launch {

                try {

                    val specialization = etSpecialization.text.toString().trim()
                    val qualification = etQualification.text.toString().trim()
                    val experience = etExperience.text.toString().trim()
                    val registrationNumber = etRegistrationNumber.text.toString().trim()
                    val clinicName = etClinicName.text.toString().trim()
                    val consultationFee = etConsultationFee.text.toString().trim()
                    val clinicAddress = etClinicAddress.text.toString().trim()
                    val bio = etBio.text.toString().trim()

                    // Validate text fields
                    if (
                        specialization.isEmpty() ||
                        qualification.isEmpty() ||
                        experience.isEmpty() ||
                        registrationNumber.isEmpty() ||
                        clinicName.isEmpty() ||
                        consultationFee.isEmpty() ||
                        clinicAddress.isEmpty() ||
                        bio.isEmpty()
                    ) {
                        Toast.makeText(
                            requireContext(),
                            "Please complete all fields.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }



                    // Get logged-in user
                    val user = repository.getCurrentUser()

                    // Create verification object
                    val verification = DoctorVerification(
                        uid = user.uid,
                        fullName = user.name,
                        email = user.email,
                        phone = user.phone,

                        specialization = specialization,
                        qualification = qualification,
                        experience = experience,
                        registrationNumber = registrationNumber,

                        clinicName = clinicName,
                        consultationFee = consultationFee,
                        clinicAddress = clinicAddress,
                        bio = bio,

                        profileImageUrl = "",
                        licenseUrl = "",
                        degreeUrl = "",

                        status = "PENDING"
                    )

                    // Save to Firestore
                    repository.submitVerification(verification)

                    Toast.makeText(
                        requireContext(),
                        "Verification submitted successfully!",
                        Toast.LENGTH_LONG
                    ).show()

                    findNavController().popBackStack()

                } catch (e: Exception) {

                    Toast.makeText(
                        requireContext(),
                        e.message ?: "Something went wrong",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        tvSkip.setOnClickListener {

            findNavController().popBackStack()

        }

        return view
    }
    private val degreePickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            uri?.let {

                selectedDegreeUri = it

                tvDegreeFile.text =
                    "Degree Selected ✔"

            }
        }
    private val licensePickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            uri?.let {

                selectedLicenseUri = it

                tvLicenseFile.text =
                    "License Selected ✔"

            }
        }
    private val imagePickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            uri?.let {

                selectedProfileImageUri = it

                imgProfile.setImageURI(it)

            }
        }
}