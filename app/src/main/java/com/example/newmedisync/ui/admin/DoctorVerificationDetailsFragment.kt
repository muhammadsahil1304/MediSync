package com.example.newmedisync.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.example.newmedisync.firebase.DoctorVerificationRepository
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class DoctorVerificationDetailsFragment : Fragment() {

    private lateinit var imgDoctor: ImageView
    private lateinit var tvName: TextView
    private lateinit var chipStatus: Chip
    private lateinit var tvSpecialization: TextView
    private lateinit var tvQualification: TextView
    private lateinit var tvExperience: TextView
    private lateinit var tvRegistration: TextView
    private lateinit var tvClinic: TextView
    private lateinit var tvFee: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvBio: TextView

    private lateinit var btnApprove: Button
    private lateinit var btnReject: Button

    private lateinit var repository: DoctorVerificationRepository

    private var doctorUid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        doctorUid = arguments?.getString("doctorUid") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_doctor_verification_details,
            container,
            false
        )

        repository = DoctorVerificationRepository()

        imgDoctor = view.findViewById(R.id.imgDoctor)
        tvName = view.findViewById(R.id.tvName)
        chipStatus = view.findViewById(R.id.chipStatus)
        tvSpecialization = view.findViewById(R.id.tvSpecialization)
        tvQualification = view.findViewById(R.id.tvQualification)
        tvExperience = view.findViewById(R.id.tvExperience)
        tvRegistration = view.findViewById(R.id.tvRegistration)
        tvClinic = view.findViewById(R.id.tvClinic)
        tvFee = view.findViewById(R.id.tvFee)
        tvAddress = view.findViewById(R.id.tvAddress)
        tvBio = view.findViewById(R.id.tvBio)

        btnApprove = view.findViewById(R.id.btnApprove)
        btnReject = view.findViewById(R.id.btnReject)

        loadDoctor()

        btnApprove.setOnClickListener {

            lifecycleScope.launch {

                try {

                    repository.approveDoctor(doctorUid)

                    Toast.makeText(
                        requireContext(),
                        "Doctor Approved Successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().popBackStack()

                } catch (e: Exception) {

                    Toast.makeText(
                        requireContext(),
                        e.message,
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }

        }

        btnReject.setOnClickListener {

            lifecycleScope.launch {

                try {

                    repository.rejectDoctor(doctorUid)

                    Toast.makeText(
                        requireContext(),
                        "Doctor Rejected",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().popBackStack()

                } catch (e: Exception) {

                    Toast.makeText(
                        requireContext(),
                        e.message,
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }

        }

        return view
    }

    private fun loadDoctor() {

        lifecycleScope.launch {

            try {

                val doctor = repository.getDoctorVerification(doctorUid)

                imgDoctor.setImageResource(R.drawable.people)

                tvName.text = doctor.fullName
                chipStatus.text = doctor.status
                tvSpecialization.text = doctor.specialization
                tvQualification.text = doctor.qualification
                tvExperience.text = doctor.experience
                tvRegistration.text = doctor.registrationNumber
                tvClinic.text = doctor.clinicName
                tvFee.text = doctor.consultationFee
                tvAddress.text = doctor.clinicAddress
                tvBio.text = doctor.bio

            } catch (e: Exception) {

                Toast.makeText(
                    requireContext(),
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()

            }

        }

    }
}