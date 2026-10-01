package com.example.newmedisync.ui.appointment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.newmedisync.R
import com.example.newmedisync.databinding.FragmentAppointmentSummaryBinding

class AppointmentSummaryFragment : Fragment() {

    private var _binding: FragmentAppointmentSummaryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AppointmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[AppointmentViewModel::class.java]

        setupListeners()
        setupObservers()

        viewModel.loadPatientProfile()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnConfirmAppointment.setOnClickListener {
            binding.btnConfirmAppointment.isEnabled = false
            viewModel.confirmBooking()
        }
    }

    private fun setupObservers() {
        viewModel.selectedDoctor.observe(viewLifecycleOwner) { doctor ->
            if (doctor != null) {
                val name = if (doctor.fullName.startsWith("Dr.")) doctor.fullName else "Dr. ${doctor.fullName}"
                binding.tvDoctorName.text = name
                binding.tvSpecialization.text = doctor.specialization

                if (doctor.profileImageUrl.isNotBlank()) {
                    Glide.with(this)
                        .load(doctor.profileImageUrl)
                        .placeholder(R.drawable.people)
                        .into(binding.imgDoctor)
                } else {
                    binding.imgDoctor.setImageResource(R.drawable.people)
                }
            }
        }

        viewModel.selectedLocation.observe(viewLifecycleOwner) { location ->
            if (location != null) {
                binding.tvLocationName.text = location.name
                binding.tvLocationAddress.text = location.address
                binding.tvFee.text = "₹${location.consultationFee.toInt()}"
            }
        }

        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            if (date != null) {
                binding.tvDate.text = date.dateString
            }
        }

        viewModel.selectedTimeSlot.observe(viewLifecycleOwner) { slot ->
            if (slot != null) {
                binding.tvTimeSlot.text = slot.time
            }
        }

        viewModel.patientName.observe(viewLifecycleOwner) { name ->
            if (!name.isNullOrBlank()) {
                binding.tvPatientName.text = name
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnConfirmAppointment.isEnabled = !isLoading
        }

        viewModel.bookingResult.observe(viewLifecycleOwner) { result ->
            val (success, appointmentId) = result
            if (success && appointmentId.isNotBlank()) {
                viewModel.resetBookingState()
                val bundle = Bundle().apply { putString("appointmentId", appointmentId) }
                findNavController().navigate(R.id.navigation_appointmentConfirmed, bundle)
            } else if (!success && appointmentId.isNotBlank()) {
                Toast.makeText(requireContext(), appointmentId, Toast.LENGTH_LONG).show()
                binding.btnConfirmAppointment.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
