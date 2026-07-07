package com.example.newmedisync.ui.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R

class DoctorVerificationFragment : Fragment() {

    private lateinit var imgProfile: ImageView
    private lateinit var imgCamera: ImageView

    private lateinit var etSpecialization: AutoCompleteTextView

    private lateinit var btnUploadLicense: Button
    private lateinit var btnUploadDegree: Button
    private lateinit var btnSubmitVerification: Button

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
            // TODO : Open Image Picker
        }

        imgCamera.setOnClickListener {
            // TODO : Open Image Picker
        }

        btnUploadLicense.setOnClickListener {
            // TODO : Pick License PDF/Image
        }

        btnUploadDegree.setOnClickListener {
            // TODO : Pick Degree PDF/Image
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
}