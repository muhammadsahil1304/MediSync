package com.example.newmedisync.ui.appointment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newmedisync.R
import com.example.newmedisync.adapter.DateAdapter
import com.example.newmedisync.adapter.TimeSlotAdapter
import com.example.newmedisync.databinding.FragmentSelectSlotBinding

class SelectSlotFragment : Fragment() {

    private var _binding: FragmentSelectSlotBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AppointmentViewModel
    private lateinit var timeSlotAdapter: TimeSlotAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectSlotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[AppointmentViewModel::class.java]

        setupListeners()
        setupObservers()

        viewModel.generateAvailableDates()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnContinueSummary.setOnClickListener {
            val date = viewModel.selectedDate.value
            val slot = viewModel.selectedTimeSlot.value

            if (date == null || slot == null) {
                Toast.makeText(requireContext(), "Please select both a date and a time slot", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            findNavController().navigate(R.id.navigation_appointmentSummary)
        }
    }

    private fun setupObservers() {
        viewModel.selectedDoctor.observe(viewLifecycleOwner) { doctor ->
            if (doctor != null) {
                val name = if (doctor.fullName.startsWith("Dr.")) doctor.fullName else "Dr. ${doctor.fullName}"
                binding.tvDoctorName.text = name
                binding.tvSpecialization.text = doctor.specialization
            }
        }

        viewModel.selectedLocation.observe(viewLifecycleOwner) { location ->
            if (location != null) {
                binding.tvLocationName.text = location.name
                binding.tvLocationAddress.text = location.address
            }
        }

        viewModel.availableDates.observe(viewLifecycleOwner) { dates ->
            if (dates.isNullOrEmpty()) return@observe

            val dateAdapter = DateAdapter(dates) { selectedDateItem ->
                viewModel.selectDate(selectedDateItem)
            }
            binding.rvDates.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.rvDates.adapter = dateAdapter
        }

        timeSlotAdapter = TimeSlotAdapter(emptyList()) { selectedSlot ->
            viewModel.selectTimeSlot(selectedSlot)
        }
        binding.rvTimeSlots.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvTimeSlots.adapter = timeSlotAdapter

        viewModel.timeSlots.observe(viewLifecycleOwner) { slots ->
            timeSlotAdapter.updateSlots(slots)
        }

        viewModel.selectedTimeSlot.observe(viewLifecycleOwner) { slot ->
            val date = viewModel.selectedDate.value
            if (date != null && slot != null) {
                binding.tvSelectedSlotPreview.text = "${date.dayNum} ${date.monthStr} • ${slot.time}"
            } else if (date != null) {
                binding.tvSelectedSlotPreview.text = "${date.dayNum} ${date.monthStr}"
            } else {
                binding.tvSelectedSlotPreview.text = "Select Date & Time"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
