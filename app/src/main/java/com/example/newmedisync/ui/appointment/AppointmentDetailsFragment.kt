package com.example.newmedisync.ui.appointment

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.newmedisync.R
import com.example.newmedisync.databinding.FragmentAppointmentDetailsBinding
import com.example.newmedisync.firebase.AppointmentRepository
import com.example.newmedisync.model.Appointment
import kotlinx.coroutines.launch

class AppointmentDetailsFragment : Fragment() {

    private var _binding: FragmentAppointmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val repository = AppointmentRepository()
    private lateinit var viewModel: AppointmentViewModel
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
        _binding = FragmentAppointmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[AppointmentViewModel::class.java]

        setupListeners()
        setupObservers()

        if (appointmentId.isNotBlank()) {
            viewModel.loadAppointmentDetails(appointmentId)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCancelAppointment.setOnClickListener {
            showCancelConfirmationDialog()
        }
    }

    private fun setupObservers() {
        viewModel.selectedAppointment.observe(viewLifecycleOwner) { appointment ->
            if (appointment == null) return@observe

            currentAppointment = appointment
            displayAppointment(appointment)
        }
    }

    private fun displayAppointment(appointment: Appointment) {
        val doctorName = if (appointment.doctorName.startsWith("Dr.")) appointment.doctorName else "Dr. ${appointment.doctorName}"
        binding.tvDoctorName.text = doctorName
        binding.tvSpecialization.text = appointment.doctorSpecialization
        binding.tvLocationName.text = appointment.locationName
        binding.tvLocationAddress.text = appointment.locationAddress
        binding.tvDateTime.text = "${appointment.date} • ${appointment.timeSlot}"
        binding.tvFee.text = "₹${appointment.consultationFee.toInt()}"
        binding.tvAppointmentId.text = "Appointment ID: ${appointment.appointmentId}"
        binding.tvPatientName.text = "Patient: ${appointment.patientName}"

        binding.tvStatusBadge.text = appointment.status
        when (appointment.status.uppercase()) {
            "UPCOMING" -> {
                binding.tvStatusBadge.setTextColor(Color.parseColor("#1565C0"))
                binding.tvStatusBadge.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_blue)
                binding.layoutBottomBar.visibility = View.VISIBLE
                binding.btnCancelAppointment.visibility = View.VISIBLE
            }
            "COMPLETED" -> {
                binding.tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"))
                binding.tvStatusBadge.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_blue)
                binding.layoutBottomBar.visibility = View.GONE
                binding.btnCancelAppointment.visibility = View.GONE
            }
            "CANCELLED" -> {
                binding.tvStatusBadge.setTextColor(Color.parseColor("#C62828"))
                binding.tvStatusBadge.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_red)
                binding.layoutBottomBar.visibility = View.GONE
                binding.btnCancelAppointment.visibility = View.GONE
            }
            else -> {
                binding.tvStatusBadge.setTextColor(Color.parseColor("#1565C0"))
                binding.tvStatusBadge.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_blue)
                binding.layoutBottomBar.visibility = View.VISIBLE
                binding.btnCancelAppointment.visibility = View.VISIBLE
            }
        }

        if (appointment.doctorProfileImageUrl.isNotBlank()) {
            Glide.with(this)
                .load(appointment.doctorProfileImageUrl)
                .placeholder(R.drawable.people)
                .into(binding.imgDoctor)
        } else {
            binding.imgDoctor.setImageResource(R.drawable.people)
        }
    }

    private fun showCancelConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Appointment?")
            .setMessage("Are you sure you want to cancel this appointment?")
            .setNegativeButton("Keep Appointment", null)
            .setPositiveButton("Cancel Appointment") { _, _ ->
                cancelAppointment()
            }
            .show()
    }

    private fun cancelAppointment() {
        val appt = currentAppointment ?: return
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCancelAppointment.isEnabled = false

        lifecycleScope.launch {
            try {
                repository.cancelAppointment(appt.appointmentId, "PATIENT")
                binding.progressBar.visibility = View.GONE

                val updatedAppt = appt.copy(status = "Cancelled", cancelledBy = "PATIENT")
                currentAppointment = updatedAppt
                displayAppointment(updatedAppt)

                Toast.makeText(requireContext(), "Appointment cancelled successfully.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnCancelAppointment.isEnabled = true
                Toast.makeText(requireContext(), e.localizedMessage ?: "Unable to cancel appointment. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
