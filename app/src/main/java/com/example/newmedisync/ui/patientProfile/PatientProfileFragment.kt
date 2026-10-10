package com.example.newmedisync.ui.patientProfile

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.newmedisync.R
import com.example.newmedisync.adapter.DoctorAppointmentAdapter
import com.example.newmedisync.adapter.PrescriptionAdapter
import com.example.newmedisync.databinding.FragmentPatientProfileBinding
import com.example.newmedisync.firebase.AppointmentRepository
import com.example.newmedisync.firebase.PatientRepository
import com.example.newmedisync.model.PatientModel
import com.example.newmedisync.room.AppDatabase
import com.example.newmedisync.room.VisitEntity
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PatientProfileFragment : Fragment() {

    private var _binding: FragmentPatientProfileBinding? = null
    private val binding get() = _binding!!

    private var patientUid = ""
    private var patientName = ""
    private var patientPhone = ""
    private var patientAge = ""
    private var patientBlood = ""
    private var patientGender = ""

    private val patientRepository = PatientRepository()
    private val appointmentRepository = AppointmentRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientProfileBinding.inflate(inflater, container, false)
        arguments?.let {
            patientUid = it.getString("patientUid", "")
            patientName = it.getString("name", "")
            patientPhone = it.getString("phone", "")
            patientAge = it.getString("age", "")
            patientBlood = it.getString("blood", "")
            patientGender = it.getString("gender", "")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        loadFullPatientProfile()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnVisit.setOnClickListener {
            showAddVisitDialog()
        }

        binding.btnPrescription.setOnClickListener {
            val bundle = Bundle().apply {
                putString("patientUid", patientUid)
                putString("name", patientName)
                putString("phone", patientPhone)
            }
            findNavController().navigate(
                R.id.action_patientProfileFragment_to_navigation_prescription,
                bundle
            )
        }
    }

    private fun loadFullPatientProfile() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                if (patientUid.isBlank() && patientPhone.isNotBlank()) {
                    patientUid = patientRepository.getUidByPhone(patientPhone) ?: ""
                }

                val db = FirebaseFirestore.getInstance()

                var resolvedName = patientName
                var resolvedPhone = patientPhone
                var userEmail = ""

                if (patientUid.isNotBlank()) {
                    val userDoc = db.collection("users").document(patientUid).get().await()
                    if (userDoc.exists()) {
                        resolvedName = userDoc.getString("name") ?: resolvedName
                        resolvedPhone = userDoc.getString("phone") ?: resolvedPhone
                        userEmail = userDoc.getString("email") ?: ""
                    }
                }

                patientName = resolvedName
                patientPhone = resolvedPhone

                var patientModel: PatientModel? = null
                if (patientUid.isNotBlank()) {
                    val patientDoc = db.collection("patients").document(patientUid).get().await()
                    if (patientDoc.exists()) {
                        patientModel = patientDoc.toObject(PatientModel::class.java)
                    }
                }

                binding.progressBar.visibility = View.GONE
                displayHeaderAndMedicalInfo(patientModel, userEmail)
                loadConsultationHistory()
                loadPrescriptionHistory()

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                displayFallbackInfo()
            }
        }
    }

    private fun displayHeaderAndMedicalInfo(patientModel: PatientModel?, email: String) {
        binding.tvPatientName.text = if (patientName.isNotBlank()) patientName else "Patient Profile"

        val ageVal = if (patientModel != null && patientModel.age > 0) "${patientModel.age} yrs" else if (patientAge.isNotBlank()) "$patientAge yrs" else ""
        val genderVal = patientModel?.gender?.ifBlank { patientGender } ?: patientGender
        val bloodVal = patientModel?.bloodGroup?.ifBlank { patientBlood } ?: patientBlood

        val detailsList = listOf(ageVal, genderVal, bloodVal).filter { it.isNotBlank() }
        binding.tvPatientDetails.text = if (detailsList.isNotEmpty()) detailsList.joinToString(" • ") else "Patient Profile"

        binding.tvPhone.text = if (patientPhone.isNotBlank()) patientPhone else if (email.isNotBlank()) email else "No contact provided"
        binding.tvBlood.text = if (bloodVal.isNotBlank()) bloodVal else "N/A"

        if (patientModel != null && patientModel.profileImageUrl.isNotBlank()) {
            Glide.with(this)
                .load(patientModel.profileImageUrl)
                .placeholder(R.drawable.patients)
                .into(binding.imgPatient)
        } else {
            binding.imgPatient.setImageResource(R.drawable.patients)
        }

        // Allergies
        binding.cgAllergies.removeAllViews()
        if (patientModel != null && patientModel.allergies.isNotEmpty()) {
            binding.cgAllergies.visibility = View.VISIBLE
            binding.tvNoAllergies.visibility = View.GONE
            patientModel.allergies.forEach { allergy ->
                val chip = Chip(requireContext()).apply {
                    text = allergy
                    setChipBackgroundColorResource(R.color.chipRed)
                    setTextColor(android.graphics.Color.parseColor("#D32F2F"))
                }
                binding.cgAllergies.addView(chip)
            }
        } else {
            binding.cgAllergies.visibility = View.GONE
            binding.tvNoAllergies.visibility = View.VISIBLE
        }

        // Diseases / Conditions
        binding.cgDiseases.removeAllViews()
        if (patientModel != null && patientModel.diseases.isNotEmpty()) {
            binding.cgDiseases.visibility = View.VISIBLE
            binding.tvNoDiseases.visibility = View.GONE
            patientModel.diseases.forEach { disease ->
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

        // Current Medications
        if (patientModel != null && patientModel.medications.isNotEmpty()) {
            binding.tvCurrentMedications.visibility = View.VISIBLE
            binding.tvCurrentMedications.text = patientModel.medications.joinToString("\n") { "• $it" }
        } else {
            binding.tvCurrentMedications.text = "No current medications recorded."
        }

        // Emergency Contact
        if (patientModel != null && (patientModel.emergencyName.isNotBlank() || patientModel.emergencyPhone.isNotBlank())) {
            binding.tvEmergencyContact.text = "${patientModel.emergencyName} (${patientModel.emergencyPhone})"
        } else {
            binding.tvEmergencyContact.text = "Not provided"
        }
    }

    private fun displayFallbackInfo() {
        binding.tvPatientName.text = if (patientName.isNotBlank()) patientName else "Patient Profile"
        binding.tvPhone.text = if (patientPhone.isNotBlank()) patientPhone else "Not provided"
        binding.tvBlood.text = if (patientBlood.isNotBlank()) patientBlood else "N/A"
        binding.tvPatientDetails.text = "$patientAge yrs • $patientGender • $patientBlood"
        binding.tvCurrentMedications.text = "No current medications recorded."
        binding.tvEmergencyContact.text = "Not provided"
    }

    private fun loadConsultationHistory() {
        val doctorUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        lifecycleScope.launch {
            try {
                if (patientUid.isNotBlank()) {
                    val appts = appointmentRepository.getPatientAppointments(patientUid)
                        .filter { it.doctorId == doctorUid }

                    if (appts.isNotEmpty()) {
                        binding.recyclerHistory.visibility = View.VISIBLE
                        binding.tvEmptyHistory.visibility = View.GONE

                        val lastVisitAppt = appts.firstOrNull()
                        if (lastVisitAppt != null) {
                            binding.tvLastVisit.text = lastVisitAppt.date
                        }

                        val adapter = DoctorAppointmentAdapter(appts) { appointment ->
                            val bundle = Bundle().apply { putString("appointmentId", appointment.appointmentId) }
                            findNavController().navigate(R.id.navigation_doctorAppointmentDetails, bundle)
                        }
                        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
                        binding.recyclerHistory.adapter = adapter
                        return@launch
                    }
                }

                binding.recyclerHistory.visibility = View.GONE
                binding.tvEmptyHistory.visibility = View.VISIBLE
                binding.tvLastVisit.text = "--"

            } catch (e: Exception) {
                binding.recyclerHistory.visibility = View.GONE
                binding.tvEmptyHistory.visibility = View.VISIBLE
            }
        }
    }

    private fun loadPrescriptionHistory() {
        lifecycleScope.launch {
            try {
                if (patientUid.isNotBlank()) {
                    val prescs = patientRepository.getPatientPrescriptions(patientUid)

                    if (prescs.isNotEmpty()) {
                        binding.recyclerPrescriptions.visibility = View.VISIBLE
                        binding.tvEmptyPrescriptions.visibility = View.GONE

                        val adapter = PrescriptionAdapter(prescs) { record ->
                            val bundle = Bundle().apply { putString("prescriptionId", record.id) }
                            findNavController().navigate(R.id.navigation_prescription, bundle)
                        }
                        binding.recyclerPrescriptions.layoutManager = LinearLayoutManager(requireContext())
                        binding.recyclerPrescriptions.adapter = adapter
                        return@launch
                    }
                }

                binding.recyclerPrescriptions.visibility = View.GONE
                binding.tvEmptyPrescriptions.visibility = View.VISIBLE

            } catch (e: Exception) {
                binding.recyclerPrescriptions.visibility = View.GONE
                binding.tvEmptyPrescriptions.visibility = View.VISIBLE
            }
        }
    }

    private fun showAddVisitDialog() {
        val currentDoctorUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val currentDoctorName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Doctor"

        val dialog = com.example.newmedisync.ui.doctorAppointment.RecordConsultationDialogFragment.newInstance(
            patientUid = patientUid,
            patientName = patientName,
            doctorUid = currentDoctorUid,
            doctorName = currentDoctorName,
            appointmentId = "",
            visitDate = "",
            visitTime = ""
        )
        dialog.onConsultationSaved = {
            loadConsultationHistory()
        }
        dialog.show(parentFragmentManager, "RecordConsultationProfile")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
