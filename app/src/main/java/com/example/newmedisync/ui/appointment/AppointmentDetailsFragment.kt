package com.example.newmedisync.ui.appointment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.newmedisync.R
import com.example.newmedisync.databinding.FragmentAppointmentDetailsBinding

class AppointmentDetailsFragment : Fragment() {

    private var _binding: FragmentAppointmentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AppointmentViewModel
    private var appointmentId: String = ""

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
    }

    private fun setupObservers() {
        viewModel.selectedAppointment.observe(viewLifecycleOwner) { appointment ->
            if (appointment == null) return@observe

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
            when (appointment.status) {
                "Upcoming" -> {
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#1565C0"))
                    binding.tvStatusBadge.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_blue)
                }
                "Completed" -> {
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"))
                    binding.tvStatusBadge.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_blue)
                }
                "Cancelled" -> {
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#C62828"))
                    binding.tvStatusBadge.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_chip_red)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
