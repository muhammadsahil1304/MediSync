package com.example.newmedisync.ui.doctorAppointment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.newmedisync.databinding.DialogRecordConsultationBinding
import com.example.newmedisync.firebase.AppointmentRepository
import com.example.newmedisync.firebase.FollowUpRepository
import com.example.newmedisync.firebase.NotificationRepository
import com.example.newmedisync.firebase.PatientRepository
import com.example.newmedisync.model.FollowUpItem
import com.example.newmedisync.model.NotificationItem
import com.example.newmedisync.model.VisitModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RecordConsultationDialogFragment : DialogFragment() {

    private var _binding: DialogRecordConsultationBinding? = null
    private val binding get() = _binding!!

    private val patientRepository = PatientRepository()
    private val appointmentRepository = AppointmentRepository()

    private var patientUid: String = ""
    private var patientName: String = ""
    private var doctorUid: String = ""
    private var doctorName: String = ""
    private var appointmentId: String = ""
    private var visitDate: String = ""
    private var visitTime: String = ""

    var onConsultationSaved: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar_Fullscreen)

        arguments?.let {
            patientUid = it.getString("patientUid", "")
            patientName = it.getString("patientName", "")
            doctorUid = it.getString("doctorUid", "")
            doctorName = it.getString("doctorName", "")
            appointmentId = it.getString("appointmentId", "")
            visitDate = it.getString("visitDate", "")
            visitTime = it.getString("visitTime", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRecordConsultationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentAuthUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (doctorUid.isBlank()) doctorUid = currentAuthUid
        val authName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Doctor"
        if (doctorName.isBlank()) doctorName = if (authName.startsWith("Dr.")) authName else "Dr. $authName"

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        if (visitDate.isBlank()) visitDate = sdf.format(Calendar.getInstance().time)
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        if (visitTime.isBlank()) visitTime = sdfTime.format(Calendar.getInstance().time)

        binding.tvPatientHeader.text = "Patient: ${if (patientName.isNotBlank()) patientName else "Patient"}"
        binding.tvDateHeader.text = "Date: $visitDate • $visitTime"

        setupDatePicker()
        setupListeners()
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        binding.etFollowUpDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)
                    binding.etFollowUpDate.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnSaveConsultation.setOnClickListener {
            saveConsultation()
        }
    }

    private fun saveConsultation() {
        val chiefComplaint = binding.etChiefComplaint.text.toString().trim()
        val symptoms = binding.etSymptoms.text.toString().trim()
        val diagnosis = binding.etDiagnosis.text.toString().trim()
        val treatmentPlan = binding.etTreatmentPlan.text.toString().trim()
        val followUpDate = binding.etFollowUpDate.text.toString().trim()

        if (chiefComplaint.isBlank()) {
            binding.etChiefComplaint.error = "Chief complaint is required"
            binding.etChiefComplaint.requestFocus()
            return
        }

        if (diagnosis.isBlank()) {
            binding.etDiagnosis.error = "Diagnosis is required"
            binding.etDiagnosis.requestFocus()
            return
        }

        // Vitals validation
        val bpSysStr = binding.etBpSystolic.text.toString().trim()
        val bpDiaStr = binding.etBpDiastolic.text.toString().trim()
        val pulseStr = binding.etPulse.text.toString().trim()
        val tempStr = binding.etTemperature.text.toString().trim()
        val spO2Str = binding.etSpO2.text.toString().trim()
        val respRateStr = binding.etRespRate.text.toString().trim()

        val bpSys = bpSysStr.toIntOrNull() ?: 0
        if (bpSysStr.isNotBlank() && bpSys !in 50..250) {
            binding.etBpSystolic.error = "Invalid Systolic BP (50 - 250)"
            binding.etBpSystolic.requestFocus()
            return
        }

        val bpDia = bpDiaStr.toIntOrNull() ?: 0
        if (bpDiaStr.isNotBlank() && bpDia !in 30..150) {
            binding.etBpDiastolic.error = "Invalid Diastolic BP (30 - 150)"
            binding.etBpDiastolic.requestFocus()
            return
        }

        val pulse = pulseStr.toIntOrNull() ?: 0
        if (pulseStr.isNotBlank() && pulse !in 30..220) {
            binding.etPulse.error = "Invalid Heart Rate (30 - 220)"
            binding.etPulse.requestFocus()
            return
        }

        val temp = tempStr.toDoubleOrNull() ?: 0.0
        if (tempStr.isNotBlank() && temp !in 90.0..110.0) {
            binding.etTemperature.error = "Invalid Temperature (90 - 110 °F)"
            binding.etTemperature.requestFocus()
            return
        }

        val spO2 = spO2Str.toIntOrNull() ?: 0
        if (spO2Str.isNotBlank() && spO2 !in 50..100) {
            binding.etSpO2.error = "Invalid SpO2 (50 - 100%)"
            binding.etSpO2.requestFocus()
            return
        }

        val respRate = respRateStr.toIntOrNull() ?: 0
        if (respRateStr.isNotBlank() && respRate !in 8..60) {
            binding.etRespRate.error = "Invalid Respiratory Rate (8 - 60)"
            binding.etRespRate.requestFocus()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveConsultation.isEnabled = false

        lifecycleScope.launch {
            try {
                val visit = VisitModel(
                    patientUid = patientUid,
                    doctorUid = doctorUid,
                    appointmentId = appointmentId,
                    patientName = patientName,
                    doctorName = doctorName,
                    date = visitDate,
                    time = visitTime,
                    purpose = chiefComplaint,
                    chiefComplaint = chiefComplaint,
                    symptoms = symptoms,
                    diagnosis = diagnosis,
                    treatmentPlan = treatmentPlan,
                    followUpDate = followUpDate,
                    bpSystolic = bpSys,
                    bpDiastolic = bpDia,
                    pulseRate = pulse,
                    temperature = temp,
                    spO2 = spO2,
                    respiratoryRate = respRate,
                    timestamp = System.currentTimeMillis()
                )

                patientRepository.saveVisit(visit)

                if (followUpDate.isNotBlank()) {
                    val followUpTs = try {
                        val sdfFU = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        sdfFU.parse(followUpDate)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }

                    val followUpItem = FollowUpItem(
                        patientId = patientUid,
                        patientName = patientName,
                        doctorId = doctorUid,
                        doctorName = doctorName,
                        sourceAppointmentId = appointmentId,
                        recommendedDate = followUpDate,
                        recommendedTimestamp = followUpTs,
                        reason = "Follow-up for $diagnosis",
                        instructions = treatmentPlan,
                        status = "RECOMMENDED"
                    )
                    FollowUpRepository().saveFollowUp(followUpItem)

                    // Send notification to patient
                    val notif = NotificationItem(
                        recipientUid = patientUid,
                        recipientRole = "patient",
                        title = "Follow-up Recommended 📅",
                        message = "Dr. $doctorName recommended a follow-up consultation on $followUpDate.",
                        type = "GENERAL",
                        relatedAppointmentId = appointmentId,
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                    NotificationRepository().saveNotification(notif)
                }

                if (appointmentId.isNotBlank()) {
                    appointmentRepository.updateAppointmentStatus(appointmentId, "Completed")
                }

                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Consultation & Vitals saved successfully!", Toast.LENGTH_SHORT).show()
                onConsultationSaved?.invoke()
                dismiss()

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnSaveConsultation.isEnabled = true
                Toast.makeText(requireContext(), "Error saving consultation: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            patientUid: String,
            patientName: String,
            doctorUid: String,
            doctorName: String,
            appointmentId: String,
            visitDate: String,
            visitTime: String
        ): RecordConsultationDialogFragment {
            return RecordConsultationDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("patientUid", patientUid)
                    putString("patientName", patientName)
                    putString("doctorUid", doctorUid)
                    putString("doctorName", doctorName)
                    putString("appointmentId", appointmentId)
                    putString("visitDate", visitDate)
                    putString("visitTime", visitTime)
                }
            }
        }
    }
}
