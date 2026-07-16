package com.example.newmedisync.ui.docVerify

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R

class DoctorVerificationFragment : Fragment() {

    private lateinit var imgProfile: ImageView
    private lateinit var imgCamera: ImageView

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

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

            licensePickerLauncher.launch("*/*")

        }

        btnUploadDegree.setOnClickListener {

            degreePickerLauncher.launch("*/*")

        }

        btnSubmitVerification.setOnClickListener {

            // TODO:
            // 1. Validate all fields
            // 2. Upload profile image
            // 3. Upload documents
            // 4. Save doctor details to Firestore
            // 5. Update verification status = PENDING

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