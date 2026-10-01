package com.example.newmedisync.ui.appointment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.newmedisync.R
import com.example.newmedisync.databinding.FragmentAppointmentConfirmedBinding

class AppointmentConfirmedFragment : Fragment() {

    private var _binding: FragmentAppointmentConfirmedBinding? = null
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
        _binding = FragmentAppointmentConfirmedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[AppointmentViewModel::class.java]

        setupDetails()
        setupListeners()
    }

    private fun setupDetails() {
        val doctor = viewModel.selectedDoctor.value
        val location = viewModel.selectedLocation.value
        val date = viewModel.selectedDate.value
        val slot = viewModel.selectedTimeSlot.value

        if (doctor != null) {
            val name = if (doctor.fullName.startsWith("Dr.")) doctor.fullName else "Dr. ${doctor.fullName}"
            binding.tvDoctorName.text = name
            binding.tvSpecialization.text = doctor.specialization
        }

        if (date != null && slot != null) {
            binding.tvDateTime.text = "${date.dateString} • ${slot.time}"
        }

        if (location != null) {
            binding.tvLocationName.text = location.name
        }

        binding.tvAppointmentId.text = "Appointment ID: #$appointmentId"
    }

    private fun setupListeners() {
        binding.btnViewAppointment.setOnClickListener {
            val bundle = Bundle().apply { putString("appointmentId", appointmentId) }
            findNavController().navigate(R.id.navigation_appointmentDetails, bundle)
        }

        binding.btnBackToAppointments.setOnClickListener {
            findNavController().navigate(R.id.navigation_myAppointments)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
