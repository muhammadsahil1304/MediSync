package com.example.newmedisync.ui.doctorAppointment

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.example.newmedisync.databinding.FragmentDoctorAppointmentDetailsBinding
import com.example.newmedisync.firebase.AppointmentRepository
import com.example.newmedisync.firebase.PatientRepository
import com.example.newmedisync.model.Appointment
import com.example.newmedisync.model.PatientModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DoctorAppointmentDetailsFragment : Fragment() {

    private var _binding: FragmentDoctorAppointmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val repository = AppointmentRepository()
    private var appointmentId: String = ""
    private var currentAppointment: Appointment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appointmentId = arguments?.getString("appointmentId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorAppointmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        loadAppointmentDetails()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnMarkCompleted.setOnClickListener {
            showMarkCompletedDialog()
        }

        binding.btnViewPatient.setOnClickListener {
            navigateToPatientProfile()
        }
    }

    private fun loadAppointmentDetails() {
        if (appointmentId.isBlank()) return

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val appointment = repository.getAppointmentById(appointmentId)
                binding.progressBar.visibility = View.GONE

                if (appointment != null) {
                    currentAppointment = appointment
                    displayAppointment(appointment)
                } else {
                    Toast.makeText(requireContext(), "Appointment not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayAppointment(appointment: Appointment) {
        binding.tvPatientName.text = if (appointment.patientName.isNotBlank()) appointment.patientName else "Patient"
        binding.tvDate.text = appointment.date
        binding.tvTimeSlot.text = appointment.timeSlot
        binding.tvLocationName.text = appointment.locationName
        binding.tvLocationAddress.text = appointment.locationAddress
        binding.tvFee.text = "₹${appointment.consultationFee.toInt()}"
        binding.tvAppointmentId.text = appointment.appointmentId

        binding.tvStatusBadge.text = appointment.status
        when (appointment.status.uppercase()) {
            "UPCOMING" -> {
                binding.tvStatusBadge.setTextColor(Color.parseColor("#1565C0"))
                binding.tvStatusBadge.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_blue)
                binding.btnMarkCompleted.visibility = View.VISIBLE
            }
            "COMPLETED" -> {
                binding.tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"))
                binding.tvStatusBadge.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_blue)
                binding.btnMarkCompleted.visibility = View.GONE
            }
            "CANCELLED" -> {
                binding.tvStatusBadge.setTextColor(Color.parseColor("#C62828"))
                binding.tvStatusBadge.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_red)
                binding.btnMarkCompleted.visibility = View.GONE
            }
            else -> {
                binding.tvStatusBadge.setTextColor(Color.parseColor("#1565C0"))
                binding.tvStatusBadge.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_blue)
                binding.btnMarkCompleted.visibility = View.VISIBLE
            }
        }
    }

    private fun showMarkCompletedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Complete Appointment")
            .setMessage("Mark this appointment as completed?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Confirm") { _, _ ->
                markAsCompleted()
            }
            .show()
    }

    private fun markAsCompleted() {
        val appt = currentAppointment ?: return
        binding.progressBar.visibility = View.VISIBLE
        binding.btnMarkCompleted.isEnabled = false

        lifecycleScope.launch {
            try {
                repository.updateAppointmentStatus(appt.appointmentId, "Completed")
                binding.progressBar.visibility = View.GONE

                val updatedAppt = appt.copy(status = "Completed")
                currentAppointment = updatedAppt
                displayAppointment(updatedAppt)

                Toast.makeText(requireContext(), "Appointment marked as completed!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnMarkCompleted.isEnabled = true
                Toast.makeText(requireContext(), "Unable to update appointment. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToPatientProfile() {
        val appt = currentAppointment ?: return
        val patientId = appt.patientId

        lifecycleScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val patientDoc = db.collection("patients").document(patientId).get().await()
                val userDoc = db.collection("users").document(patientId).get().await()

                val patientModel = patientDoc.toObject(PatientModel::class.java)
                val patientName = userDoc.getString("name") ?: appt.patientName
                val patientPhone = userDoc.getString("phone") ?: ""

                val bundle = Bundle().apply {
                    putString("name", patientName)
                    putString("phone", patientPhone)
                    putString("age", if (patientModel != null) "${patientModel.age}" else "")
                    putString("blood", patientModel?.bloodGroup ?: "")
                    putString("gender", patientModel?.gender ?: "")
                }

                findNavController().navigate(R.id.action_doctorAppointmentDetails_to_patientProfile, bundle)
            } catch (e: Exception) {
                val bundle = Bundle().apply {
                    putString("name", appt.patientName)
                    putString("phone", "")
                    putString("age", "")
                    putString("blood", "")
                    putString("gender", "")
                }
                findNavController().navigate(R.id.action_doctorAppointmentDetails_to_patientProfile, bundle)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
